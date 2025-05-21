package org.example.service;

import org.example.entity.Asset;
import org.example.repository.AssetRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class AssetService {

    private final AssetRepository assetRepository;

    public AssetService(AssetRepository assetRepository) {
        this.assetRepository = assetRepository;
    }

    public Optional<Asset> findByTicker(String ticker) {
        return assetRepository.findByTicker(ticker);
    }

    public List<Asset> findAll() {
        return assetRepository.findAll();
    }

    public Asset findById(Integer id) {
        return assetRepository.findById(id).orElse(null);
    }
}