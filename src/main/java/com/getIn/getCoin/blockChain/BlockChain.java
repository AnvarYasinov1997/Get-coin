package com.getIn.getCoin.blockChain;

import com.getIn.getCoin.dtos.Block;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.List;

public interface BlockChain {
    boolean compareCheckSum(String checkSum) throws IOException, NoSuchAlgorithmException;
    void saveOrUpdateBlocks(List<String> checkSums, List<Block> blocks);
    void readBlock() throws Exception;
    void writeBlock(final List<String> args) throws Exception;
}
