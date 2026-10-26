
package face_app.gateway.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public record FaceShapePredictionResponse(

        @JsonProperty("face_shape")
        String faceShape,

        double confidence,

        List<Score> scores,

        @JsonProperty("model_version")
        String modelVersion

) {

    public record Score(
            String label,
            double probability
    ) {
    }
}
