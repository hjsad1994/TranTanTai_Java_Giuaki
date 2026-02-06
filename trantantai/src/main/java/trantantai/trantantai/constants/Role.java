package trantantai.trantantai.constants;

/**
 * Role enum for type-safe role references in code.
 * The numeric ID is for code reference only, not stored in MongoDB.
 * MongoDB stores the role name as String (e.g., "ADMIN", "USER").
 */
public enum Role {
    ADMIN(1),
    USER(2);

    private final int id;

    Role(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }
}
