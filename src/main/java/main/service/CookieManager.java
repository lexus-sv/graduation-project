package main.service;

import lombok.extern.slf4j.Slf4j;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Slf4j
public class CookieManager {

    private static CookieManager instance;

    public CookieManager() {
    }

    public void deleteCookie(HttpServletResponse response) throws IOException {
        Cookie cookie = new Cookie("token", null);
        cookie.setMaxAge(0);
        cookie.setPath("/");
        response.addCookie(cookie);
    }

    public void addCookie(HttpServletResponse response, String token, int age){
        Cookie cookie = new Cookie("token", token);
        cookie.setMaxAge(age);
        cookie.setPath("/");
        response.addCookie(cookie);
        log.info("Cookie with age "+age+"seconds created successfully");
    }

    public static CookieManager getInstance() {
        if(instance==null){
            instance = new CookieManager();
        }
        return instance;
    }
}
