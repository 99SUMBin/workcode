package com.fusen.workcode.config;

import org.springframework.boot.autoconfigure.mongo.MongoProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Created by 01367594 on 2017/11/1.
 */
@Configuration
@ConfigurationProperties(prefix = "mongodb")
public class MultipleMongoProperties {
    private MongoProperties interaction = new MongoProperties();

    public MultipleMongoProperties() {
//        System.out.println("primary->"+primary.getUri()+",secondary->"+secondary.getUri());
    }

    public MongoProperties getInteraction() {
        return interaction;
    }

    public void setInteraction(MongoProperties interaction) {
        this.interaction = interaction;
    }
}
