package main;

import java.io.Serializable;

public class InitInfo implements Serializable {

    private String title="DevPub";
    private String subtitle="Рассказы разработчиков";
    private String phone="+7 903 666-44-55";
    private String email="alakai20136@gmail.com";
    private String copyright = "Алексей Сухилин";
    private String copyrightFrom = "2019";

    public InitInfo() {
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getSubtitle() {
        return subtitle;
    }

    public void setSubtitle(String subtitle) {
        this.subtitle = subtitle;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getCopyright() {
        return copyright;
    }

    public void setCopyright(String copyright) {
        this.copyright = copyright;
    }

    public String getCopyrightFrom() {
        return copyrightFrom;
    }

    public void setCopyrightFrom(String copyrightFrom) {
        this.copyrightFrom = copyrightFrom;
    }

    @Override
    public String toString() {
        return "InitInfo{" +
                "title='" + title + '\'' +
                ", subtitle='" + subtitle + '\'' +
                ", phone='" + phone + '\'' +
                ", email='" + email + '\'' +
                ", copyright='" + copyright + '\'' +
                ", copyrightFrom='" + copyrightFrom + '\'' +
                '}';
    }
}
