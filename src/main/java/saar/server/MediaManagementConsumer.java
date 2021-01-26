package saar.server;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rabbitmq.client.*;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import saar.server.dto.Response;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class MediaManagementConsumer extends DefaultConsumer {
    String queueName = "media-management-events-mock";
    private final Channel channel;

    public MediaManagementConsumer(Channel channel) {
        super(channel);
        this.channel = channel;
    }

    @Override
    public void handleConsumeOk(String consumerTag) {
        System.out.println("started to consume from queue: " + queueName + " consumerTag: " + consumerTag);
        super.handleConsumeOk(consumerTag);
    }

    @Override
    public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {
        String jsonStr = new String(body, StandardCharsets.UTF_8);
        ObjectMapper objectMapper = new ObjectMapper();
        Response response = objectMapper.readValue(jsonStr, Response.class);
    }

    public void consume() throws IOException {
        channel.basicConsume(queueName, this);
    }
}
