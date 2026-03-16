package com.example.hrm.config;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CloudinaryConfig {
    @Bean
    public Cloudinary cloudinary() {
        return new Cloudinary(ObjectUtils.asMap(
                "cloud_name", "dute4ytrb",
                "api_key", "296253783464895",
                "api_secret", "ciO1FKJe4IjdryUfdS7_wy-ZEN4"
        ));
    }
}