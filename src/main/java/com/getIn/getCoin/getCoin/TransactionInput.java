package com.getIn.getCoin.getCoin;

import com.getIn.getCoin.getCoin.json.TransactionInputJson;

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

    public TransactionInputJson toTransactionInputJson() {
        return new TransactionInputJson(this.transactionOutputId, this.UTXO.toTransactionOutputJson());
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
