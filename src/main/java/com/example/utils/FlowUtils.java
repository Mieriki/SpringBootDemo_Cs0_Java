package com.example.utils;

import jakarta.annotation.Resource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.concurrent.TimeUnit;

@Component
public class FlowUtils {
    @Resource
    StringRedisTemplate template;

    @Value("${spring.flow.mark-time}")
    int markTime;

    @Value("${spring.flow.max-number}")
    int maxNumber;

    @Value("${spring.flow.prohibition-time}")
    int prohibitionTime;

    public boolean limitOnceCheck(String key, int blockTime) {
        if (Boolean.TRUE.equals(template.hasKey(key))) {
            return false;
        } else {
            template.opsForValue().set(key, "", blockTime, TimeUnit.SECONDS);
            return true;
        }
    }

    public boolean limitPeriodChech(String address) {
        if (Boolean.TRUE.equals(template.hasKey(Const.FLOW_LIMIT_COUNTER + address))) {
            long increment = Optional.ofNullable(template.opsForValue().increment(Const.FLOW_LIMIT_COUNTER)).orElse(0L);
            if (increment > maxNumber) {
                template.opsForValue().set(Const.FLOW_LIMIT_BLOCK + address, "", prohibitionTime, TimeUnit.SECONDS);
                return false;
            }
        } else {
            template.opsForValue().set(Const.FLOW_LIMIT_COUNTER + address, "1", markTime, TimeUnit.SECONDS);
        }
        return true;
    }
}
