package worksmobile.intern.apigateway.dto;

import lombok.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class DeletedInformation {
    private String path;
    private String domain;
    private String method;
}
