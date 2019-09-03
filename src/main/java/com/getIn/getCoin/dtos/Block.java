package com.getIn.getCoin.dtos;

public class Block {
    private String from;
    private String to;
    private String amount;
    private String hash;

    public Block() {

    }

    public Block(final String from,
                 final String to,
                 final String amount,
                 final String hash) {
        this.from = from;
        this.to = to;
        this.amount = amount;
        this.hash = hash;
    }

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public String getTo() {
        return to;
    }

    public void setTo(String to) {
        this.to = to;
    }

    public String getAmount() {
        return amount;
    }

    public void setAmount(String amount) {
        this.amount = amount;
    }

    public String getHash() {
        return hash;
    }

    public void setHash(String hash) {
        this.hash = hash;
    }

    public static BlockBuilder builder() {
        return new BlockBuilder();
    }

    public static class BlockBuilder {
        private String from;
        private String to;
        private String amount;
        private String hash;

        public BlockBuilder from(final String from) {
            this.from = from;
            return this;
        }

        public BlockBuilder to(final String to) {
            this.to = to;
            return this;
        }

        public BlockBuilder amount(final String amount) {
            this.amount = amount;
            return this;
        }

        public BlockBuilder hash(final String hash) {
            this.hash = hash;
            return this;
        }

        public Block build() {
            return new Block(from, to, amount, hash);
        }
    }
}
