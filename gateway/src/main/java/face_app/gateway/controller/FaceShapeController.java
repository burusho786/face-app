package face_app.gateway.controller;

import face_app.gateway.dto.FaceShapePredictionResponse;
import face_app.gateway.service.FaceShapeService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1/face-shape")
public class FaceShapeController {

    private final FaceShapeService faceShapeService;

    public FaceShapeController(FaceShapeService faceShapeService) {
        this.faceShapeService = faceShapeService;
    }

    @PostMapping(
            value = "/predict",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<FaceShapePredictionResponse> predict(
            @RequestPart("file") MultipartFile file
    ) {
        FaceShapePredictionResponse response =
                faceShapeService.predict(file);

        return ResponseEntity.ok(response);
    }
}