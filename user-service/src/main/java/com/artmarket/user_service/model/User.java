package com.artmarket.user_service.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Pattern;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;

@Entity
@Setter
@Getter
@Builder
@Table(name = "users")
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "user_gen")
    @SequenceGenerator(name = "user_gen", sequenceName = "user_seq")
    @Column(name = "id", nullable = false)
    @JdbcTypeCode(SqlTypes.BIGINT)
    Long id;

    @Column(unique = true, nullable = false)
    String keycloakId;
    @NonNull
    String email;
    @NonNull
    String firstName;
    @NonNull
    String lastName;

    @Enumerated(EnumType.STRING)
    UserType type;

    @Builder.Default
    Instant createdAt = Instant.now();
}

