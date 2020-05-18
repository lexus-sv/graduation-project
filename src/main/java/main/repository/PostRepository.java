package main.repository;

import main.model.ModerationStatus;
import main.model.Post;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;

@Repository
public interface PostRepository extends CrudRepository<Post, Integer> {
    List<Post> findAllByActiveTrueAndModerationStatusAndTimeBefore(ModerationStatus moderationStatus, Date time);
    List<Post> findAllByTitleContainingAndModerationStatusAndTimeBeforeAndActiveTrue(String title, ModerationStatus moderationStatus, Date time);
}
