package main.api.general.calendar;

import java.util.HashMap;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CalendarResponse {

  private List<Long> years;
  private HashMap<String, Long> posts;
}
