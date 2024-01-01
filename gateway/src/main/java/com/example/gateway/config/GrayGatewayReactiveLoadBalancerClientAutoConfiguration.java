package com.example.gateway.config;

import com.example.gateway.gray.GrayReactiveLoadBalancerClientFilter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.cloud.gateway.config.LoadBalancerProperties;
import org.springframework.cloud.loadbalancer.support.LoadBalancerClientFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GrayGatewayReactiveLoadBalancerClientAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean({GrayReactiveLoadBalancerClientFilter.class})
    public GrayReactiveLoadBalancerClientFilter grayReactiveLoadBalancerClientFilter(LoadBalancerClientFactory clientFactory, LoadBalancerProperties properties) {
        return new GrayReactiveLoadBalancerClientFilter(clientFactory, properties);
    }
}
