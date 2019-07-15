package pl.khuzzuk.wfrp.communicator.event;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import pl.khuzzuk.messaging.Bus;

@Configuration
public class EventsConfiguration {
    @Bean
    Bus<Event> bus() {
        return Bus.initializeBus(Event.class);
    }
}
