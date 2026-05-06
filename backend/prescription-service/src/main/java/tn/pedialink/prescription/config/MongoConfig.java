package tn.pedialink.prescription.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

@Configuration
@EnableMongoRepositories(basePackages = "tn.pedialink.prescription.repository")
public class MongoConfig {
}