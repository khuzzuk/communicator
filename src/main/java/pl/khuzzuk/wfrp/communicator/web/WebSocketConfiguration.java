package pl.khuzzuk.wfrp.communicator.web;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.HandlerMapping;
import org.springframework.web.reactive.handler.SimpleUrlHandlerMapping;
import org.springframework.web.reactive.socket.WebSocketHandler;
import org.springframework.web.reactive.socket.WebSocketMessage;
import org.springframework.web.reactive.socket.server.support.WebSocketHandlerAdapter;
import pl.khuzzuk.messaging.Bus;
import pl.khuzzuk.wfrp.communicator.channel.Message;
import pl.khuzzuk.wfrp.communicator.event.Event;
import reactor.core.publisher.Flux;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import static pl.khuzzuk.wfrp.communicator.event.Event.MESSAGE;

@Slf4j
@Configuration
public class WebSocketConfiguration {
    @Bean
    Executor executor() {
        return Executors.newSingleThreadExecutor();
    }

    @Bean
    HandlerMapping handlerMapping(WebSocketHandler webSocketHandler) {
        return new SimpleUrlHandlerMapping() {
            {
                setUrlMap(Map.of("/ws/channel", webSocketHandler));
                setOrder(10);
            }
        };
    }

    @Bean
    WebSocketHandlerAdapter webSocketHandlerAdapter() {
        return new WebSocketHandlerAdapter();
    }

    @Bean
    WebSocketHandler webSocketHandler(ObjectMapper objectMapper, Bus<Event> bus) {
        Flux<Message> messagesPublisher = Flux.<Message>create(messageFluxSink ->
                bus.subscribingFor(MESSAGE).accept(messageFluxSink::next).subscribe()).share();

        return session -> {
            session.receive()
                    .map(WebSocketMessage::getPayloadAsText)
                    .map(text -> safeReadMessage(objectMapper, text))
                    .subscribe(message -> bus.message(MESSAGE).withContent(message).send(),
                            Throwable::printStackTrace);
            return session.send(messagesPublisher.map(message -> {
                try {
                    return objectMapper.writeValueAsString(message);
                } catch (JsonProcessingException e) {
                    return "error";
                }
            }).map(session::textMessage));
        };
    }

    private Message safeReadMessage(ObjectMapper objectMapper, String text) {
        try {
            Message message = objectMapper.readValue(text, Message.class);
            log.info(String.format("received: %s", message));
            return message;
        } catch (IOException e) {
            log.error("Cannot parse message", e);
            return null;
        }
    }
}
