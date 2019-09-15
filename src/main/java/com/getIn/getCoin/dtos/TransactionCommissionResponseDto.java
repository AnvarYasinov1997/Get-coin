package com.getIn.getCoin.dtos;

public class TransactionCommissionResponseDto {

    private Boolean status;

    private String publicKey;

    public TransactionCommissionResponseDto() {
    }

    public TransactionCommissionResponseDto(final Boolean status, final String publicKey) {
        this.status = status;
        this.publicKey = publicKey;
    }

    public Boolean getStatus() {
        return status;
    }

    public void setStatus(Boolean status) {
        this.status = status;
    }

    public String getPublicKey() {
        return publicKey;
    }

    public void setPublicKey(String publicKey) {
        this.publicKey = publicKey;
    }
}
