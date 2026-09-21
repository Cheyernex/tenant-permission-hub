package com.cmtdevsolutions.iam.resource.config;

import org.flywaydb.core.Flyway;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import javax.sql.DataSource;

@Configuration
@Profile("!docker")
public class FlywayConfig {

    @Value("${spring.flyway.locations:classpath:db/migration}")
    private String[] locations;

    @Bean
    public Flyway flyway(DataSource dataSource) {
        Flyway flyway = Flyway.configure()
                .dataSource(dataSource)
                .locations(locations)
                .baselineOnMigrate(true)
                .validateOnMigrate(true)
                .load();
        flyway.migrate();
        return flyway;
    }
}