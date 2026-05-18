package com.finance.manager.controller;

import com.finance.manager.model.ReceiptAttachment;
import com.finance.manager.security.AuthPrincipal;
import com.finance.manager.service.OpenAiKeyResolver;
import com.finance.manager.service.OpenAiReceiptExtractionService;
import com.finance.manager.service.ReceiptStorageService;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestController
public class ReceiptController {

    private final ReceiptStorageService receiptStorageService;
    private final OpenAiKeyResolver openAiKeyResolver;
    private final OpenAiReceiptExtractionService receiptExtractionService;

    public ReceiptController(
            ReceiptStorageService receiptStorageService,
            OpenAiKeyResolver openAiKeyResolver,
            OpenAiReceiptExtractionService receiptExtractionService) {
        this.receiptStorageService = receiptStorageService;
        this.openAiKeyResolver = openAiKeyResolver;
        this.receiptExtractionService = receiptExtractionService;
    }

    @PostMapping("/transactions/{id}/receipt")
    public ResponseEntity<ReceiptAttachment> upload(
            @AuthenticationPrincipal AuthPrincipal principal,
            @PathVariable Long id,
            @RequestParam("file") MultipartFile file) throws IOException {
        return ResponseEntity.ok(receiptStorageService.upload(principal.userId(), id, file));
    }

    @GetMapping("/transactions/{id}/receipt")
    public ResponseEntity<Resource> download(
            @AuthenticationPrincipal AuthPrincipal principal,
            @PathVariable Long id) throws IOException {
        ReceiptAttachment meta = receiptStorageService.getMetadata(principal.userId(), id);
        if (meta == null) {
            return ResponseEntity.notFound().build();
        }
        Resource resource = receiptStorageService.loadAsResource(principal.userId(), id);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + meta.getOriginalFilename() + "\"")
                .contentType(MediaType.parseMediaType(meta.getContentType()))
                .body(resource);
    }

    @GetMapping("/transactions/{id}/receipt/meta")
    public ResponseEntity<ReceiptAttachment> metadata(
            @AuthenticationPrincipal AuthPrincipal principal,
            @PathVariable Long id) {
        ReceiptAttachment meta = receiptStorageService.getMetadata(principal.userId(), id);
        if (meta == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(meta);
    }

    @DeleteMapping("/transactions/{id}/receipt")
    public ResponseEntity<Void> delete(
            @AuthenticationPrincipal AuthPrincipal principal,
            @PathVariable Long id) {
        receiptStorageService.delete(principal.userId(), id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/ai/extract-receipt")
    public ResponseEntity<Map<String, Object>> extractReceipt(
            @AuthenticationPrincipal AuthPrincipal principal,
            @RequestParam("file") MultipartFile file) {
        Map<String, Object> result = new HashMap<>();
        Optional<OpenAiKeyResolver.ResolvedKey> resolved =
                openAiKeyResolver.resolveForUser(principal.userId());
        if (resolved.isEmpty()) {
            result.put("source", "no_api_key");
            return ResponseEntity.ok(result);
        }
        OpenAiKeyResolver.ResolvedKey key = resolved.get();
        Optional<Map<String, Object>> extracted =
                receiptExtractionService.extract(key.apiKey(), file);
        if (extracted.isPresent()) {
            result.putAll(extracted.get());
            result.put("source", key.source() == OpenAiKeyResolver.ResolvedKey.Source.PLATFORM
                    ? "openai_platform"
                    : "openai");
        } else {
            result.put("source", "openai_failed");
        }
        return ResponseEntity.ok(result);
    }
}
