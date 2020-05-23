package main.repository;

import main.model.ModerationStatus;
import main.model.Post;
import main.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;
import java.util.Optional;

@Repository
public interface PostRepository extends JpaRepository<Post, Integer> {
    Optional<Post> findByIdAndActiveTrueAndModerationStatus(int id, ModerationStatus moderationStatus);
    List<Post> findAllByActiveTrueAndModerationStatusAndTimeBefore(ModerationStatus moderationStatus, Date time);
    List<Post> findAllByTitleContainingAndModerationStatusAndTimeBeforeAndActiveTrue(String title, ModerationStatus moderationStatus, Date time);
    List<Post> findAllByTimeAfterAndTimeBeforeAndActiveTrueAndModerationStatus(Date time, Date time2, ModerationStatus moderationStatus);
    @Query("select p from Post p join TagToPost ttp on ttp.post.id=p.id join Tag t on t.id=ttp.tag.id where t.name like %?1% and p.active=true and p.moderationStatus=?2")
    List<Post> findAllByTag(String tagName, ModerationStatus moderationStatus);
    List<Post> findAllByActiveTrueAndModeratorOrModerationStatusAndActiveTrue(User moderator, ModerationStatus moderationStatus);
    //inactive - скрытые, ещё не опубликованы (is_active = 0)
    //pending - активные, ожидают утверждения модератором (is_active = 1,
    //moderation_status = NEW)
    //declined - отклонённые по итогам модерации (is_active = 1, moderation_status =
    //DECLINED)
    //published - опубликованные по итогам модерации (is_active = 1, moderation_status =
    //ACCEPTED)
    List<Post> findAllByActiveFalse();
    List<Post> findAllByActiveTrueAndModerationStatus(ModerationStatus moderationStatus);

}
