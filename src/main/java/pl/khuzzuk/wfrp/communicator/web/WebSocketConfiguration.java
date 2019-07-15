package pl.khuzzuk.wfrp.communicator.web;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.HandlerMapping;
import org.springframework.web.reactive.handler.SimpleUrlHandlerMapping;
import org.springframework.web.reactive.socket.WebSocketHandler;
import org.springframework.web.reactive.socket.server.support.WebSocketHandlerAdapter;
import pl.khuzzuk.messaging.Bus;
import pl.khuzzuk.messaging.Cancellable;
import pl.khuzzuk.wfrp.communicator.channel.ChannelListener;
import pl.khuzzuk.wfrp.communicator.event.Event;

import java.util.List;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

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
        return session -> {
            ChannelListener listener = new ChannelListener();
            List<Cancellable<Event>> userSubscription = List.of(
                    bus.subscribingFor(Event.MESSAGE).accept(listener::receiveMessage).subscribe()
            );
            listener.onComplete(() -> userSubscription.forEach(bus::unSubscribe));
            return session.send(listener.share().map(message -> {
                try {
                    return objectMapper.writeValueAsString(message);
                } catch (JsonProcessingException e) {
                    e.printStackTrace();
                    return "error";
                }
            }).map(session::textMessage));
        };
    }
}
