package com.finance.manager.repository;

import com.finance.manager.model.AppNotification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AppNotificationRepository extends JpaRepository<AppNotification, Long> {

    List<AppNotification> findByOwner_IdOrderByCreatedAtDesc(Long ownerId);

    long countByOwner_IdAndReadFalse(Long ownerId);

    Optional<AppNotification> findByIdAndOwner_Id(Long id, Long ownerId);

    void deleteByOwner_IdAndTypeAndReferenceTypeAndReferenceIdAndReadFalse(
            Long ownerId,
            AppNotification.NotificationType type,
            String referenceType,
            Long referenceId);

    List<AppNotification> findByOwner_IdAndTypeAndReferenceTypeAndReferenceId(
            Long ownerId,
            AppNotification.NotificationType type,
            String referenceType,
            Long referenceId);

    void deleteByOwner_Id(Long ownerId);
}
