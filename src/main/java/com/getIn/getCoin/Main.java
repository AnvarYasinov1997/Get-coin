package com.getIn.getCoin;

import com.getIn.getCoin.blockChain.BlockChain;
import com.getIn.getCoin.blockChain.BlockChainImpl;

import java.util.Arrays;

public class Main {

    private static final String DEFAULT_BLOCKCHAIN_DIR = "Blockchain";

    private static final String DEFAULT_PARENT_FOLDER_DIR = "/home/anvar";

    public static void main(String[] args) throws Exception {
        final String blocksFolderName = DEFAULT_BLOCKCHAIN_DIR;
        final String parentFolderName = DEFAULT_PARENT_FOLDER_DIR;
        final BlockChain blockChain = new BlockChainImpl(blocksFolderName, parentFolderName);
        if (args[0].equals("-r") || args[0].equals("--read")) {
            blockChain.readBlock();
        }
        if (args[0].equals("-w") || args[0].equals("--write")) {
            blockChain.writeBlock(Arrays.asList("Anvar", "Vasya", "1000"));
        }
    }

    private static void checkArgs(String... args) throws Exception {
        if (args.length == 0) throw new Exception("Введите аргументы");
        if (args[0].equals("-r") || args[0].equals("--read")) {
            return;
        }
        if (args[0].equals("-w") || args[0].equals("--write")) {
            if (args.length != 4) throw new Exception("Колличество аргументов при записименьше 4");
        }
        throw new Exception("Вы ввели неправильные ключи");
    }
}
