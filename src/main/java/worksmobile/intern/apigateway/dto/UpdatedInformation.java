package worksmobile.intern.apigateway.dto;

import lombok.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class UpdatedInformation {
    RoutingInformation prevRoutingInformation;
    RoutingInformation updatedRoutingInformation;
}
