package edu.wpi.grip.web.swagger.api;


import io.swagger.annotations.Info;
import io.swagger.annotations.SwaggerDefinition;

@SwaggerDefinition(
    info = @Info(
        description = "GRIP as a Service",
        version = "0.1.0",
        title = "GRIP Rest API",
        termsOfService = "http://localhost:8081/terms.html"
    )
)
public interface APIConfig {
}
