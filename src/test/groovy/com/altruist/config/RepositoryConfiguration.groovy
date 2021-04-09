package com.altruist.config


import groovy.sql.Sql
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile

import javax.sql.DataSource
import java.sql.SQLException

@Configuration
class RepositoryConfiguration {

    @Profile("test")
    @Bean(destroyMethod = "close")
    Sql sql(DataSource dataSource) throws SQLException {
        return new Sql(dataSource)
    }
}
