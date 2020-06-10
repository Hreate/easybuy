package com.easybuy.system.validator;

import com.easybuy.system.util.IPv6PrefixConvert;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple2;
import reactor.util.function.Tuples;

import java.util.ArrayList;

public class IPv6Validator {
    public static Mono<Boolean> validateIPv6(String IPv6Str, String hostName) {
        //将请求IP切割，转换成整型数组
        var list = new ArrayList<Integer>();
        for (var i : hostName.split(":")) {
            list.add(Integer.parseInt(i, 16));
        }
        var hostnameArr = list.toArray(new Integer[0]);
        //将配置的黑名单IP网段过滤出来
        Flux<String> ipSegment = Flux.fromArray(IPv6Str.split(","))
                .map(ip -> ip.replaceAll("\\s*", ""))
                .filter(e -> e.contains("/"));
        Mono<Boolean> booleanMono1 = IPv6Validator.validateIP(hostnameArr, ipSegment);
        Mono<Boolean> booleanMono2 = Flux.fromArray(IPv6Str.split(","))
                .map(ip -> ip.replaceAll("\\s*", ""))
                .filter(e -> !e.contains("/"))
                .collectList()
                .map(e -> {
                    var flag = false;
                    for (var ip : e) {
                        if (ip.equals(hostName)) {
                            flag = true;
                        }
                    }
                    return flag;
                });
        return Mono.zip(booleanMono1, booleanMono2)
                .flatMap(e -> {
                    if (e.getT1() || e.getT2()) {
                        return Mono.just(true);
                    } else {
                        return Mono.just(false);
                    }
                });
    }

    public static Mono<Boolean> validateIP(Integer[] sourceArr, Flux<String> ipSegment) {
        var list = new ArrayList<Integer>();
        return ipSegment.map(e -> {
            var arr = e.split("/");
            var list1 = new ArrayList<Tuple2<Integer, Integer>>();
            var prefix = IPv6PrefixConvert.convertPrefix(arr[1]);
            var segment= arr[0].split(":");
            for (var i = 0; i < prefix.length; i++) {
                Tuple2<Integer, Integer> tuple2 = Tuples.of(Integer.parseInt(segment[i]), prefix[i]);
                list1.add(tuple2);
            }
            return list1;
        })
                .map(e -> {
                    list.clear();
                    for (var i = 0; i < e.size(); i++) {
                        list.add(sourceArr[i] & e.get(i).getT2());
                    }
                    var sourceSeg = list.toString();
                    list.clear();
                    for (var i : e) {
                        list.add(i.getT1() & i.getT2());
                    }
                    var segment = list.toString();
                    return sourceSeg.equals(segment);
                })
                .filter(e -> e)
                .collectList()
                .flatMap(e -> {
                    if (e != null && e.size() > 0) {
                        return Mono.just(true);
                    } else {
                        return Mono.just(false);
                    }
                });
    }
}
