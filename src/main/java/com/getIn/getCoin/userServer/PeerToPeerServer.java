package com.getIn.getCoin.userServer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.getIn.getCoin.blockChain.*;
import com.getIn.getCoin.dtos.*;
import io.reactivex.Emitter;
import io.reactivex.Observable;
import io.reactivex.disposables.Disposable;
import io.reactivex.observers.DisposableObserver;
import io.reactivex.schedulers.Schedulers;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

public class PeerToPeerServer {

    private static final String DEFAULT_PARENT_FOLDER_DIR = "/home/anvar";

    private final String userId;

    private final String port;

    private final String ipAddress;

    private Socket mainServerSocket;

    private final ServerSocket serverSocket;

    private final BlockChain blockChain;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private final Map<String, UserDto> networkNodes = new ConcurrentHashMap<>();

    private ExecutorService executorService;

    public PeerToPeerServer(final String userId,
                            final String port,
                            final String ipAddress,
                            final String mainServerPort,
                            final String mainServerIpAddress) throws Exception {
        this.userId = userId;
        this.port = port;
        this.ipAddress = ipAddress;
        this.blockChain = BlockChain.getInstance(DEFAULT_PARENT_FOLDER_DIR, "MEkwEwYHKoZIzj0CAQYIKoZIzj0DAQEDMgAE5LAbf0VpQ3Gp6nEqMM/WdgWJvEZgCiarNXZn6b1JG0ih5+d9ksMYb17c+Nb9xmAz");
        this.serverSocket = new ServerSocket(Integer.valueOf(this.port));
        this.mainServerSocket = new Socket(mainServerIpAddress, Integer.valueOf(mainServerPort));
        this.initServer();
    }

    public static void main(String[] args) throws Exception {
        final PeerToPeerServer peerToPeerServer = new PeerToPeerServer(
                "1", "8081", "localhost",
                "8080", "localhost");
        peerToPeerServer.startServer();
        peerToPeerServer.clientProcess();
    }

    public void startServer() {
        System.out.println("> Start server...");
        Observable.generate(() -> this.serverSocket, this::serverBiConsumerAccept, ServerSocket::close)
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.computation())
                .map(this::serializeNetworkNodesDto)
                .subscribe(this::handleServerRequests, Throwable::printStackTrace);
    }

    public void clientProcess() {
        System.out.println("> Insert data in format: (\"--transfer -amount 100 -to 1)\"" +
                " where sum is amount is amount of coins for payment and to is user public key...");
        Observable.generate(this::getTerminalReader, this::clientBiConsumerAccept, BufferedReader::close)
                .subscribeOn(Schedulers.computation())
                .subscribe(new DisposableObserver<String>() {
                    @Override
                    public void onNext(String s) {
                        final List<String> arguments = Arrays.asList(s.split(" "));
                        PeerToPeerServer.this.handleCommandLineArguments(arguments, this);
                    }

                    @Override
                    public void onError(Throwable e) {
                        e.printStackTrace();
                    }

                    @Override
                    public void onComplete() {
                        System.out.println("Complete");
                    }
                });
    }

    private void handleServerRequests(final NetworkNodesDto networkNodesDto) throws IOException {
        switch (RequestType.valueOf(networkNodesDto.getRequestType())) {
            case ADD_BLOCK: {
                final BlockDto blockDto = objectMapper.readValue(networkNodesDto.getDto(), BlockDto.class);
                blockChain.validateBlock(new Block(blockDto));
                break;
            }
            case UPDATE_NETWORK_NODES: {
                final DeletedNodesDto dto = objectMapper.readValue(networkNodesDto.getDto(), DeletedNodesDto.class);
                dto.getDeletedUserIds().forEach(networkNodes::remove);
                System.out.println("> Network nodes updated...");
                break;
            }
            default:
                System.out.println("> Server already initialized...");
        }
    }

    private void handleCommandLineArguments(final List<String> arguments, final Disposable observer) {
        if (!arguments.isEmpty()) {
            final String key = arguments.get(0);
            switch (key) {
                case "--wallet": {
                    if (arguments.size() == 3) {
                        final String command = arguments.get(1);
                        if (command.equals("new")) blockChain.generateNewWallet();
                        else System.out.println("> Invalid command");
                    } else if (arguments.size() == 4) {
                        final String command = arguments.get(1);
                        if (command.equals("create")) {
                            final String publicKey = arguments.get(2);
                            final String privateKey = arguments.get(3);
                            blockChain.generateWallet(publicKey, privateKey);
                            System.out.println("Wallet created");
                        } else System.out.println("> Invalid command");
                    } else if (arguments.size() == 2) {
                        final String command = arguments.get(1);
                        if (command.equals("remove")) {
                            blockChain.removeWallet();
                        }
                    } else System.out.println("> Invalid arguments");
                    break;
                }
                case "--mine": {
                    if (arguments.size() == 2) {
                        final String command = arguments.get(1);
                        if (command.equals("start")) {
                            this.blockChain.enableMineMode();
                            final Block block = this.blockChain.mineBlock(this.blockChain.generateBlock());
                            this.sendBlockToPeers(block);
                        } else if (command.equals("stop")) {
                            blockChain.disableMineMode();
                            System.out.println("> Mining stopped");
                        } else System.out.println("> Invalid command");
                    } else System.out.println("> Enter a command, start or stop");
                    break;
                }
                case "--transfer": {
                    if (arguments.size() == 4) {
                        final String toWallet = arguments.get(1);
                        final String amount = arguments.get(2);
                        final String commission = arguments.get(3); // не сделал
                        final Transaction transaction = blockChain.createTransaction(toWallet, Long.valueOf(amount));
                        transaction.toTransactionDto();
                    } else System.out.println("> Enter a command, \"to\", \"amount\", \"commission\" ");
                    break;
                }
                case "--balance": {
                    final Wallet wallet = blockChain.getUserWallet();
                    if (wallet != null) System.out.println(wallet.getBalance());
                    else System.out.println("> User wallet has not created");
                    break;
                }
                case "--exit": {
                    observer.dispose();
                    break;
                }
                default:
                    System.out.println("> Key is not found");
            }
        } else System.out.println("> Command line is empty");
    }

    private void sendBlockToPeers(final Block block) {
        this.networkNodes.forEach((key, value) -> {
            this.executorService.execute(() -> {
                try {
                    final Socket socket = new Socket(value.getIpAddress(), Integer.valueOf(value.getPort()));
                    final StringWriter stringWriter = new StringWriter();
                    final PrintWriter printWriter = new PrintWriter(socket.getOutputStream(), true);
                    final BlockDto blockDto = block.toBlockDto();
                    final String blockDtoString = objectMapper.writeValueAsString(blockDto);
                    final NetworkNodesDto networkNodesDto = new NetworkNodesDto(RequestType.CONFIRM_BLOCK.name(), blockDtoString);
                    final String networkNodesDtoString = objectMapper.writeValueAsString(networkNodesDto);
                    objectMapper.writeValue(stringWriter, networkNodesDtoString);
                    printWriter.println(stringWriter);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
        });
    }

    private void initServer() throws Exception {
        final StringWriter stringWriter = new StringWriter();
        objectMapper.writeValue(stringWriter, UserDto.builder()
                .userId(userId)
                .port(port)
                .ipAddress(ipAddress)
                .build());
        final PrintWriter printWriter = new PrintWriter(mainServerSocket.getOutputStream(), true);
        printWriter.println(stringWriter);
        final NetworkNodesDto networkNodesDto = serializeNetworkNodesDto(mainServerSocket);
        if (RequestType.valueOf(networkNodesDto.getRequestType()) == RequestType.INITIALIZE) {
            final InitializeDto initializeDto = objectMapper.readValue(networkNodesDto.getDto(), InitializeDto.class);
            initializeDto.getUserDtoList().forEach(it -> networkNodes.put(it.getUserId(), it));
            initializeDto.getUTXOsDtoList().forEach(it -> BlockChain.UTXOs.put(it.getId(), new TransactionOutput(it)));
            blockChain.uploadBlockChainFromServerData(initializeDto.getBlockDtoList().stream().map(Block::new).collect(Collectors.toList()));
            int poolSize = networkNodes.size() < 10 ? 10 : networkNodes.size();
            this.executorService = Executors.newFixedThreadPool(poolSize);
        } else throw new Exception("> Client has not be initialized");
        System.out.println("> Peer initialized...");
    }

    private NetworkNodesDto serializeNetworkNodesDto(final Socket socket) throws IOException {
        final BufferedReader requestReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        final String message = requestReader.readLine();
        return objectMapper.readValue(message, NetworkNodesDto.class);
    }

    private BufferedReader getTerminalReader() {
        return new BufferedReader(new InputStreamReader(System.in));
    }

    private void clientBiConsumerAccept(final BufferedReader bufferedReader, final Emitter<String> emitter) {
        try {
            final String command = bufferedReader.readLine();
            if (command != null) emitter.onNext(command);
            else emitter.onComplete();
        } catch (IOException e) {
            e.printStackTrace();
            emitter.onComplete();
            throw new RuntimeException();
        }
    }

    private void serverBiConsumerAccept(final ServerSocket serverSocket, final Emitter<Socket> emitter) {
        try {
            emitter.onNext(serverSocket.accept());
        } catch (IOException e) {
            e.printStackTrace();
            emitter.onComplete();
            throw new RuntimeException();
        }
    }

    private void returnBlockValidatedResult(final Socket clientSocket, final boolean blockChainStatus) {
        try {
            try (final BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(clientSocket.getOutputStream()))) {
                final BlockChainStatus status = blockChainStatus ? BlockChainStatus.READABLE : BlockChainStatus.CORRUPTED;
                bufferedWriter.write(status.toString() + "\n");
                bufferedWriter.flush();
                System.out.println("Block added");
            } finally {
                if (clientSocket != null) clientSocket.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private enum RequestType {
        INITIALIZE, ADD_BLOCK, UPDATE_NETWORK_NODES, CONFIRM_BLOCK
    }

    private enum BlockChainStatus {
        READABLE, CORRUPTED
    }

}