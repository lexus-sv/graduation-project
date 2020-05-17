
package main.model.response.user;

public class UserFullInfo implements UserBehavior {

    private String email;

    private int id;

    private boolean moderation;

    private int moderationCount;

    private String name;

    private String photo;

    private boolean settings;

    public UserFullInfo(int id, String name, String photo, String email, boolean moderation, int moderationCount, boolean settings) {
        this.email = email;
        this.id = id;
        this.moderation = moderation;
        this.moderationCount = moderationCount;
        this.name = name;
        this.photo = photo;
        this.settings = settings;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public boolean getModeration() {
        return moderation;
    }

    public void setModeration(boolean moderation) {
        this.moderation = moderation;
    }

    public int getModerationCount() {
        return moderationCount;
    }

    public void setModerationCount(int moderationCount) {
        this.moderationCount = moderationCount;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhoto() {
        return photo;
    }

    public void setPhoto(String photo) {
        this.photo = photo;
    }

    public boolean getSettings() {
        return settings;
    }

    public void setSettings(boolean settings) {
        this.settings = settings;
    }

}
