package com.ltcuong.identity_service.configuration;

import com.ltcuong.identity_service.enums.Role;
import com.ltcuong.identity_service.entity.User;
import com.ltcuong.identity_service.repository.UserRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.HashSet;

@Slf4j
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Configuration
public class ApplicationInitConfig {

    // inject PasswordEncoder
    PasswordEncoder passwordEncoder;

    // tạo role admin 1 lần duy nhất
    @Bean
    ApplicationRunner applicationRunner(UserRepository userRepository){
        return args -> {
            // kiểm tra tồn tại admin hay chưa

            if(userRepository.findByUsername("admin").isEmpty()){
                // nếu chưa
                // tạo user admin
                var roles = new HashSet<String>();
                roles.add(Role.ADMIN.name());

                // ngày 9/9 đã làm đến đây
                User user = User.builder()
                        .username("admin")
                        .password(passwordEncoder.encode("admin"))
//                        .roles(roles)
                        .build();
                userRepository.save(user);
                log.warn("admin user created with default password: admin, please change it");
            }
        };
    }
}
