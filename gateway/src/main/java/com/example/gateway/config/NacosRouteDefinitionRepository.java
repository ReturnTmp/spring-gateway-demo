package com.example.gateway.config;

import com.alibaba.cloud.nacos.NacosConfigManager;
import com.alibaba.nacos.api.config.listener.Listener;
import com.alibaba.nacos.api.exception.NacosException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.event.RefreshRoutesEvent;
import org.springframework.cloud.gateway.route.RouteDefinition;
import org.springframework.cloud.gateway.route.RouteDefinitionRepository;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ApplicationEventPublisherAware;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.concurrent.Executor;

@Component
public class NacosRouteDefinitionRepository implements RouteDefinitionRepository, ApplicationEventPublisherAware {

    private static final Logger log = LoggerFactory.getLogger(NacosRouteDefinitionRepository.class);

    @Autowired
    private NacosConfigManager nacosConfigManager;

    // 更新路由信息需要的
    private ApplicationEventPublisher applicationEventPublisher;

    private String dataId = "gateway-router.json";

    private String group = "DEFAULT_GROUP";

    @Value("${spring.cloud.nacos.config.server-addr}")
    private String serverAddr;

    private ObjectMapper objectMapper = new ObjectMapper();

    @PostConstruct
    public void dynamicRouteByNacosListener() {
        try {
            nacosConfigManager.getConfigService().addListener(dataId, group, new Listener() {

                public void receiveConfigInfo(String configInfo) {
                    log.info("自动更新配置...\r\n{}", configInfo);
                    applicationEventPublisher.publishEvent(new RefreshRoutesEvent(this));
                }

                public Executor getExecutor() {
                    return null;
                }
            });
        } catch (NacosException e) {
            e.printStackTrace();
        }
    }

    @Override
    public Flux<RouteDefinition> getRouteDefinitions() {
        try {
            String configInfo = nacosConfigManager.getConfigService().getConfig(dataId, group, 5000);
            List<RouteDefinition> gatewayRouteDefinitions = objectMapper.readValue(configInfo, new TypeReference<List<RouteDefinition>>() {
            });
            return Flux.fromIterable(gatewayRouteDefinitions);
        } catch (NacosException e) {
            e.printStackTrace();
        } catch (JsonMappingException e) {
            e.printStackTrace();
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        return Flux.fromIterable(Lists.newArrayList());
    }

    @Override
    public Mono<Void> save(Mono<RouteDefinition> route) {
        return null;
    }

    @Override
    public Mono<Void> delete(Mono<String> routeId) {
        return null;
    }

    @Override
    public void setApplicationEventPublisher(ApplicationEventPublisher applicationEventPublisher) {
        this.applicationEventPublisher = applicationEventPublisher;
    }
}
