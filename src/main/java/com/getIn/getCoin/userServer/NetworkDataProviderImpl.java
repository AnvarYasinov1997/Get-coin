package com.getIn.getCoin.userServer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.getIn.getCoin.blockChain.Block;
import com.getIn.getCoin.dtos.*;

import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.FutureTask;
import java.util.stream.Collectors;

public class NetworkDataProviderImpl implements NetworkDataProvider {

    private final Map<String, UserDto> networkNodes;

    private final ObjectMapper objectMapper;

    private final ExecutorService executorService;

    public NetworkDataProviderImpl(final Map<String, UserDto> networkNodes,
                                   final ObjectMapper objectMapper) {
        this.networkNodes = networkNodes;
        this.objectMapper = objectMapper;
        this.executorService =
                Executors.newFixedThreadPool(1000, runnable -> new Thread(runnable, "Nodes executor"));
    }

    @Override
    public int getNodesCount() {
        return networkNodes.size();
    }

    @Override
    public void addNodes(final List<UserDto> userDto) {
        userDto.forEach(it -> this.networkNodes.put(it.getUserId(), it));
    }

    @Override
    public void removeNodesByIds(final List<String> nodeIds) {
        nodeIds.forEach(networkNodes::remove);
    }

    @Override
    public void sendBlockToPeers(final Block block) {
        this.networkNodes.forEach((key, value) -> this.executorService.execute(() -> {
            try {
                final Socket socket = new Socket(value.getIpAddress(), Integer.valueOf(value.getPort()));
                final StringWriter stringWriter = new StringWriter();
                final PrintWriter printWriter = new PrintWriter(socket.getOutputStream(), true);
                final BlockDto blockDto = block.toBlockDto();
                final String blockDtoString = objectMapper.writeValueAsString(blockDto);
                final NetworkNodesDto networkNodesDto = new NetworkNodesDto(RequestType.CONFIRM_BLOCK.name(), blockDtoString);
                final String networkNodesDtoString = objectMapper.writeValueAsString(networkNodesDto);
                this.objectMapper.writeValue(stringWriter, networkNodesDtoString);
                printWriter.println(stringWriter);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }));
    }

    @Override
    public List<TransactionCommissionResponseDto> checkCommissionFromPeers(final Integer commission) {
        final List<FutureTask> futureTasks =
                new ArrayList<>(this.networkNodes.values())
                        .stream()
                        .map(it -> (Callable<String>) () -> NetworkDataProviderImpl.this.sendCommissionRequest(it, commission))
                        .map(FutureTask::new)
                        .collect(Collectors.toList());
        futureTasks.forEach(this.executorService::execute);
        final List<String> responseList = new ArrayList<>();
        for (final FutureTask it : futureTasks) {
            try {
                final String response = (String) it.get();
                responseList.add(response);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        final List<TransactionCommissionResponseDto> responseDtoList = new ArrayList<>();
        for (final String it : responseList) {
            try {
                responseDtoList.add(this.objectMapper.readValue(it, TransactionCommissionResponseDto.class));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return responseDtoList;
    }

    private String sendCommissionRequest(final UserDto userDto, Integer commission) throws IOException {
        final Socket socket = new Socket(userDto.getIpAddress(), Integer.valueOf(userDto.getPort()));
        final StringWriter stringWriter = new StringWriter();
        final PrintWriter printWriter = new PrintWriter(socket.getOutputStream(), true);
        final TransactionCommissionRequestDto requestDto = new TransactionCommissionRequestDto(commission);
        final String requestDtoString = objectMapper.writeValueAsString(requestDto);
        final NetworkNodesDto networkNodesDto = new NetworkNodesDto(RequestType.CONFIRM_BLOCK.name(), requestDtoString);
        final String networkNodesDtoString = objectMapper.writeValueAsString(networkNodesDto);
        this.objectMapper.writeValue(stringWriter, networkNodesDtoString);
        final BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        printWriter.println(stringWriter);
        return bufferedReader.readLine();
    }

}
