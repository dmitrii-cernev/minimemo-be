package md.cernev.minimemo.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
public class UserDto {
    private String id;
    private String firstName;
    private String lastName;
    private String login;
    private String token;
}
