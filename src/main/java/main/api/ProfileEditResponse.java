package main.api;

import lombok.Data;

@Data
public class ProfileEditResponse {
    private boolean result;
    private ProfileErrors errors;
}
