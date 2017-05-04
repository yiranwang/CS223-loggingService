package com.yyy.tippers.logging.utils;

/**
 * Created by yiranwang on 5/3/17.
 */

import java.io.IOException;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicInteger;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.gemfire.CacheFactoryBean;
import org.springframework.data.gemfire.LocalRegionFactoryBean;
import org.springframework.data.gemfire.repository.config.EnableGemfireRepositories;

import com.gemstone.gemfire.cache.GemFireCache;


// this class is used to start the Geode and create a region named "hello" to store the Transaction

@Configuration
@EnableGemfireRepositories
public class GeodeApplication implements CommandLineRunner{

    @Bean
    Properties gemfireProperties() {
        Properties gemfireProperties = new Properties();
        gemfireProperties.setProperty("name", "DataGemFireApplication");
        gemfireProperties.setProperty("mcast-port", "0");
        gemfireProperties.setProperty("log-level", "config");
        return gemfireProperties;
    }

    @Bean
    CacheFactoryBean gemfireCache() {
        CacheFactoryBean gemfireCache = new CacheFactoryBean();
        gemfireCache.setClose(true);
        gemfireCache.setProperties(gemfireProperties());
        return gemfireCache;
    }

    @Bean
    LocalRegionFactoryBean<AtomicInteger, TransactionLog> helloRegion(final GemFireCache cache) {
        LocalRegionFactoryBean<AtomicInteger, TransactionLog> helloRegion = new LocalRegionFactoryBean<AtomicInteger, TransactionLog>();
        helloRegion.setCache(cache);
        helloRegion.setClose(false);
        helloRegion.setName("hello");
        helloRegion.setPersistent(false);
        return helloRegion;
    }

    @Autowired
    TransactionLogRepository TransactionRepository;


    public void run(String... strings) throws Exception {
        List<TransactionLog> list = TransactionRepository.findAll();
        System.out.println("start to a new region, and the current region size is:" + list.size());

    }
}
