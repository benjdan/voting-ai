/* --------------------------------------------
 * (c) All rights reserved.
 */
package com.voting.infrastructure.security;

import com.voting.domain.port.PromptSecurityPort;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Configuration
public class PromptSecurityConfig {
    private static final Logger LOGGER = LogManager.getLogger(PromptSecurityConfig.class);
    @Bean
    @Primary
    public PromptSecurityPort promptSecurityPort(UPSSSecurityAdapter adapter) {
        LOGGER.info("Initializing UPSS Security Framework");
        LOGGER.info("Security adapter registered: {}", adapter.getClass().getSimpleName());
        return adapter;
    }
}
