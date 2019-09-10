package com.getIn.getCoin.blockChain;

import com.fasterxml.jackson.core.type.TypeReference;
import com.getIn.getCoin.blockChain.json.BlockJson;
import com.getIn.getCoin.blockChain.json.TransactionOutputJson;
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

    private Long winnerTransferAmount = 50L;

    public static Long minimumTransactionAmount = 1L;

    private Integer difficulty = 6;

    private Wallet userWallet;

    private final PublicKey userPublicKey;

    private final String parentFolderDir;

    private final List<Block> blockChain;

    private final List<Transaction> waitTransactions;

    private boolean mineMode = false;

    private BlockChain(final String parentFolderDir, final String userPublicKey) {
        this.userPublicKey = BlockChainUtils.decodePublicKey(BlockChainUtils.getKeyBytesFromString(userPublicKey));
        this.parentFolderDir = parentFolderDir;
        this.blockChain = new ArrayList<>();
        this.waitTransactions = new ArrayList<>();
    }

    static {
        Security.addProvider(new BouncyCastleProvider());
    }

    public static BlockChain getInstance(final String parentFolderDir, final String userPublicKey) {
        if (blockChainInstance != null) {
            return blockChainInstance;
        } else return new BlockChain(parentFolderDir, userPublicKey);
    }

    public void uploadBlockChainFromServerData(final List<Block> blockChain) {
        this.blockChain.addAll(blockChain);
    }

    public void uploadBlockChain() {
        final List<File> blockFiles = BlockChainUtils.getBlocksFromDir(getPathsByDir(BLOCK_CHAIN_DIR));
        final List<String> blockContents = blockFiles.stream().map(File::getPath).map(BlockChainUtils::getFileContent).collect(Collectors.toList());
        final List<BlockJson> jsonBlocks = blockContents.stream().map(it -> BlockChainUtils.serializeStringToObject(it, BlockJson.class)).collect(Collectors.toList());
        final List<Block> blocks = jsonBlocks.stream().map(Block::new).collect(Collectors.toList());
        final File UTXOsFile = new File(getPathsByDir(UTXOS_JSON));
        final String UTXOsContent = BlockChainUtils.getFileContent(UTXOsFile.getPath());
        final Map<String, TransactionOutputJson> UTXOsJson = BlockChainUtils.serializeStringToHashMap(UTXOsContent, new TypeReference<HashMap<String, TransactionOutputJson>>() {
        });
        this.blockChain.addAll(blocks);
        for (Map.Entry<String, TransactionOutputJson> it : UTXOsJson.entrySet()) {
            UTXOs.put(it.getKey(), new TransactionOutput(it.getValue()));
        }
    }

    public void generateNewWallet() {
        if (this.userWallet == null) {
            final Pair<PublicKey, PrivateKey> keys = Wallet.createNewWallet();
            System.out.println("> Public key: " + BlockChainUtils.getStringFromKey(keys.getKey()));
            System.out.println("> Private key: " + BlockChainUtils.getStringFromKey(keys.getValue()));
            this.userWallet = new Wallet(keys.getKey(), keys.getValue());
        }
    }

    public void generateWallet(final String publicKey, final String privateKey) {
        if (this.userWallet == null) {
            this.userWallet = new Wallet(publicKey, privateKey);
        }
    }

    public boolean removeWallet() {
        if (userWallet == null) return false;
        else userWallet = null;
        return true;
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

    public Wallet getUserWallet() {
        return this.userWallet;
    }

    public Block generateBlock() {
        final Block previousBlock = this.blockChain.get(this.blockChain.size() - 1);
        final List<Transaction> transactions = new ArrayList<>(this.waitTransactions);
        final Block newBlock = new Block(previousBlock.getHash(), this.userPublicKey, this.winnerTransferAmount, transactions);
        this.waitTransactions.clear();
        return newBlock;
    }

    public Transaction createTransaction(final String recipientPublicKeyString, final Long amount) {
        final PublicKey recipientPublicKey = BlockChainUtils.decodePublicKey(BlockChainUtils.getKeyBytesFromString(recipientPublicKeyString));
        final Transaction transaction = this.userWallet.generateTransaction(recipientPublicKey, amount);
        if (mineMode) waitTransactions.add(transaction);
        return transaction;
    }

    public Block mineBlock(final Block block) {
        final String target = BlockChainUtils.getDifficultyString(this.difficulty);
        while (!block.getHash().substring(0, this.difficulty).equals(target) && this.mineMode) {
            block.incrementNonce();
            block.calculateHash();
        }
        if (this.mineMode) {
            System.out.println("> Block Mined!!! : " + block.getHash());
            return block;
        }
        System.out.println("> Mine stopped!!!");
        return null;
    }

    public void enableMineMode() {
        this.mineMode = true;
    }

    public void disableMineMode() {
        this.mineMode = false;
    }

    private String getPathsByDir(final String dir) {
        return this.parentFolderDir + dir;
    }

    public synchronized boolean validateBlock(final Block block) {
        this.blockChain.add(block);
        if (isChainValid()) return true;
        else this.blockChain.remove(this.blockChain.size() - 1);
        return false;
    }

    private boolean isChainValid() {
        Block currentBlock;
        Block previousBlock;
        String hashTarget = new String(new char[difficulty]).replace('\0', '0');
        HashMap<String, TransactionOutput> tempUTXOs = new HashMap<>();
        final Transaction zeroTransaction = blockChain.get(0).getTransactions().get(0);
        final TransactionOutput zeroTransactionOutput = zeroTransaction.getOutputs().get(0);
        tempUTXOs.put(zeroTransactionOutput.getId(), zeroTransactionOutput);

        for (int i = 1; i < this.blockChain.size(); i++) {

            currentBlock = blockChain.get(i);
            previousBlock = blockChain.get(i - 1);

            if (!currentBlock.getHash().equals(currentBlock.calculateHash())) {
                System.out.println("#Current Hashes not equal");
                return false;
            }

            if (!previousBlock.getHash().equals(currentBlock.getPreviousHash())) {
                System.out.println("#Previous Hashes not equal");
                return false;
            }

            if (!currentBlock.getHash().substring(0, difficulty).equals(hashTarget)) {
                System.out.println("#This block hasn't been mined");
                return false;
            }

            TransactionOutput tempOutput;
            for (int t = 0; t < currentBlock.getTransactions().size(); t++) {
                Transaction currentTransaction = currentBlock.getTransactions().get(t);

                if (!currentTransaction.verifySignature()) {
                    System.out.println("#Signature on Transaction(" + t + ") is Invalid");
                    return false;
                }
                if (!currentTransaction.getInputsAmount().equals(currentTransaction.getOutputsAmount())) {
                    System.out.println("#Inputs are note equal to outputs on Transaction(" + t + ")");
                    return false;
                }

                for (TransactionInput input : currentTransaction.getInputs()) {
                    tempOutput = tempUTXOs.get(input.getTransactionOutputId());

                    if (tempOutput == null) {
                        System.out.println("#Referenced input on Transaction(" + t + ") is Missing");
                        return false;
                    }

                    if (!input.getUTXO().getAmount().equals(tempOutput.getAmount())) {
                        System.out.println("#Referenced input Transaction(" + t + ") value is Invalid");
                        return false;
                    }

                    tempUTXOs.remove(input.getTransactionOutputId());
                }

                for (TransactionOutput output : currentTransaction.getOutputs()) {
                    tempUTXOs.put(output.getId(), output);
                }

                if (currentTransaction.getOutputs().get(0).getRecipient() != currentTransaction.getRecipient()) {
                    System.out.println("#Transaction(" + t + ") output reciepient is not who it should be");
                    return false;
                }
                if (currentTransaction.getOutputs().get(1).getRecipient() != currentTransaction.getSender()) {
                    System.out.println("#Transaction(" + t + ") output 'change' is not sender.");
                    return false;
                }

            }

        }
        System.out.println("Blockchain is valid");
        return true;
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
