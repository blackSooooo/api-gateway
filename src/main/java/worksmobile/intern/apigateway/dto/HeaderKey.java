package worksmobile.intern.apigateway.dto;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Getter
@Component
public class HeaderKey {
    @Value("${works.key}")
    private String worksKey;
}
