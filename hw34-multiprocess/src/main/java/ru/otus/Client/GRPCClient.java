package ru.otus.Client;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.otus.Request;
import ru.otus.ServiceGrpc;

public class GRPCClient {
    private static final Logger log = LoggerFactory.getLogger(GRPCClient.class);

    public static void main(String[] args) {
        log.info("numbers Client is starting...");

        ManagedChannel channel = ManagedChannelBuilder.forAddress("localhost", 8080)
                .usePlaintext()
                .build();

        var stub = ServiceGrpc.newStub(channel);
        var responseObserver = new ClientObserver();

        Request request = Request.newBuilder()
                .setFirstValue(0)
                .setLastValue(30)
                .build();

        stub.getNumbers(request, responseObserver);

        long currentValue = 0;
        int lastUsedServerValue = -1;

        while (!responseObserver.isCompleted()) {
            try {
                int serverVal = responseObserver.getLastValue();

                if (serverVal != -1 && serverVal != lastUsedServerValue) {
                    currentValue += serverVal + 1;
                    lastUsedServerValue = serverVal;
                } else {
                    currentValue += 1;
                }

                log.info("currentValue:{}", currentValue);
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                log.error("interrupted", e);
                Thread.currentThread().interrupt();
                break;
            }
        }

        channel.shutdown();
    }
}