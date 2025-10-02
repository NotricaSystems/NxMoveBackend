package com.next.move.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.web.firewall.StrictHttpFirewall;

@Configuration
public class SecurityFirewallConfig {

    @Bean
    public StrictHttpFirewall allowSemicolonFirewall() {
        StrictHttpFirewall firewall = new StrictHttpFirewall();
        firewall.setAllowSemicolon(true); // allow ';' in URL
        return firewall;
    }

    @Bean
    public WebSecurityCustomizer webSecurityCustomizer(StrictHttpFirewall firewall) {
        return web -> web.httpFirewall(firewall);
    }
}
