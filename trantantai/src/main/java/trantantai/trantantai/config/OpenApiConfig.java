package trantantai.trantantai.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.servers.Server;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(
    info = @Info(
        title = "TranTanTai Book Store API",
        version = "v1.0.0",
        description = "REST API documentation for TranTanTai Book Store application. " +
                      "Provides endpoints for: Books, Categories, Reviews, Images, Wishlist, " +
                      "Inventory Management, Reports & Statistics, Content Moderation, and MoMo Payment Integration.\n\n" +
                      "## Admin APIs\n" +
                      "- **Content Moderation**: Test text/image moderation using OpenAI API\n" +
                      "- **Review Management**: View, manage and delete reviews\n" +
                      "- **Inventory**: Stock tracking and export\n" +
                      "- **Reports**: Sales statistics and analytics",
        contact = @Contact(
            name = "TranTanTai",
            email = "trantantai@example.com"
        )
    ),
    servers = {
        @Server(url = "http://localhost:8080", description = "Local Development Server")
    }
)
public class OpenApiConfig {
    // Configuration via annotations - no additional beans needed
}
