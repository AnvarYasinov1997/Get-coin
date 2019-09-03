package com.getIn.getCoin.blockChain;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.getIn.getCoin.dtos.Block;
import kotlin.text.Charsets;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class BlockChainImpl implements BlockChain {

    private final String blocksFolderName;

    private final String parentFolderName;

    protected final String blocksPaths;

    protected final ObjectMapper objectMapper = new ObjectMapper();

    public BlockChainImpl(final String blocksFolderName, final String parentFolderName) {
        this.blocksFolderName = blocksFolderName;
        this.parentFolderName = parentFolderName;
        this.blocksPaths = new StringBuilder()
                .append(parentFolderName)
                .append("/")
                .append(blocksFolderName)
                .toString();
        createImageFolder(blocksFolderName, parentFolderName);
    }

    @Override
    public boolean compareCheckSum(String checkSum) throws IOException, NoSuchAlgorithmException {
        final File blockChainDir = new File(this.blocksPaths);
        final File[] blocks = blockChainDir.listFiles();
        final File zeroBlock = Objects.requireNonNull(blocks)[0];
        final String zeroBlockHash = getHash(getFileContent(zeroBlock.getName()));
        return zeroBlockHash.equals(checkSum);
    }

    @Override
    public void saveOrUpdateBlocks(final List<String> checkSums, final List<Block> blocks) {

    }

    @Override
    public void readBlock() throws Exception {
        final Integer lastBlock = getLastBlock(this.blocksPaths);
        final String fileContent = getFileContent(getFileName(0));
        final String[] listOfContents = fileContent.split("\n");
        final List<String> listOfHashes = new ArrayList<>();

        if (lastBlock > 1) {
            System.out.println("Check from blocks: ");
            for (int it = 2; it <= lastBlock; it++) {
                final String fileName = getFileName(it);
                final byte[] fileByteArray = Files.readAllBytes(Paths.get(fileName));
                final String dataJsonString = new String(fileByteArray).trim();
                final Block dataJson = objectMapper.readValue(dataJsonString, Block.class);
                listOfHashes.add(getHash(getFileContent(getFileName(it - 1))));
                if (dataJson.getHash().equals(listOfHashes.get(it - 2))) {
                    System.out.println("[Block: " + (it - 1) + "] -> Readable");
                } else {
                    System.out.println("[Block: " + (it - 1) + "] -> Corrupted");
                }
            }
        }

        System.out.println();

        listOfHashes.add(getHash(getFileContent(getFileName(lastBlock))));

        int index = 0;

        for (String it : listOfContents) {
            if (it.equals(listOfHashes.get(index))) {
                System.out.println("[Block: " + (index + 1) + "] -> Readable");
            } else {
                System.out.println("[Block: " + (index + 1) + "] -> Corrupted");
            }
            index++;
        }
    }

    @Override
    public void writeBlock(final List<String> args) throws Exception {
        final Integer lastBlock = getLastBlock(this.blocksPaths);
        final String newBlock = getFileName(lastBlock + 1);
        final String hash;
        if (lastBlock > 0) {
            hash = getHash(getFileContent(getFileName(lastBlock)));
        } else hash = "";
        final Block block = Block.builder()
                .from(args.get(0))
                .to(args.get(1))
                .amount(args.get(2))
                .hash(hash)
                .build();
        final String json = new ObjectMapper().writeValueAsString(block);
        saveNewFile(newBlock, json);
        extendZeroBlock(getHash(getFileContent(newBlock)));
    }

    private Integer getLastBlock(final String dir) throws Exception {
        Integer max = 0;
        final File file = new File(dir);
        if (!file.exists()) throw new Exception("File is not found");
        final File[] files = Objects.requireNonNull(file.listFiles());
        for (File it : files) {
            final Character maxBlockChar = it.getName().toCharArray()[0];
            final Integer maxBlock = Integer.valueOf(maxBlockChar.toString());
            if (maxBlock > max) max = maxBlock;
        }
        return max;
    }

    private void extendZeroBlock(final String hash) throws IOException {
        final String zero = getFileName(0);

        if (!new File(zero).exists()) {
            saveNewFile(zero, "");
        }

        final File zeroFile = new File(zero);

        final byte[] fileByteArray = Files.readAllBytes(zeroFile.toPath());

        final String currentContent = new String(fileByteArray);

        if (currentContent.isEmpty()) {
            updateFile(zeroFile, currentContent + hash);
        } else {
            updateFile(zeroFile, currentContent + "\n" + hash);
        }
    }

    protected String getFileContent(final String fileName) throws IOException {
        return new String(Files.readAllBytes(Paths.get(fileName)));
    }

    private String getFileName(final Integer base) {
        return new StringBuilder()
                .append(this.parentFolderName)
                .append("/")
                .append(this.blocksFolderName)
                .append("/")
                .append(base)
                .append(Constants.EXTENSION)
                .toString();
    }

    private void createImageFolder(final String imageFolderName, final String parentFolderName) {
        final File file = new File(parentFolderName, imageFolderName);
        file.setReadable(true, false);
        file.setExecutable(true, false);
        file.setWritable(true, false);
        if (!file.exists()) {
            file.mkdirs();
        }
    }

    private String getHash(final String data) throws NoSuchAlgorithmException {
        final byte[] hash = MessageDigest.getInstance("SHA-256").digest(data.getBytes(Charsets.UTF_8));
        return bytesToHex(hash);
    }

    private String bytesToHex(final byte[] hash) {
        final StringBuilder hexString = new StringBuilder();
        for (byte hash1 : hash) {
            final String hex = Integer.toHexString(0xff & hash1);
            if (hex.length() == 1) hexString.append('0');
            hexString.append(hex);
        }
        return hexString.toString();
    }

    private void saveNewFile(final String paths, final String json) {
        OutputStream outputStream = null;
        try {
            outputStream = new FileOutputStream(paths);
            outputStream.write(json.getBytes(Charsets.UTF_8));
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("trouble from file");
        } finally {
            try {
                if (outputStream != null) outputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void updateFile(final File file, final String newContent) {
        FileWriter fileWriter = null;
        try {
            fileWriter = new FileWriter(file);
            fileWriter.write(newContent);
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException("trouble with file");
        } finally {
            try {
                if (fileWriter != null) fileWriter.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static class Constants {
        private static final String EXTENSION = ".json";
    }

}
