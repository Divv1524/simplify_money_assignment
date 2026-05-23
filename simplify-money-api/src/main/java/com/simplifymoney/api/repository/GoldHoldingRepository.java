package com.simplifymoney.api.repository;

import com.simplifymoney.api.model.GoldHolding;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface GoldHoldingRepository extends MongoRepository<GoldHolding, String> {
    Optional<GoldHolding> findByUserId(String userId);
}
