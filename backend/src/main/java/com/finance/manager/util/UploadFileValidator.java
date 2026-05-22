package com.finance.manager.util;

import org.springframework.http.HttpStatus;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.io.InputStream;
import java.util.Locale;
import java.util.Set;

public final class UploadFileValidator {

    private static final Set<String> ALLOWED_TYPES = Set.of(
            "image/jpeg", "image/png", "image/webp", "image/gif", "application/pdf");

    private UploadFileValidator() {}

    public static void validateReceiptFile(MultipartFile file, long maxBytes) {
        if (file == null || file.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "File is required");
        }
        if (file.getSize() > maxBytes) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "File too large");
        }
        String contentType = normalizeContentType(file.getContentType());
        if (!ALLOWED_TYPES.contains(contentType)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Unsupported file type");
        }
        try {
            if (!magicBytesMatch(file, contentType)) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "File content does not match type");
            }
        } catch (IOException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Unable to read file");
        }
    }

    public static String safeFilename(String original) {
        if (original == null || original.isBlank()) {
            return "receipt";
        }
        String base = original.replaceAll("[^a-zA-Z0-9._-]", "_");
        if (base.length() > 120) {
            base = base.substring(0, 120);
        }
        return base.isBlank() ? "receipt" : base;
    }

    private static String normalizeContentType(String contentType) {
        if (contentType == null) {
            return "";
        }
        return contentType.toLowerCase(Locale.ROOT).split(";")[0].trim();
    }

    private static boolean magicBytesMatch(MultipartFile file, String contentType) throws IOException {
        byte[] header = new byte[12];
        try (InputStream in = file.getInputStream()) {
            int read = in.read(header);
            if (read < 4) {
                return false;
            }
        }
        return switch (contentType) {
            case "image/jpeg" -> header[0] == (byte) 0xFF && header[1] == (byte) 0xD8;
            case "image/png" -> header[0] == (byte) 0x89 && header[1] == 0x50 && header[2] == 0x4E && header[3] == 0x47;
            case "image/gif" -> header[0] == 'G' && header[1] == 'I' && header[2] == 'F';
            case "image/webp" -> header[0] == 'R' && header[1] == 'I' && header[2] == 'F' && header[3] == 'F'
                    && header[8] == 'W' && header[9] == 'E' && header[10] == 'B' && header[11] == 'P';
            case "application/pdf" -> header[0] == '%' && header[1] == 'P' && header[2] == 'D' && header[3] == 'F';
            default -> false;
        };
    }
}
