package main.repository;

import main.api.general.calendar.CalendarObject;
import main.model.ModerationStatus;
import main.model.Post;
import main.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;

@Repository
public interface PostRepository extends JpaRepository<Post, Integer> {
    List<Post> findAllByActiveTrueAndModerationStatusAndTimeBefore(ModerationStatus moderationStatus, Date time);

    List<Post> findAllByTitleContainingAndModerationStatusAndTimeBeforeAndActiveTrue(String title, ModerationStatus moderationStatus, Date time);

    @Query("select p from Post p where function('date', p.time) = function('date', ?1) and p.moderationStatus=main.model.ModerationStatus.ACCEPTED and p.active = true ")
    List<Post> findActiveByDate(String date);

    @Query("select p from Post p join TagToPost ttp on ttp.post.id=p.id join Tag t on t.id=ttp.tag.id where t.name like %?1% and p.active=true and p.moderationStatus=main.model.ModerationStatus.ACCEPTED")
    List<Post> findAllByTag(String tagName);

    List<Post> findAllByActiveTrueAndModeratorOrModerationStatusAndActiveTrue(User moderator, ModerationStatus moderationStatus);

    List<Post> findAllByActiveFalse();

    int countByModerationStatusNot(ModerationStatus moderationStatus);

    List<Post> findAllByActiveTrueAndModerationStatus(ModerationStatus moderationStatus);

    @Query("select new main.api.request.CalendarObject(function('date',p.time), count(p)) from Post p " +
            "where function('year', p.time)=?1 and p.active=true and p.moderationStatus=main.model.ModerationStatus.ACCEPTED " +
            "group by function('date', p.time) ")
    List<CalendarObject> getCalendarQuery(int year);

    @Query("select function('year', p.time) from Post p where p.active=true and p.moderationStatus=main.model.ModerationStatus.ACCEPTED group by function('year', p.time) having function('count', p)>0 ")
    List<Long> getYears();

}
