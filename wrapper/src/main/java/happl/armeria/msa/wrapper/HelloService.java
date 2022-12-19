package happl.armeria.msa.wrapper;

import com.linecorp.armeria.server.ServiceRequestContext;
import example.armeria.grpc.Hello;
import example.armeria.grpc.HelloServiceGrpc;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;

import java.util.concurrent.TimeUnit;


public class HelloService extends HelloServiceGrpc.HelloServiceImplBase {

    static String toMessage(String name) {
        return "Hello, " + name + '!';
    }

    private static Hello.HelloReply buildReply(Object message) {
        return Hello.HelloReply.newBuilder().setMessage(String.valueOf(message)).build();
    }

    @Override
    public void hello(Hello.HelloRequest request, StreamObserver<Hello.HelloReply> responseObserver) {
        if (request.getName().isEmpty()) {
            responseObserver.onError(
                    Status.FAILED_PRECONDITION.withDescription("Name cannot be empty").asException()
            );
        } else {
            responseObserver.onNext(buildReply(toMessage(request.getName())));
//            responseObserver.onNext(buildReply(toMessage(request.getName() + " Second Value!")));
            responseObserver.onCompleted();
        }
    }

    @Override
    public void lazyHello(Hello.HelloRequest request, StreamObserver<Hello.HelloReply> responseObserver) {
        ServiceRequestContext.current().eventLoop().schedule(() -> {
            responseObserver.onNext(buildReply(toMessage(request.getName())));
            responseObserver.onCompleted();
        }, 3, TimeUnit.SECONDS);
    }

    @Override
    public void blockingHello(Hello.HelloRequest request, StreamObserver<Hello.HelloReply> responseObserver) {
        ServiceRequestContext.current().blockingTaskExecutor().submit(() -> {
            try {
                // Simulate a blocking API call.
                Thread.sleep(3000);
            } catch (Exception ignored) {
                // Do nothing.
            }
            responseObserver.onNext(buildReply(toMessage(request.getName())));
            responseObserver.onCompleted();
        });
    }

    @Override
    public void lotsOfReplies(Hello.HelloRequest request, StreamObserver<Hello.HelloReply> responseObserver) {
        super.lotsOfReplies(request, responseObserver);
    }

    @Override
    public StreamObserver<Hello.HelloRequest> lotsOfGreetings(StreamObserver<Hello.HelloReply> responseObserver) {
        return super.lotsOfGreetings(responseObserver);
    }

    @Override
    public StreamObserver<Hello.HelloRequest> bidiHello(StreamObserver<Hello.HelloReply> responseObserver) {
        return new StreamObserver<>() {
            @Override
            public void onNext(Hello.HelloRequest value) {
                // Respond to every request received.
                responseObserver.onNext(buildReply(toMessage(value.getName())));
            }

            @Override
            public void onError(Throwable t) {
                responseObserver.onError(t);
            }

            @Override
            public void onCompleted() {
                responseObserver.onCompleted();
            }
        };
    }
}
