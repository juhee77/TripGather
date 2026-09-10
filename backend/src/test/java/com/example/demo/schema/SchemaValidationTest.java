package com.example.demo.schema;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

/**
 * Flyway 마이그레이션으로 만들어진 스키마가 JPA 엔티티와 일치하는지 검증한다.
 *
 * local 프로필은 ddl-auto=update 라 누락 컬럼이 자동 생성되어 드리프트가 드러나지 않지만,
 * prod 프로필은 ddl-auto=validate 이므로 드리프트가 있으면 애플리케이션이 기동 자체에 실패한다.
 * 이 테스트는 prod 와 동일한 validate 모드로 컨텍스트를 띄워, 배포 전에 드리프트를 잡는다.
 */
@SpringBootTest
@ActiveProfiles("schemacheck")
@DisplayName("엔티티-마이그레이션 스키마 정합성")
class SchemaValidationTest {

    @Test
    @DisplayName("Flyway 로 만든 스키마가 모든 엔티티 매핑을 만족한다")
    void migratedSchemaSatisfiesEntityMappings() {
        // @SpringBootTest 가 ddl-auto=validate 로 컨텍스트를 기동하는 것 자체가 검증이다.
        // 컬럼이 하나라도 어긋나면 SchemaManagementException 으로 실패한다.
    }
}
