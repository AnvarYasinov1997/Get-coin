package com.getIn.getCoin.mainServer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.getIn.getCoin.blockChain.MainServerBlockChain;
import com.getIn.getCoin.blockChain.MainServerBlockChainImpl;
import com.getIn.getCoin.dtos.InitializeDto;
import com.getIn.getCoin.dtos.UserData;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;

class MainServerImpl implements MainServer {

    private static final Long usersCount = 100L;

    private static final String DEFAULT_BLOCKCHAIN_DIR = "Blockchain";

    private static final String DEFAULT_PARENT_FOLDER_DIR = "/home/anvar";

    private final MainServerBlockChain blockChain;

    private final ServerSocket serverSocket;

    private final ExecutorService executorService;

    private final ScheduledExecutorService scheduledExecutorService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private final Map<String, UserData> networkNodes = new ConcurrentHashMap<>();

    public static void main(String[] args) throws IOException {
        final MainServer mainServer = new MainServerImpl("8080");
        mainServer.startServer();
    }

    public MainServerImpl(final String serverPort) throws IOException {
        this.blockChain = new MainServerBlockChainImpl(DEFAULT_BLOCKCHAIN_DIR, DEFAULT_PARENT_FOLDER_DIR);
        this.serverSocket = new ServerSocket(Integer.valueOf(serverPort));
        this.executorService = Executors.newFixedThreadPool(usersCount.intValue());
        this.scheduledExecutorService = Executors.newScheduledThreadPool(1);
    }

    @Override
    public void startServer() {
        this.scheduledExecutorService.schedule(this::scheduleNetworkNodes, 1000 * 60 * 15, TimeUnit.MILLISECONDS);
        consume();
        System.out.println("> Main server started...");
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
                final UserData userData = objectMapper.readValue(request, UserData.class);
                final InitializeDto initializeDto = blockChain.selectBlocks();
                final List<UserData> usersData = new ArrayList<>();
                for (String it : networkNodes.keySet()) {
                    usersData.add(networkNodes.get(it));
                }
                networkNodes.put(userData.getUserId(), userData);
                initializeDto.setUserDataList(usersData);
                objectMapper.writeValue(stringWriter, initializeDto);
                printWriter.println(stringWriter.toString());
                System.out.println("> User with id: " + userData.getUserId() + " initialized...");
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
            final UserData userData = networkNodes.get(it);
            try {
                new Socket(userData.getIpAddress(), Integer.parseInt(userData.getPort()));
                System.out.println("User with id " + userData.getUserId() + " is active");
            } catch (IOException e) {
                deleteNodes.add(it);
            }
        }
        System.out.println("> Users with ids -> " + deleteNodes.toString() + "\n" +"  is not active...");
        deleteNodes.forEach(networkNodes::remove);
    }

}
