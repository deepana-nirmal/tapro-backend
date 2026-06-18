package qr_ordering_system.service;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Set;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import qr_ordering_system.exception.BadRequestException;
import qr_ordering_system.exception.ResourceNotFoundException;

@Service
public class CategoryImageService {

    private static final long MAX_FILE_SIZE_BYTES = 5L * 1024 * 1024;
    private static final Set<String> ALLOWED_CONTENT_TYPES = Set.of(
            "image/jpeg",
            "image/png",
            "image/webp"
    );

    private final Path uploadRoot;

    public CategoryImageService(@Value("${app.upload-dir:uploads}") String uploadDir) {
        this.uploadRoot = Paths.get(uploadDir).toAbsolutePath().normalize().resolve("categories");

        try {
            Files.createDirectories(uploadRoot);
        } catch (IOException e) {
            throw new IllegalStateException("Unable to initialize category upload directory", e);
        }
    }

    public String store(Long restaurantId, MultipartFile file) {
        validate(file);

        String extension = getExtension(file.getOriginalFilename(), file.getContentType());
        String storedFilename = "category-" + System.currentTimeMillis() + extension;
        Path restaurantDirectory = uploadRoot.resolve(String.valueOf(restaurantId)).normalize();

        try {
            Files.createDirectories(restaurantDirectory);
            try (InputStream inputStream = file.getInputStream()) {
                Files.copy(inputStream, restaurantDirectory.resolve(storedFilename), StandardCopyOption.REPLACE_EXISTING);
            }
        } catch (IOException e) {
            throw new IllegalStateException("Unable to store category image", e);
        }

        return "/api/categories/images/" + restaurantId + "/" + storedFilename;
    }

    public Resource loadAsResource(Long restaurantId, String filename) {
        try {
            Path file = uploadRoot.resolve(String.valueOf(restaurantId)).resolve(filename).normalize();
            Resource resource = new UrlResource(file.toUri());

            if (!resource.exists() || !resource.isReadable()) {
                throw new ResourceNotFoundException("Category image not found");
            }

            return resource;
        } catch (IOException e) {
            throw new ResourceNotFoundException("Category image not found");
        }
    }

    public String detectContentType(Long restaurantId, String filename) {
        try {
            String contentType = Files.probeContentType(uploadRoot.resolve(String.valueOf(restaurantId)).resolve(filename));
            return contentType != null ? contentType : "application/octet-stream";
        } catch (IOException e) {
            return "application/octet-stream";
        }
    }

    private void validate(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BadRequestException("Category image file is required");
        }

        if (file.getSize() > MAX_FILE_SIZE_BYTES) {
            throw new BadRequestException("Category image must be 5MB or smaller");
        }

        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_CONTENT_TYPES.contains(contentType)) {
            throw new BadRequestException("Only JPG, PNG, and WEBP images are allowed");
        }
    }

    private String getExtension(String originalFilename, String contentType) {
        String sanitized = StringUtils.cleanPath(originalFilename == null ? "" : originalFilename);
        int dotIndex = sanitized.lastIndexOf('.');
        if (dotIndex >= 0) {
            return sanitized.substring(dotIndex);
        }

        return switch (contentType) {
            case "image/png" -> ".png";
            case "image/webp" -> ".webp";
            default -> ".jpg";
        };
    }
}
