package com.getIn.getCoin.blockChain;

import com.getIn.getCoin.blockChain.json.BlockJson;
import com.getIn.getCoin.blockChain.json.TransactionJson;
import com.getIn.getCoin.dtos.BlockDto;
import com.getIn.getCoin.dtos.TransactionDto;

import java.security.PublicKey;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

public class Block {

    private String hash;

    private final List<Transaction> transactions;

    private final BlockHeader blockHeader;

    public Block(final String previousHash,
                 final PublicKey winnerPublicKey,
                 final Long winnerTransferAmount,
                 final List<Transaction> transactions) {
        this.transactions = new ArrayList<>();
        final Transaction zeroTransaction = new Transaction(null, winnerPublicKey, winnerTransferAmount, null);
        this.transactions.add(zeroTransaction);
        this.transactions.addAll(this.addTransaction(transactions));
        final String merkleRoot = BlockChainUtils.getMerkleRoot(this.transactions);
        this.blockHeader = new BlockHeader(previousHash, merkleRoot, Long.valueOf(this.transactions.size()));
        calculateHash();
    }

    public Block(final BlockJson blockJson) {
        this.hash = blockJson.getHash();
        this.blockHeader = new BlockHeader(blockJson.getBlockHeader());
        this.transactions = blockJson.getTransactions().stream().map(Transaction::new).collect(Collectors.toList());
    }

    public Block(final BlockDto blockDto) {
        this.hash = blockDto.getHash();
        this.blockHeader = new BlockHeader(blockDto.getBlockHeader());
        this.transactions = blockDto.getTransactions().stream().map(Transaction::new).collect(Collectors.toList());
    }

    public BlockJson toBlockJson() {
        final List<TransactionJson> transactionJsons = this.transactions.stream().map(Transaction::toTransactionJson).collect(Collectors.toList());
        return new BlockJson(this.hash, this.blockHeader.toBlockHeaderJson(), transactionJsons);
    }

    public BlockDto toBlockDto() {
        final List<TransactionDto> transactionDtos = this.transactions.stream().map(Transaction::toTransactionDto).collect(Collectors.toList());
        return new BlockDto(this.hash, this.blockHeader.toBlockHeaderDto(), transactionDtos);
    }

    public String getHash() {
        return hash;
    }

    public void incrementNonce() {
        this.blockHeader.incrementNonce();
    }

    public void calculateHash() {
        final String blockHeaderJson = BlockChainUtils.serializeObjectToString(this.blockHeader.toBlockHeaderJson());
        this.hash = BlockChainUtils.getHash(blockHeaderJson);
    }

    private List<Transaction> addTransaction(final List<Transaction> transactions) {
        return transactions.stream().filter(this::validateTransaction).collect(Collectors.toList());
    }

    private boolean validateTransaction(final Transaction transaction) {
        if (transaction == null) return false;
        if (!transaction.processTransaction()) {
            System.out.println("Transaction failed to process. Discarded.");
            return false;
        }
        System.out.println("Transaction Successfully added to Block");
        return true;
    }

    private class BlockHeader {
        private Long nonce = 0L;

        private final Long timestamp;

        private final String merkleRoot;

        private final String previousHash;

        private final Long transactionCount;

        public BlockHeader(final String previousHash,
                           final String merkleRoot,
                           final Long transactionCount) {
            this.previousHash = previousHash;
            this.transactionCount = transactionCount;
            this.merkleRoot = merkleRoot;
            this.timestamp = new Date().getTime();
        }

        public BlockHeader(final BlockJson.BlockHeaderJson blockHeaderJson) {
            this.nonce = blockHeaderJson.getNonce();
            this.timestamp = blockHeaderJson.getTimestamp();
            this.merkleRoot = blockHeaderJson.getMerkleRoot();
            this.previousHash = blockHeaderJson.getPreviousHash();
            this.transactionCount = blockHeaderJson.getTransactionCount();
        }

        public BlockHeader(final BlockDto.BlockHeaderDto blockHeaderDto) {
            this.nonce = blockHeaderDto.getNonce();
            this.timestamp = blockHeaderDto.getTimestamp();
            this.merkleRoot = blockHeaderDto.getMerkleRoot();
            this.previousHash = blockHeaderDto.getPreviousHash();
            this.transactionCount = blockHeaderDto.getTransactionCount();
        }

        public BlockJson.BlockHeaderJson toBlockHeaderJson() {
            return new BlockJson.BlockHeaderJson(this.nonce, this.timestamp, this.merkleRoot, this.previousHash, this.transactionCount);
        }

        public BlockDto.BlockHeaderDto toBlockHeaderDto() {
            return new BlockDto.BlockHeaderDto(this.nonce, this.timestamp, this.merkleRoot, this.previousHash, this.transactionCount);
        }

        public void incrementNonce() {
            nonce++;
        }
    }
}
