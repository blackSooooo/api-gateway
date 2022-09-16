package worksmobile.intern.apigateway.repository;

import lombok.Getter;
import lombok.ToString;
import org.springframework.context.annotation.Configuration;
import worksmobile.intern.apigateway.dto.RoutingData;

import java.util.HashMap;
import java.util.Map;

@Configuration
@Getter
@ToString
public class RoutingDataSet {

    private final Map<DoubleKeyMap, RoutingData> routingDataLists = new HashMap<>();

    public void add(DoubleKeyMap key, RoutingData value) {
        routingDataLists.put(key, value);
    }

    public void delete(DoubleKeyMap key) {
        routingDataLists.remove(key);
    }
    public RoutingData get(String path, String method) {
        DoubleKeyMap<String, String> key = new DoubleKeyMap<>(path, method);
        return routingDataLists.get(key);
    }

}