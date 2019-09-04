package com.getIn.getCoin.blockChain.json;

public class TransactionOutputJson {

    private String id;

    private String recipient;

    private String parentTransactionId;

    private Long amount;

    public TransactionOutputJson() {
    }

    public TransactionOutputJson(final String id,
                                 final String recipient,
                                 final String parentTransactionId,
                                 final Long amount) {
        this.id = id;
        this.recipient = recipient;
        this.parentTransactionId = parentTransactionId;
        this.amount = amount;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getRecipient() {
        return recipient;
    }

    public void setRecipient(String recipient) {
        this.recipient = recipient;
    }

    public String getParentTransactionId() {
        return parentTransactionId;
    }

    public void setParentTransactionId(String parentTransactionId) {
        this.parentTransactionId = parentTransactionId;
    }

    public Long getAmount() {
        return amount;
    }

    public void setAmount(Long amount) {
        this.amount = amount;
    }
}
