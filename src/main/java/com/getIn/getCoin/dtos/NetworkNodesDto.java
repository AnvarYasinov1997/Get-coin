package com.getIn.getCoin.dtos;

public class NetworkNodesDto {
    private String requestType;

    private String dto;

    public NetworkNodesDto() {
    }

    public NetworkNodesDto(final String dto, final String requestType) {
        this.dto = dto;
        this.requestType = requestType;
    }

    public String getRequestType() {
        return requestType;
    }

    public void setRequestType(String requestType) {
        this.requestType = requestType;
    }

    public String getDto() {
        return dto;
    }

    public void setDto(String dto) {
        this.dto = dto;
    }
}