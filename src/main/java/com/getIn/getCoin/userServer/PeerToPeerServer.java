package com.getIn.getCoin.userServer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.getIn.getCoin.blockChain.*;
import com.getIn.getCoin.dtos.*;
import com.getIn.getCoin.nioServer.NioServer;
import com.getIn.getCoin.nioServer.NioServerImpl;
import io.reactivex.Emitter;
import io.reactivex.Observable;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

import java.io.*;
import java.net.Socket;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

public class PeerToPeerServer {

    private static final String mainServerPort = "8080";

    private static final String mainServerIpAddress = "localhost";

    private static final String DEFAULT_PARENT_FOLDER_DIR = "/home/anvar";

    private final String userId;

    private final String port;

    private final String ipAddress;

    private Socket mainServerSocket;

    private final BlockChainClientRunner blockChainClientRunner;

    private final ClientBlockChain clientBlockChain;

    private final NioServer nioServer;

    private final ObjectMapper objectMapper;

    private final NetworkDataProvider networkDataProvider;

    private ExecutorService executorService;

    private Disposable observer;

    public PeerToPeerServer(final String userId,
                            final String port,
                            final String ipAddress,
                            final String mainServerPort,
                            final String mainServerIpAddress,
                            final ObjectMapper objectMapper,
                            final NetworkDataProvider networkDataProvider,
                            final NioServer nioServer,
                            final ClientBlockChain clientBlockChain,
                            final BlockChainClientRunner blockChainClientRunner) throws Exception {
        this.userId = userId;
        this.port = port;
        this.ipAddress = ipAddress;
        this.nioServer = nioServer;
        this.objectMapper = objectMapper;
        this.networkDataProvider = networkDataProvider;
        this.blockChainClientRunner = blockChainClientRunner;
        this.clientBlockChain = clientBlockChain;
        this.mainServerSocket = new Socket(mainServerIpAddress, Integer.valueOf(mainServerPort));
        this.initServer();
    }

    public static void main(String[] args) throws Exception {
        final BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(System.in));
        System.out.println("> Insert your peer port");
        final String serverPort = bufferedReader.readLine();
        System.out.println("> Insert your inet ip address");
        final String inetIpAddress = bufferedReader.readLine();
        System.out.println("> Insert your user id");
        final String userId = bufferedReader.readLine();
        System.out.println("> Insert your wallet public key");
        final String publicKey = bufferedReader.readLine();
        final ObjectMapper objectMapper = new ObjectMapper();
        System.out.println("> Insert parentFolder dir");
        final String parentFolderDirInput = bufferedReader.readLine();
        final String parentFolderDir;
        if (!parentFolderDirInput.isEmpty()) {
            parentFolderDir = parentFolderDirInput;
        } else parentFolderDir = DEFAULT_PARENT_FOLDER_DIR;
        System.out.println("> Insert commission amount for create transactions");
        final Integer commissionAmount = Integer.valueOf(bufferedReader.readLine());
        final NetworkDataProvider networkDataProvider = new NetworkDataProviderImpl(new ConcurrentHashMap<>(), objectMapper);
        final BlockChainImpl serverBlockChain = BlockChainImpl.getInstance(parentFolderDir, commissionAmount, publicKey);
        final RequestHandler requestHandler = new RequestHandlerImpl(objectMapper, serverBlockChain, networkDataProvider);
        final NioServer nioServer = NioServerImpl.getInstance(serverPort, requestHandler);
        final PeerToPeerServer peerToPeerServer = new PeerToPeerServer(
                userId, serverPort, inetIpAddress,
                mainServerPort, mainServerIpAddress,
                objectMapper, networkDataProvider,
                nioServer, serverBlockChain, serverBlockChain);
        peerToPeerServer.startServer();
        peerToPeerServer.clientProcess();
    }

    public void startServer() throws IOException {
        System.out.println("> Start server...");
        nioServer.start();
    }

    public void clientProcess() {
        System.out.println("> Insert data");
        this.observer = Observable.generate(this::getTerminalReader, this::clientBiConsumerAccept, BufferedReader::close)
                .observeOn(Schedulers.computation())
                .map(s -> Arrays.asList(s.split(" ")))
                .subscribe(PeerToPeerServer.this::handleCommandLineArguments);
    }

    private void handleCommandLineArguments(final List<String> arguments) throws Exception {
        if (!arguments.isEmpty()) {
            final String key = arguments.get(0);
            switch (key) {
                case "--wallet": {
                    if (arguments.size() == 3) {
                        final String command = arguments.get(1);
                        if (command.equals("new")) clientBlockChain.generateNewWallet();
                        else System.out.println("> Invalid command");
                    } else if (arguments.size() == 4) {
                        final String command = arguments.get(1);
                        if (command.equals("create")) {
                            final String publicKey = arguments.get(2);
                            final String privateKey = arguments.get(3);
                            clientBlockChain.generateWallet(publicKey, privateKey);
                            System.out.println("Wallet created");
                        } else System.out.println("> Invalid command");
                    } else if (arguments.size() == 2) {
                        final String command = arguments.get(1);
                        if (command.equals("remove")) {
                            clientBlockChain.removeWallet();
                        }
                    } else System.out.println("> Invalid arguments");
                    break;
                }
                case "--mine": {
                    if (arguments.size() == 2) {
                        final String command = arguments.get(1);
                        if (command.equals("start")) {
                            this.clientBlockChain.enableMineMode();
                            final Block block = this.clientBlockChain.mineBlock(this.clientBlockChain.generateBlock());
                            this.networkDataProvider.sendBlockToPeers(block);
                        } else if (command.equals("stop")) {
                            clientBlockChain.disableMineMode();
                            System.out.println("> Mining stopped");
                        } else System.out.println("> Invalid command");
                    } else System.out.println("> Enter a command, start or stop");
                    break;
                }
                case "--transfer": {
                    if (arguments.size() == 3) {
                        final String toWallet = arguments.get(1);
                        final String amount = arguments.get(2);
                        final List<String> minerKeys =
                                this.networkDataProvider.checkCommissionFromPeers(clientBlockChain.getCommission())
                                        .stream()
                                        .filter(TransactionCommissionResponseDto::getStatus)
                                        .map(TransactionCommissionResponseDto::getPublicKey)
                                        .collect(Collectors.toList());
                        final Transaction transaction = clientBlockChain.createTransaction(toWallet, Long.valueOf(amount));
                        transaction.toTransactionDto();
                    } else System.out.println("> Enter a command, \"to\", \"amount\", \"commission\" ");
                    break;
                }
                case "--balance": {
                    final Wallet wallet = clientBlockChain.getUserWallet();
                    if (wallet != null) System.out.println(wallet.getBalance());
                    else System.out.println("> User wallet has not created");
                    break;
                }
                case "--exit": {
                    this.nioServer.stop();
                    this.observer.dispose();
                    break;
                }
                default:
                    System.out.println("> Key is not found");
            }
        } else System.out.println("> Command line is empty");
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
            this.networkDataProvider.addNodes(initializeDto.getUserDtoList());
            initializeDto.getUTXOsDtoList().forEach(it -> BlockChainImpl.UTXOs.put(it.getId(), new TransactionOutput(it)));
            blockChainClientRunner.uploadBlockChainFromServerData(initializeDto.getBlockDtoList().stream().map(Block::new).collect(Collectors.toList()));
            int poolSize = this.networkDataProvider.getNodesCount() < 10 ? 10 : this.networkDataProvider.getNodesCount();
            this.executorService = Executors.newFixedThreadPool(poolSize, runnable -> new Thread(runnable, "Main executor"));
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

    private void returnResponse(final Socket clientSocket, final String data) {
        try {
            try (final BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(clientSocket.getOutputStream()))) {
                bufferedWriter.write(data + "\n");
                bufferedWriter.flush();
                System.out.println("Response returned");
            } finally {
                if (clientSocket != null) clientSocket.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}