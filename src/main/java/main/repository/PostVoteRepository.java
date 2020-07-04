package main.repository;

import java.util.Optional;
import main.model.Post;
import main.model.PostVote;
import main.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PostVoteRepository extends JpaRepository<PostVote, Integer> {

  Optional<PostVote> findByUserAndPost(User user, Post post);
}
