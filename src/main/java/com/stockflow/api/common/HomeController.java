package com.stockflow.api.common;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class HomeController {

    @GetMapping("/")
    public Map<String, Object> home() {
        return Map.of(
                "name", "StockFlow API",
                "status", "running",
                "description", "Spring Boot API for inventory, stock movements, orders, low-stock reporting, and sales metrics.",
                "links", Map.of(
                        "dashboard", "https://stock-flow-dashboard.vercel.app",
                        "swagger", "https://stockflow-spring-api.onrender.com/swagger-ui.html",
                        "health", "https://stockflow-spring-api.onrender.com/actuator/health",
                        "repository", "https://github.com/alvarolomba/stockflow-api"
                ),
                "demo", Map.of(
                        "email", "demo@alvarolomba.dev",
                        "password", "DemoPassword123!"
                )
        );
    }
}
