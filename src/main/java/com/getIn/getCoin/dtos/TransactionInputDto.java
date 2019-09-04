package com.getIn.getCoin.dtos;

public class TransactionInputDto {

    private String transactionOutputId;

    private TransactionOutputDto UTXO;

    public TransactionInputDto() {
    }

    public TransactionInputDto(final String transactionOutputId,
                               final TransactionOutputDto UTXO) {
        this.transactionOutputId = transactionOutputId;
        this.UTXO = UTXO;
    }

    public String getTransactionOutputId() {
        return transactionOutputId;
    }

    public void setTransactionOutputId(String transactionOutputId) {
        this.transactionOutputId = transactionOutputId;
    }

    public TransactionOutputDto getUTXO() {
        return UTXO;
    }

    public void setUTXO(TransactionOutputDto UTXO) {
        this.UTXO = UTXO;
    }

}
