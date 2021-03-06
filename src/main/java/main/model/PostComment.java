package main.model;

import java.util.Date;
import java.util.List;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import org.hibernate.annotations.Type;

@Entity
@Table(name = "post_comments")
public class PostComment {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private int id;

  @ManyToOne
  @JoinColumn(name = "parent_id")
  private PostComment parent;

  @OneToMany(mappedBy = "parent")
  private List<PostComment> children;

  @ManyToOne
  @JoinColumn(name = "post_id", nullable = false)
  private Post post;

  @ManyToOne
  @JoinColumn(name = "user_id", nullable = false)
  private User user;

  @Temporal(TemporalType.TIMESTAMP)
  @Column(nullable = false)
  private Date time;

  @Type(type = "text")
  @Column(nullable = false)
  private String text;

  public int getId() {
    return id;
  }

  public void setId(int id) {
    this.id = id;
  }

  public PostComment getParent() {
    return parent;
  }

  public void setParent(PostComment parent) {
    this.parent = parent;
  }

  public List<PostComment> getChildren() {
    return children;
  }

  public void setChildren(List<PostComment> children) {
    this.children = children;
  }

  public Post getPost() {
    return post;
  }

  public void setPost(Post post) {
    this.post = post;
  }

  public User getUser() {
    return user;
  }

  public void setUser(User user) {
    this.user = user;
  }

  public Date getTime() {
    return time;
  }

  public void setTime(Date time) {
    this.time = time;
  }

  public String getText() {
    return text;
  }

  public void setText(String text) {
    this.text = text;
  }
}
