package worksmobile.intern.apigateway.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.data.redis.core.script.RedisScript;

import java.util.Collections;
import java.util.List;

@Configuration
public class LuaScriptConfig {
    @Bean
    public RedisScript<List<Long>> script() {
        Resource redisScriptSource = new ClassPathResource("scripts/RedisScript.lua");
        return RedisScript.of(redisScriptSource, (Class<List<Long>>) Collections.<Long>emptyList().getClass());
    }
}
