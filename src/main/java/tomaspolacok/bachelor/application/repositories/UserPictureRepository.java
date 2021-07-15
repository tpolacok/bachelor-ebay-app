package tomaspolacok.bachelor.application.repositories;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import tomaspolacok.bachelor.application.entities.Picture;
import tomaspolacok.bachelor.application.entities.PictureCategory;
import tomaspolacok.bachelor.application.entities.User;
import tomaspolacok.bachelor.application.entities.UserPicture;

public interface UserPictureRepository extends JpaRepository<UserPicture, Long> {
	
	@Query("select distinct up.picture from UserPicture up where up.user = :user")
	public Page<Picture> findPicturesByUser(@Param("user") User user, Pageable pageable);
	
	@Query("select distinct up.picture from UserPicture up where up.user = :user and up.category = :category")
	public Page<Picture> findPicturesByUserAndCategory(@Param("user") User user, @Param("category") PictureCategory category, Pageable pageable);
	
	@Modifying
	@Transactional
	@Query("delete from UserPicture up where up.user = :user and up.picture = :picture")
	public void deleteByUserAndPictureId(@Param("user") User user, @Param("picture") Picture picture);
	
	@Modifying
	@Transactional
	@Query("delete from UserPicture up where up.user = :user and up.picture = :picture and up.category = :category")
	public void deleteByUserAndPictureAndCategory(@Param("user") User user, @Param("category") PictureCategory category, @Param("picture") Picture picture);	
	
	public UserPicture findByUserAndCategoryAndPicture(User u, PictureCategory category, Picture picture);
}
