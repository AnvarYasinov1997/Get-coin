package com.getIn.getCoin.userServer;

import com.getIn.getCoin.blockChain.Block;
import com.getIn.getCoin.dtos.TransactionCommissionResponseDto;
import com.getIn.getCoin.dtos.UserDto;

import java.util.List;

public interface NetworkDataProvider {
    void removeNodesByIds(final List<String> nodeIds);

    void sendBlockToPeers(final Block block);

    int getNodesCount();

    void addNodes(final List<UserDto> userDtos);

    List<TransactionCommissionResponseDto> checkCommissionFromPeers(final Integer commission);
}
