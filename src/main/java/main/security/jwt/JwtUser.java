package main.security.jwt;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Date;

public class JwtUser implements UserDetails {

    private final int id;
    private final boolean isModerator;
    private final Date registrationDate;
    private final String name;
    private final String email;
    private final String password;
    private final String code;
    private final String photo;
    private Collection<? extends GrantedAuthority> authorities;

    public JwtUser(int id, boolean isModerator, Date registrationDate, String name, String email, String password, String code, String photo, Collection<? extends GrantedAuthority> authorities) {
        this.id = id;
        this.isModerator = isModerator;
        this.registrationDate = registrationDate;
        this.name = name;
        this.email = email;
        this.password = password;
        this.code = code;
        this.photo = photo;
        this.authorities = authorities;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return email;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    public int getId() {
        return id;
    }

    public boolean isModerator() {
        return isModerator;
    }

    public Date getRegistrationDate() {
        return registrationDate;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public String getCode() {
        return code;
    }

    public String getPhoto() {
        return photo;
    }
}
