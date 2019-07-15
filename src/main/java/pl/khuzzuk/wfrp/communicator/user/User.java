package pl.khuzzuk.wfrp.communicator.user;

import lombok.Data;

@Data
public class User {
    private Long id;
    private String login;
    private String password;
}
