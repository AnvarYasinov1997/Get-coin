package com.getIn.getCoin.dtos;

public class TransactionCommissionResponseDto {

    public Boolean status;

    public TransactionCommissionResponseDto() {
    }

    public TransactionCommissionResponseDto(final Boolean status) {
        this.status = status;
    }

    public Boolean getStatus() {
        return status;
    }

    public void setStatus(final Boolean status) {
        this.status = status;
    }
}
