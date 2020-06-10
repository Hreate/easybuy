package com.easybuy.system.condition;


import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;

public class BlacklistCondition implements Condition {
    @Value("${spring.cloud.gateway.interceptor.black-list-ipv4}")
    private String IPv4Str;

    @Value("${spring.cloud.gateway.interceptor.black-list-ipv6}")
    private String IPv6Str;
    @Override
    public boolean matches(ConditionContext conditionContext, AnnotatedTypeMetadata annotatedTypeMetadata) {
        boolean flag = false;
        if (IPv4Str != null || IPv6Str != null) {
            flag = true;
        }
        return true;
    }
}
