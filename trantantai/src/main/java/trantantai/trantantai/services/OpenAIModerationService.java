package trantantai.trantantai.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import trantantai.trantantai.config.OpenAIConfig;
import trantantai.trantantai.viewmodels.ModerationResultVm;

import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Service for content moderation using OpenAI's Moderation API.
 * Provides text and image moderation capabilities to detect harmful content.
 */
@Service
public class OpenAIModerationService {

    private static final Logger logger = Logger.getLogger(OpenAIModerationService.class.getName());
    private static final int TIMEOUT_MS = 10000; // 10 second timeout

    private final OpenAIConfig openAIConfig;
    private final RestTemplate restTemplate;

    @Autowired
    public OpenAIModerationService(OpenAIConfig openAIConfig) {
        this.openAIConfig = openAIConfig;
        
        // Create RestTemplate with timeout configuration
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(TIMEOUT_MS);
        factory.setReadTimeout(TIMEOUT_MS);
        this.restTemplate = new RestTemplate(factory);
    }

    /**
     * Moderate text content using OpenAI's moderation API.
     *
     * @param text the text content to moderate
     * @return ModerationResultVm containing flagged status and categories
     * @throws IllegalStateException if moderation service is unavailable
     */
    public ModerationResultVm moderateText(String text) {
        if (text == null || text.isBlank()) {
            // Empty text is not flagged
            return new ModerationResultVm(false, Map.of(), Map.of());
        }

        try {
            // Build request body for text moderation
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("model", openAIConfig.getModerationModel());
            requestBody.put("input", text);

            // Make API call
            Map<String, Object> response = callModerationApi(requestBody);
            ModerationResultVm result = ModerationResultVm.from(response);

            // Log moderation result (NOT the content itself)
            logger.info("Text moderation: flagged=" + result.flagged() + 
                       ", categories=" + getFlaggedCategories(result));

            return result;

        } catch (RestClientException e) {
            logger.log(Level.SEVERE, "OpenAI moderation API unavailable: " + e.getMessage(), e);
            throw new IllegalStateException("Content moderation service unavailable");
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Content moderation failed: " + e.getMessage(), e);
            throw new IllegalStateException("Content moderation failed");
        }
    }

    /**
     * Moderate image content using OpenAI's moderation API.
     *
     * @param imageBytes the raw image bytes
     * @param mimeType the MIME type of the image (e.g., "image/jpeg")
     * @return ModerationResultVm containing flagged status and categories
     * @throws IllegalStateException if moderation service is unavailable
     */
    public ModerationResultVm moderateImage(byte[] imageBytes, String mimeType) {
        if (imageBytes == null || imageBytes.length == 0) {
            // Empty image is not flagged
            return new ModerationResultVm(false, Map.of(), Map.of());
        }

        try {
            // Encode image to base64 with data URL format
            String base64Image = Base64.getEncoder().encodeToString(imageBytes);
            String dataUrl = "data:" + mimeType + ";base64," + base64Image;

            // Build multimodal input for image moderation
            Map<String, Object> imageInput = new HashMap<>();
            imageInput.put("type", "image_url");
            Map<String, String> imageUrl = new HashMap<>();
            imageUrl.put("url", dataUrl);
            imageInput.put("image_url", imageUrl);

            // Build request body
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("model", openAIConfig.getModerationModel());
            requestBody.put("input", List.of(imageInput));

            // Make API call
            Map<String, Object> response = callModerationApi(requestBody);
            ModerationResultVm result = ModerationResultVm.from(response);

            // Log moderation result (NOT the image itself)
            logger.info("Image moderation: flagged=" + result.flagged() + 
                       ", categories=" + getFlaggedCategories(result));

            return result;

        } catch (RestClientException e) {
            logger.log(Level.SEVERE, "OpenAI moderation API unavailable: " + e.getMessage(), e);
            throw new IllegalStateException("Content moderation service unavailable");
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Image moderation failed: " + e.getMessage(), e);
            throw new IllegalStateException("Content moderation failed");
        }
    }

    /**
     * Make the actual API call to OpenAI moderation endpoint.
     */
    @SuppressWarnings("unchecked")
    private Map<String, Object> callModerationApi(Map<String, Object> requestBody) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(openAIConfig.getApiKey());

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

        ResponseEntity<Map> response = restTemplate.exchange(
                openAIConfig.getModerationEndpoint(),
                HttpMethod.POST,
                entity,
                Map.class
        );

        Map<String, Object> responseBody = response.getBody();
        if (responseBody == null) {
            throw new IllegalStateException("Empty response from moderation API");
        }

        return responseBody;
    }

    /**
     * Extract only the flagged category names for logging.
     * Does not log confidence scores to keep logs clean.
     */
    private String getFlaggedCategories(ModerationResultVm result) {
        if (result.categories() == null || result.categories().isEmpty()) {
            return "[]";
        }
        
        List<String> flagged = result.categories().entrySet().stream()
                .filter(Map.Entry::getValue)
                .map(Map.Entry::getKey)
                .toList();
        
        return flagged.toString();
    }
}
