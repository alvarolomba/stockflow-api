package com.stockflow.api.config;

import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import javax.sql.DataSource;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.jdbc.autoconfigure.DataSourceProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

@Configuration
@EnableConfigurationProperties(DataSourceProperties.class)
public class DatabaseConfig {

    @Bean
    DataSource dataSource(DataSourceProperties properties, Environment environment) {
        String databaseUrl = environment.getProperty("DATABASE_URL");
        if (databaseUrl != null && !databaseUrl.isBlank()) {
            configureDatabaseUrl(properties, normalizeDatabaseUrl(databaseUrl));
        }
        return properties.initializeDataSourceBuilder().build();
    }

    private String normalizeDatabaseUrl(String databaseUrl) {
        String normalized = databaseUrl.trim();
        if (normalized.startsWith("DATABASE_URL=")) {
            normalized = normalized.substring("DATABASE_URL=".length()).trim();
        }
        if ((normalized.startsWith("\"") && normalized.endsWith("\""))
                || (normalized.startsWith("'") && normalized.endsWith("'"))) {
            normalized = normalized.substring(1, normalized.length() - 1).trim();
        }
        return normalized;
    }

    private void configureDatabaseUrl(DataSourceProperties properties, String databaseUrl) {
        if (databaseUrl.startsWith("jdbc:")) {
            properties.setUrl(disableServerPreparedStatements(databaseUrl));
            properties.setDriverClassName("org.postgresql.Driver");
            return;
        }
        if (databaseUrl.startsWith("postgres://") || databaseUrl.startsWith("postgresql://")) {
            URI uri = URI.create(databaseUrl);
            properties.setUrl(disableServerPreparedStatements(toJdbcPostgresUrl(uri)));
            properties.setDriverClassName("org.postgresql.Driver");
            applyCredentials(properties, uri);
            return;
        }
        properties.setUrl(databaseUrl);
    }

    private String toJdbcPostgresUrl(URI uri) {
        StringBuilder jdbcUrl = new StringBuilder("jdbc:postgresql://")
                .append(uri.getHost());
        if (uri.getPort() != -1) {
            jdbcUrl.append(':').append(uri.getPort());
        }
        jdbcUrl.append(uri.getPath());
        if (uri.getQuery() != null && !uri.getQuery().isBlank()) {
            jdbcUrl.append('?').append(uri.getQuery());
        }
        return jdbcUrl.toString();
    }

    private String disableServerPreparedStatements(String jdbcUrl) {
        if (!jdbcUrl.startsWith("jdbc:postgresql:") || jdbcUrl.contains("prepareThreshold=")) {
            return jdbcUrl;
        }

        String separator = jdbcUrl.contains("?") ? "&" : "?";
        return jdbcUrl + separator + "prepareThreshold=0";
    }

    private void applyCredentials(DataSourceProperties properties, URI uri) {
        String userInfo = uri.getUserInfo();
        if (userInfo == null || userInfo.isBlank()) {
            return;
        }

        String[] parts = userInfo.split(":", 2);
        properties.setUsername(decode(parts[0]));
        if (parts.length > 1) {
            properties.setPassword(decode(parts[1]));
        }
    }

    private String decode(String value) {
        return URLDecoder.decode(value, StandardCharsets.UTF_8);
    }
}
