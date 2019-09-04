package com.getIn.getCoin.getCoin;

import com.fasterxml.jackson.core.type.TypeReference;
import com.getIn.getCoin.getCoin.json.BlockJson;
import com.getIn.getCoin.getCoin.json.TransactionOutputJson;
import javafx.util.Pair;
import org.bouncycastle.jce.provider.BouncyCastleProvider;

import java.io.File;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Security;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


public class BlockChain {

    private static BlockChain blockChainInstance = null;

    private static final String ROOT_DIR = "/Getcoin";

    private static final String BLOCK_CHAIN_DIR = ROOT_DIR + "/Blockchain";

    private static final String UTXOS_JSON = ROOT_DIR + "/UTXOs.json";

    public static final Map<String, TransactionOutput> UTXOs = new HashMap<>();

    public static Long minimumTransactionAmount = 1L;

    private Integer difficulty = 6;

    private final String parentFolderDir;

    private final List<Block> blockChain;

    private final List<Transaction> waitTransactions;

    private final boolean mineActive = false;

    private BlockChain(final String parentFolderDir) {
        this.parentFolderDir = parentFolderDir;
        this.blockChain = new ArrayList<>();
        this.waitTransactions = new ArrayList<>();
    }

    public static BlockChain getInstance(final String parentFolderDir) {
        if (blockChainInstance != null) {
            return blockChainInstance;
        } else return new BlockChain(parentFolderDir);
    }

    public void uploadBlockChain() {
        final List<File> blockFiles = BlockChainUtils.getBlocksFromDir(getPathsByDir(BLOCK_CHAIN_DIR));
        final List<String> blockContents = blockFiles.stream().map(File::getPath).map(BlockChainUtils::getFileContent).collect(Collectors.toList());
        final List<BlockJson> jsonBlocks = blockContents.stream().map(it -> BlockChainUtils.serializeStringToObject(it, BlockJson.class)).collect(Collectors.toList());
        final List<Block> blocks = jsonBlocks.stream().map(Block::new).collect(Collectors.toList());
        final File UTXOsFile = new File(getPathsByDir(UTXOS_JSON));
        final String UTXOsContent = BlockChainUtils.getFileContent(UTXOsFile.getPath());
        final Map<String, TransactionOutputJson> UTXOsJson = BlockChainUtils.serializeStringToHashMap(UTXOsContent, new TypeReference<HashMap<String, TransactionOutputJson>>() {});
        this.blockChain.addAll(blocks);
        for (Map.Entry<String, TransactionOutputJson> it : UTXOsJson.entrySet()) {
            UTXOs.put(it.getKey(), new TransactionOutput(it.getValue()));
        }
    }

    static {
        Security.addProvider(new BouncyCastleProvider());
    }

    public Wallet generateNewWallet() {
        final Pair<PublicKey, PrivateKey> keys = Wallet.createNewWallet();
        System.out.println("> Public key: " + BlockChainUtils.getStringFromKey(keys.getKey()));
        System.out.println("> Private key: " + BlockChainUtils.getStringFromKey(keys.getValue()));
        return new Wallet(keys.getKey(), keys.getValue());
    }

    public List<Block> selectBlocks() {
        return this.blockChain;
    }

    public List<TransactionOutput> selectUTXOs() {
        final List<TransactionOutput> UTXOsList = new ArrayList<>();
        for (final Map.Entry<String, TransactionOutput> it : UTXOs.entrySet()) {
            UTXOsList.add(it.getValue());
        }
        return UTXOsList;
    }

    public Wallet getWallet(final String publicKey, final String privateKey) {
        return new Wallet(publicKey, privateKey);
    }

    public boolean mineBlock(final Block block) {
        final String target = BlockChainUtils.getDifficultyString(this.difficulty);
        while (!block.getHash().substring(0, this.difficulty).equals(target) && mineActive) {
            block.incrementNonce();
            block.calculateHash();
        }
        if (!mineActive) {
            System.out.println("> Block Mined!!! : " + block.getHash());
            return true;
        }
        System.out.println("> Mine stopped!!!");
        return false;
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
