package main.repository;

import main.api.MyStatisticsResponse;
import main.model.GlobalSettings;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface GlobalSettingsRepository extends JpaRepository<GlobalSettings, Integer> {
    @Query("select new main.api.MyStatisticsResponse(" +
            "count(p), " +
            "(select count(pv) from PostVote pv where pv.value=true), " +
            "(select count(pv) from PostVote pv where pv.value=false), " +
            "(select sum(p.viewCount) from Post p), " +
            "function('date_format', max(p.time), '%k:%i %d.%m.%Y')) from Post p")
    MyStatisticsResponse getGlobalStats();

    GlobalSettings findByCode(String code);
}
