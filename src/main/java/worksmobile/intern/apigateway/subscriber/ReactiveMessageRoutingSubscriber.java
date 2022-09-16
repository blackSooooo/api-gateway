package worksmobile.intern.apigateway.subscriber;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import worksmobile.intern.apigateway.dto.DeletedInformation;
import worksmobile.intern.apigateway.dto.RoutingInformation;
import worksmobile.intern.apigateway.dto.UpdatedInformation;
import worksmobile.intern.apigateway.service.GatewayService;

@Service
@AllArgsConstructor
public class ReactiveMessageRoutingSubscriber {
    private final ObjectMapper objectMapper;
    private final GatewayService gatewayService;
    private static final Logger logger = LoggerFactory.getLogger(ReactiveMessageRoutingSubscriber.class);

    public void registerEvent(String msg) {
        try {
            RoutingInformation routingInformation = objectMapper.readValue(msg, RoutingInformation.class);
            gatewayService.registerRouting(routingInformation);
        } catch (Exception e) {
            logger.info("exception in register subscribe");
        }
    }

    public void updateEvent(String msg) {
        try {
            UpdatedInformation updatedInformation = objectMapper.readValue(msg, UpdatedInformation.class);
            RoutingInformation prevRoutingInformation = updatedInformation.getPrevRoutingInformation();
            RoutingInformation updatedRoutingInformation = updatedInformation.getUpdatedRoutingInformation();
            if (prevRoutingInformation != null) {
                DeletedInformation deletedInformation = DeletedInformation.builder()
                        .path(prevRoutingInformation.getPath())
                        .domain(prevRoutingInformation.getDomain())
                        .method(prevRoutingInformation.getMethod())
                        .build();
                gatewayService.unregisterRouting(deletedInformation);
            }
            gatewayService.registerRouting(updatedRoutingInformation);
        } catch (Exception e) {
            logger.info("exception in update subscribe");
        }
    }

    public void deleteEvent(String msg) {
        try {
            DeletedInformation deletedInformation = objectMapper.readValue(msg, DeletedInformation.class);
            gatewayService.unregisterRouting(deletedInformation);
        } catch (Exception e) {
            logger.info("exception in delete subscribe");
        }
    }

}
