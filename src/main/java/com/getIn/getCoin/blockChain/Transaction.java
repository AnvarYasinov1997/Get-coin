package com.getIn.getCoin.blockChain;

import com.getIn.getCoin.blockChain.json.TransactionInputJson;
import com.getIn.getCoin.blockChain.json.TransactionJson;
import com.getIn.getCoin.blockChain.json.TransactionOutputJson;
import com.getIn.getCoin.dtos.TransactionDto;
import com.getIn.getCoin.dtos.TransactionInputDto;
import com.getIn.getCoin.dtos.TransactionOutputDto;

import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class Transaction {

    private String transactionId;

    private byte[] signature;

    private final PublicKey sender;

    private final PublicKey recipient;

    private final Long amount;

    private final List<TransactionInput> inputs;

    private final List<TransactionOutput> outputs;

    private static int sequence = 0;

    public Transaction(final PublicKey from,
                       final PublicKey to,
                       final Long amount,
                       final List<TransactionInput> inputs) {
        this.sender = from;
        this.recipient = to;
        this.amount = amount;
        this.inputs = inputs;
        this.outputs = new ArrayList<>();
    }

    public Transaction(final TransactionJson transactionJson) {
        this.amount = transactionJson.getAmount();
        this.signature = transactionJson.getSignature();
        this.transactionId = transactionJson.getTransactionId();
        this.sender = BlockChainUtils.decodePublicKey(BlockChainUtils.getKeyBytesFromString(transactionJson.getSender()));
        this.recipient = BlockChainUtils.decodePublicKey(BlockChainUtils.getKeyBytesFromString(transactionJson.getRecipient()));
        this.inputs = transactionJson.getInputs().stream().map(TransactionInput::new).collect(Collectors.toList());
        this.outputs = transactionJson.getOutputs().stream().map(TransactionOutput::new).collect(Collectors.toList());
        sequence = transactionJson.getSequence();
    }

    public Transaction(final TransactionDto transactionDto) {
        this.amount = transactionDto.getAmount();
        this.signature = transactionDto.getSignature();
        this.transactionId = transactionDto.getTransactionId();
        this.sender = BlockChainUtils.decodePublicKey(BlockChainUtils.getKeyBytesFromString(transactionDto.getSender()));
        this.recipient = BlockChainUtils.decodePublicKey(BlockChainUtils.getKeyBytesFromString(transactionDto.getRecipient()));
        this.inputs = transactionDto.getInputs().stream().map(TransactionInput::new).collect(Collectors.toList());
        this.outputs = transactionDto.getOutputs().stream().map(TransactionOutput::new).collect(Collectors.toList());
        sequence = transactionDto.getSequence();
    }

    public TransactionJson toTransactionJson() {
        final String senderString  = BlockChainUtils.getStringFromKey(this.sender);
        final String recipientString  = BlockChainUtils.getStringFromKey(this.recipient);
        final List<TransactionInputJson> transactionInputJsons = this.inputs.stream().map(TransactionInput::toTransactionInputJson).collect(Collectors.toList());
        final List<TransactionOutputJson> transactionOutputJsons = this.outputs.stream().map(TransactionOutput::toTransactionOutputJson).collect(Collectors.toList());
        return new TransactionJson(this.transactionId, this.signature, senderString, recipientString, this.amount, transactionInputJsons, transactionOutputJsons, sequence);
    }

    public TransactionDto toTransactionDto() {
        final String senderString = BlockChainUtils.getStringFromKey(this.sender);
        final String recipientString = BlockChainUtils.getStringFromKey(this.recipient);
        final List<TransactionInputDto> transactionInputDtos = this.inputs.stream().map(TransactionInput::toTransactionInputDto).collect(Collectors.toList());
        final List<TransactionOutputDto> transactionOutputDtos = this.outputs.stream().map(TransactionOutput::toTransactionOutputDto).collect(Collectors.toList());
        return new TransactionDto(this.transactionId, this.signature, senderString, recipientString, this.amount, transactionInputDtos, transactionOutputDtos, sequence);
    }

    public boolean processTransaction() {
        if (!verifySignature()) {
            System.out.println("#Transaction Signature failed to verify");
            return false;
        }

        for (TransactionInput inputs : inputs) {
            inputs.setUTXO(BlockChainImpl.UTXOs.get(inputs.getTransactionOutputId()));
        }

        if (this.getInputsAmount() < BlockChainImpl.minimumTransactionAmount) {
            System.out.println("Transaction Inputs too small: " + getInputsAmount());
            System.out.println("Please enter the amount greater than " + BlockChainImpl.minimumTransactionAmount);
            return false;
        }

        final Long leftOver = this.getInputsAmount() - amount;
        this.transactionId = this.calculateHash();
        outputs.add(new TransactionOutput(this.recipient, this.transactionId, this.amount));
        outputs.add(new TransactionOutput(this.sender, this.transactionId, leftOver));

        for (final TransactionOutput outputs : outputs) {
            BlockChainImpl.UTXOs.put(outputs.getId(), outputs);
        }

        for (final TransactionInput inputs : inputs) {
            if (inputs.getUTXO() == null) continue;
            BlockChainImpl.UTXOs.remove(inputs.getUTXO().getId());
        }

        return true;
    }

    public void generateSignature(final PrivateKey privateKey) {
        final String data = new StringBuilder()
                .append(BlockChainUtils.getStringFromKey(this.sender))
                .append(BlockChainUtils.getStringFromKey(this.recipient))
                .append(this.amount)
                .toString();
        this.signature = BlockChainUtils.applyECDSASig(privateKey, data);
    }

    public boolean verifySignature() {
        final String data = new StringBuilder()
                .append(BlockChainUtils.getStringFromKey(this.sender))
                .append(BlockChainUtils.getStringFromKey(this.recipient))
                .append(this.amount)
                .toString();
        return BlockChainUtils.verifyECDSASig(this.sender, data, this.signature);
    }

    public Long getInputsAmount() {
        Long total = 0L;
        for (TransactionInput i : inputs) {
            if (i.getUTXO() == null) continue;
            total += i.getUTXO().getAmount();
        }
        return total;
    }

    public Long getOutputsAmount() {
        Long total = 0L;
        for (TransactionOutput o : outputs) {
            total += o.getAmount();
        }
        return total;
    }

    private String calculateHash() {
        return BlockChainUtils.getHash(generateTransactionData());
    }

    private String generateTransactionData() {
        sequence++;
        return new StringBuilder()
                .append(BlockChainUtils.getStringFromKey(this.sender))
                .append(BlockChainUtils.getStringFromKey(this.recipient))
                .append(this.amount)
                .append(sequence)
                .toString();
    }

    public String getTransactionId() {
        return transactionId;
    }

    public List<TransactionInput> getInputs() {
        return inputs;
    }

    public List<TransactionOutput> getOutputs() {
        return outputs;
    }

    public PublicKey getRecipient() {
        return recipient;
    }

    public PublicKey getSender() {
        return sender;
    }
}
