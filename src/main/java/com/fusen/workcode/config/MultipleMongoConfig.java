package com.fusen.workcode.config;

import com.mongodb.MongoClientURI;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.mongo.MongoProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.mongodb.MongoDbFactory;
import org.springframework.data.mongodb.MongoTransactionManager;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.SimpleMongoDbFactory;

import static org.springframework.data.mongodb.core.aggregation.Aggregation.group;

/**
 * Created by 01367594 on 2017/11/1.
 */
@Configuration
public class MultipleMongoConfig {

    @Autowired
    private MultipleMongoProperties mongoProperties;


    @Bean(name = "interactionMongoDbFactory")
    @Qualifier(value = "interactionMongoDbFactory")
    @Primary
    public MongoDbFactory interactionMongoDbFactory()
    {
        return new SimpleMongoDbFactory(new MongoClientURI(this.mongoProperties.getInteraction().getUri()));
    }

    @Primary
    @Bean(name = "interactionMongoTemplate")
    public MongoTemplate primaryMongoTemplate() throws Exception {
//        MappingMongoConverter converter =
//                new MappingMongoConverter(interactionFactory(this.mongoProperties.getInteraction()), new MongoMappingContext());
//        converter.setTypeMapper(new DefaultMongoTypeMapper(null));
//        return new MongoTemplate(interactionFactory(this.mongoProperties.getInteraction()),converter);
        group("evaluate", "publishDate");
        return new MongoTemplate(interactionMongoDbFactory());
    }

    @Qualifier("mongoDbFactory")
    public MongoDbFactory interactionFactory(MongoProperties mongo) throws Exception {
//        return new SimpleMongoDbFactory(new MongoClientURI(primaryUrl));
        SimpleMongoDbFactory dbFactory = new SimpleMongoDbFactory(new MongoClientURI(mongo.getUri()));
        return dbFactory;
    }

    @Bean(name = "mongodbTransactionManager")
    public MongoTransactionManager transactionManager() {

        return new MongoTransactionManager( interactionMongoDbFactory());
    }


}
