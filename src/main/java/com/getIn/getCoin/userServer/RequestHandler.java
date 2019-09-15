package com.getIn.getCoin.userServer;

import java.io.Closeable;

public interface RequestHandler {
    void handleRequest(final String request,
                       final String socketChannelHash,
                       final Closeable closeConnectionCallback) throws Exception;
}
