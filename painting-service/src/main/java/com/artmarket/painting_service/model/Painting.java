package com.artmarket.painting_service.model;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Date;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Setter
@Getter
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Painting {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "painting_gen")
    @SequenceGenerator(name = "painting_gen", sequenceName = "painting_seq", allocationSize = 1)
    @Column(name = "id", nullable = false)
    @JdbcTypeCode(SqlTypes.BIGINT)
    Long id;
    @NonNull
    String title;
    @NonNull
    String description;
    @NonNull
    String author;
    @NonNull
    Date releaseDate;
    @NonNull
    BigDecimal price;
    @NonNull
    String imageURL;
    @NonNull
    String userId;
   @Builder.Default
   Instant createdAt = Instant.now();
}
