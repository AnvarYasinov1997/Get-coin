package com.getIn.getCoin.blockChain;

import com.getIn.getCoin.blockChain.json.TransactionInputJson;
import com.getIn.getCoin.dtos.TransactionInputDto;

public class TransactionInput {

    private final String transactionOutputId;

    private TransactionOutput UTXO;

    public TransactionInput(final String transactionOutputId) {
        this.transactionOutputId = transactionOutputId;
    }

    public TransactionInput(final TransactionInputJson transactionInputJson) {
        this.transactionOutputId = transactionInputJson.getTransactionOutputId();
        this.UTXO = new TransactionOutput(transactionInputJson.getUTXO());
    }

    public TransactionInput(final TransactionInputDto transactionInputDto) {
        this.transactionOutputId = transactionInputDto.getTransactionOutputId();
        this.UTXO = new TransactionOutput(transactionInputDto.getUTXO());
    }

    public TransactionInputJson toTransactionInputJson() {
        return new TransactionInputJson(this.transactionOutputId, this.UTXO.toTransactionOutputJson());
    }

    public TransactionInputDto toTransactionInputDto() {
        return new TransactionInputDto(this.transactionOutputId, this.UTXO.toTransactionOutputDto());
    }

    public String getTransactionOutputId() {
        return transactionOutputId;
    }

    public TransactionOutput getUTXO() {
        return UTXO;
    }

    public void setUTXO(TransactionOutput UTXO) {
        this.UTXO = UTXO;
    }

}
