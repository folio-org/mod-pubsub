package org.folio.spring.config;

import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.PropertySource;

@Import(ApplicationConfig.class)
@PropertySource("classpath:test-application.properties")
public class TestConfig {
}
