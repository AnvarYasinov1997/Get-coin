package com.getIn.getCoin.dtos;

import java.util.List;

public class TransactionDto {

    private String transactionId;

    private byte[] signature;

    private String sender;

    private String recipient;

    private String processer;

    private Long commissionAmount;

    private Long amount;

    private List<TransactionInputDto> inputs;

    private List<TransactionOutputDto> outputs;

    private int sequence;

    public TransactionDto() {
    }

    public TransactionDto(final String transactionId,
                          final byte[] signature,
                          final String sender,
                          final String recipient,
                          final String processer,
                          final Long commissionAmount,
                          final Long amount,
                          final List<TransactionInputDto> inputs,
                          final List<TransactionOutputDto> outputs,
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

    public List<TransactionInputDto> getInputs() {
        return inputs;
    }

    public void setInputs(List<TransactionInputDto> inputs) {
        this.inputs = inputs;
    }

    public List<TransactionOutputDto> getOutputs() {
        return outputs;
    }

    public void setOutputs(List<TransactionOutputDto> outputs) {
        this.outputs = outputs;
    }

    public int getSequence() {
        return sequence;
    }

    public void setSequence(int sequence) {
        this.sequence = sequence;
    }
}
