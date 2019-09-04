package com.getIn.getCoin.mainServer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.getIn.getCoin.blockChain.Block;
import com.getIn.getCoin.blockChain.BlockChain;
import com.getIn.getCoin.blockChain.TransactionOutput;
import com.getIn.getCoin.dtos.*;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;
import java.util.stream.Collectors;

class MainServerImpl implements MainServer {

    private static final Long usersCount = 100L;

    private static final String DEFAULT_PARENT_FOLDER_DIR = "/home/anvar";

    private final BlockChain blockChain;

    private final ServerSocket serverSocket;

    private final ExecutorService executorService;

    private final ScheduledExecutorService scheduledExecutorService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private final Map<String, UserDto> networkNodes = new ConcurrentHashMap<>();

    public static void main(String[] args) throws IOException {
        final MainServer mainServer = new MainServerImpl("8080");
        mainServer.startServer();

    }

    public MainServerImpl(final String serverPort) throws IOException {
        this.blockChain = BlockChain.getInstance(DEFAULT_PARENT_FOLDER_DIR);
        this.serverSocket = new ServerSocket(Integer.valueOf(serverPort));
        this.executorService = Executors.newFixedThreadPool(usersCount.intValue());
        this.scheduledExecutorService = Executors.newScheduledThreadPool(1);
    }

    @Override
    public void startServer() {
        this.scheduledExecutorService.schedule(this::scheduleNetworkNodes, 1000 * 60 * 15, TimeUnit.MILLISECONDS);
        this.blockChain.uploadBlockChain();
        System.out.println("> Main server started...");
        consume();
    }

    private void consume() {
        boolean active = true;
        try {
            while (active) {
                final Socket clientSocket = serverSocket.accept();
                System.out.println("> Server get request...");
                executorService.execute(() -> handleRequest(clientSocket));
            }
        } catch (Exception e) {
            e.printStackTrace();
            executorService.shutdown();
        }
    }

    private void handleRequest(final Socket clientSocket) {
        try {
            BufferedReader bufferedReader = null;
            try {
                bufferedReader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                final StringWriter stringWriter = new StringWriter();
                final PrintWriter printWriter = new PrintWriter(clientSocket.getOutputStream(), true);
                final String request = bufferedReader.readLine();
                final UserDto userDto = objectMapper.readValue(request, UserDto.class);
                final List<UserDto> userDtoList = new ArrayList<>();
                for (final String it : networkNodes.keySet()) {
                    userDtoList.add(networkNodes.get(it));
                }
                final List<BlockDto> blockDtoList = blockChain.selectBlocks().stream().map(Block::toBlockDto).collect(Collectors.toList());
                final List<TransactionOutputDto> transactionOutputDtoList = blockChain.selectUTXOs().stream().map(TransactionOutput::toTransactionOutputDto).collect(Collectors.toList());
                final InitializeDto initializeDto = new InitializeDto(userDtoList, blockDtoList, transactionOutputDtoList);
                final String stringInitializeDto = objectMapper.writeValueAsString(initializeDto);
                final NetworkNodesDto networkNodesDto = new NetworkNodesDto(stringInitializeDto, "INITIALIZE");
                objectMapper.writeValue(stringWriter, networkNodesDto);
                printWriter.println(stringWriter);
                networkNodes.put(userDto.getUserId(), userDto);
                System.out.println("> User with id: " + userDto.getUserId() + " initialized...");
            } finally {
                if (bufferedReader != null) bufferedReader.close();
                if (clientSocket != null) clientSocket.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void scheduleNetworkNodes() {
        final List<String> deleteNodes = new ArrayList<>();
        for (String it : networkNodes.keySet()) {
            final UserDto userDto = networkNodes.get(it);
            try {
                new Socket(userDto.getIpAddress(), Integer.parseInt(userDto.getPort()));
                System.out.println("User with id " + userDto.getUserId() + " is active");
            } catch (IOException e) {
                deleteNodes.add(it);
            }
        }
        System.out.println("> Users with ids -> " + deleteNodes.toString() + "\n" +"  is not active...");
        deleteNodes.forEach(networkNodes::remove);
    }

}
