package com.easybuy.system.condition;


import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;

public class BlacklistCondition implements Condition {
    @Override
    public boolean matches(ConditionContext conditionContext, AnnotatedTypeMetadata annotatedTypeMetadata) {
        boolean IPv4 = conditionContext.getEnvironment().containsProperty("spring.cloud.gateway.interceptor.black-list-ipv4");
        boolean IPv6 = conditionContext.getEnvironment().containsProperty("spring.cloud.gateway.interceptor.black-list-ipv6");
        if (IPv4 || IPv6) {
            return true;
        } else {
            return false;
        }
    }
}
