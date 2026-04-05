---
name: springboot-security
description: Java Spring Boot 서비스의 인증/인가, 유효성 검사, CSRF, 비밀 정보 관리, 보안 헤더, 속도 제한(Rate Limiting) 및 보안 모범 사례 가이드입니다.
---

# Spring Boot 보안 가이드라인

인증 추가, 로그인 처리, 엔드포인트 보안 설정 또는 민감한 정보 처리 시 이 가이드를 참조하세요.

## 활성화 시점

- 인증 추가 (JWT, OAuth2, 세션 기반)
- 인가 구현 (@PreAuthorize, 역할 기반 접근 제어)
- 사용자 입력 유효성 검사 (Bean Validation, 커스텀 검증기)
- CORS, CSRF 또는 보안 헤더 설정
- 비밀 정보 관리 (Vault, 환경 변수)
- 속도 제한(Rate Limiting) 또는 브루트 포스 방어 추가
- 의존성 취약점(CVE) 스캔

## 인증 (Authentication)

- 취소 목록(Revocation list)이 포함된 무상태(Stateless) JWT 또는 불투명 토큰을 권장합니다.
- 세션의 경우 `httpOnly`, `Secure`, `SameSite=Strict` 쿠키 설정을 사용합니다.
- `OncePerRequestFilter` 또는 리소스 서버를 통해 토큰을 검증합니다.

```java
@Component
public class JwtAuthFilter extends OncePerRequestFilter {
  private final JwtService jwtService;

  public JwtAuthFilter(JwtService jwtService) {
    this.jwtService = jwtService;
  }

  @Override
  protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
      FilterChain chain) throws ServletException, IOException {
    String header = request.getHeader(HttpHeaders.AUTHORIZATION);
    if (header != null && header.startsWith("Bearer ")) {
      String token = header.substring(7);
      Authentication auth = jwtService.authenticate(token);
      SecurityContextHolder.getContext().setAuthentication(auth);
    }
    chain.doFilter(request, response);
  }
}
```

## 인가 (Authorization)

- 메소드 보안 활성화: `@EnableMethodSecurity`
- `@PreAuthorize("hasRole('ADMIN')")` 또는 `@PreAuthorize("@authz.canEdit(#id)")` 사용
- 기본적으로 접근을 차단하고 필요한 스코프만 허용합니다.

```java
@RestController
@RequestMapping("/api/admin")
public class AdminController {

  @PreAuthorize("hasRole('ADMIN')")
  @GetMapping("/users")
  public List<UserDto> listUsers() {
    return userService.findAll();
  }

  @PreAuthorize("@authz.isOwner(#id, authentication)")
  @DeleteMapping("/users/{id}")
  public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
    userService.delete(id);
    return ResponseEntity.noContent().build();
  }
}
```

## 입력 유효성 검사 (Input Validation)

- 컨트롤러에서 `@Valid`와 함께 Bean Validation 사용
- DTO에 제약 조건 적용: `@NotBlank`, `@Email`, `@Size`, 커스텀 검증기 등
- 렌더링 전 HTML 입력을 화이트리스트 방식으로 정제(Sanitize)

```java
// 나쁜 예: 유효성 검사 없음
@PostMapping("/users")
public User createUser(@RequestBody UserDto dto) {
  return userService.create(dto);
}

// 좋은 예: 검증된 DTO
public record CreateUserDto(
    @NotBlank @Size(max = 100) String name,
    @NotBlank @Email String email,
    @NotNull @Min(0) @Max(150) Integer age
) {}

@PostMapping("/users")
public ResponseEntity<UserDto> createUser(@Valid @RequestBody CreateUserDto dto) {
  return ResponseEntity.status(HttpStatus.CREATED)
      .body(userService.create(dto));
}
```

## SQL 인젝션 방지

- Spring Data 리포지토리 또는 파라미터화된 쿼리 사용
- 네이티브 쿼리의 경우 `:param` 바인딩 사용 (문자열 결합 절대 금지)

```java
// 나쁜 예: 네이티브 쿼리에서 문자열 결합
@Query(value = "SELECT * FROM users WHERE name = '" + name + "'", nativeQuery = true)

// 좋은 예: 파라미터화된 네이티브 쿼리
@Query(value = "SELECT * FROM users WHERE name = :name", nativeQuery = true)
List<User> findByName(@Param("name") String name);

// 좋은 예: Spring Data 메소드 명명 규칙 사용 (자동 파라미터화)
List<User> findByEmailAndActiveTrue(String email);
```

## 비밀번호 암호화

- 항상 BCrypt 또는 Argon2로 해싱하여 저장 (평문 저장 금지)
- `PasswordEncoder` 빈을 사용

```java
@Bean
public PasswordEncoder passwordEncoder() {
  return new BCryptPasswordEncoder(12); // cost factor 12
}

// 서비스 내부
public User register(CreateUserDto dto) {
  String hashedPassword = passwordEncoder.encode(dto.password());
  return userRepository.save(new User(dto.email(), hashedPassword));
}
```

## CSRF 방어

- 브라우저 세션 기반 애플리케이션인 경우 CSRF 활성화 유지
- Bearer 토큰을 사용하는 순수 API 서버인 경우 CSRF 비활성화 가능 (Stateless 인증에 의존)

```java
http
  .csrf(csrf -> csrf.disable())
  .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS));
```

## 비밀 정보 관리 (Secret Management)

- 소스 코드에 비밀 정보를 포함하지 말고 환경 변수나 Vault 사용
- `application.yml`에는 실제 값 대신 플레이스홀더 사용
- 토큰 및 DB 접속 정보를 정기적으로 갱신(Rotation)

```yaml
# 나쁜 예: application.yml에 하드코딩
spring:
  datasource:
    password: mySecretPassword123

# 좋은 예: 환경 변수 사용
spring:
  datasource:
    password: ${DB_PASSWORD}

# 좋은 예: Spring Cloud Vault 통합
spring:
  cloud:
    vault:
      uri: https://vault.example.com
      token: ${VAULT_TOKEN}
```

## 보안 관련 헤더

```java
http
  .headers(headers -> headers
    .contentSecurityPolicy(csp -> csp
      .policyDirectives("default-src 'self'"))
    .frameOptions(HeadersConfigurer.FrameOptionsConfig::sameOrigin)
    .xssProtection(Customizer.withDefaults())
    .referrerPolicy(rp -> rp.policy(ReferrerPolicyHeaderWriter.ReferrerPolicy.NO_REFERRER)));
```

## CORS 설정

- 컨트롤러 단위가 아닌 보안 필터 수준에서 설정 권장
- 허용할 Origin을 엄격히 제한 (운영 환경에서 `*` 사용 금지)

```java
@Bean
public CorsConfigurationSource corsConfigurationSource() {
  CorsConfiguration config = new CorsConfiguration();
  config.setAllowedOrigins(List.of("https://app.example.com"));
  config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE"));
  config.setAllowedHeaders(List.of("Authorization", "Content-Type"));
  config.setAllowCredentials(true);
  config.setMaxAge(3600L);

  UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
  source.registerCorsConfiguration("/api/**", config);
  return source;
}

// SecurityFilterChain 내부
http.cors(cors -> cors.configurationSource(corsConfigurationSource()));
```

## 속도 제한 (Rate Limiting)

- 비용이 큰 엔드포인트에 Bucket4j 또는 게이트웨이 수준의 제한 적용
- 임계값 초과 시 로그 기록 및 429(Too Many Requests) 응답 코드 반환

```java
// Endpoint başına rate limiting için Bucket4j kullanma
@Component
public class RateLimitFilter extends OncePerRequestFilter {
  private final Map<String, Bucket> buckets = new ConcurrentHashMap<>();

  private Bucket createBucket() {
    return Bucket.builder()
        .addLimit(Bandwidth.classic(100, Refill.intervally(100, Duration.ofMinutes(1))))
        .build();
  }

  @Override
  protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
      FilterChain chain) throws ServletException, IOException {
    String clientIp = request.getRemoteAddr();
    Bucket bucket = buckets.computeIfAbsent(clientIp, k -> createBucket());

    if (bucket.tryConsume(1)) {
      chain.doFilter(request, response);
    } else {
      response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
      response.getWriter().write("{\"error\": \"Rate limit exceeded\"}");
    }
  }
}
```

## 의존성 보안

- CI 단계에서 OWASP Dependency Check 또는 Snyk 실행
- Spring Boot 및 Spring Security를 지원되는 최신 버전으로 유지
- 알려진 CVE 존재 시 빌드 실패 처리

## 로깅 및 개인정보(PII)

- 토큰, 비밀번호, 카드 정보 등 민감한 정보를 절대 로그에 남기지 않음
- 민감한 필드는 마스킹 처리

## 배포 전 체크리스트

- [ ] 인증 토큰이 올바르게 검증되고 만료 처리가 되는가?
- [ ] 모든 민감한 경로에 인가 설정이 되어 있는가?
- [ ] 모든 입력값이 유효성 검사를 거치고 정제되었는가?
- [ ] SQL 인젝션 위험이 있는 코드가 없는가?
- [ ] CSRF 설정이 적절하게 구성되었는가?
- [ ] 비밀 정보가 외부 설정으로 분리되었으며 소스에 포함되지 않았는가?
- [ ] 보안 관련 헤더가 설정되었는가?
- [ ] 주요 API에 속도 제한이 적용되었는가?
- [ ] 의존성에 취약점이 없는지 스캔하였는가?
- [ ] 로그에 민감한 정보가 노출되지 않는가?

**주의**: "기본적으로 차단(Deny by default)", "입력값 검증", "최소 권한의 원칙"을 항상 준수하세요.
