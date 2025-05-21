package org.example.repository;

import org.example.entity.Asset;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AssetRepository extends JpaRepository<Asset, Integer> {
    Optional<Asset> findByTicker(String ticker);
}