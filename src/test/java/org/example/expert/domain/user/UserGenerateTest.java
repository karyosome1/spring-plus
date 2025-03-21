package org.example.expert.domain.user;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import javax.sql.DataSource;
import java.sql.*;
import java.util.UUID;

@SpringBootTest(properties = "spring.profiles.active=test")
public class UserGenerateTest {

    @Autowired
    private DataSource dataSource;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private static final int BATCH_SIZE = 100000;
    private static final int TOTAL_USERS = 1000000;

    @Test
    public void generate() {
        try (Connection conn = dataSource.getConnection()) {
            conn.setAutoCommit(false);
            String sql = "INSERT INTO users (email, password, nickname, user_role) VALUES (?, ?, ?, ?)";

            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                String encodedPassword = passwordEncoder.encode("password123!");

                for (int i = 1; i <= TOTAL_USERS; i++) {
                    String email = UUID.randomUUID() + "@example.com";
                    String nickname = UUID.randomUUID().toString().substring(0, 8);
                    String role = (i % 2 == 0) ? "ROLE_USER" : "ROLE_ADMIN";

                    ps.setString(1, email);
                    ps.setString(2, encodedPassword);
                    ps.setString(3, nickname);
                    ps.setString(4, role);
                    ps.addBatch();

                    if (i % BATCH_SIZE == 0) {
                        ps.executeBatch();
                        conn.commit();
                        System.out.println(i + " 명 추가되었습니다");
                    }
                }
                ps.executeBatch();
                conn.commit();
            }
            System.out.println("모든 데이터가 성공적으로 추가되었습니다");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
