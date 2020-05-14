package main.model;

import javax.persistence.*;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "tags")
public class Tag {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(nullable = false)
    private String name;

    @OneToMany(mappedBy = "tag")
    private List<TagToPost> taggedPosts;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<TagToPost> getTaggedPosts() {
        return taggedPosts;
    }

    public void setTaggedPosts(List<TagToPost> taggedPosts) {
        this.taggedPosts = taggedPosts;
    }
}
