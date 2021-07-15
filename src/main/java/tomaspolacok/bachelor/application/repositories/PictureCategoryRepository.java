package tomaspolacok.bachelor.application.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import tomaspolacok.bachelor.application.entities.PictureCategory;
import tomaspolacok.bachelor.application.entities.User;

public interface PictureCategoryRepository extends JpaRepository<PictureCategory, Long> {

	List<PictureCategory> findByUser(User u);
	
	PictureCategory findByNameAndUser(String name, User u);
}
