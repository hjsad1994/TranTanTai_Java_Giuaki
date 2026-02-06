package trantantai.trantantai.validators;

import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.util.Arrays;
import java.util.List;

/**
 * Validator for image file uploads.
 * Validates file type and size constraints.
 */
@Component
public class ImageValidator {

    // Maximum file size: 5MB
    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024;

    // Allowed MIME types for images
    private static final List<String> ALLOWED_CONTENT_TYPES = Arrays.asList(
            "image/jpeg",
            "image/jpg",
            "image/png",
            "image/gif",
            "image/webp"
    );

    // Allowed file extensions
    private static final List<String> ALLOWED_EXTENSIONS = Arrays.asList(
            ".jpg", ".jpeg", ".png", ".gif", ".webp"
    );

    /**
     * Validate an uploaded image file.
     *
     * @param file the multipart file to validate
     * @throws IllegalArgumentException if validation fails
     */
    public void validateImage(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Vui lòng chọn file");
        }

        // Validate file size
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new IllegalArgumentException(
                    String.format("Kích thước file vượt quá %d MB cho phép", MAX_FILE_SIZE / (1024 * 1024))
            );
        }

        // Validate content type
        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_CONTENT_TYPES.contains(contentType.toLowerCase())) {
            throw new IllegalArgumentException(
                    "Định dạng file không hợp lệ. Chỉ chấp nhận: JPEG, PNG, GIF, WebP"
            );
        }

        // Validate file extension (defense in depth)
        String originalFilename = file.getOriginalFilename();
        if (originalFilename != null) {
            String lowerFilename = originalFilename.toLowerCase();
            boolean validExtension = ALLOWED_EXTENSIONS.stream()
                    .anyMatch(lowerFilename::endsWith);
            if (!validExtension) {
                throw new IllegalArgumentException(
                        "Đuôi file không hợp lệ. Chỉ chấp nhận: jpg, jpeg, png, gif, webp"
                );
            }
        }
    }

    /**
     * Validate multiple image files.
     *
     * @param files the list of multipart files to validate
     * @param maxCount maximum number of files allowed
     * @throws IllegalArgumentException if validation fails
     */
    public void validateImages(List<MultipartFile> files, int maxCount) {
        if (files == null || files.isEmpty()) {
            return; // Empty list is valid (no images)
        }

        if (files.size() > maxCount) {
            throw new IllegalArgumentException(
                    String.format("Chỉ được tải tối đa %d ảnh", maxCount)
            );
        }

        for (MultipartFile file : files) {
            validateImage(file);
        }
    }

    /**
     * Get the maximum allowed file size in bytes.
     *
     * @return max file size in bytes
     */
    public long getMaxFileSize() {
        return MAX_FILE_SIZE;
    }

    /**
     * Get the list of allowed content types.
     *
     * @return list of allowed MIME types
     */
    public List<String> getAllowedContentTypes() {
        return ALLOWED_CONTENT_TYPES;
    }
}
