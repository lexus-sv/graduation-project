package main.repository;

import java.util.Optional;
import main.api.general.StatisticsResponse;
import main.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User, Integer> {

  Optional<User> findByEmail(String email);

  boolean existsByEmail(String email);

  @Query("select new main.api.general.StatisticsResponse(" +
      "count(p), " +
      "(select count(pv) from PostVote pv join Post p on pv.post=p where pv.value=true and p.user=?1), "
      +
      "(select count(pv) from PostVote pv join Post p on pv.post=p where pv.value=false and p.user=?1), "
      +
      "(select sum(p.viewCount) from Post p where p.user =?1), " +
      "function('date_format', max(p.time), '%k:%i %d.%m.%Y')) from User u join Post p on u.id=p.user.id where p.user=?1")
  StatisticsResponse getStatisticsByUser(User user);
}
