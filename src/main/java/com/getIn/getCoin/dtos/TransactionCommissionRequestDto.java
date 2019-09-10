package com.getIn.getCoin.dtos;

public class TransactionCommissionRequestDto {

    private Long commissionAmount;

    public TransactionCommissionRequestDto() {
    }

    public TransactionCommissionRequestDto(final Long commissionAmount) {
        this.commissionAmount = commissionAmount;
    }

    public Long getCommissionAmount() {
        return commissionAmount;
    }

    public void setCommissionAmount(final Long commissionAmount) {
        this.commissionAmount = commissionAmount;
    }
}
