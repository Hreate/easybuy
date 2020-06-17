package com.easybuy.system.filter;

import com.easybuy.system.condition.BlacklistCondition;
import com.easybuy.system.validator.IPv4Validator;
import com.easybuy.system.validator.IPv6Validator;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.context.annotation.Conditional;
import org.springframework.core.Ordered;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Objects;
import java.util.stream.Stream;

//@Component
@Conditional(BlacklistCondition.class)
public class BlacklistFilter implements GlobalFilter, Ordered {
    @Value("${spring.cloud.gateway.interceptor.black-list-ipv4}")
    private String IPv4Str;

    @Value("${spring.cloud.gateway.interceptor.black-list-ipv6}")
    private String IPv6Str;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        //获取请求IP
        String ipv4 = null;
        ServerHttpRequest request = exchange.getRequest();
//        System.out.println(request.getHeaders());
        //X-Forwarded-For：Squid 服务代理
        var ipAddresses = request.getHeaders().get("X-Forwarded-For");
        if (ipAddresses == null || ipAddresses.size() == 0) {
            //Proxy-Client-IP：apache 服务代理
            ipAddresses = request.getHeaders().get("Proxy-Client-IP");
        }
        if (ipAddresses == null || ipAddresses.size() == 0) {
            //WL-Proxy-Client-IP：weblogic 服务代理
            ipAddresses = request.getHeaders().get("WL-Proxy-Client-IP");
        }
        if (ipAddresses == null || ipAddresses.size() == 0) {
            //HTTP_CLIENT_IP：有些代理服务器
            ipAddresses = request.getHeaders().get("HTTP_CLIENT_IP");
        }
        if (ipAddresses == null || ipAddresses.size() == 0) {
            //X-Real-IP：nginx服务代理
            ipAddresses = request.getHeaders().get("X-Real-IP");
        }
        if (ipAddresses == null || ipAddresses.size() == 0) {
            ipAddresses = request.getHeaders().get("Host");
        }

        //有些网络通过多层代理，那么获取到的ip就会有多个，一般都是通过逗号（,）分割开来，并且第一个ip为客户端的真实IP
        if (ipAddresses != null && ipAddresses.size() != 0) {
            ipv4 = ipAddresses.get(0).split(":")[0];
        }
        if ("localhost".equals(ipv4)) {
            ipv4 = "127.0.0.1";
        }
        var IPv6 = Objects.requireNonNull(request.getRemoteAddress()).getHostName();
        if (IPv6.contains("DESKTOP")) {
            IPv6 = "0:0:0:0:0:0:0:1";
        }
        Mono<Boolean> IPv6Result = IPv6Validator.validateIPv6(IPv6Str, IPv6);
        Mono<Boolean> IPv4Result = Mono.just(false);
        if (ipv4 != null && ipv4.length() > 0) {
            IPv4Result= IPv4Validator.validateIPv4(IPv4Str, ipv4);
        }

        return Mono.zip(IPv6Result,IPv4Result)
                .flatMapMany(results -> Flux.just(results.getT1(), results.getT2()))
                .filter(bool -> bool)
                .collectList()
                .flatMap(resultList -> {
                    if (resultList.size() > 0) {
                        return Mono.empty();
                    } else {
                        return chain.filter(exchange);
                    }
                });
    }

    @Override
    public int getOrder() {
        return 0;
    }
}
