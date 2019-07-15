package pl.khuzzuk.wfrp.communicator.channel;

import lombok.AllArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pl.khuzzuk.messaging.Bus;
import pl.khuzzuk.messaging.Cancellable;
import pl.khuzzuk.wfrp.communicator.event.Event;
import pl.khuzzuk.wfrp.communicator.user.User;
import reactor.core.publisher.Flux;

import java.time.LocalDateTime;
import java.util.List;

import static pl.khuzzuk.wfrp.communicator.event.Event.ENTER_CHANNEL;
import static pl.khuzzuk.wfrp.communicator.event.Event.MESSAGE;

@AllArgsConstructor
@RestController
@RequestMapping(value = "/channel", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
public class ChannelService {
    private final Bus<Event> bus;

    @RequestMapping
    public Flux<Message> enter(@RequestBody User user) {
        ChannelListener listener = new ChannelListener();
        List<Cancellable<Event>> userSubscription = List.of(
                bus.subscribingFor(Event.MESSAGE).accept(listener::receiveMessage).subscribe()
        );
        listener.onComplete(() -> userSubscription.forEach(bus::unSubscribe));

        bus.message(ENTER_CHANNEL).withContent(user).send();
        bus.message(MESSAGE).withContent(Message.builder()
                .author(user)
                .time(LocalDateTime.now())
                .text(String.format("%s entered channel.", user.getLogin()))
                .build()).send();

        return Flux.concat(listener);
    }
}
