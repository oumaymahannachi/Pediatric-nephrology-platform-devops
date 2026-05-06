package tn.pedialink.dossiermedical.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.config.EnableMongoAuditing;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

@Configuration
@EnableMongoRepositories(basePackages = "tn.pedialink.dossiermedical.repository")
@EnableMongoAuditing
public class MongoConfig {
}
