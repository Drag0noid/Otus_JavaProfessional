package ru.otus.service;

import io.grpc.Server;
import io.grpc.ServerBuilder;
import io.grpc.stub.StreamObserver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.otus.Request;
import ru.otus.Response;
import ru.otus.ServiceGrpc;

import java.io.IOException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class GRPCService {
    private static final Logger LOG = LoggerFactory.getLogger(GRPCService.class);
    private static final int PORT = 8080;

    public static void main(String[] args) throws IOException, InterruptedException {
        Server server = ServerBuilder.forPort(PORT)
                .addService(new NumberServiceImpl())
                .build()
                .start();

        LOG.info("Server started on port {}", PORT);
        server.awaitTermination();
    }

    static class NumberServiceImpl extends ServiceGrpc.ServiceImplBase {
        private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

        @Override
        public void getNumbers(Request request, StreamObserver<Response> responseObserver) {
            int start = request.getFirstValue();
            int end = request.getLastValue();

            LOG.info("Request received: start={} end={}", start, end);

            final int[] current = {start};

            scheduler.scheduleAtFixedRate(() -> {
                if (current[0] <= end) {
                    Response resp = Response.newBuilder()
                            .setCurrentValue(current[0])
                            .build();
                    responseObserver.onNext(resp);
                    LOG.info("Sent number: {}", current[0]);
                    current[0]++;
                } else {
                    responseObserver.onCompleted();
                    LOG.info("Completed sending numbers");
                    scheduler.shutdown();
                }
            }, 0, 500, TimeUnit.MILLISECONDS);
        }
    }
}
