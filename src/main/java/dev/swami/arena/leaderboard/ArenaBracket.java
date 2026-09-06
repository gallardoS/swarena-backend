package dev.swami.arena.leaderboard;

enum ArenaBracket {

    TWO_V_TWO("2v2", 2),
    THREE_V_THREE("3v3", 3),
    FIVE_V_FIVE("5v5", 5);

    private final String value;
    private final int databaseType;

    ArenaBracket(String value, int databaseType) {
        this.value = value;
        this.databaseType = databaseType;
    }

    String value() {
        return value;
    }

    int databaseType() {
        return databaseType;
    }

    static ArenaBracket from(String value) {
        for (ArenaBracket bracket : values()) {
            if (bracket.value.equalsIgnoreCase(value)) {
                return bracket;
            }
        }
        throw new InvalidLeaderboardRequestException("Bracket must be one of: 2v2, 3v3, 5v5");
    }
}
