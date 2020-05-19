package main.repository;

import main.model.ModerationStatus;
import main.model.Post;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;
import java.util.Optional;

@Repository
public interface PostRepository extends CrudRepository<Post, Integer> {
    Optional<Post> findByIdAndActiveTrueAndModerationStatus(int id, ModerationStatus moderationStatus);
    List<Post> findAllByActiveTrueAndModerationStatusAndTimeBefore(ModerationStatus moderationStatus, Date time);
    List<Post> findAllByTitleContainingAndModerationStatusAndTimeBeforeAndActiveTrue(String title, ModerationStatus moderationStatus, Date time);
    List<Post> findAllByTimeBetweenAndActiveTrueAndModerationStatus(Date time, Date time2, ModerationStatus moderationStatus);
}
