package main.api;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class SettingsRequest {
    private Boolean MULTIUSER_MODE;
    private Boolean POST_PREMODERATION;
    private Boolean STATISTICS_IS_PUBLIC;
}
