
package main.api.response;

import java.util.List;


public class Tags {

    private List<Tag> tags;

    public Tags(List<Tag> tags) {
        this.tags = tags;
    }

    public List<Tag> getTags() {
        return tags;
    }

    public void setTags(List<Tag> tags) {
        this.tags = tags;
    }

}
