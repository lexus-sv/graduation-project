package main.api;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

@Data
public class ProfileEditRequest {
    private Object photo;
    private Boolean removePhoto;
    private String name;
    private String email;
    private String password;
}
