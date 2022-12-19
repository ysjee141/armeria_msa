package happl.armeria.msa.restserver;

import com.linecorp.armeria.common.SessionProtocol;
import com.linecorp.armeria.spring.ArmeriaServerConfigurator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ServerConfig {

    @Bean
    public ArmeriaServerConfigurator config(AnnotatedService service) {
        return serverBuilder -> {
            serverBuilder.port(8090, SessionProtocol.HTTP);
            serverBuilder.annotatedService("/", service);
        };
    }

}
