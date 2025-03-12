package com.app.music.config;
                     
//no need handled by api-gateway

//@Configuration
//@EnableWebSecurity
//@EnableMethodSecurity
//public class SecurityConfig {
//	
//	
//	@Autowired
//	private KeycloakJwtAuthoritiesConverter keycloakJwtAuthoritiesConverter;
//	
//	 @Bean
//	    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
//	        http
//	        .csrf(csrf -> csrf
//					.disable()  // Disable CSRF protection globally
//					)
//	            .authorizeHttpRequests(auth -> auth
//	                .requestMatchers("/api/v1/music/public/**").permitAll() // Public APIs
////	                .requestMatchers("/api/v1/music/admin/**").hasRole("admin") // Only Admins can access
//	                .anyRequest().authenticated()
//	            )
////	    		.oauth2ResourceServer(oauth->oauth.jwt(org.springframework.security.config.Customizer.withDefaults())); // Enable JWT authentication
//
//	            ;
////	            .oauth2ResourceServer(oauth2 -> oauth2.jwt(jwt -> jwt.jwtAuthenticationConverter(keycloakJwtAuthoritiesConverter)));
//
//	        return http.build();
//	    }
//}
