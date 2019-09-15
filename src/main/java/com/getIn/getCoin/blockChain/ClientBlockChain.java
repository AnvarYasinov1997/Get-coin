package com.getIn.getCoin.blockChain;

public interface ClientBlockChain {
    Integer getCommission();

    void generateNewWallet();

    void generateWallet(final String publicKey, final String privateKey);

    boolean removeWallet();

    Wallet getUserWallet();

    Block generateBlock();

    Transaction createTransaction(final String recipientPublicKeyString, final Long amount);

    Block mineBlock(final Block block) throws Exception;

    void enableMineMode();

    void disableMineMode();
}
