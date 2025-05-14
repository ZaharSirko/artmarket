package com.artmarket.painting_service.repository;

import com.artmarket.painting_service.model.Painting;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PaintingRepository extends JpaRepository<Painting, Long> {
    Optional<Painting> findAllByUserId(String userId);
}
