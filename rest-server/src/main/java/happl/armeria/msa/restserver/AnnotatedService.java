package happl.armeria.msa.restserver;

import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.linecorp.armeria.client.eureka.EurekaEndpointGroup;
import com.linecorp.armeria.client.grpc.GrpcClients;
import com.linecorp.armeria.client.logging.LoggingClient;
import com.linecorp.armeria.common.HttpResponse;
import com.linecorp.armeria.common.SessionProtocol;
import com.linecorp.armeria.common.grpc.GrpcSerializationFormats;
import com.linecorp.armeria.server.ServiceRequestContext;
import com.linecorp.armeria.server.annotation.Get;
import com.linecorp.armeria.server.annotation.Param;
import example.armeria.grpc.Hello;
import example.armeria.grpc.Hello.HelloReply;
import example.armeria.grpc.HelloServiceGrpc;
import io.grpc.stub.StreamObserver;
import io.grpc.stub.StreamObservers;
import org.springframework.stereotype.Component;

import java.util.concurrent.CompletableFuture;


@Component
public class AnnotatedService {

    EurekaEndpointGroup eurekaEndpointGroup = EurekaEndpointGroup
            .builder("http://localhost:8761/eureka")
            .build();

    HelloServiceGrpc.HelloServiceStub service = GrpcClients
//                    .builder("gproto+http://127.0.0.1:8091/")
            .builder(SessionProtocol.HTTP, eurekaEndpointGroup)
            .serializationFormat(GrpcSerializationFormats.PROTO)
//                    .responseTimeoutMillis(10000)
            .decorator(LoggingClient.newDecorator())
            .build(HelloServiceGrpc.HelloServiceStub.class);

    HelloServiceGrpc.HelloServiceFutureStub helloServiceStub =
            GrpcClients
//                    .builder("gproto+http://127.0.0.1:8091/")
                    .builder(SessionProtocol.HTTP, eurekaEndpointGroup)
                    .serializationFormat(GrpcSerializationFormats.PROTO)
//                    .responseTimeoutMillis(10000)
                    .decorator(LoggingClient.newDecorator())
                    .build(HelloServiceGrpc.HelloServiceFutureStub.class);

    HelloServiceGrpc.HelloServiceBlockingStub helloService =
            GrpcClients
//                    .builder("gproto+http://127.0.0.1:8091/")
                    .builder(SessionProtocol.HTTP, eurekaEndpointGroup)
                    .serializationFormat(GrpcSerializationFormats.PROTO)
//                    .responseTimeoutMillis(10000)
                    .decorator(LoggingClient.newDecorator())
                    .build(HelloServiceGrpc.HelloServiceBlockingStub.class);


    @Get("/{name}")
    public HttpResponse get(
            ServiceRequestContext ctx,
            @Param String name
    ) throws Exception {
        Hello.HelloRequest request = Hello.HelloRequest.newBuilder()
                .setName(name).build();

        CompletableFuture<HttpResponse> f = new CompletableFuture<>();

        StreamObserver<HelloReply> z = new StreamObserver<HelloReply>() {
            @Override
            public void onNext(HelloReply value) {

            }

            @Override
            public void onError(Throwable t) {

            }

            @Override
            public void onCompleted() {

            }
        };

        service.hello(request, new StreamObserver<HelloReply>() {
            @Override
            public void onNext(HelloReply value) {
                f.complete(HttpResponse.of(value.getMessage()));
            }

            @Override
            public void onError(Throwable t) {

            }

            @Override
            public void onCompleted() {

            }
        });


        /*ListenableFuture<HelloReply> future = helloServiceStub.hello(request);
        Futures.addCallback(future, new FutureCallback<HelloReply>() {
            @Override
            public void onSuccess(HelloReply result) {
                f.complete(HttpResponse.of(result.getMessage()));
            }

            @Override
            public void onFailure(Throwable t) {

            }
        }, ctx.eventLoop());*/

        return HttpResponse.from(f);
    }

}
