package com.getIn.getCoin.nioServer;

import java.io.IOException;

public interface NioServer {
    void start() throws IOException;

    void stop();
}


