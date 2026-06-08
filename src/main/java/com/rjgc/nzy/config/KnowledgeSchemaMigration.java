package com.rjgc.nzy.config;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

@Component
@RequiredArgsConstructor
public class KnowledgeSchemaMigration implements ApplicationRunner {

    private final DataSource dataSource;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        try (Connection connection = dataSource.getConnection();
             Statement statement = connection.createStatement()) {
            if (!hasColumn(statement, "knowledge_document", "knowledge_category")) {
                statement.execute("""
                        ALTER TABLE knowledge_document
                        ADD COLUMN knowledge_category VARCHAR(64) NOT NULL DEFAULT '通用知识'
                        """);
            }
        }
    }

    private boolean hasColumn(Statement statement, String tableName, String columnName) throws Exception {
        try (ResultSet resultSet = statement.executeQuery(
                "SHOW COLUMNS FROM " + tableName + " LIKE '" + columnName + "'")) {
            return resultSet.next();
        }
    }
}
