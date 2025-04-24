package com.course.gateway.filter;

import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.core.Ordered;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import javax.annotation.Resource;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class LoginAdminGatewayFilter implements GatewayFilter, Ordered {

    private static final Logger LOG = LoggerFactory.getLogger(LoginAdminGatewayFilter.class);

    @Resource
    private RedisTemplate redisTemplate;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String path = exchange.getRequest().getURI().getPath();
        // 请求地址中不包含/admin/的，不是控台请求，不需要拦截
//        if (!path.contains("/admin/")) {
//            return chain.filter(exchange);
//        }
        if (path.contains("/system/admin/user/login")
                || path.contains("/system/admin/user/logout")
                || path.contains("/system/admin/kaptcha")
                || path.contains("/web/member/signIn")) {
            LOG.info("不需要控台登录验证：{}", path);
            return chain.filter(exchange);
        }
        //获取header的token参数
        String token = exchange.getRequest().getHeaders().getFirst("token");
        LOG.info("控台登录验证开始，token：{}", token);
        if (token == null || token.isEmpty()) {
            LOG.info( "token为空，请求被拦截" );
            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
            return exchange.getResponse().setComplete();
        }
        Object object = redisTemplate.opsForValue().get(token);
        if (object == null) {
            LOG.warn( "token无效，请求被拦截" );
            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
            return exchange.getResponse().setComplete();
        }
        LOG.info("已登录：{}", object);
        // 增加权限校验，gateway里没有LoginUserDto，所以全部用JSON操作
        LOG.info("接口权限校验，请求地址：{}", path);
        boolean exist = false;
        JSONObject loginUserDto = JSONUtil.parseObj(String.valueOf(object));
        if(path.contains("/admin/")){
            // 管理系统拦截
            JSONArray requests = loginUserDto.getJSONArray("requests");
            // 遍历所有【权限请求】，判断当前请求的地址是否在【权限请求】里
            for (int i = 0, l = requests.size(); i < l; i++) {
                String request = (String) requests.get(i);
                if (path.contains(request)) {
                    exist = true;
                    break;
                }
            }
        } else if (path.contains("/web/")) {
            // 前台系统拦截
            String mobile = loginUserDto.getStr("mobile");
            Integer role = loginUserDto.getInt("role");
            // 超级管理员不限制
            if (!StringUtils.isEmpty(mobile) && role == 2) exist = true;
            else {
                String deviceId = loginUserDto.getStr("deviceId");
                // h5判断
                if (!StringUtils.isEmpty(mobile) && deviceId == null) exist = true;
                else {
                    String memberId = loginUserDto.getStr("id");
                    Object o = redisTemplate.opsForValue().get("member-device-id:" + memberId);
                    JSONArray deviceInfoList = new JSONArray();
                    if (o != null) deviceInfoList = JSONUtil.parseArray(o.toString());
                    // 安卓和ios设备判断
                    List<String> deviceIdList = deviceInfoList.stream().map(deviceInfo
                                    -> JSONUtil.parseObj(deviceInfo).getStr("deviceId")).collect(Collectors.toList());
                    if (!StringUtils.isEmpty(mobile) && deviceIdList.contains(deviceId)) exist = true;
                }
            }
        }
        if (exist) {
            LOG.info("权限校验通过");
        } else {
            LOG.warn("权限校验未通过");
            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
            return exchange.getResponse().setComplete();
        }
        return chain.filter(exchange);
    }

    @Override
    public int getOrder()
    {
        return 1;
    }
}