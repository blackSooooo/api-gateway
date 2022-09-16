package worksmobile.intern.apigateway.service;

import org.springframework.util.MultiValueMap;
import worksmobile.intern.apigateway.dto.ClientRequest;
import worksmobile.intern.apigateway.dto.RoutingData;

import java.util.List;
import java.util.Map;

public interface ParameterVerificationService {
    public boolean isValidType(String type, String value);
    public void checkValid(List<String> parameterLists, String type, String validateType, Map<String, Object> value);
    public void checkValid(String parameter, String type, String validateType, Map<String, Object> value);
    public boolean isValidIntegerValue(Map<String, Object> option, String parameterValue);
    public boolean isValidAllowedValue(Map<String, Object> option, String parameterValue);
    public boolean isValidLengthValue(Map<String, Object> option, String parameterValue);
    public void checkPathSatisfied(List<Map<String, Object>> required, Map<String, String> parameter);
    public void checkRequiredSatisfied(List<Map<String, Object>> required, MultiValueMap<String, String> parameter);
    public void checkRequiredSatisfied(List<Map<String, Object>> required, Map<String, Object> parameter);
    public void checkOptionalSatisfied(List<Map<String, Object>> optional, MultiValueMap<String, String> parameter);
    public void checkOptionalSatisfied(List<Map<String, Object>> optional, Map<String, Object> parameter);
    public void checkParameters(List<Map<String, Object>> backendPathVariables, Map<String, String> clientPathVariables);
    public void checkParameters(Map<String, List<Map<String, Object>>> backendQueryParameters, MultiValueMap<String, String> clientQueryParameters);
    public void checkParameters(Map<String, List<Map<String, Object>>> backendBodyParameters, Map<String, Object> clientBodyParameters);
    public void checkVerification(ClientRequest  clientRequest, RoutingData routingData);
}
