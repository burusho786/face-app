package face_app.gateway.dto;

import java.util.List;

public record StyleGuideResponse(
        String faceShape,
        String summary,
        List<String> visualClues,
        List<HairStyleOption> hairstyles,
        List<FrameOption> glasses,
        List<String> uncertaintyFactors,
        List<String> stylingTips,
        String disclaimer
) {
    public record HairStyleOption(
            String name,
            String explanation,
            List<String> lengths,
            List<String> textures,
            String maintenanceLevel
    ) {
    }

    public record FrameOption(
            String name,
            String explanation
    ) {
    }
}