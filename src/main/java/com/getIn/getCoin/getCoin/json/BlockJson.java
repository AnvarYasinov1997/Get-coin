package com.getIn.getCoin.getCoin.json;

import java.util.List;

public class BlockJson {

    private String hash;

    private BlockHeaderJson blockHeader;

    private List<TransactionJson> transactions;

    public BlockJson() {
    }

    public BlockJson(final String hash,
                     final BlockHeaderJson blockHeader,
                     final List<TransactionJson> transactions) {
        this.hash = hash;
        this.blockHeader = blockHeader;
        this.transactions = transactions;
    }

    public String getHash() {
        return hash;
    }

    public void setHash(String hash) {
        this.hash = hash;
    }

    public BlockHeaderJson getBlockHeader() {
        return blockHeader;
    }

    public void setBlockHeader(BlockHeaderJson blockHeader) {
        this.blockHeader = blockHeader;
    }

    public List<TransactionJson> getTransactions() {
        return transactions;
    }

    public void setTransactions(List<TransactionJson> transactions) {
        this.transactions = transactions;
    }

    public static class BlockHeaderJson {

        private Long nonce = 0L;

        private Long timestamp;

        private String merkleRoot;

        private String previousHash;

        private Long transactionCount;

        public BlockHeaderJson() {
        }

        public BlockHeaderJson(Long nonce, Long timestamp, String merkleRoot, String previousHash, Long transactionCount) {
            this.nonce = nonce;
            this.timestamp = timestamp;
            this.merkleRoot = merkleRoot;
            this.previousHash = previousHash;
            this.transactionCount = transactionCount;
        }

        public Long getNonce() {
            return nonce;
        }

        public void setNonce(Long nonce) {
            this.nonce = nonce;
        }

        public Long getTimestamp() {
            return timestamp;
        }

        public void setTimestamp(Long timestamp) {
            this.timestamp = timestamp;
        }

        public String getMerkleRoot() {
            return merkleRoot;
        }

        public void setMerkleRoot(String merkleRoot) {
            this.merkleRoot = merkleRoot;
        }

        public String getPreviousHash() {
            return previousHash;
        }

        public void setPreviousHash(String previousHash) {
            this.previousHash = previousHash;
        }

        public Long getTransactionCount() {
            return transactionCount;
        }

        public void setTransactionCount(Long transactionCount) {
            this.transactionCount = transactionCount;
        }
    }

}
