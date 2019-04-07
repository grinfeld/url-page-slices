package com.mikerusoft.cassandra.pageslices.conf;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "slices")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UrlConfig {
    private String[] urls;
}
