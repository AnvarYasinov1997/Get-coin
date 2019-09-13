package com.getIn.getCoin.nioServer;

import java.io.*;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

class TestClient {
    public static void main(String[] args) throws IOException {
        System.out.println("> Server started");
        Socket socket = new Socket("localhost", 8080);
        BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        bufferedWriter.write("Hello" + "\n");
        bufferedWriter.flush();
        System.out.println(bufferedReader.readLine());
        socket.close();
    }
}

public class AsynchronousMainServer {

    public static void main(String[] args) throws Exception {
        final Map<SocketChannel, String> connectionBufferKeys = new HashMap<>();
        final Map<SocketChannel, String> responses = new HashMap<>();
        final List<SocketChannel> executedTask = new ArrayList<>();
        final ExecutorService executorService = Executors.newFixedThreadPool(1000);
        final DirectBufferPool directBufferPool = new DirectBufferPool((1024 * 1024), 1000);
        final ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
        final Selector selector = Selector.open();
        serverSocketChannel.bind(new InetSocketAddress(8080));
        serverSocketChannel.configureBlocking(false);
        serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);
        System.out.println("> Server started....");
        while (true) {
            selector.select();
            for (SelectionKey selectionKey : selector.selectedKeys()) {
                if (selectionKey.isValid()) {
                    if (selectionKey.isAcceptable()) {
                        System.out.println();
                        final SocketChannel socketChannel = serverSocketChannel.accept();
                        socketChannel.configureBlocking(false);
                        connectionBufferKeys.put(socketChannel, directBufferPool.allocate(256));
                        socketChannel.register(selector, SelectionKey.OP_READ);
                    } else if (selectionKey.isReadable()) {
                        System.out.println();
                        final SocketChannel socketChannel = (SocketChannel) selectionKey.channel();
                        final String connectionBufferKey = connectionBufferKeys.get(socketChannel);
                        final ByteBuffer byteBuffer = directBufferPool.getBufferByKey(connectionBufferKey);
                        final int bytesRead = socketChannel.read(byteBuffer);
                        if (bytesRead == -1) {
                            System.out.println("> Connection closed");
                            socketChannel.close();
                            directBufferPool.removeBuffer(connectionBufferKey);
                        } else if (bytesRead > 0 && byteBuffer.get(byteBuffer.position() - 1) == '\n') {
                            socketChannel.register(selector, SelectionKey.OP_WRITE);
                        }
                    } else if (selectionKey.isWritable()) {
                        final SocketChannel socketChannel = (SocketChannel) selectionKey.channel();
                        final String connectionBufferKey = connectionBufferKeys.get(socketChannel);
                        final ByteBuffer byteBuffer = directBufferPool.getBufferByKey(connectionBufferKey);
                        if (!executedTask.contains(socketChannel)) {
                            byteBuffer.flip();
                            final String request =
                                    new String(byteBuffer.array(), byteBuffer.position(), byteBuffer.limit()).replace("\n", "");
                            executedTask.add(socketChannel);
                            executorService.execute(() -> {
                                try {
                                    System.out.println("Handle request");
                                    Thread.sleep(5000);
                                    responses.put(socketChannel, request + "handled" + "\n");
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                            });
                        }
                        final String response = responses.get(socketChannel);
                        if (response != null) {
                            executedTask.remove(socketChannel);
                            responses.remove(socketChannel);
                            byteBuffer.clear();
                            final ByteBuffer newBuffer = ByteBuffer.wrap(response.getBytes());
                            byteBuffer.put(newBuffer);
                            byteBuffer.flip();

                            int bytesWritten = socketChannel.write(byteBuffer);
                            System.out.println("> Bytes written " + bytesWritten);
                            if (!byteBuffer.hasRemaining()) {
                                byteBuffer.compact();
                                socketChannel.register(selector, SelectionKey.OP_READ);
                            }
                        }
                    }
                }
            }
            selector.selectedKeys().clear();
        }
    }

    private static class DirectBufferPool {

        private long currentPoolSize;

        private long currentBusyMemory;

        private long currentFreeMemory;

        private final long maxMemorySize;

        private final long maxBufferPool;

        private final Map<String, ByteBuffer> buffers = new HashMap<>();

        public DirectBufferPool(final long maxMemorySize,
                                final int maxBufferPool) {
            this.maxMemorySize = maxMemorySize;
            this.maxBufferPool = maxBufferPool;
        }

        public String allocate(final int bufferSize) {
            if (this.currentFreeMemory < bufferSize) {
                if (bufferSize < (this.maxMemorySize / this.maxBufferPool)) {
                    return allocateBuffer(bufferSize);
                } else if (currentPoolSize == (maxBufferPool - 1)) {
                    return allocateBuffer(bufferSize);
                } else throw new RuntimeException("Busy pool memory ");
            } else throw new RuntimeException("Not enough memory");
        }

        public void removeBuffer(final String key) {
            ByteBuffer removeBuffer = this.buffers.get(key);
            this.currentPoolSize--;
            this.currentFreeMemory += removeBuffer.remaining();
            this.currentBusyMemory -= removeBuffer.remaining();
            this.buffers.remove(key);
            removeBuffer = null;
            System.gc();
        }

        public ByteBuffer getBufferByKey(final String bufferKey) {
            return this.buffers.get(bufferKey);
        }

        public long getCurrentFreeMemory() {
            return currentFreeMemory;
        }

        public long getCurrentBusyMemory() {
            return currentBusyMemory;
        }

        public long getCurrentPoolSize() {
            return currentPoolSize;
        }

        private String allocateBuffer(final int bufferSize) {
            final ByteBuffer byteBuffer = ByteBuffer.allocate(bufferSize);
            final String key = UUID.randomUUID().toString();
            this.buffers.put(key, byteBuffer);
            this.currentFreeMemory -= bufferSize;
            this.currentBusyMemory += bufferSize;
            this.currentPoolSize++;
            return key;
        }

        private void destroyBuffer(Buffer buffer) {
            if (buffer.isDirect()) {
                try {
                    if (!buffer.getClass().getName().equals("java.nio.DirectByteBuffer")) {
                        final Field attField = buffer.getClass().getDeclaredField("att");
                        attField.setAccessible(true);
                        buffer = (Buffer) attField.get(buffer);
                    }

                    Method cleanerMethod = buffer.getClass().getMethod("cleaner");
                    cleanerMethod.setAccessible(true);
                    Object cleaner = cleanerMethod.invoke(buffer);
                    Method cleanMethod = cleaner.getClass().getMethod("clean");
                    cleanMethod.setAccessible(true);
                    cleanMethod.invoke(cleaner);
                } catch (Exception e) {
                    throw new RuntimeException("Could not destroy direct buffer " + buffer, e);
                }
            }
        }

    }

}
