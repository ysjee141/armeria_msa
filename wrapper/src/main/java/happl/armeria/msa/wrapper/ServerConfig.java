package happl.armeria.msa.wrapper;

import com.linecorp.armeria.common.SessionProtocol;
import com.linecorp.armeria.common.grpc.GrpcSerializationFormats;
import com.linecorp.armeria.server.docs.DocService;
import com.linecorp.armeria.server.docs.DocServiceFilter;
import com.linecorp.armeria.server.eureka.EurekaUpdatingListener;
import com.linecorp.armeria.server.grpc.GrpcService;
import com.linecorp.armeria.spring.ArmeriaServerConfigurator;
import example.armeria.grpc.Hello;
import example.armeria.grpc.HelloServiceGrpc;
import io.grpc.protobuf.services.ProtoReflectionService;
import io.grpc.reflection.v1alpha.ServerReflectionGrpc;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Optional;

@Configuration
public class ServerConfig {

    private final DiscoveryConfig discoveryConfig;

    public ServerConfig(DiscoveryConfig discoveryConfig) {
        this.discoveryConfig = discoveryConfig;
    }

    @Bean
    public ArmeriaServerConfigurator config() {
        return serverBuilder -> {

            EurekaUpdatingListener listener = EurekaUpdatingListener.builder("http://localhost:8761/eureka")
                    .instanceId("wrapper-service")
                    .hostname("grpc-wrapper")
                    .ipAddr("127.0.0.1")
                    .build();

            serverBuilder.serverListener(listener);

            final Hello.HelloRequest request = Hello.HelloRequest.newBuilder().setName("Armeria").build();
            final GrpcService grpcService = GrpcService.builder()
                    .addService(new HelloService())
                    .addService(ProtoReflectionService.newInstance())
                    .supportedSerializationFormats(GrpcSerializationFormats.values())
                    .enableUnframedRequests(true)
                    .build();
            serverBuilder.service(grpcService)
                    .service("prefix:/prefix", grpcService)
                    .serviceUnder("/docs",
                            DocService.builder()
                                    .exampleRequests(HelloServiceGrpc.SERVICE_NAME, "Hello", request)
                                    .exampleRequests(HelloServiceGrpc.SERVICE_NAME, "LazyHello", request)
                                    .exampleRequests(HelloServiceGrpc.SERVICE_NAME, "BlockingHello", request)
                                    .exclude(DocServiceFilter.ofServiceName(ServerReflectionGrpc.SERVICE_NAME))
                                    .build());
        };
    }

}
