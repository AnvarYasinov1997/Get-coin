package com.getIn.getCoin.blockChain.json;

import java.util.List;

public class TransactionJson {

    private String transactionId;

    private byte[] signature;

    private String sender;

    private String recipient;

    private String processer;

    private Long commissionAmount;

    private Long amount;

    private List<TransactionInputJson> inputs;

    private List<TransactionOutputJson> outputs;

    private int sequence;

    public TransactionJson() {
    }

    public TransactionJson(final String transactionId,
                           final byte[] signature,
                           final String sender,
                           final String recipient,
                           final String processer,
                           final Long commissionAmount,
                           final Long amount,
                           final List<TransactionInputJson> inputs,
                           final List<TransactionOutputJson> outputs,
                           final int sequence) {
        this.transactionId = transactionId;
        this.signature = signature;
        this.sender = sender;
        this.recipient = recipient;
        this.processer = processer;
        this.commissionAmount = commissionAmount;
        this.amount = amount;
        this.inputs = inputs;
        this.outputs = outputs;
        this.sequence = sequence;
    }

    public String getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }

    public byte[] getSignature() {
        return signature;
    }

    public void setSignature(byte[] signature) {
        this.signature = signature;
    }

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public String getRecipient() {
        return recipient;
    }

    public void setRecipient(String recipient) {
        this.recipient = recipient;
    }

    public String getProcesser() {
        return processer;
    }

    public void setProcesser(String processer) {
        this.processer = processer;
    }

    public Long getCommissionAmount() {
        return commissionAmount;
    }

    public void setCommissionAmount(Long commissionAmount) {
        this.commissionAmount = commissionAmount;
    }

    public Long getAmount() {
        return amount;
    }

    public void setAmount(Long amount) {
        this.amount = amount;
    }

    public List<TransactionInputJson> getInputs() {
        return inputs;
    }

    public void setInputs(List<TransactionInputJson> inputs) {
        this.inputs = inputs;
    }

    public List<TransactionOutputJson> getOutputs() {
        return outputs;
    }

    public void setOutputs(List<TransactionOutputJson> outputs) {
        this.outputs = outputs;
    }

    public int getSequence() {
        return sequence;
    }

    public void setSequence(int sequence) {
        this.sequence = sequence;
    }
}
