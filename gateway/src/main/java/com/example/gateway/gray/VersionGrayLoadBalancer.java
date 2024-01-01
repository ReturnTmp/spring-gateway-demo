package com.example.gateway.gray;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.loadbalancer.reactive.DefaultResponse;
import org.springframework.cloud.client.loadbalancer.reactive.EmptyResponse;
import org.springframework.cloud.client.loadbalancer.reactive.Request;
import org.springframework.cloud.client.loadbalancer.reactive.Response;
import org.springframework.cloud.loadbalancer.core.NoopServiceInstanceListSupplier;
import org.springframework.cloud.loadbalancer.core.ReactorServiceInstanceLoadBalancer;
import org.springframework.cloud.loadbalancer.core.ServiceInstanceListSupplier;
import org.springframework.http.HttpHeaders;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public class VersionGrayLoadBalancer implements ReactorServiceInstanceLoadBalancer {

    private ObjectProvider<ServiceInstanceListSupplier> serviceInstanceListSupplierProvider;
    private String serviceId;
    private final AtomicInteger position;


    public VersionGrayLoadBalancer(ObjectProvider<ServiceInstanceListSupplier> serviceInstanceListSupplierProvider, String serviceId) {
        this(serviceInstanceListSupplierProvider, serviceId, new Random().nextInt(1000));
    }

    public VersionGrayLoadBalancer(ObjectProvider<ServiceInstanceListSupplier> serviceInstanceListSupplierProvider, String serviceId, int seedPosition) {
        this.serviceId = serviceId;
        this.serviceInstanceListSupplierProvider = serviceInstanceListSupplierProvider;
        this.position = new AtomicInteger(seedPosition);
    }

    @Override
    public Mono<Response<ServiceInstance>> choose(Request request) {
        HttpHeaders headers = (HttpHeaders) request.getContext();
        ServiceInstanceListSupplier supplier = this.serviceInstanceListSupplierProvider.getIfAvailable(NoopServiceInstanceListSupplier::new);
        return ((Flux) supplier.get()).next().map(list -> processInstanceResponse((List<ServiceInstance>) list, headers));
    }

    private Response<ServiceInstance> processInstanceResponse(List<ServiceInstance> instances, HttpHeaders headers) {
        if (instances.isEmpty()) {
            return new EmptyResponse();
        } else {
            String reqVersion = headers.getFirst("version");

            if (StringUtils.isEmpty(reqVersion)) {
                return processRibbonInstanceResponse(instances);
            }

            List<ServiceInstance> serviceInstances = instances.stream()
                    .filter(instance -> reqVersion.equals(instance.getMetadata().get("version")))
                    .collect(Collectors.toList());

            if (serviceInstances.size() > 0) {
                return processRibbonInstanceResponse(serviceInstances);
            } else {
                return processRibbonInstanceResponse(instances);
            }
        }
    }

    /**
     * 负载均衡器
     * 参考 org.springframework.cloud.loadbalancer.core.RoundRobinLoadBalancer#getInstanceResponse
     *
     * @author javadaily
     */
    private Response<ServiceInstance> processRibbonInstanceResponse(List<ServiceInstance> instances) {
        int pos = Math.abs(this.position.incrementAndGet());
        ServiceInstance instance = instances.get(pos % instances.size());
        return new DefaultResponse(instance);
    }
}
