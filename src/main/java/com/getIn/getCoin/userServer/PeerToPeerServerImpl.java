package com.getIn.getCoin.userServer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.getIn.getCoin.blockChain.BlockChain;
import com.getIn.getCoin.blockChain.BlockChainImpl;
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

public class PeerToPeerServerImpl implements PeerToPeerServer {

    private static final String DEFAULT_BLOCKCHAIN_DIR = "Blockchain";

    private static final String DEFAULT_PARENT_FOLDER_DIR = "/home/anvar";

    private final String userId;

    private final String port;

    private final String ipAddress;

    private final Socket mainServerSocket;

    private final ServerSocket serverSocket;

    private final BlockChain blockChain;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private final Map<String, UserData> networkNodes = new ConcurrentHashMap<>();

    private ExecutorService executorService;

    public static void main(String[] args) throws Exception {
        final PeerToPeerServer peerToPeerServer = new PeerToPeerServerImpl(
                "1", "8081", "localhost",
                "8080", "localhost",
                new BlockChainImpl(DEFAULT_BLOCKCHAIN_DIR, DEFAULT_PARENT_FOLDER_DIR)
        );
        new Thread(peerToPeerServer::startServer).start();
        final BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(System.in));
        boolean active = true;
        while (active) {
            System.out.println("> Insert data in format: (\"--payment -amount 100 -to 1)\"" +
                    " where sum is amount is amount of coins for payment and to is user id...");
            final String command = bufferedReader.readLine();
            final List<String> arguments = Arrays.asList(command.split(" "));
            final String key = arguments.get(0);
            arguments.remove(0);
            if (key.equals("--payment")) {

            }
        }
    }

    public PeerToPeerServerImpl(final String userId,
                                final String port,
                                final String ipAddress,
                                final String mainServerPort,
                                final String mainServerIpAddress,
                                final BlockChain blockChain) throws Exception {
        this.userId = userId;
        this.port = port;
        this.ipAddress = ipAddress;
        this.blockChain = blockChain;
        this.mainServerSocket = new Socket(mainServerIpAddress, Integer.valueOf(mainServerPort));
        this.serverSocket = new ServerSocket(Integer.valueOf(this.port));
        this.initServer();
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
                        final CheckSumDto dto = objectMapper.readValue(networkNodesDto.getDto(), CheckSumDto.class);
                        executorService.execute(() -> {
                            validateCheckSum(clientSocket, dto);
                            System.out.println("> Checksum validated...");
                        });
                    }
                    case UPDATE_NETWORK_NODES: {
                        final DeletedNodesDto dto = objectMapper.readValue(networkNodesDto.getDto(), DeletedNodesDto.class);
                        executorService.execute(() -> {
                            dto.getDeletedUserIds().forEach(networkNodes::remove);
                            System.out.println("> Network nodes updated...");
                        });
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

    private void validateCheckSum(final Socket clientSocket, final CheckSumDto dto) {
        boolean blockChainStatus = false;
        try {
            blockChainStatus = blockChain.compareCheckSum(dto.getCheckSum());
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

            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void initServer() throws Exception {
        final StringWriter stringWriter = new StringWriter();
        objectMapper.writeValue(stringWriter, UserData.builder()
                .userId(userId)
                .port(port)
                .ipAddress(ipAddress)
                .build());
        final PrintWriter printWriter = new PrintWriter(mainServerSocket.getOutputStream(), true);
        printWriter.println(stringWriter.toString());
        final NetworkNodesDto networkNodesDto = serializeNetworkNodesDto(mainServerSocket);
        if (RequestType.valueOf(networkNodesDto.getRequestType()) == RequestType.INITIALIZE) {
            final InitializeDto initializeDto = objectMapper.readValue(networkNodesDto.getDto(), InitializeDto.class);
            initializeDto.getUserDataList().forEach(it -> networkNodes.put(it.getUserId(), it));
            blockChain.saveOrUpdateBlocks(initializeDto.getCheckSums(), initializeDto.getBlocks());
            this.executorService = Executors.newFixedThreadPool(networkNodes.size());
        } else throw new Exception("> Client has not be initialized");
        System.out.println("> Peer initialized...");
    }

    private NetworkNodesDto serializeNetworkNodesDto(final Socket socket) throws IOException {
        final BufferedReader requestReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        final String message = requestReader.readLine();
        return objectMapper.readValue(message, NetworkNodesDto.class);
    }

    private enum RequestType {
        INITIALIZE, VALIDATE_CHECKSUM, UPDATE_NETWORK_NODES
    }

    private enum BlockChainStatus {
        READABLE, CORRUPTED
    }

}