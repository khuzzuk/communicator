package pl.khuzzuk.wfrp.communicator.channel;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import pl.khuzzuk.messaging.Bus;
import pl.khuzzuk.messaging.Cancellable;
import pl.khuzzuk.wfrp.communicator.event.Event;
import pl.khuzzuk.wfrp.communicator.user.User;

import javax.annotation.PostConstruct;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

import static pl.khuzzuk.wfrp.communicator.event.Event.ALL_MESSAGES;
import static pl.khuzzuk.wfrp.communicator.event.Event.ENTER_CHANNEL;
import static pl.khuzzuk.wfrp.communicator.event.Event.MESSAGE;

@RequiredArgsConstructor
@Slf4j
@Component
public class Channel implements AutoCloseable {
    private final Bus<Event> bus;
    private List<Cancellable<Event>> subscriptions = Collections.emptyList();
    private List<User> users = new ArrayList<>();
    private SortedSet<Message> messages = new TreeSet<>(Comparator.comparing(Message::getTime));

    @PostConstruct
    private void startChannel() {
        subscriptions = List.of(
                bus.subscribingFor(ENTER_CHANNEL).<User>accept(users::add).subscribe(),
                bus.subscribingFor(MESSAGE).<Message>accept(message -> messages.add(message)).subscribe(),
                bus.subscribingFor(ALL_MESSAGES).withResponse(() -> List.copyOf(messages)).subscribe());
    }

    @Override
    public void close() {
        subscriptions.forEach(bus::unSubscribe);
    }

    @Scheduled(fixedRate = 1000)
    public void emitTestMessage() {
        log.info("test message");
        bus.message(MESSAGE).withContent(Message.builder().text("test message").time(LocalDateTime.now()).build()).send();
    }
}
