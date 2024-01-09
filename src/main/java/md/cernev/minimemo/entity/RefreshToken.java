package md.cernev.minimemo.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class RefreshToken {
    private String id;
    private String type;
    private String userLogin;
    private String refreshToken;
    private String expiration;
}
