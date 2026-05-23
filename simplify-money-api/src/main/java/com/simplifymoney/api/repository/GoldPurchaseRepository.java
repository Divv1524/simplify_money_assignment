package com.simplifymoney.api.repository;

import com.simplifymoney.api.model.GoldPurchase;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface GoldPurchaseRepository extends MongoRepository<GoldPurchase, String> {
    Optional<GoldPurchase> findByIdempotencyKey(String idempotencyKey);
    List<GoldPurchase> findByUserIdOrderByCreatedAtDesc(String userId);
}
