package com.example.hrm.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * Swagger/OpenAPI Configuration
 * 
 * Sau khi chạy server, truy cập:
 * - Swagger UI: http://localhost:8080/swagger-ui/index.html
 * - API Docs JSON: http://localhost:8080/v3/api-docs
 */
@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("HRM System API - Evaluation & Training Module")
                        .version("1.0.0")
                        .description("""
                                # HRM System API Documentation
                                
                                ## Module 5: Đánh giá & Đào tạo
                                
                                ### Chức năng chính:
                                1. **Evaluation**: Đánh giá hiệu suất nhân viên theo KPI
                                2. **Performance Ranking**: Xếp hạng và đề xuất thăng chức
                                3. **Training**: Quản lý đào tạo và chứng chỉ
                                4. **Auto Recommendation**: Tự động đề xuất khóa học dựa trên KPI yếu
                                
                                ### Workflow:
                                1. Employee tự đánh giá → Manager đánh giá → Hệ thống phân loại (A/B/C/D)
                                2. Hệ thống tự động đề xuất training nếu Classification C/D
                                3. Manager gán khóa học/mentor cho nhân viên
                                4. Employee hoàn thành → Upload chứng chỉ → Manager verify
                                
                                ---
                                **Developed by**: HRM Team
                                """)
                        .contact(new Contact()
                                .name("HRM Support")
                                .email("support@hrm.example.com")
                                .url("https://hrm.example.com"))
                        .license(new License()
                                .name("MIT License")
                                .url("https://opensource.org/licenses/MIT")))
                .servers(List.of(
                        new Server()
                                .url("http://localhost:8080")
                                .description("Local Development Server"),
                        new Server()
                                .url("https://api.hrm.example.com")
                                .description("Production Server")
                ));
    }
}
