package com.getIn.getCoin.dtos;

import java.util.List;

public class BlockDto {

    private String hash;

    private BlockHeaderDto blockHeader;

    private List<TransactionDto> transactions;

    public BlockDto() {
    }

    public BlockDto(final String hash,
                    final BlockHeaderDto blockHeader,
                    final List<TransactionDto> transactions) {
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

    public BlockHeaderDto getBlockHeader() {
        return blockHeader;
    }

    public void setBlockHeader(BlockHeaderDto blockHeader) {
        this.blockHeader = blockHeader;
    }

    public List<TransactionDto> getTransactions() {
        return transactions;
    }

    public void setTransactions(List<TransactionDto> transactions) {
        this.transactions = transactions;
    }

    public static class BlockHeaderDto {

        private Long nonce = 0L;

        private Long timestamp;

        private String merkleRoot;

        private String previousHash;

        private Long transactionCount;

        public BlockHeaderDto() {
        }

        public BlockHeaderDto(Long nonce, Long timestamp, String merkleRoot, String previousHash, Long transactionCount) {
            this.nonce = nonce;
            this.merkleRoot = merkleRoot;
            this.timestamp = timestamp;
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
