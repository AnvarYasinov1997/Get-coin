package com.getIn.getCoin.dtos;

public class TransactionCommissionResponseDto {

    private Boolean status;

    private String peerHost;

    private String peerPort;

    private String publicKey;

    public TransactionCommissionResponseDto() {
    }

    public TransactionCommissionResponseDto(Boolean status, String peerHost, String peerPort, String publicKey) {
        this.status = status;
        this.peerHost = peerHost;
        this.peerPort = peerPort;
        this.publicKey = publicKey;
    }

    public Boolean getStatus() {
        return status;
    }

    public void setStatus(Boolean status) {
        this.status = status;
    }

    public String getPeerHost() {
        return peerHost;
    }

    public void setPeerHost(String peerHost) {
        this.peerHost = peerHost;
    }

    public String getPeerPort() {
        return peerPort;
    }

    public void setPeerPort(String peerPort) {
        this.peerPort = peerPort;
    }

    public String getPublicKey() {
        return publicKey;
    }

    public void setPublicKey(String publicKey) {
        this.publicKey = publicKey;
    }
}
