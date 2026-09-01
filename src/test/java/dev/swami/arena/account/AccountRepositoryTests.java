package dev.swami.arena.account;

import org.h2.jdbcx.JdbcDataSource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;

import static org.assertj.core.api.Assertions.assertThat;

class AccountRepositoryTests {

    private JdbcTemplate jdbcTemplate;
    private AccountRepository repository;

    @BeforeEach
    void setUp() {
        JdbcDataSource dataSource = new JdbcDataSource();
        dataSource.setURL("jdbc:h2:mem:repository-test;MODE=MySQL;DB_CLOSE_DELAY=-1");
        jdbcTemplate = new JdbcTemplate(dataSource);

        jdbcTemplate.execute("DROP ALL OBJECTS");
        jdbcTemplate.execute("""
                CREATE TABLE account (
                    id BIGINT AUTO_INCREMENT PRIMARY KEY,
                    username VARCHAR(32) NOT NULL UNIQUE,
                    salt BINARY(32) NOT NULL,
                    verifier BINARY(32) NOT NULL,
                    expansion TINYINT NOT NULL,
                    reg_mail VARCHAR(255) NOT NULL,
                    email VARCHAR(255) NOT NULL,
                    joindate TIMESTAMP NOT NULL
                )
                """);
        jdbcTemplate.execute("CREATE TABLE realmlist (id INT PRIMARY KEY)");
        jdbcTemplate.execute("""
                CREATE TABLE realmcharacters (
                    realmid INT NOT NULL,
                    acctid BIGINT NOT NULL,
                    numchars TINYINT NOT NULL,
                    PRIMARY KEY (realmid, acctid)
                )
                """);
        jdbcTemplate.update("INSERT INTO realmlist (id) VALUES (1), (2)");
        repository = new AccountRepository(jdbcTemplate);
    }

    @Test
    void createsAccountAndInitializesEveryRealm() {
        byte[] salt = new byte[32];
        byte[] verifier = new byte[32];
        salt[0] = 12;
        verifier[31] = 34;

        long accountId = repository.create(
                "SWAMI",
                "SWAMI@EXAMPLE.COM",
                salt,
                verifier,
                2
        );

        assertThat(jdbcTemplate.queryForObject(
                "SELECT username FROM account WHERE id = ?",
                String.class,
                accountId
        )).isEqualTo("SWAMI");
        assertThat(jdbcTemplate.queryForObject(
                "SELECT salt FROM account WHERE id = ?",
                byte[].class,
                accountId
        )).containsExactly(salt);
        assertThat(jdbcTemplate.queryForObject(
                "SELECT verifier FROM account WHERE id = ?",
                byte[].class,
                accountId
        )).containsExactly(verifier);
        assertThat(jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM realmcharacters WHERE acctid = ? AND numchars = 0",
                Integer.class,
                accountId
        )).isEqualTo(2);
    }
}
