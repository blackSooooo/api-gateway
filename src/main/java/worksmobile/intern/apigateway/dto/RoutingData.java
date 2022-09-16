package worksmobile.intern.apigateway.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

import java.util.List;
import java.util.Map;

@ToString
@Getter
@Builder
public class RoutingData {
    private String id;
    private int rateLimit;
    private String baseUrl;
    private String path;
    private String method;
    private Map<String, List<Map<String, Object>>> query;
    private Map<String, List<Map<String, Object>>> body;
    private List<Map<String, Object>> pathVariable;
}
