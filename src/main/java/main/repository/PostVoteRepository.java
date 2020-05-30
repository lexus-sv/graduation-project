package main.repository;

import main.model.Post;
import main.model.PostVote;
import main.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PostVoteRepository extends JpaRepository<PostVote, Integer> {
    Optional<PostVote> findByValueAndUserAndPost(boolean value, User user, Post post);
    Optional<PostVote> findByUserAndPost(User user, Post post);
}
