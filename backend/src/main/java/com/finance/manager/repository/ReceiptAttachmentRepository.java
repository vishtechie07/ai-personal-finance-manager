package com.finance.manager.repository;

import com.finance.manager.model.ReceiptAttachment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ReceiptAttachmentRepository extends JpaRepository<ReceiptAttachment, Long> {

    Optional<ReceiptAttachment> findByTransaction_IdAndOwner_Id(Long transactionId, Long ownerId);

    boolean existsByTransaction_IdAndOwner_Id(Long transactionId, Long ownerId);

    java.util.List<ReceiptAttachment> findByOwner_Id(Long ownerId);

    void deleteByOwner_Id(Long ownerId);
}
