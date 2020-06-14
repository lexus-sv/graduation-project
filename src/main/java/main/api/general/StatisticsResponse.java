package main.api.general;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class StatisticsResponse {
    private long postsCount;
    private long likesCount;
    private long dislikesCount;
    private long viewsCount;
    private String firstPublication;
}