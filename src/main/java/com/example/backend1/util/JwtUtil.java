package com.example.backend1.util;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;
import com.example.backend1.model.Role;

import java.security.Key;
import java.util.Date;



@Component
public class JwtUtil {
    private static final String SECRET = "MyVerySecretKeyForJWTTokenGenerationThatIsLongEnough123456789";
    private static final long EXPIRATION_TIME = 86400000;

    private Key getSigningKey(){
        return Keys.hmacShaKeyFor(SECRET.getBytes());
    }

    public String generateToken(String email, Role role, String nom, String prenom, boolean verified, boolean adminVerified, Long id){
        return Jwts.builder()
                .setSubject(email)
                .claim("role", role.name())
                .claim("nom", nom)
                .claim("prenom", prenom)
                .claim("verified", verified)
                .claim("adminVerified", adminVerified)
                .claim("id", id)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    public String extractEmail(String token){
        return getClaims(token).getSubject();
    }
    public Role extractRole(String token) {
        String roleString = (String) getClaims(token).get("role");
        return Role.valueOf(roleString);
    }
    public boolean validateToken(String token){
        try{
            getClaims(token);
            return !isTokenExpired(token);
        }catch (JwtException | IllegalArgumentException e){
            return false;
        }
    }
    public Long extractUserId(String token) {
        Object id = getClaims(token).get("id");
        if (id == null) return null;
        return Long.valueOf(id.toString());
    }

    public boolean isTokenExpired(String token){
        return getClaims(token).getExpiration().before(new Date());
    }
    private Claims getClaims (String token ){
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
}
