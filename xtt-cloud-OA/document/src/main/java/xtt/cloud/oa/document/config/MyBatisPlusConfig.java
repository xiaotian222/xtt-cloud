package xtt.cloud.oa.document.config;

import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
//import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * MyBatis Plus 配置类
 * 
 * @author xtt
 * @since 2023.0.3.3
 */
@Configuration
public class MyBatisPlusConfig {

    /**
     * 配置 MyBatis Plus 拦截器（分页插件）
     */
    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor() {
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();
        // 添加分页插件
//        PaginationInnerInterceptor paginationInterceptor = new PaginationInnerInterceptor(DbType.MYSQL);
//        // 设置单页分页条数限制，默认无限制
//        paginationInterceptor.setMaxLimit(1000L);
//        // 设置溢出总页数后是否进行处理
//        paginationInterceptor.setOverflow(false);
//        interceptor.addInnerInterceptor(paginationInterceptor);
        return interceptor;
    }
}

