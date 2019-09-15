package com.getIn.getCoin.nioServer;

import com.getIn.getCoin.getCoinUtils.DirectBufferPool;
import com.getIn.getCoin.userServer.RequestHandler;
import io.reactivex.Emitter;
import io.reactivex.Observable;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class NioServerImpl implements NioServer, NioServerContainer {

    private static NioServerImpl nioServerInstance;

    private final Selector selector;

    private ExecutorService executorService;

    private Disposable serverSubscriber;

    private final Observable<Set<SelectionKey>> serverDataStream;

    private final ServerSocketChannel serverSocketChannel;

    private final DirectBufferPool directBufferPool;

    private final RequestHandler requestHandler;

    private final List<String> executeTasks = new ArrayList<>();

    private final Map<String, String> connectionBufferKeys = new HashMap<>();

    private final Map<String, String> readyForResponseQueue = new HashMap<>();

    private NioServerImpl(final String serverPort,
                          final RequestHandler requestHandler) throws IOException {
        this.serverSocketChannel = ServerSocketChannel.open();
        this.requestHandler = requestHandler;
        this.selector = Selector.open();
        this.directBufferPool = new DirectBufferPool((1024 * 1024), 1000);
        this.serverSocketChannel.bind(new InetSocketAddress(Integer.valueOf(serverPort)));
        this.serverSocketChannel.configureBlocking(false);
        this.serverSocketChannel.register(this.selector, SelectionKey.OP_ACCEPT);
        this.serverDataStream =
                Observable.generate(() -> this.selector, this::serverBiConsumerAccept, Selector::close)
                        .subscribeOn(Schedulers.io())
                        .observeOn(Schedulers.computation());
        this.executorService =
                Executors.newFixedThreadPool(1000, runnable -> new Thread(runnable, "Server executor"));
    }

    public static NioServer getInstance(final String serverPort, final RequestHandler requestHandler) throws IOException {
        if (nioServerInstance != null) return nioServerInstance;
        else return new NioServerImpl(serverPort, requestHandler);
    }

    public static NioServerContainer getNioServerContainer() throws Exception {
        if (nioServerInstance != null) return nioServerInstance;
        throw new Exception("Nio server has not be instantiated");
    }

    @Override
    public void putResponse(final String socketChannelKey, final String response) {
        this.readyForResponseQueue.put(socketChannelKey, response);
    }

    @Override
    public void start() {
        this.serverSubscriber = this.serverDataStream.subscribe(this::consume, Throwable::printStackTrace);
    }

    @Override
    public void stop() {
        this.serverSubscriber.dispose();
    }

    private void consume(final Set<SelectionKey> selectionKeys) {
        try {
            for (SelectionKey key : selectionKeys) {
                if (key.isValid()) {
                    if (key.isAcceptable()) {
                        final SocketChannel socketChannel = this.serverSocketChannel.accept();
                        socketChannel.configureBlocking(false);
                        connectionBufferKeys.put(String.valueOf(socketChannel.hashCode()), directBufferPool.allocate(256));
                        socketChannel.register(selector, SelectionKey.OP_READ);
                    } else if (key.isReadable()) {
                        final SocketChannel socketChannel = (SocketChannel) key.channel();
                        final String connectionBufferKey = this.connectionBufferKeys.get(String.valueOf(socketChannel.hashCode()));
                        final ByteBuffer byteBuffer = this.directBufferPool.getBufferByKey(connectionBufferKey);
                        final int bytesRead = socketChannel.read(byteBuffer);
                        if (bytesRead == -1) {
                            System.out.println("> Connection closed");
                            socketChannel.close();
                            this.directBufferPool.removeBuffer(connectionBufferKey);
                            this.connectionBufferKeys.remove(connectionBufferKey);
                        } else if (bytesRead > 0 && byteBuffer.get(byteBuffer.position() - 1) == '\n') {
                            socketChannel.register(selector, SelectionKey.OP_WRITE);
                        }
                    } else if (key.isWritable()) {
                        final SocketChannel socketChannel = (SocketChannel) key.channel();
                        final String socketChannelHash = String.valueOf(socketChannel.hashCode());
                        final String connectionBufferKey = this.connectionBufferKeys.get(socketChannelHash);
                        final ByteBuffer byteBuffer = this.directBufferPool.getBufferByKey(connectionBufferKey);
                        if (!executeTasks.contains(String.valueOf(socketChannel.hashCode()))) {
                            executorService.execute(() -> {
                                try {
                                    byteBuffer.flip();
                                    final String request =
                                            new String(byteBuffer.array(), byteBuffer.position(), byteBuffer.limit()).replace("\n", "");
                                    NioServerImpl.this.requestHandler.handleRequest(request, socketChannelHash, () -> {
                                        try {
                                            socketChannel.close();
                                            NioServerImpl.this.directBufferPool.removeBuffer(connectionBufferKey);
                                            NioServerImpl.this.executeTasks.remove(connectionBufferKey);
                                            NioServerImpl.this.connectionBufferKeys.remove(connectionBufferKey);
                                        } catch (IOException e) {
                                            e.printStackTrace();
                                            throw new RuntimeException("Connection close failure");
                                        }
                                    });
                                } catch (Exception e) {
                                    throw new RuntimeException(e.getMessage());
                                }
                            });
                            this.executeTasks.add(connectionBufferKey);
                        }
                        final String response;
                        if ((response = this.readyForResponseQueue.get(socketChannelHash)) != null) {
                            byteBuffer.clear();
                            final ByteBuffer newBuffer = ByteBuffer.wrap(response.getBytes());
                            byteBuffer.put(newBuffer);
                            byteBuffer.flip();
                            final int bytesWritten = socketChannel.write(byteBuffer);
                            System.out.println("> Bytes written " + bytesWritten);
                            if (!byteBuffer.hasRemaining()) {
                                byteBuffer.compact();
                                socketChannel.close();
                                this.readyForResponseQueue.remove(socketChannelHash);
                                this.directBufferPool.removeBuffer(connectionBufferKey);
                                this.executeTasks.remove(connectionBufferKey);
                                this.connectionBufferKeys.remove(connectionBufferKey);
                            }
                        }
                    }
                }
            }
            selectionKeys.clear();
        } catch (final Exception e) {
            e.printStackTrace();
        }

    }

    private void serverBiConsumerAccept(final Selector selector, final Emitter<Set<SelectionKey>> emitter) {
        try {
            final int confectionCount = selector.select();
            emitter.onNext(selector.selectedKeys());
        } catch (IOException e) {
            e.printStackTrace();
            emitter.onComplete();
            throw new RuntimeException();
        }
    }

}
