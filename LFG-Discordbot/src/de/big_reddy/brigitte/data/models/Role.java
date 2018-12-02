package de.big_reddy.brigitte.data.models;

public enum Role {
    TANK("tank"),
    DPS("dps"),
    SUPPORT("support"),
    FLEX("flex"),
    NONE("none");

    private final String identifier;

    private Role(final String identifier) {
        this.identifier = identifier;
    }

    @Override
    public String toString() {
        return this.identifier;
    }

    public static Role getRoleByIdentifier(final String identifier) {
        switch (identifier) {
            case "tank":
                return Role.TANK;
            case "dps":
                return Role.DPS;
            case "support":
                return Role.SUPPORT;
            case "flex":
                return Role.FLEX;
            default:
                return null;
        }
    }

}
