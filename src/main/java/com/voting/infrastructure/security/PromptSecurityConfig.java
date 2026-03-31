/* --------------------------------------------
 * (c) All rights reserved.
 */
package com.voting.infrastructure.security;

import com.voting.domain.port.PromptSecurityPort;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Configuration
@Slf4j
public class PromptSecurityConfig {
    @Bean
    @Primary
    public PromptSecurityPort promptSecurityPort(UPSSSecurityAdapter adapter) {
        log.info("Initializing UPSS Security Framework");
        log.info("Security adapter registered: {}", adapter.getClass().getSimpleName());
        return adapter;
    }
}
