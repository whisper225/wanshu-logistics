package com.wanshu.web.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;

/**
 * Neo4j 会注册 {@code reactiveTransactionManager}，与 JDBC 的 {@code transactionManager} 并存时，
 * {@code @Transactional} 无法唯一选择。将数据源事务管理器标为 {@link Primary}，保证 MyBatis 等业务走 JDBC 事务。
 */
@Configuration
public class TransactionManagerConfig {

    @Bean
    @Primary
    public PlatformTransactionManager transactionManager(DataSource dataSource) {
        return new DataSourceTransactionManager(dataSource);
    }
}
