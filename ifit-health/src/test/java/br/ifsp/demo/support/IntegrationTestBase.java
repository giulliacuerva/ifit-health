package br.ifsp.demo.support;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public abstract class IntegrationTestBase {
    @LocalServerPort
    protected int port;

    @BeforeAll
    static void takeDatabaseSnapshot() {
        DatabaseState.snapshotOnce();
    }

    @AfterEach
    void restoreDatabase() {
        DatabaseState.restore();
    }
}
