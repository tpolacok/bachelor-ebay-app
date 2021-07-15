package tomaspolacok.bachelor.application.repositories;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import tomaspolacok.bachelor.application.entities.Picture;

public interface PictureRepository extends JpaRepository<Picture, String> {
	
	public Optional<Picture> findByGoogleId(String googleId);
}
