package saar.server;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rabbitmq.client.*;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.apache.http.ssl.SSLContextBuilder;
import org.apache.http.ssl.SSLContexts;
import saar.server.dto.Response;

import javax.net.ssl.SSLContext;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.TimeoutException;


@SpringBootApplication
public class ServerApplication {
    @Bean
    public MediaManagementConsumer consume(Connection connection) throws IOException {
        Channel channel = connection.createChannel();
        MediaManagementConsumer mediaManagementConsumer = new MediaManagementConsumer(channel);
        mediaManagementConsumer.consume();

        return mediaManagementConsumer;
    }

    @Bean
    public Connection connectionFactory() throws IOException, NoSuchAlgorithmException, KeyManagementException, TimeoutException {
        try {
            com.rabbitmq.client.ConnectionFactory factory = new com.rabbitmq.client.ConnectionFactory();
            factory.setHost("localhost");
            factory.setPort(5672);
            factory.setUsername("nice");
            factory.setPassword("nice");

            return factory.newConnection();
        } catch (Exception e) {
            return createTLSConnectionFactory().newConnection();
        }
    }


    public ConnectionFactory createTLSConnectionFactory() throws KeyManagementException, NoSuchAlgorithmException {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost");
        factory.setPort(5671);
        factory.setUsername("nice");
        factory.setPassword("nice");
        factory.setAutomaticRecoveryEnabled(true);

        SSLContextBuilder sslContextBuilder = SSLContexts.custom();
        SSLContext sslContext = sslContextBuilder.build();

        factory.useSslProtocol(sslContext);

        return factory;
    }

    public static void main(String[] args) {
        SpringApplication.run(ServerApplication.class, args);
    }
}
