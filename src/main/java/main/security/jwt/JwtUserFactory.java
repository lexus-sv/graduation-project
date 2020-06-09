package main.security.jwt;

import main.model.User;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.ArrayList;
import java.util.List;

public final class JwtUserFactory {

    public JwtUserFactory() {
    }

    public static JwtUser create(User user){
        return new JwtUser(
                user.getId(),
                user.isModerator(),
                user.getRegistrationDate(),
                user.getName(),
                user.getEmail(),
                user.getPassword(),
                user.getCode(),
                user.getPhoto(),
                getAuthorities(user)
        );
    }

    private static List<GrantedAuthority> getAuthorities(User user){
        List<GrantedAuthority> authorities = new ArrayList<>();
        authorities.add(new SimpleGrantedAuthority("ROLE_USER"));
        if(user.isModerator()){
            authorities.add(new SimpleGrantedAuthority("ROLE_MODERATOR"));
        }
        return authorities;
    }
}
