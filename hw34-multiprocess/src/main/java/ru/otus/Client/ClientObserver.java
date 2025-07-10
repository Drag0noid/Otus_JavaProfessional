package ru.otus.Client;

import io.grpc.stub.StreamObserver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.otus.Response;

public class ClientObserver implements StreamObserver<Response> {
    private static final Logger log = LoggerFactory.getLogger(ClientObserver.class);

    private volatile int lastValue = -1;
    private volatile boolean completed = false;

    @Override
    public void onNext(Response response) {
        lastValue = response.getCurrentValue();
        log.info("new value:{}", lastValue);
    }

    @Override
    public void onError(Throwable t) {
        log.error("Error from server", t);
        completed = true;
    }

    @Override
    public void onCompleted() {
        log.info("request completed");
        completed = true;
    }

    public int getLastValue() {
        return lastValue;
    }

    public boolean isCompleted() {
        return completed;
    }
}