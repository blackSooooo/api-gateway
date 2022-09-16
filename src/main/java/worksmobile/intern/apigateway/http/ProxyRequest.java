package worksmobile.intern.apigateway.http;

import lombok.Builder;
import lombok.Getter;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.util.MultiValueMap;
import reactor.core.publisher.Flux;
import worksmobile.intern.apigateway.dto.HeaderKey;
import worksmobile.intern.apigateway.utils.BeanUtils;

@Getter
@Builder
public class ProxyRequest {
    private HttpHeaders headers;
    private HttpMethod method;
    private String baseURL;
    private String path;
    private Flux<DataBuffer> body;
    private MultiValueMap<String, String> query;

    public boolean isBodyRequired() {
        return this.method == HttpMethod.POST || this.method == HttpMethod.PATCH ||
                this.method == HttpMethod.PUT || this.method == HttpMethod.TRACE;
    }

    public void setHeadersWithService() {
        HeaderKey headerKey = (HeaderKey) BeanUtils.getBean("headerKey");
        headers.add("Authorization", "Bearer " + headerKey.getWorksKey());
    }
}
