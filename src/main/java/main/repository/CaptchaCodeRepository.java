package main.repository;

import java.util.Date;
import java.util.Optional;
import javax.transaction.Transactional;
import main.model.CaptchaCode;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CaptchaCodeRepository extends JpaRepository<CaptchaCode, Integer> {

  @Transactional
  void removeAllByTimeBefore(Date time);

  Optional<CaptchaCode> findFirstBySecretCode(String secretCode);
}
