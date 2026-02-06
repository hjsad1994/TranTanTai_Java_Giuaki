package trantantai.trantantai.entities;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "role")
public class RoleEntity {

    @Id
    private String id;

    @Indexed(unique = true)
    private String name;

    private String description;

    // Default constructor
    public RoleEntity() {
    }

    // All-args constructor
    public RoleEntity(String name, String description) {
        this.name = name;
        this.description = description;
    }

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
