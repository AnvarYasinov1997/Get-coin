package com.getIn.getCoin.getCoin.json;

public class TransactionInputJson {

    private String transactionOutputId;

    private TransactionOutputJson UTXO;

    public TransactionInputJson() {
    }

    public TransactionInputJson(String transactionOutputId, TransactionOutputJson UTXO) {
        this.transactionOutputId = transactionOutputId;
        this.UTXO = UTXO;
    }

    public String getTransactionOutputId() {
        return transactionOutputId;
    }

    public void setTransactionOutputId(String transactionOutputId) {
        this.transactionOutputId = transactionOutputId;
    }

    public TransactionOutputJson getUTXO() {
        return UTXO;
    }

    public void setUTXO(TransactionOutputJson UTXO) {
        this.UTXO = UTXO;
    }
}
