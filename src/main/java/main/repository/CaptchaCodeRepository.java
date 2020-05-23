package main.repository;

import main.model.CaptchaCode;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import javax.transaction.Transactional;
import java.util.Date;
import java.util.Optional;

@Repository
public interface CaptchaCodeRepository extends JpaRepository<CaptchaCode, Integer> {
    @Transactional
    void removeAllByTimeBefore(Date time);
    Optional<CaptchaCode> findFirstBySecretCode(String secretCode);
}
