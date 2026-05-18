package com.finance.manager.service;

import com.finance.manager.config.StorageProperties;
import com.finance.manager.model.ReceiptAttachment;
import com.finance.manager.model.Transaction;
import com.finance.manager.model.User;
import com.finance.manager.repository.ReceiptAttachmentRepository;
import com.finance.manager.repository.TransactionRepository;
import com.finance.manager.repository.UserRepository;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Set;
import java.util.UUID;

@Service
public class ReceiptStorageService {

    private static final Set<String> ALLOWED_TYPES = Set.of(
            "image/jpeg", "image/png", "image/webp", "image/gif", "application/pdf");

    private final StorageProperties storageProperties;
    private final ReceiptAttachmentRepository receiptRepository;
    private final TransactionRepository transactionRepository;
    private final UserRepository userRepository;

    public ReceiptStorageService(
            StorageProperties storageProperties,
            ReceiptAttachmentRepository receiptRepository,
            TransactionRepository transactionRepository,
            UserRepository userRepository) {
        this.storageProperties = storageProperties;
        this.receiptRepository = receiptRepository;
        this.transactionRepository = transactionRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public ReceiptAttachment upload(Long userId, Long transactionId, MultipartFile file) throws IOException {
        if (file == null || file.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "File is required");
        }
        if (file.getSize() > storageProperties.getMaxFileSize()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "File too large");
        }
        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_TYPES.contains(contentType)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Unsupported file type");
        }

        Transaction transaction = transactionRepository.findByIdAndOwner_Id(transactionId, userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Transaction not found"));

        receiptRepository.findByTransaction_IdAndOwner_Id(transactionId, userId)
                .ifPresent(existing -> deleteFile(existing.getStoredFilename()));

        Path dir = Path.of(storageProperties.getPath());
        Files.createDirectories(dir);
        String storedName = UUID.randomUUID() + extensionFor(contentType);
        Path target = dir.resolve(storedName);
        Files.copy(file.getInputStream(), target);

        ReceiptAttachment attachment = receiptRepository.findByTransaction_IdAndOwner_Id(transactionId, userId)
                .orElse(new ReceiptAttachment());
        User owner = userRepository.findById(userId).orElseThrow();
        attachment.setOwner(owner);
        attachment.setTransaction(transaction);
        attachment.setOriginalFilename(file.getOriginalFilename() != null ? file.getOriginalFilename() : storedName);
        attachment.setStoredFilename(storedName);
        attachment.setContentType(contentType);
        attachment.setSizeBytes(file.getSize());
        return receiptRepository.save(attachment);
    }

    public ReceiptAttachment getMetadata(Long userId, Long transactionId) {
        return receiptRepository.findByTransaction_IdAndOwner_Id(transactionId, userId)
                .orElse(null);
    }

    public Resource loadAsResource(Long userId, Long transactionId) throws IOException {
        ReceiptAttachment attachment = receiptRepository.findByTransaction_IdAndOwner_Id(transactionId, userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Receipt not found"));
        Path path = Path.of(storageProperties.getPath()).resolve(attachment.getStoredFilename());
        Resource resource = new UrlResource(path.toUri());
        if (!resource.exists() || !resource.isReadable()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Receipt file missing");
        }
        return resource;
    }

    @Transactional
    public void delete(Long userId, Long transactionId) {
        ReceiptAttachment attachment = receiptRepository.findByTransaction_IdAndOwner_Id(transactionId, userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Receipt not found"));
        deleteFile(attachment.getStoredFilename());
        receiptRepository.delete(attachment);
    }

    private void deleteFile(String storedFilename) {
        try {
            Files.deleteIfExists(Path.of(storageProperties.getPath()).resolve(storedFilename));
        } catch (IOException ignored) {
        }
    }

    private String extensionFor(String contentType) {
        return switch (contentType) {
            case "image/png" -> ".png";
            case "image/webp" -> ".webp";
            case "image/gif" -> ".gif";
            case "application/pdf" -> ".pdf";
            default -> ".jpg";
        };
    }
}
