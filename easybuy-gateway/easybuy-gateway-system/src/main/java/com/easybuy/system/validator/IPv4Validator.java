package com.easybuy.system.validator;

import com.easybuy.system.util.SubnetMaskConvert;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple2;
import reactor.util.function.Tuples;

import java.util.ArrayList;

public class IPv4Validator {
    public static Mono<Void> validateIPv4(String IPv4Str, String hostName, ServerWebExchange exchange, GatewayFilterChain chain) {
        //将请求IP切割，转换成整型数组
        var list = new ArrayList<Integer>();
        for (var i : hostName.split("\\.")) {
            list.add(Integer.parseInt(i));
        }
        var hostnameArr = list.toArray(new Integer[0]);
        //将配置的黑名单IP网段过滤出来
        Flux<String> ipSegment = Flux.fromArray(IPv4Str.split(","))
                .filter(e -> e.contains("/"));
        Mono<Boolean> booleanMono1 = IPv4Validator.validateIP(hostnameArr, ipSegment);
        Mono<Boolean> booleanMono2 = Flux.fromArray(IPv4Str.split(","))
                .filter(e -> !e.contains("/"))
                .collectList()
                .flatMap(e -> {
                    var flag = false;
                    for (var ip : e) {
                        if (ip.equals(hostName)) {
                            flag = true;
                            break;
                        }
                    }
                    return Mono.just(flag);
                });
        return Mono.zip(booleanMono1, booleanMono2)
                .flatMap(e -> {
                    if (e.getT1() || e.getT2()) {
                        return Mono.empty();
                    } else {
                        return chain.filter(exchange);
                    }
                });
    }

    /**
     * 验证请求IP是否符合黑名单中的IP网段
     * @param sourceArr 请求IP
     * @param ipSegment
     * @return
     */
    public static Mono<Boolean> validateIP(Integer[] sourceArr, Flux<String> ipSegment) {
        var list = new ArrayList<Integer>();
        //IP网段，如192.168.0.0/16，将IP网络段与标识位分开，并将网络段与标识位封装成Tuple2
        return ipSegment.flatMap(e -> {
            //将IP网络段与标识位分开
            var arr = e.split("/");
            //创建一个List，方便返回flux
            var list1 = new ArrayList<Tuple2<Integer, Integer>>();
            //将标识位传入转换子网掩码的方法，返回子网掩码数组
            var subnetMask = SubnetMaskConvert.toSubnetMask(arr[1]);
            var segment = arr[0].split("\\.");
            //将子网掩码数组的每项与网络段封装成Tuple2，并按顺序装进list
            for (var i = 0; i < subnetMask.length; i++) {
                Tuple2<Integer, Integer> tuple2 = Tuples.of(Integer.parseInt(segment[i]), subnetMask[i]);
                list1.add(tuple2);
            }
            //将list封装成flux返回
            return Mono.just(list1);
        })
                .flatMap(e -> {
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
                    return Mono.just(sourceSeg.equals(segment));
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
