package main.api.general.profile;

import lombok.Data;

@Data
public class ProfileErrors {
    private String email;
    private String photo;
    private String name;
    private String password;
}
