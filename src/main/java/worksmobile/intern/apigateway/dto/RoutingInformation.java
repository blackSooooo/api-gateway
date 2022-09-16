package worksmobile.intern.apigateway.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.List;
import java.util.Map;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class RoutingInformation {
    private String id;
    private String path;
    private String method;
    private String domain;
    private int rateLimit;
    private String baseUrl;
    private Map<String, List<Map<String, Object>>> query;
    private Map<String, List<Map<String, Object>>> body;
    private List<Map<String, Object>> pathVariable;
}
