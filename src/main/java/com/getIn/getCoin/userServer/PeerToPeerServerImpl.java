package com.getIn.getCoin.userServer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.getIn.getCoin.blockChain.Block;
import com.getIn.getCoin.blockChain.BlockChain;
import com.getIn.getCoin.blockChain.TransactionOutput;
import com.getIn.getCoin.dtos.*;

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

public class PeerToPeerServerImpl implements PeerToPeerServer {

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

    public PeerToPeerServerImpl(final String userId,
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
        final PeerToPeerServerImpl peerToPeerServer = new PeerToPeerServerImpl(
                "1", "8081", "localhost",
                "8080", "localhost");

        new Thread(peerToPeerServer::startServer).start();
        final BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(System.in));
        System.out.println("> Insert data in format: (\"--transfer -amount 100 -to 1)\"" +
                " where sum is amount is amount of coins for payment and to is user public key...");
        boolean active = true;
        while (active) {
            final String command = bufferedReader.readLine();
            final List<String> arguments = Arrays.asList(command.split(" "));
            final String key = arguments.get(0);
            switch (key) {
                case "--mine": {
                    peerToPeerServer.executeTask(TaskType.MINE);
                }
                case "--stopmine": {
                    peerToPeerServer.executeTask(TaskType.MINE_STOP);
                }
                case "--transfer": {

                }
                case "--balance": {

                }

            }
        }
    }

    @Override
    public void startServer() {
        System.out.println("> Start server...");
        try {
            boolean flag = true;
            while (flag) {
                final Socket clientSocket = serverSocket.accept();
                final NetworkNodesDto networkNodesDto = serializeNetworkNodesDto(clientSocket);
                switch (RequestType.valueOf(networkNodesDto.getRequestType())) {
                    case VALIDATE_CHECKSUM: {
                        final BlockDto blockDto = objectMapper.readValue(networkNodesDto.getDto(), BlockDto.class);
                        executorService.execute(() -> {
                            validateBlock(clientSocket);
                            System.out.println("> Checksum validated...");
                        });
                        break;
                    }
                    case UPDATE_NETWORK_NODES: {
                        final DeletedNodesDto dto = objectMapper.readValue(networkNodesDto.getDto(), DeletedNodesDto.class);
                        executorService.execute(() -> {
                            dto.getDeletedUserIds().forEach(networkNodes::remove);
                            System.out.println("> Network nodes updated...");
                        });
                        break;
                    }
                    default: {
                        System.out.println("> Server already initialized...");
                        continue;
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void executeTask(final TaskType taskType) {
        switch (taskType) {
            case MINE: {
                this.executorService.execute(() -> {
                    this.blockChain.enableMineMode();
                    final Block block = this.blockChain.mineBlock(this.blockChain.generateBlock());
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
                });
                break;
            }
            case MINE_STOP: {
                this.blockChain.disableMineMode();
                break;
            }
            default:
                System.out.println("Mock");
        }
    }

    private void validateBlock(final Socket clientSocket) {
        boolean blockChainStatus = true;
        try {
            // validate bloks
            System.out.println("Validate block");
        } catch (Exception e) {
            e.printStackTrace();
        }
        BlockChainStatus status = blockChainStatus ? BlockChainStatus.READABLE : BlockChainStatus.CORRUPTED;
        BufferedWriter bufferedWriter = null;
        try {
            try {
                bufferedWriter = new BufferedWriter(new OutputStreamWriter(clientSocket.getOutputStream()));
                bufferedWriter.write(status.toString() + "\n");
                bufferedWriter.flush();
                System.out.println("CheckSum validated");
            } finally {
                if (bufferedWriter != null) bufferedWriter.close();
                if (clientSocket != null) clientSocket.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
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

    private enum RequestType {
        INITIALIZE, VALIDATE_CHECKSUM, UPDATE_NETWORK_NODES, CONFIRM_BLOCK
    }

    private enum BlockChainStatus {
        READABLE, CORRUPTED
    }

    public enum TaskType {
        MINE, MINE_STOP
    }

}