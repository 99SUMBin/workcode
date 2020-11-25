package com.fusen.workcode.config;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;

@Configuration
public class DatasourceConfiguration {

    @Bean(name = "mysqlprimarydataSource")
    @Qualifier(value = "mysqlprimarydataSource")
    @Primary
    @ConfigurationProperties(prefix = "c3p0")
    public DataSource mysqlprimarydataSource() {
        return DataSourceBuilder.create().type(com.mchange.v2.c3p0.ComboPooledDataSource.class).build();
    }

    @Bean(name = "sqlserverprimaryDataSource")
    @Qualifier("sqlserverprimaryDataSource")
    @ConfigurationProperties(prefix = "primary.c3p0")
    public DataSource sqlserverprimaryDataSource() {
        return DataSourceBuilder.create().type(com.mchange.v2.c3p0.ComboPooledDataSource.class).build();
    }

    @Bean(name = "mysqlprimaryJdbcTemplate")
    public JdbcTemplate mysqlprimaryJdbcTemplate(
            @Qualifier("mysqlprimarydataSource") DataSource dataSource) {
        return new JdbcTemplate(dataSource);
    }

    @Bean(name = "sqlserverprimaryJdbcTemplate")
    public JdbcTemplate sqlserverprimaryJdbcTemplate(
            @Qualifier("sqlserverprimaryDataSource") DataSource dataSource) {
        return new JdbcTemplate(dataSource);
    }

    @Bean(name = "sqlserverTransactionManager")
    public PlatformTransactionManager sqlserverprimaryDataSource(@Qualifier("sqlserverprimaryDataSource") DataSource dataSource) {
        return new DataSourceTransactionManager(dataSource);
    }

    @Bean(name = "mysqlTransactionManager")
    public PlatformTransactionManager mysqlprimarydataSource(@Qualifier("mysqlprimarydataSource") DataSource dataSource) {
        return new DataSourceTransactionManager(dataSource);
    }


}
