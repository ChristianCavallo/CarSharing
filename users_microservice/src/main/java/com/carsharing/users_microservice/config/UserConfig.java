package com.carsharing.users_microservice.config;

import com.carsharing.users_microservice.entities.User;
import com.carsharing.users_microservice.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class UserConfig {

    @Value("${admin_user_id}")
    private String admin_id;

    @Value("${admin_username}")
    private String admin_user;

    @Value("${admin_password}")
    private String admin_pass;

    @Autowired
    UserService userService;

    @Bean
    public void InitializeAdministrator(){
        User u = new User();
        u.getRoles().add("ADMINISTRATOR");
        u.setName(admin_user);
        u.setEmail(admin_user);
        u.setPassword(admin_pass);
        u.setId(admin_id);
        userService.initializeAdminUser(u);
    }

}
