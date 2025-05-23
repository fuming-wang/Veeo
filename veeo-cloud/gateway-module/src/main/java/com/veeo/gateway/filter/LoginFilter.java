package com.veeo.gateway.filter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.veeo.common.util.JwtUtils;
import com.veeo.common.util.R;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import org.springframework.http.HttpHeaders;
import java.util.function.Consumer;

@Slf4j
@Component
public class LoginFilter implements GlobalFilter, Ordered {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        ServerHttpResponse response = exchange.getResponse();
        String path = request.getURI().getPath();
        // 拦截路径

        if(shouldSkipTokenCheck(path)){
            log.info("路径是: {} 不拦截", path);
            return chain.filter(exchange);
        }
        log.info("路径是: {} 需要进行token校验", path);
        String token = request.getHeaders().getFirst("token");
        log.info("token: {}", token);
        // 不能进行转换
        if (!JwtUtils.checkToken(token)) {
            try {
                log.info("用户未登录");
                return buildErrorResponse(response, "请登录后再操作");
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }
        }
        Long userId = JwtUtils.getUserId(token);
        log.info("userId: {}", userId);
        Consumer<HttpHeaders> headers = httpHeaders -> {
            httpHeaders.add("User-Id", String.valueOf(userId));
        };
        request.mutate().headers(headers).build();
        return chain.filter(exchange);
    }

    private Mono<Void> buildErrorResponse(ServerHttpResponse response, String message) throws JsonProcessingException {
        // 设置响应状态码
        response.setStatusCode(HttpStatus.FORBIDDEN); // 可以根据需要设置为 401 Unauthorized

        // 设置跨域相关头部
        response.getHeaders().add("Access-Control-Allow-Origin", "*");
        response.getHeaders().add("Cache-Control", "no-cache");

        // 设置响应内容类型
        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);

        // 设置响应体内容
        R errorResponse = R.error().message(message);
        return response.writeWith(Mono.just(response.bufferFactory().wrap(objectMapper.writeValueAsString(errorResponse).getBytes())));
    }

    private boolean shouldSkipTokenCheck(String path) {
        return path.startsWith("/veeo/login")
                || path.startsWith("/veeo/index")
                || path.startsWith("/veeo/cdn")
                || path.startsWith("/veeo/file");
    }


    @Override
    public int getOrder() {
        return 0;
    }
}
