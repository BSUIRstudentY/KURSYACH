package org.example.config;

import org.example.controller.*;
import org.example.repository.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@ComponentScan(basePackages = {"org.example.controller", "org.example.repository"})
public class AppConfig {

    // Явное определение бинов не требуется, если используется @ComponentScan
    // Однако можно добавить, если есть специфические настройки


    // Убедитесь, что все репозитории доступны
    // Эти бины обычно создаются автоматически через Spring Data JPA
}