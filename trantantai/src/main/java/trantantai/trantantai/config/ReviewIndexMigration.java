package trantantai.trantantai.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Component;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Migration to drop the unique compound index on reviews collection.
 * This allows users to submit multiple reviews for the same product.
 */
@Component
public class ReviewIndexMigration {

    private static final Logger logger = Logger.getLogger(ReviewIndexMigration.class.getName());

    private final MongoTemplate mongoTemplate;

    @Autowired
    public ReviewIndexMigration(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void dropUniqueReviewIndex() {
        try {
            // Drop the old unique compound index if it exists
            mongoTemplate.getCollection("reviews").dropIndex("bookId_1_userId_1");
            logger.info("Successfully dropped unique index 'bookId_1_userId_1' from reviews collection");
        } catch (Exception e) {
            // Index might not exist, which is fine
            if (e.getMessage() != null && e.getMessage().contains("index not found")) {
                logger.info("Index 'bookId_1_userId_1' does not exist - no action needed");
            } else {
                logger.log(Level.WARNING, "Could not drop index (may not exist): " + e.getMessage());
            }
        }
    }
}
