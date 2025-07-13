package ru.otus.service;

import io.grpc.Server;
import io.grpc.ServerBuilder;
import ru.otus.Request;
import ru.otus.Response;
import ru.otus.ServiceGrpc;
import io.grpc.stub.StreamObserver;

public class GRPCService {

    public static void main(String[] args) throws Exception {
        Server server = ServerBuilder.forPort(8080)
                .addService(new ServiceImpl())
                .build()
                .start();

        System.out.println("Server started on port 8080");
        server.awaitTermination();
    }

    private static class ServiceImpl extends ServiceGrpc.ServiceImplBase {
        @Override
        public void getNumbers(Request request, StreamObserver<Response> responseObserver) {
            int from = request.getFirstValue();
            int to = request.getLastValue();

            new Thread(() -> {
                try {
                    for (int i = from + 1; i <= to; i++) {
                        Response response = Response.newBuilder()
                                .setCurrentValue(i)
                                .build();

                        responseObserver.onNext(response);
                        Thread.sleep(2000);

                        if (i == 10) {
                            break;
                        }
                    }
                } catch (InterruptedException e) {
                    responseObserver.onError(e);
                    Thread.currentThread().interrupt();
                    return;
                }
                responseObserver.onCompleted();
            }).start();
        }
    }
}
