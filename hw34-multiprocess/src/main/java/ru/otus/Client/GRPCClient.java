package ru.otus.Client;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.otus.Request;
import ru.otus.Response;
import ru.otus.ServiceGrpc;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class GRPCClient {
    private static final Logger LOG = LoggerFactory.getLogger(GRPCClient.class);
    private static final String HOST = "localhost";
    private static final int PORT = 8080;

    public static void main(String[] args) throws InterruptedException {
        ManagedChannel channel = ManagedChannelBuilder.forAddress(HOST, PORT)
                .usePlaintext()
                .build();

        ServiceGrpc.NumbersServiceStub asyncStub = ServiceGrpc.newStub(channel);

        CountDownLatch latch = new CountDownLatch(1);
        AccumulatorObserver observer = new AccumulatorObserver(latch);

        Request request = Request.newBuilder()
                .setFirstValue(1)
                .setLastValue(30)
                .build();

        asyncStub.getNumbers(request, observer);

        LOG.info("Waiting for server to send all numbers...");
        latch.await(35, TimeUnit.SECONDS);

        LOG.info("Final accumulated value: {}", observer.getAccumulatedSum());

        channel.shutdown();
    }

    private static class AccumulatorObserver implements io.grpc.stub.StreamObserver<Response> {
        private final CountDownLatch latch;
        private int accumulatedSum = 0;

        public AccumulatorObserver(CountDownLatch latch) {
            this.latch = latch;
        }

        @Override
        public void onNext(Response response) {
            int val = response.getCurrentValue();
            accumulatedSum += val * 2;
            LOG.info("Received: {}, accumulatedSum: {}", val, accumulatedSum);
        }

        @Override
        public void onError(Throwable t) {
            LOG.error("Error from server", t);
            latch.countDown();
        }

        @Override
        public void onCompleted() {
            LOG.info("Stream completed");
            latch.countDown();
        }

        public int getAccumulatedSum() {
            return accumulatedSum;
        }
    }
}
