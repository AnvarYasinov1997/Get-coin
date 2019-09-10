package com.getIn.getCoin.blockChain;

import com.getIn.getCoin.blockChain.json.TransactionOutputJson;
import com.getIn.getCoin.dtos.TransactionOutputDto;

import java.security.PublicKey;

public class TransactionOutput {

    private final String id;

    private final PublicKey recipient;

    private final String parentTransactionId;

    private final Long amount;

    public TransactionOutput(final PublicKey recipient,
                             final String parentTransactionId,
                             final Long amount) {
        this.recipient = recipient;
        this.parentTransactionId = parentTransactionId;
        this.amount = amount;
        this.id = BlockChainUtils.getHash(generateTransactionOutputData());
    }

    public TransactionOutput(final TransactionOutputJson transactionOutputJson) {
        this.id = transactionOutputJson.getId();
        this.amount = transactionOutputJson.getAmount();
        this.parentTransactionId = transactionOutputJson.getParentTransactionId();
        this.recipient = BlockChainUtils.decodePublicKey(BlockChainUtils.getKeyBytesFromString(transactionOutputJson.getRecipient()));
    }

    public TransactionOutput(final TransactionOutputDto transactionOutputDto) {
        this.id = transactionOutputDto.getId();
        this.amount = transactionOutputDto.getAmount();
        this.parentTransactionId = transactionOutputDto.getParentTransactionId();
        this.recipient = BlockChainUtils.decodePublicKey(BlockChainUtils.getKeyBytesFromString(transactionOutputDto.getRecipient()));
    }

    public TransactionOutputJson toTransactionOutputJson() {
        final String recipientString = BlockChainUtils.getStringFromKey(this.recipient);
        return new TransactionOutputJson(this.id, recipientString, this.parentTransactionId, this.amount);
    }

    public TransactionOutputDto toTransactionOutputDto() {
        final String recipientString = BlockChainUtils.getStringFromKey(this.recipient);
        return new TransactionOutputDto(this.id, recipientString, this.parentTransactionId, this.amount);
    }

    public boolean isMine(final PublicKey publicKey) {
        return publicKey.equals(recipient);
    }

    private String generateTransactionOutputData() {
        return new StringBuilder()
                .append(BlockChainUtils.getStringFromKey(this.recipient))
                .append(this.amount)
                .append(this.parentTransactionId)
                .toString();
    }

    public String getId() {
        return id;
    }

    public Long getAmount() {
        return amount;
    }

    public PublicKey getRecipient() {
        return recipient;
    }
}
