package worksmobile.intern.apigateway.service;

import org.springframework.stereotype.Service;
import org.springframework.util.MultiValueMap;
import worksmobile.intern.apigateway.dto.ClientRequest;
import worksmobile.intern.apigateway.dto.RoutingData;
import worksmobile.intern.apigateway.error.Error;
import worksmobile.intern.apigateway.error.ErrorResponse;
import worksmobile.intern.apigateway.exception.ResponseException;
import worksmobile.intern.apigateway.utils.TypeChecker;

import java.util.List;
import java.util.Map;

@Service
public class ParameterVerificationServiceImpl implements ParameterVerificationService {

    private final String REQUIRED = "required";
    private final String OPTIONAL = "optional";
    private final String INTEGER = "integer";
    private final String STRING = "string";
    private final String BOOLEAN = "boolean";
    private final String RANGE = "range";
    private final String ALLOWED = "allowed";
    private final String LENGTH = "length";
    private final String TYPE = "type";
    private final String NAME = "name";
    private final String VALIDATE_TYPE = "validateType";

    // 타입 체크 (integer, boolean -> string 이라면 false, string -> integer 이 필요한 경우도 있어서 string 인 경우 항상 valid)
    @Override
    public boolean isValidType(String type, String value) {
        return (type.equals(INTEGER) && TypeChecker.isInteger(value)) ||
                (type.equals(BOOLEAN) && TypeChecker.isBoolean(value)) ||
                type.equals(STRING);
    }

    // validateType = range 일 때, 정수형 범위 안에 들어오는 유효한 값인지 검증
    @Override
    public boolean isValidIntegerValue(Map<String, Object> option, String parameterValue) {
        int value = Integer.parseInt(parameterValue);
        int minVal = (int) option.get("minVal");
        int maxVal = (int) option.get("maxVal");
        if (value < minVal || value > maxVal) {
            return false;
        }
        return true;
    }

    // validateType = allowed 일 때, 파라미터 값이 해당 어레이 안에 존재하는 유효한 값인지 검증
    @Override
    public boolean isValidAllowedValue(Map<String, Object> option, String parameterValue) {
        List<String> allowedLists = (List) option.get(ALLOWED);
        return allowedLists.contains(parameterValue);
    }

    @Override
    public boolean isValidLengthValue(Map<String, Object> option, String parameterValue) {
        int value = parameterValue.length();
        int minLength = (int) option.get("minLength");
        int maxLength = (int) option.get("maxLength");
        if (value < minLength || value > maxLength) {
            return false;
        }
        return true;
    }

    @Override
    public void checkValid(List<String> parameterLists, String type, String validateType, Map<String, Object> value) {
        for (String parameterName : parameterLists) {
            // 해당 파라미터의 타입이 올바르지 않은 경우
            if (!isValidType(type, parameterName)) {
                throw new ResponseException(new ErrorResponse(Error.BAD_REQUEST));
            }
            // 해당 파라미터의 값이 올바르지 않은 경우
            // 1. 정수형 범위 : minVal ~ maxVal
            if (validateType.equals(RANGE) && !isValidIntegerValue(value, parameterName)) {
                throw new ResponseException(new ErrorResponse(Error.INVALID_PARAMETER));
            }
            // 2. 허용된 값 체크 : [READER, WRITER, ...] 일 때, 해당 값만 허용
            if (validateType.equals(ALLOWED) && !isValidAllowedValue(value, parameterName)) {
                throw new ResponseException(new ErrorResponse(Error.INVALID_PARAMETER));
            }
            // 3. string 길이 : minLength ~ maxLength
            if (validateType.equals(LENGTH) && !isValidLengthValue(value, parameterName)) {
                throw new ResponseException(new ErrorResponse(Error.INVALID_PARAMETER));
            }
        }
    }

    @Override
    public void checkValid(String parameter, String type, String validateType, Map<String, Object> value) {
        if (!isValidType(type, parameter)) {
            throw new ResponseException(new ErrorResponse(Error.BAD_REQUEST));
        }
        if (validateType.equals(RANGE) && !isValidIntegerValue(value, parameter)) {
            throw new ResponseException(new ErrorResponse(Error.INVALID_PARAMETER));
        }
        if (validateType.equals(ALLOWED) && !isValidAllowedValue(value, parameter)) {
            throw new ResponseException(new ErrorResponse(Error.INVALID_PARAMETER));
        }
        if (validateType.equals(LENGTH) && !isValidLengthValue(value, parameter)) {
            throw new ResponseException(new ErrorResponse(Error.INVALID_PARAMETER));
        }
    }


    @Override
    public void checkPathSatisfied(List<Map<String, Object>> required, Map<String, String> parameter) {
        for (Map<String, Object> value : required) {
            String type = (String) value.get(TYPE);
            String name = (String) value.get(NAME);
            if (!isValidType(type, parameter.get(name))) {
                throw new ResponseException(new ErrorResponse(Error.BAD_REQUEST));
            }
        }
    }
    @Override
    public void checkRequiredSatisfied(List<Map<String, Object>> required, MultiValueMap<String, String> parameter) {
        for (Map<String, Object> value : required) {
            String type = (String) value.get(TYPE);
            String name = (String) value.get(NAME);
            String validateType = (String) value.get(VALIDATE_TYPE);
            // 필수 지정 파라미터가 존재하는데 클라이언트에서 넘긴 파라미터가 하나도 없거나, 필수 지정 파라미터가 존재하지 않는 경우
            if (parameter.isEmpty() || parameter.get(name) == null || parameter.get(name).size() == 0) {
                throw new ResponseException(new ErrorResponse(Error.MISSING_PARAMETER));
            }
            checkValid(parameter.get(name), type, validateType, value);
        }
    }

    @Override
    public void checkRequiredSatisfied(List<Map<String, Object>> required, Map<String, Object> parameter) {
        for (Map<String, Object> value : required) {
            String type = (String) value.get(TYPE);
            String name = (String) value.get(NAME);
            String validateType = (String) value.get(VALIDATE_TYPE);
            // 필수 지정 파라미터가 존재하지 않는 경우
            if (parameter.get(name) == null) {
                throw new ResponseException(new ErrorResponse(Error.MISSING_PARAMETER));
            }
            if (type.equals(INTEGER) || type.equals(STRING) || type.equals(BOOLEAN)) {
                checkValid((String) parameter.get(name), type, validateType, value);
            }
        }
    }

    @Override
    public void checkOptionalSatisfied(List<Map<String, Object>> optional, MultiValueMap<String, String> parameter) {
        for (Map<String, Object> value : optional) {
            String type = (String) value.get(TYPE);
            String name = (String) value.get(NAME);
            String validateType = (String) value.get(VALIDATE_TYPE);
            if (parameter.get(name) != null) {
                checkValid(parameter.get(name), type, validateType, value);
            }
        }
    }

    @Override
    public void checkOptionalSatisfied(List<Map<String, Object>> optional, Map<String, Object> parameter) {
        for (Map<String, Object> value : optional) {
            String type = (String) value.get(TYPE);
            String name = (String) value.get(NAME);
            String validateType = (String) value.get(VALIDATE_TYPE);
            if (parameter.get(name) != null) {
                checkValid((String) parameter.get(name), type, validateType, value);
            }
        }
    }

    @Override
    public void checkParameters(List<Map<String, Object>> backendPathVariables, Map<String, String> clientPathVariables) {
        // path 파라미터는 선택 옵션 x 무조건 필수
        if (backendPathVariables.size() != 0) {
            checkPathSatisfied(backendPathVariables, clientPathVariables);
        }
    }
    @Override
    public void checkParameters(Map<String, List<Map<String, Object>>> backendQueryParameters, MultiValueMap<String, String> clientQueryParameters) {
        List<Map<String, Object>> requiredParameters = backendQueryParameters.get(REQUIRED);
        if (requiredParameters != null) {
            checkRequiredSatisfied(requiredParameters, clientQueryParameters);
        }

        List<Map<String, Object>> optionalParameters = backendQueryParameters.get(OPTIONAL);
        if (optionalParameters != null) {
            checkOptionalSatisfied(optionalParameters, clientQueryParameters);
        }
    }

    @Override
    public void checkParameters(Map<String, List<Map<String, Object>>> backendBodyParameters, Map<String, Object> clientBodyParameters) {
        List<Map<String, Object>> requiredParameters = backendBodyParameters.get(REQUIRED);
        if (requiredParameters != null) {
            checkRequiredSatisfied(requiredParameters, clientBodyParameters);
        }
        List<Map<String, Object>> optionalParameters = backendBodyParameters.get(OPTIONAL);
        if (optionalParameters != null) {
            checkOptionalSatisfied(optionalParameters, clientBodyParameters);
        }
    }

    @Override
    public void checkVerification(ClientRequest clientRequest, RoutingData routingData) {
        // query 파라미터 검증
        if (routingData.getQuery() != null) {
            checkParameters(routingData.getQuery(), clientRequest.getQuery());
        }
        // path variable 검증
        if (routingData.getPathVariable() != null && clientRequest.getPathVariables().size() != 0) {
            checkParameters(routingData.getPathVariable(), clientRequest.getPathVariables());
        }
        // body 파라미터 검증
        if (routingData.getBody() != null) {
            checkParameters(routingData.getBody(), clientRequest.getConvertedBody());
        }
    }
}
