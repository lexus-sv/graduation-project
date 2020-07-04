package main;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class InitInfo {

  private String title;
  private String subtitle;
  private String phone;
  private String email;
  private String copyright;
  private String copyrightFrom;

}
