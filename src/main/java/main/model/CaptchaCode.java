package main.model;

import java.util.Date;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

@Entity
@Table(name = "captcha_codes")
public class CaptchaCode {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private int id;

  @Temporal(TemporalType.TIMESTAMP)
  @Column(nullable = false)
  private Date time;

  @Column(nullable = false, columnDefinition = "TINYTEXT")
  private String code;

  @Column(nullable = false, columnDefinition = "TINYTEXT")
  private String secretCode;

  public int getId() {
    return id;
  }

  public void setId(int id) {
    this.id = id;
  }

  public Date getTime() {
    return time;
  }

  public void setTime(Date time) {
    this.time = time;
  }

  public String getCode() {
    return code;
  }

  public void setCode(String code) {
    this.code = code;
  }

  public String getSecretCode() {
    return secretCode;
  }

  public void setSecretCode(String secretCode) {
    this.secretCode = secretCode;
  }

  @Override
  public String toString() {
    return "CaptchaCode{" +
        "id=" + id +
        ", time=" + time +
        ", code='" + code + '\'' +
        ", secretCode='" + secretCode + '\'' +
        '}';
  }
}
