package main.model;

import java.util.Date;
import java.util.List;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
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
@Table(name = "posts")
public class Post {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private int id;

  @Type(type = "true_false")
  @Column(nullable = false)
  private boolean active;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private ModerationStatus moderationStatus;

  @ManyToOne
  @JoinColumn(name = "moderator_id")
  private User moderator;

  @ManyToOne
  @JoinColumn(name = "author_id", nullable = false)
  private User user;

  @Temporal(value = TemporalType.TIMESTAMP)
  @Column(nullable = false)
  private Date time;

  @Column(nullable = false)
  private String title;

  @Type(type = "text")
  @Column(nullable = false)
  private String text;

  @Column(nullable = false)
  private int viewCount;

  @OneToMany(mappedBy = "post")
  private List<PostVote> postVotes;

  @OneToMany(mappedBy = "post", cascade = CascadeType.ALL)
  private List<TagToPost> tags;

  @OneToMany(mappedBy = "post")
  private List<PostComment> postComments;

  public int getId() {
    return id;
  }

  public void setId(int id) {
    this.id = id;
  }

  public boolean isActive() {
    return active;
  }

  public void setActive(boolean active) {
    this.active = active;
  }

  public ModerationStatus getModerationStatus() {
    return moderationStatus;
  }

  public void setModerationStatus(ModerationStatus moderationStatus) {
    this.moderationStatus = moderationStatus;
  }

  public User getModerator() {
    return moderator;
  }

  public void setModerator(User moderator) {
    this.moderator = moderator;
  }

  public User getUser() {
    return user;
  }

  public void setUser(User author) {
    this.user = author;
  }

  public Date getTime() {
    return time;
  }

  public void setTime(Date time) {
    this.time = time;
  }

  public String getTitle() {
    return title;
  }

  public void setTitle(String title) {
    this.title = title;
  }

  public String getText() {
    return text;
  }

  public void setText(String text) {
    this.text = text;
  }

  public int getViewCount() {
    return viewCount;
  }

  public void setViewCount(int vievCount) {
    this.viewCount = vievCount;
  }

  public List<PostVote> getPostVotes() {
    return postVotes;
  }

  public void setPostVotes(List<PostVote> postVotes) {
    this.postVotes = postVotes;
  }

  public List<TagToPost> getTags() {
    return tags;
  }

  public void setTags(List<TagToPost> tags) {
    this.tags = tags;
  }

  public List<PostComment> getPostComments() {
    return postComments;
  }

  public void setPostComments(List<PostComment> postComments) {
    this.postComments = postComments;
  }
}