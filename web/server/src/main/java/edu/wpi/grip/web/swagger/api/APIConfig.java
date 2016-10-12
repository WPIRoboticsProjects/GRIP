package edu.wpi.grip.web.swagger.api;


import io.swagger.annotations.Info;
import io.swagger.annotations.SwaggerDefinition;

/**
 * Used to create the header to the <code>swagger.yaml/json</code> file when requested.
 */
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
