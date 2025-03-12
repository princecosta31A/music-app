package com.app.music.config;

//no need handled by api-gateway

//@Component
//public class KeycloakJwtAuthoritiesConverter implements Converter<Jwt, AbstractAuthenticationToken> {
//
//    @Override
//    public AbstractAuthenticationToken convert(Jwt jwt) {
//        Collection<GrantedAuthority> authorities = extractAuthorities(jwt);
//        return new JwtAuthenticationToken(jwt, authorities);
//    }
//
//    private Collection<GrantedAuthority> extractAuthorities(Jwt jwt) {
//        if (jwt.getClaimAsMap("realm_access") != null) {
//            Map<String, Object> realmAccess = jwt.getClaimAsMap("realm_access");
//
//            List<String> keycloakRoles = (List<String>) realmAccess.get("roles");
//            List<GrantedAuthority> roles = new ArrayList<>();
//
//            for (String keycloakRole : keycloakRoles) {
//            	 roles.add(new SimpleGrantedAuthority("ROLE_" + keycloakRole));
//            }
//            return roles;
//        }
//        return new ArrayList<>();
//    }
//}
