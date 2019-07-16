package pl.khuzzuk.wfrp.communicator.channel;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import pl.khuzzuk.wfrp.communicator.user.User;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = {"time"})
public class Message implements Comparable<Message> {
    private User author;
    private LocalDateTime time;
    private String text;

    @Override
    public int compareTo(Message other) {
        return time.compareTo(other.time);
    }
}
