package com.getIn.getCoin.dtos;

public class TransactionCommissionRequestDto {

    private Integer commissionAmount;

    public TransactionCommissionRequestDto() {
    }

    public TransactionCommissionRequestDto(final Integer commissionAmount) {
        this.commissionAmount = commissionAmount;
    }

    public Integer getCommissionAmount() {
        return commissionAmount;
    }

    public void setCommissionAmount(final Integer commissionAmount) {
        this.commissionAmount = commissionAmount;
    }
}
