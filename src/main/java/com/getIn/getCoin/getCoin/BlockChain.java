package com.getIn.getCoin.getCoin;

import com.getIn.getCoin.getCoin.json.BlockJson;
import com.getIn.getCoin.getCoin.json.TransactionOutputJson;
import org.bouncycastle.jce.provider.BouncyCastleProvider;

import java.io.File;
import java.security.Security;
import java.util.*;
import java.util.stream.Collectors;


public class BlockChain {

    private static BlockChain blockChainInstance = null;

    private static final String ROOT_DIR = "/get-coin-data";

    private static final String BLOCK_CHAIN_DIR = ROOT_DIR + "/block-chain";

    private static final String UTXOS_JSON = ROOT_DIR + "UTXOs";

    public static final Map<String, TransactionOutput> UTXOs = new HashMap<>();

    public static Long minimumTransactionAmount = 1L;

    private Integer difficulty = 6;

    private final String parentFolderDir;

    private final List<Block> blockChain;

    private final Queue<Transaction> waitTransactions;

    private BlockChain(final String parentFolderDir) {
        this.parentFolderDir = parentFolderDir;
        this.blockChain = new ArrayList<>();
        this.waitTransactions = new ArrayDeque<>();
    }

    public static BlockChain getInstance(final String parentFolderDir) {
        if (blockChainInstance != null) {
            return blockChainInstance;
        } else return new BlockChain(parentFolderDir);
    }

    public static void main(String[] args) {
    }

    public static class Test {

        private byte[] bytes;

        public Test() {
        }

        public Test(byte[] bytes) {
            this.bytes = bytes;
        }

        public byte[] getBytes() {
            return bytes;
        }

        public void setBytes(byte[] bytes) {
            this.bytes = bytes;
        }

        @Override
        public String toString() {
            return "Test{" +
                    "bytes=" + Arrays.toString(bytes) +
                    '}';
        }
    }

    public void uploadBlockChain() {
        final List<File> blockFiles = BlockChainUtils.getBlocksFromDir(getPathsByDir(BLOCK_CHAIN_DIR));
        final List<String> blockContents = blockFiles.stream().map(File::getName).map(BlockChainUtils::getFileContent).collect(Collectors.toList());
        final List<BlockJson> jsonBlocks = blockContents.stream().map(it -> BlockChainUtils.serializeStringToObject(it, BlockJson.class)).collect(Collectors.toList());
        final List<Block> blocks = jsonBlocks.stream().map(Block::new).collect(Collectors.toList());
        final File UTXOsFile = new File(getPathsByDir(UTXOS_JSON));
        final String UTXOsContent = BlockChainUtils.getFileContent(UTXOsFile.getName());
        final Map<String, TransactionOutputJson> UTXOsJson = BlockChainUtils.serializeStringToObject(UTXOsContent, HashMap.class);
        this.blockChain.addAll(blocks);
        for (Map.Entry<String, TransactionOutputJson> it : UTXOsJson.entrySet()) {
            UTXOs.put(it.getKey(), new TransactionOutput(it.getValue()));
        }
    }

    static {
        Security.addProvider(new BouncyCastleProvider());
    }

    public void mineBlock(final Block block) {
        final String target = BlockChainUtils.getDifficultyString(this.difficulty);
        while (!block.getHash().substring(0, this.difficulty).equals(target)) {
            block.incrementNonce();
            block.calculateHash();
        }
        System.out.println("Block Mined!!! : " + block.getHash());
    }

    private String getPathsByDir(final String dir) {
        return parentFolderDir + dir;
    }

//    public static void main(String[] args) {
//        Wallet getInWallet = new Wallet();
//        Wallet anvarWallet = new Wallet();
//        System.out.println(BlockChainUtils.getStringFromKey(getInWallet.getPublicKey()));
//        System.out.println(BlockChainUtils.getStringFromKey(getInWallet.getPrivateKey()));
//        System.out.println(BlockChainUtils.getStringFromKey(anvarWallet.getPublicKey()));
//        System.out.println(BlockChainUtils.getStringFromKey(anvarWallet.getPrivateKey()));
//        Transaction transaction = new Transaction(getInWallet.getPublicKey(), anvarWallet.getPublicKey(), 10000L, null);
//        transaction.generateSignature(getInWallet.getPrivateKey());
//        transaction.setTransactionId("0");
//        transaction.getOutputs().add(new TransactionOutput(transaction.getRecipient(), transaction.getTransactionId(), transaction.getAmount()));
//        UTXOs.put(transaction.getOutputs().get(0).getId(), transaction.getOutputs().get(0));
//        Block zeroBlock = new Block("0", Collections.singletonList(transaction));
//        BlockChain chain = BlockChain.getInstance("/home/anvar");
//        chain.mineBlock(zeroBlock);
//        final String result = BlockChainUtils.serializeObjectToString(zeroBlock.toBlockJson());
//        final Map<String, TransactionOutputJson> jsonsMap = new HashMap<>();
//        for (Map.Entry<String, TransactionOutput> it : UTXOs.entrySet()) {
//            jsonsMap.put(it.getKey(), it.getValue().toTransactionOutputJson());
//        }
//        final String utxResult = BlockChainUtils.serializeObjectToString(jsonsMap);
//        System.out.println("Block");
//        System.out.println(result);
//        System.out.println("UTXOs");
//        System.out.println(utxResult);
//    }

}
