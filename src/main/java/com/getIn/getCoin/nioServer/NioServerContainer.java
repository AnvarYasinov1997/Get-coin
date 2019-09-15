package com.getIn.getCoin.nioServer;

public interface NioServerContainer {
    void putResponse(final String socketChannelKey, final String response);
}
