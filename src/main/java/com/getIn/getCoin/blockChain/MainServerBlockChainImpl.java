package com.getIn.getCoin.blockChain;

import com.getIn.getCoin.dtos.Block;
import com.getIn.getCoin.dtos.InitializeDto;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class MainServerBlockChainImpl extends BlockChainImpl implements MainServerBlockChain {

    public MainServerBlockChainImpl(final String blocksFolderName,
                                    final String parentFolderName) {
        super(blocksFolderName, parentFolderName);
    }

    @Override
    public InitializeDto selectBlocks() throws Exception {
        final File blockChainDir = new File(super.blocksPaths);
        final File[] blocks = blockChainDir.listFiles();
        final File zeroBlock = Objects.requireNonNull(blocks)[0];
        final String[] checkSums = getFileContent(zeroBlock.getName()).split("\n");
        final List<Block> blocksData = new ArrayList<>();
        for (int i = 1; i < blocks.length; i++) {
            final String blockData = getFileContent(blocks[i].getName());
            final Block block = objectMapper.readValue(blockData, Block.class);
            blocksData.add(block);
        }
        return new InitializeDto(Arrays.asList(checkSums), blocksData);
    }

}
