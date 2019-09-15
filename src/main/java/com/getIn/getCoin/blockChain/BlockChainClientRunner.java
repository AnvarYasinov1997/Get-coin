package com.getIn.getCoin.blockChain;

import java.util.List;

public interface BlockChainClientRunner {
    void uploadBlockChainFromServerData(final List<Block> blockChain);
}
