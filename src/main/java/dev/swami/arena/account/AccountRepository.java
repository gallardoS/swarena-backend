package dev.swami.arena.account;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.Statement;

@Repository
class AccountRepository {

    private static final String INSERT_ACCOUNT = """
            INSERT INTO account (username, salt, verifier, expansion, reg_mail, email, joindate)
            VALUES (?, ?, ?, ?, ?, ?, NOW())
            """;

    private static final String INITIALIZE_REALMS = """
            INSERT INTO realmcharacters (realmid, acctid, numchars)
            SELECT id, ?, 0 FROM realmlist
            """;

    private final JdbcTemplate jdbcTemplate;

    AccountRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    long create(String username, String email, byte[] salt, byte[] verifier, int expansion) {
        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement statement = connection.prepareStatement(INSERT_ACCOUNT, Statement.RETURN_GENERATED_KEYS);
            statement.setString(1, username);
            statement.setBytes(2, salt);
            statement.setBytes(3, verifier);
            statement.setInt(4, expansion);
            statement.setString(5, email);
            statement.setString(6, email);
            return statement;
        }, keyHolder);

        Number accountId = keyHolder.getKey();
        if (accountId == null) {
            throw new IllegalStateException("The database did not return the created account id");
        }

        jdbcTemplate.update(INITIALIZE_REALMS, accountId.longValue());
        return accountId.longValue();
    }
}
