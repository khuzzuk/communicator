package pl.khuzzuk.wfrp.communicator.channel;

import reactor.core.CoreSubscriber;
import reactor.core.publisher.Flux;

import java.io.Closeable;

public class ChannelListener extends Flux<Message> implements Closeable {
    private CoreSubscriber<? super Message> actual;
    private Runnable onComplete;

    @Override
    public void subscribe(CoreSubscriber<? super Message> actual) {
        this.actual = actual;
    }

    public void receiveMessage(Message message) {
        if (actual != null) {
            actual.onNext(message);
        }
    }

    public void onComplete(Runnable onComplete) {
        this.onComplete = onComplete;
    }

    @Override
    public void close() {
        onComplete.run();
        actual.onComplete();
    }
}
