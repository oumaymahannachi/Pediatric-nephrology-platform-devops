package tn.pedialink.labresults.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.mongodb.core.convert.MongoCustomConversions;
import tn.pedialink.labresults.entity.TestType;

import java.util.Arrays;

@Configuration
public class MongoConfig {

    @Bean
    public MongoCustomConversions mongoCustomConversions() {
        return new MongoCustomConversions(Arrays.asList(
            new StringToTestTypeConverter()
        ));
    }

    static class StringToTestTypeConverter implements Converter<String, TestType> {
        @Override
        public TestType convert(String source) {
            if (source == null) return TestType.OTHER;
            try {
                return TestType.valueOf(source.toUpperCase());
            } catch (IllegalArgumentException e) {
                return TestType.OTHER;
            }
        }
    }
}
