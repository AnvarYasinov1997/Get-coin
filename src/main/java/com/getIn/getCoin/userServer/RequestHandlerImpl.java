package com.getIn.getCoin.userServer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.getIn.getCoin.blockChain.Block;
import com.getIn.getCoin.blockChain.ServerBlockChain;
import com.getIn.getCoin.blockChain.Transaction;
import com.getIn.getCoin.dtos.*;
import com.getIn.getCoin.nioServer.NioServerContainer;
import com.getIn.getCoin.nioServer.NioServerImpl;

import java.io.Closeable;

public class RequestHandlerImpl implements RequestHandler {

    private final ObjectMapper objectMapper;

    private final ServerBlockChain blockChain;

    private final NetworkDataProvider networkDataProvider;

    public RequestHandlerImpl(final ObjectMapper objectMapper,
                              final ServerBlockChain blockChain,
                              final NetworkDataProvider networkDataProvider) {
        this.blockChain = blockChain;
        this.objectMapper = objectMapper;
        this.networkDataProvider = networkDataProvider;
    }

    @Override
    public void handleRequest(final String request,
                              final String socketChannelHash,
                              final Closeable closeConnectionCallback) throws Exception {
        final NetworkNodesDto networkNodesDto =
                this.objectMapper.readValue(request, NetworkNodesDto.class);
        switch (RequestType.valueOf(networkNodesDto.getRequestType())) {
            case INITIALIZE: {
                System.out.println("> Server already initialized");
            }
            case CONFIRM_BLOCK: {
                final BlockDto requestDto =
                        this.objectMapper.readValue(networkNodesDto.getDto(), BlockDto.class);
                this.blockChain.validateBlock(new Block(requestDto));
                closeConnectionCallback.close();
                System.out.println("> Block confirmed");
                break;
            }
            case UPDATE_NETWORK_NODES: {
                final DeletedNodesDto requestDto =
                        this.objectMapper.readValue(networkNodesDto.getDto(), DeletedNodesDto.class);
                this.networkDataProvider.removeNodesByIds(requestDto.getDeletedUserIds());
                closeConnectionCallback.close();
                System.out.println("> Network nodes updated...");
                break;
            }
            case CHECK_TRANSACTION_COMMISSION: {
                final TransactionCommissionRequestDto requestDto =
                        this.objectMapper.readValue(networkNodesDto.getDto(), TransactionCommissionRequestDto.class);
                final boolean approved = this.blockChain.checkMinerCommissionAmount(requestDto.getCommissionAmount());
                final TransactionCommissionResponseDto responseDto =
                        new TransactionCommissionResponseDto(approved, this.blockChain.getPublicKey());
                final String responseString = objectMapper.writeValueAsString(responseDto);
                final NioServerContainer nioServerContainer = NioServerImpl.getNioServerContainer();
                nioServerContainer.putResponse(socketChannelHash, responseString);
                break;
            }
            case ADD_TRANSACTION: {
                final TransactionDto requestDto =
                        this.objectMapper.readValue(networkNodesDto.getDto(), TransactionDto.class);
                final Transaction transaction = new Transaction(requestDto);
                final boolean approved = this.blockChain.checkMinerCommissionOutputTransaction(transaction);
                if (approved) this.blockChain.addTransaction(transaction);
                closeConnectionCallback.close();
                break;
            }
            default:
                throw new RuntimeException("Not found request type");
        }
    }

}
