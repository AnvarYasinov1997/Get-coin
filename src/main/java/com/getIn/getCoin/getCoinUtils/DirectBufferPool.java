package com.getIn.getCoin.getCoinUtils;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class DirectBufferPool {

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
