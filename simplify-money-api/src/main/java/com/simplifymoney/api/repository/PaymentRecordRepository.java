package com.simplifymoney.api.repository;

import com.simplifymoney.api.model.PaymentRecord;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface PaymentRecordRepository extends MongoRepository<PaymentRecord, String> {
    Optional<PaymentRecord> findByPurchaseId(String purchaseId);
}
