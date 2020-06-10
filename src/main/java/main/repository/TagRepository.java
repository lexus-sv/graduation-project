package main.repository;

import main.model.Tag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TagRepository extends JpaRepository<Tag, Integer> {
    @Query("select t from Tag t join TagToPost ttp on t.id=ttp.tag.id join Post p on ttp.post.id=p.id " +
            "where p.moderationStatus=main.model.ModerationStatus.ACCEPTED and p.active=true")
    List<Tag> getRelevantTags(String name);

    Tag findFirstByName(String name);

    boolean existsByName(String name);
}
