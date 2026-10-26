package face_app.gateway.client;


import face_app.gateway.config.FaceServiceProperties;
import face_app.gateway.dto.FaceShapePredictionResponse;
import face_app.gateway.exception.ApiException;
import java.io.IOException;
import java.util.Objects;

import face_app.gateway.config.FaceServiceProperties;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;

@Component
public class FaceServiceClient {

    private final RestClient restClient;
    private final FaceServiceProperties properties;

    public FaceServiceClient(
            RestClient.Builder builder,
            FaceServiceProperties properties
    ) {
        this.properties = properties;

        SimpleClientHttpRequestFactory requestFactory =
                new SimpleClientHttpRequestFactory();

        requestFactory.setConnectTimeout(properties.getConnectTimeout());
        requestFactory.setReadTimeout(properties.getReadTimeout());

        this.restClient = builder
                .baseUrl(properties.getBaseUrl())
                .requestFactory(requestFactory)
                .build();
    }

    public FaceShapePredictionResponse predict(
            byte[] imageBytes,
            String filename
    ) {
        ByteArrayResource imageResource = new ByteArrayResource(imageBytes) {
            @Override
            public String getFilename() {
                return filename;
            }
        };

        MultiValueMap<String, Object> multipartBody =
                new LinkedMultiValueMap<>();

        // Must match FastAPI's multipart parameter: image
        multipartBody.add("image", imageResource);

        try {
            FaceShapePredictionResponse response = restClient.post()
                    .uri("/v1/face-shape/predict")
                    .header(
                            "X-Service-Token",
                            properties.getServiceToken()
                    )
                    .contentType(MediaType.MULTIPART_FORM_DATA)
                    .accept(MediaType.APPLICATION_JSON)
                    .body(multipartBody)
                    .retrieve()
                    .body(FaceShapePredictionResponse.class);

            if (response == null || response.faceShape() == null) {
                throw new ApiException(
                        HttpStatus.BAD_GATEWAY,
                        "INVALID_ML_RESPONSE",
                        "The face-analysis service returned an invalid response."
                );
            }

            return response;

        } catch (RestClientResponseException ex) {
            throw mapDownstreamError(ex.getStatusCode().value());

        } catch (ResourceAccessException ex) {
            // Includes connection failures and many transport timeouts.
            throw new ApiException(
                    HttpStatus.SERVICE_UNAVAILABLE,
                    "FACE_SERVICE_UNAVAILABLE",
                    "Face analysis is temporarily unavailable. Please try again."
            );

        } catch (RestClientException ex) {
            // Covers response conversion and other client-side failures.
            throw new ApiException(
                    HttpStatus.BAD_GATEWAY,
                    "FACE_SERVICE_ERROR",
                    "The face-analysis service returned an invalid response."
            );
        }
    }

    private ApiException mapDownstreamError(int status) {
        return switch (status) {
            case 400 -> new ApiException(
                    HttpStatus.BAD_REQUEST,
                    "INVALID_IMAGE",
                    "The uploaded file is not a valid supported image."
            );

            case 401, 403 -> new ApiException(
                    HttpStatus.BAD_GATEWAY,
                    "FACE_SERVICE_AUTH_FAILED",
                    "The face-analysis service could not authorize the request."
            );

            case 413 -> new ApiException(
                    HttpStatus.PAYLOAD_TOO_LARGE,
                    "FILE_TOO_LARGE",
                    "The uploaded image exceeds the permitted size."
            );

            case 422 -> new ApiException(
                    HttpStatus.UNPROCESSABLE_ENTITY,
                    "FACE_NOT_DETECTED",
                    "No face could be detected. Try a clear, front-facing photo."
            );

            case 503 -> new ApiException(
                    HttpStatus.SERVICE_UNAVAILABLE,
                    "MODEL_UNAVAILABLE",
                    "Face analysis is temporarily unavailable."
            );

            default -> new ApiException(
                    HttpStatus.BAD_GATEWAY,
                    "FACE_SERVICE_ERROR",
                    "The face-analysis service could not complete the request."
            );
        };
    }
}