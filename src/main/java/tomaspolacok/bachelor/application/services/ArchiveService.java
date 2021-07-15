package tomaspolacok.bachelor.application.services;

import java.io.InputStream;
import java.net.URL;
import java.util.Collections;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

import com.google.api.client.http.InputStreamContent;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.File;

import tomaspolacok.bachelor.application.entities.EbayItem;
import tomaspolacok.bachelor.application.entities.Picture;
import tomaspolacok.bachelor.application.entities.PictureCategory;
import tomaspolacok.bachelor.application.entities.User;
import tomaspolacok.bachelor.application.entities.UserPicture;
import tomaspolacok.bachelor.application.repositories.PictureCategoryRepository;
import tomaspolacok.bachelor.application.repositories.PictureRepository;
import tomaspolacok.bachelor.application.repositories.UserPictureRepository;

@Service
public class ArchiveService {
	
	@Autowired
	PictureRepository pictureRepository;
	@Autowired
	UserPictureRepository userPictureRepository;
	@Autowired
	PictureCategoryRepository pictureCategoryRepository;
	@Autowired
	UserService userService;
	@Autowired
	Drive service;
	
	@Value("${google.folder-id}")
    private String folderId;
	
	public static final String EBAY_IMAGE_URL_PREFIX = "https://i.ebayimg.com/images/g/";
	public static final int EBAY_IMAGE_URL_PREFIX_LENGTH = 31;
	public static final String EBAY_IMAGE_URL_SUFFIX = "/s-l500.jpg";
	public static final int EBAY_IMAGE_URL_SUFFIX_LENGTH = 10;
	
	/**
	 * Adds picture with category to user's archive 
	 * @param item
	 * @param pictureUrl
	 * @param category
	 */
	public void addPicture(EbayItem item, String pictureUrlId, PictureCategory category) throws Exception{
		Picture picture;
		picture = pictureRepository
						.findById(pictureUrlId)
						.orElse(null);
		if (picture == null) return;
		if (picture.getGoogleId() == null) {
			uploadFile(picture.getEbayLink(), picture);
			pictureRepository.save(picture);
		}
		User user = userService.getLoggedUser();
		UserPicture up = userPictureRepository.findByUserAndCategoryAndPicture(user, category, picture);
		if (up == null) {
			up = new UserPicture();
			up.setUser(user);
			up.setPicture(picture);
			up.setCategory(category);
			userPictureRepository.save(up);
		}
		
	}
	
	/**
	 * Delete picture from user archive
	 * @param pictureGoogleId
	 */
	public void archiveDelete(String pictureGoogleId) {
		Picture picture = pictureRepository.findByGoogleId(pictureGoogleId)
				.orElse(null);
		userPictureRepository.deleteByUserAndPictureId(userService.getLoggedUser(), picture);
	}
	
	/**
	 * Deletes picture by category
	 * @param categoryId
	 * @param pictureGoogleId
	 */
	public void archiveDeleteByCategory(Long categoryId, String pictureGoogleId) {
		Picture picture = pictureRepository.findByGoogleId(pictureGoogleId)
				.orElse(null);
		PictureCategory category = pictureCategoryRepository.findById(categoryId)
				.orElse(null);
		userPictureRepository.deleteByUserAndPictureAndCategory(userService.getLoggedUser(), category, picture);
	}
	
	/**
	 * Returns page of pictures owned by user
	 * @param page
	 * @return
	 */
	public Page<Picture> getPictures(Integer page) {
		User user = userService.getLoggedUser();
		if (user.getUserPreferences().getPictureCategory() != null) {
			return userPictureRepository.findPicturesByUserAndCategory(user, user.getUserPreferences().getPictureCategory(), user.getUserPreferences().getArchiveCount().getPageable(page));
		}
		return userPictureRepository.findPicturesByUser(user, user.getUserPreferences().getArchiveCount().getPageable(page));
	}
	
	/**
	 * Saves a new category for user with category name
	 * @param categoryName
	 * @return
	 */
	public PictureCategory saveCategory(String categoryName) {
		User user = userService.getLoggedUser();
		categoryName = categoryName.toUpperCase();
		PictureCategory cat = pictureCategoryRepository.findByNameAndUser(categoryName, user);
		if (cat == null) {
			cat = new PictureCategory();
			cat.setName(categoryName);
			cat.setUser(user);
			pictureCategoryRepository.save(cat);
		}
		return cat;
	}

	/**
	 * Gets a list of user picture categories
	 * @return
	 */
	public List<PictureCategory> getUserCategories() {
		return pictureCategoryRepository.findByUser(userService.getLoggedUser());
	}
	
	/**
	 * Retrieves picture in byte array from either google drive or ebay if it's not uploaded yet
	 * @param link
	 * @return
	 * @throws Exception
	 */
	@Cacheable("picture")
	public byte[] getPicture(String imageId) throws Exception {
		Picture picture = pictureRepository
							.findById(imageId)
							.orElse(null);
		InputStream in;
		if (picture.getGoogleId() == null) {
			in = new URL(picture.getEbayLink()).openStream();
		} else {
			in = new URL(picture.getDownloadLink()).openStream();
		}
		return IOUtils.toByteArray(in);
	}
	
	/**
	 * Uploads new picture to Google Drive
	 * @param pictureUrl
	 * @return
	 * @throws Exception
	 */
	public Picture uploadFile(String pictureUrl, Picture picture) throws Exception {
		File fileMetadata = new File();
		fileMetadata.setName(pictureUrl);
		fileMetadata.setParents(Collections.singletonList(folderId));
		InputStream in = new URL(pictureUrl).openStream();
		InputStreamContent content = new InputStreamContent("image/jpeg", in);
		File file = service.files()
				.create(fileMetadata, content)
			    .setFields("id, webContentLink")
			    .execute();
		picture.setDownloadLink(file.getWebContentLink());
		picture.setGoogleId(file.getId());
		pictureRepository.save(picture);
		return picture;
	}
	
	
}
