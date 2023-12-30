package md.cernev.minimemo.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
public class User {
    private String id;
    private String firstName;
    private String lastName;
    private String login;
    private String password;
}
