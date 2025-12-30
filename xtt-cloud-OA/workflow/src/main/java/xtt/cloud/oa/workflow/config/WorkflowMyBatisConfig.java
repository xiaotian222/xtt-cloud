package xtt.cloud.oa.workflow.config;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;

/**
 * Workflow MyBatis 配置
 * 
 * <p>注意：此配置依赖于应用提供的数据源。
 * 如果应用使用 MyBatis Plus，会自动使用应用的 SqlSessionFactory。
 * 如果应用需要自定义 Mapper 扫描，可以在应用启动类中配置 @MapperScan。
 * 
 * @author XTT Cloud
 */
@Configuration
@ConditionalOnBean(DataSource.class)
@MapperScan(basePackages = "xtt.cloud.oa.workflow.infrastructure.persistence.mapper")
public class WorkflowMyBatisConfig {
    // MyBatis Mapper 扫描配置
    // 默认扫描包：xtt.cloud.oa.workflow.infrastructure.persistence.mapper
    // 如果应用已经配置了全局 @MapperScan，此配置会被忽略（Spring 会去重）
}

