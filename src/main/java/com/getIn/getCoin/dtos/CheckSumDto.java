package com.getIn.getCoin.dtos;

public class CheckSumDto {

    private String checkSum;

    public CheckSumDto() {
    }

    public CheckSumDto(final String checkSum) {
        this.checkSum = checkSum;
    }

    public void setCheckSum(String checkSum) {
        this.checkSum = checkSum;
    }

    public String getCheckSum() {
        return checkSum;
    }
}
