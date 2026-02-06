package trantantai.trantantai.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "openai")
public class OpenAIConfig {

    private String apiKey;
    private String moderationEndpoint;
    private String moderationModel;

    // Getters and Setters
    public String getApiKey() { return apiKey; }
    public void setApiKey(String apiKey) { this.apiKey = apiKey; }

    public String getModerationEndpoint() { return moderationEndpoint; }
    public void setModerationEndpoint(String moderationEndpoint) { this.moderationEndpoint = moderationEndpoint; }

    public String getModerationModel() { return moderationModel; }
    public void setModerationModel(String moderationModel) { this.moderationModel = moderationModel; }
}
