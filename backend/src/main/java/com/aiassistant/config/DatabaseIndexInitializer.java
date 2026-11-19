package com.aiassistant.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DatabaseMetaData;

@Component
public class DatabaseIndexInitializer implements ApplicationRunner {

    private static final Logger logger = LoggerFactory.getLogger(DatabaseIndexInitializer.class);
    private final JdbcTemplate jdbcTemplate;

    public DatabaseIndexInitializer(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void run(ApplicationArguments args) {
        try {
            DataSource dataSource = jdbcTemplate.getDataSource();
            if (dataSource == null) return;

            try (Connection conn = dataSource.getConnection()) {
                DatabaseMetaData metaData = conn.getMetaData();
                String dbProductName = metaData.getDatabaseProductName() != null ? metaData.getDatabaseProductName().toLowerCase() : "";

                if (dbProductName.contains("mysql") || dbProductName.contains("mariadb")) {
                    logger.info("Verifying MySQL FULLTEXT index on document_chunks(content)...");
                    try {
                        Integer count = jdbcTemplate.queryForObject(
                                "SELECT COUNT(1) FROM information_schema.statistics " +
                                "WHERE table_schema = DATABASE() AND table_name = 'document_chunks' AND index_name = 'ft_chunk_content'",
                                Integer.class
                        );
                        if (count == null || count == 0) {
                            logger.info("FULLTEXT index missing. Creating 'ft_chunk_content' on document_chunks(content)...");
                            jdbcTemplate.execute("ALTER TABLE document_chunks ADD FULLTEXT INDEX ft_chunk_content (content)");
                            logger.info("Successfully created MySQL FULLTEXT index 'ft_chunk_content'.");
                        } else {
                            logger.info("MySQL FULLTEXT index 'ft_chunk_content' is verified and ready.");
                        }
                    } catch (Exception e) {
                        logger.warn("FULLTEXT index check/creation skipped: {}", e.getMessage());
                    }
                }
            }
        } catch (Exception e) {
            logger.warn("DatabaseIndexInitializer encountered non-critical exception: {}", e.getMessage());
        }
    }
}
