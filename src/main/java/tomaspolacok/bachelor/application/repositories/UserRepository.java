package tomaspolacok.bachelor.application.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import tomaspolacok.bachelor.application.entities.User;

@Repository("userRepository")
public interface UserRepository extends JpaRepository<User, Long> {
	
	User findByEmail(String email);
	
	User findByActivationCode(String activationCode);
	
	User findByResetCode(String resetCode);
	
	@Query("select count(u)>0 from User u where u.activationCode = :code")
	Boolean resetCodeExists(@Param("code") String code);
	
	@Query("select count(u)>0 from User u where u.resetCode = :code")
	Boolean activationCodeExists(@Param("code") String code);
}	