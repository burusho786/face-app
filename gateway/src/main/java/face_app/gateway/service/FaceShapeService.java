
package face_app.gateway.service;

import face_app.gateway.client.FaceServiceClient;
import face_app.gateway.dto.FaceShapePredictionResponse;
import face_app.gateway.exception.ApiException;
import java.io.IOException;
import java.util.Locale;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class FaceShapeService {

    private static final long MAX_UPLOAD_BYTES = 10L * 1024 * 1024;

    private final FaceServiceClient faceServiceClient;

    public FaceShapeService(FaceServiceClient faceServiceClient) {
        this.faceServiceClient = faceServiceClient;
    }

    public FaceShapePredictionResponse predict(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new ApiException(
                    HttpStatus.BAD_REQUEST,
                    "EMPTY_FILE",
                    "Upload a non-empty image."
            );
        }

        if (file.getSize() > MAX_UPLOAD_BYTES) {
            throw new ApiException(
                    HttpStatus.PAYLOAD_TOO_LARGE,
                    "FILE_TOO_LARGE",
                    "The maximum upload size is 10 MB."
            );
        }

        String filename = sanitizeFilename(file.getOriginalFilename());

        try {
            byte[] imageBytes = file.getBytes();

            validateImageSignature(imageBytes);

            return faceServiceClient.predict(imageBytes, filename);

        } catch (IOException ex) {
            throw new ApiException(
                    HttpStatus.BAD_REQUEST,
                    "UPLOAD_READ_FAILED",
                    "The uploaded image could not be read."
            );
        }
    }

    private String sanitizeFilename(String originalFilename) {
        if (originalFilename == null || originalFilename.isBlank()) {
            return "upload";
        }

        String filename = originalFilename.replace('\\', '/');
        filename = filename.substring(filename.lastIndexOf('/') + 1);

        // Avoid passing control characters or excessively long filenames.
        filename = filename.replaceAll("[\\p{Cntrl}]", "_");

        if (filename.isBlank()) {
            return "upload";
        }

        return filename.length() > 100
                ? filename.substring(0, 100)
                : filename;
    }

    private void validateImageSignature(byte[] bytes) {
        if (isJpeg(bytes) || isPng(bytes) || isWebp(bytes)) {
            return;
        }

        throw new ApiException(
                HttpStatus.BAD_REQUEST,
                "UNSUPPORTED_IMAGE",
                "Only JPEG, PNG, and WebP images are supported."
        );
    }

    private boolean isJpeg(byte[] bytes) {
        return bytes.length >= 3
                && (bytes[0] & 0xFF) == 0xFF
                && (bytes[1] & 0xFF) == 0xD8
                && (bytes[2] & 0xFF) == 0xFF;
    }

    private boolean isPng(byte[] bytes) {
        return bytes.length >= 8
                && (bytes[0] & 0xFF) == 0x89
                && bytes[1] == 0x50
                && bytes[2] == 0x4E
                && bytes[3] == 0x47
                && bytes[4] == 0x0D
                && bytes[5] == 0x0A
                && bytes[6] == 0x1A
                && bytes[7] == 0x0A;
    }

    private boolean isWebp(byte[] bytes) {
        return bytes.length >= 12
                && bytes[0] == 'R'
                && bytes[1] == 'I'
                && bytes[2] == 'F'
                && bytes[3] == 'F'
                && bytes[8] == 'W'
                && bytes[9] == 'E'
                && bytes[10] == 'B'
                && bytes[11] == 'P';
    }
}