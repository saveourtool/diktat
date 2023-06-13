package com.saveourtool.diktat.test.framework.common;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;

public class StreamGobbler extends Thread {
    private final InputStream inputStream;
    private final String streamType;
    private final BiConsumer<Exception, String> exceptionHandler;
    private final ArrayList<String> result;
    private volatile boolean isStopped = false;

    /**
     * @param inputStream the InputStream to be consumed
     * @param streamType the stream type (should be OUTPUT or ERROR)
     * @param exceptionHandler the exception handler
     */
    public StreamGobbler(
            final InputStream inputStream,
            final String streamType,
            final BiConsumer<Exception, String> exceptionHandler
    ) {
        this.inputStream = inputStream;
        this.streamType = streamType;
        this.exceptionHandler = exceptionHandler;
        this.result = new ArrayList<>();
    }

    /**
     * Consumes the output from the input stream and displays the lines consumed
     * if configured to do so.
     */
    @Override
    synchronized public void run() {
        try {
            BufferedReader bufferedReader = new BufferedReader(
                    new InputStreamReader(inputStream, Charset.defaultCharset())
            );
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                this.result.add(line);
            }
        } catch (IOException ex) {
                exceptionHandler.accept(ex, "Failed to consume and display the input stream of type " + streamType + ".");
        } finally {
            this.isStopped = true;
            notify();
        }
    }

    synchronized public List<String> getContent() {
        if (!this.isStopped) {
                try {
                    wait();
                } catch (InterruptedException e) {
                    exceptionHandler.accept(e, "Cannot get content of output stream");
                }
        }
        return this.result;
    }
}
