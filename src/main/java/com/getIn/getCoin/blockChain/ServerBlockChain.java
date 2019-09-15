package com.getIn.getCoin.blockChain;

public interface ServerBlockChain {
    void validateBlock(final Block block) throws Exception;

    boolean checkMinerCommissionAmount(final Integer requestCommissionAmount);

    boolean checkMinerCommissionOutputTransaction(final Transaction transaction);

    void addTransaction(final Transaction transaction);

    String getPublicKey();
}
