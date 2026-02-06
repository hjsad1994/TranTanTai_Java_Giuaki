package trantantai.trantantai.services;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Service for handling image uploads to Cloudinary.
 * Provides upload and delete operations with proper error handling.
 */
@Service
public class ImageUploadService {

    private static final Logger logger = Logger.getLogger(ImageUploadService.class.getName());

    private final Cloudinary cloudinary;

    @Autowired
    public ImageUploadService(Cloudinary cloudinary) {
        this.cloudinary = cloudinary;
    }

    /**
     * Upload an image to Cloudinary.
     *
     * @param file   the multipart file to upload
     * @param folder the folder in Cloudinary (e.g., "books", "reviews")
     * @return Map containing upload result with keys: public_id, secure_url, bytes, original_filename
     * @throws IOException if upload fails
     */
    @SuppressWarnings("unchecked")
    public Map<String, Object> uploadImage(MultipartFile file, String folder) throws IOException {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("File cannot be null or empty");
        }

        try {
            Map<String, Object> uploadResult = cloudinary.uploader().upload(
                    file.getBytes(),
                    ObjectUtils.asMap(
                            "folder", folder,
                            "resource_type", "image",
                            "transformation", "q_auto,f_auto" // Auto quality and format optimization
                    )
            );

            logger.info("Image uploaded successfully to folder: " + folder + 
                       ", publicId: " + uploadResult.get("public_id"));
            
            return uploadResult;
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Failed to upload image to Cloudinary", e);
            throw new IOException("Failed to upload image: " + e.getMessage(), e);
        }
    }

    /**
     * Delete an image from Cloudinary.
     *
     * @param publicId the public ID of the image to delete
     * @return true if deletion was successful
     * @throws IOException if deletion fails
     */
    @SuppressWarnings("unchecked")
    public boolean deleteImage(String publicId) throws IOException {
        if (publicId == null || publicId.isBlank()) {
            throw new IllegalArgumentException("Public ID cannot be null or blank");
        }

        try {
            Map<String, Object> result = cloudinary.uploader().destroy(
                    publicId,
                    ObjectUtils.emptyMap()
            );

            String resultStatus = (String) result.get("result");
            boolean success = "ok".equals(resultStatus);

            if (success) {
                logger.info("Image deleted successfully: " + publicId);
            } else {
                logger.warning("Image deletion returned status: " + resultStatus + " for publicId: " + publicId);
            }

            return success;
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Failed to delete image from Cloudinary: " + publicId, e);
            throw new IOException("Failed to delete image: " + e.getMessage(), e);
        }
    }

    /**
     * Extract the secure URL from upload result.
     *
     * @param uploadResult the result map from uploadImage()
     * @return the secure URL string
     */
    public String getSecureUrl(Map<String, Object> uploadResult) {
        return (String) uploadResult.get("secure_url");
    }

    /**
     * Extract the public ID from upload result.
     *
     * @param uploadResult the result map from uploadImage()
     * @return the public ID string
     */
    public String getPublicId(Map<String, Object> uploadResult) {
        return (String) uploadResult.get("public_id");
    }

    /**
     * Extract the file size from upload result.
     *
     * @param uploadResult the result map from uploadImage()
     * @return the file size in bytes
     */
    public long getFileSize(Map<String, Object> uploadResult) {
        Object bytes = uploadResult.get("bytes");
        if (bytes instanceof Number) {
            return ((Number) bytes).longValue();
        }
        return 0L;
    }
}
