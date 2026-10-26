package face_app.gateway.service;

import face_app.gateway.dto.StyleGuideResponse;
import face_app.gateway.dto.StyleGuideResponse.FrameOption;
import face_app.gateway.dto.StyleGuideResponse.HairStyleOption;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Locale;
import java.util.Map;

@Service
public class StyleGuideService {

    private static final List<String> UNCERTAINTY_FACTORS = List.of(
            "Camera angle and perspective",
            "Lighting and image framing",
            "Hair covering the forehead, cheeks or jaw",
            "Head position and image quality"
    );

    private static final String DISCLAIMER =
            "These are optional styling ideas, not fixed rules. " +
                    "Face-shape predictions can be uncertain, and personal " +
                    "preference matters more than following a category.";

    private final Map<String, StyleGuideResponse> guides = Map.of(
            "heart",
            guide(
                    "Heart",
                    "A heart-shaped outline may have a wider forehead " +
                            "and a narrower or tapered chin.",
                    List.of(
                            "The forehead may appear wider than the jaw.",
                            "The chin may look narrower or more tapered.",
                            "The visible outline varies with hairstyle and perspective."
                    ),
                    List.of(
                            hair("Side-swept fringe",
                                    "An option for changing the line across the forehead.",
                                    List.of("short", "medium"),
                                    List.of("straight", "wavy"), "low"),
                            hair("Soft shoulder-length layers",
                                    "Layers can add movement around the lower part of the face.",
                                    List.of("medium", "long"),
                                    List.of("straight", "wavy", "curly", "coily"),
                                    "medium")
                    ),
                    List.of(
                            frame("Oval frames",
                                    "A softer outline to compare with angular frames."),
                            frame("Lightweight frames",
                                    "A subtle option if you prefer a less prominent frame.")
                    ),
                    List.of(
                            "Compare fringe styles before committing to a cut.",
                            "Consider your hair texture and daily styling time.",
                            "Ask your stylist to adapt a reference photo to your hair."
                    )
            ),

            "oval",
            guide(
                    "Oval",
                    "An oval outline may look somewhat longer than it is wide, " +
                            "with a gently rounded jaw.",
                    List.of(
                            "The face may appear longer than it is wide.",
                            "The jawline may appear softly rounded.",
                            "Individual proportions vary considerably."
                    ),
                    List.of(
                            hair("Textured bob",
                                    "A shorter option with movement and flexible styling.",
                                    List.of("short", "medium"),
                                    List.of("straight", "wavy", "curly"), "low"),
                            hair("Long blended layers",
                                    "Layers can add movement without requiring a major length change.",
                                    List.of("medium", "long"),
                                    List.of("straight", "wavy", "curly", "coily"),
                                    "medium")
                    ),
                    List.of(
                            frame("Rectangular frames",
                                    "A structured shape to compare with curved frames."),
                            frame("Rounded frames",
                                    "A softer alternative if you prefer curved outlines.")
                    ),
                    List.of(
                            "Try several styles instead of assuming one style is best.",
                            "Use comfort, personal taste and hair texture as deciding factors."
                    )
            ),

            "oblong",
            guide(
                    "Oblong",
                    "An oblong outline may appear noticeably longer than it is wide.",
                    List.of(
                            "The face may appear longer than its width.",
                            "The sides may look relatively straight in some photos.",
                            "Perspective can exaggerate apparent face length."
                    ),
                    List.of(
                            hair("Fringe or curtain bangs",
                                    "A fringe changes the visual lines around the forehead.",
                                    List.of("short", "medium", "long"),
                                    List.of("straight", "wavy"), "medium"),
                            hair("Layered waves",
                                    "Layers and waves offer movement and styling variety.",
                                    List.of("medium", "long"),
                                    List.of("wavy", "curly"), "medium")
                    ),
                    List.of(
                            frame("Deeper frames",
                                    "A taller lens is an option worth trying in person."),
                            frame("Wider frames",
                                    "Compare wider frame designs for fit and comfort.")
                    ),
                    List.of(
                            "Try different fringe lengths before making a major change.",
                            "Check frame fit and lens height on your actual face."
                    )
            ),

            "square",
            guide(
                    "Square",
                    "A square outline may have a relatively broad jaw and " +
                            "similar-looking widths around the forehead and jaw.",
                    List.of(
                            "The jaw may look broader or more angular.",
                            "The forehead and jaw may appear similar in width.",
                            "Soft tissue, expression and camera position affect appearance."
                    ),
                    List.of(
                            hair("Soft layered bob",
                                    "Soft layers provide an alternative to a blunt outline.",
                                    List.of("short", "medium"),
                                    List.of("straight", "wavy", "curly"), "medium"),
                            hair("Long flowing layers",
                                    "A flexible option for adding movement at different lengths.",
                                    List.of("medium", "long"),
                                    List.of("straight", "wavy", "curly", "coily"),
                                    "medium")
                    ),
                    List.of(
                            frame("Oval frames",
                                    "Curved lines offer a contrast to more angular frames."),
                            frame("Round frames",
                                    "A rounded alternative to try alongside rectangular shapes.")
                    ),
                    List.of(
                            "Compare blunt and layered cuts with your stylist.",
                            "Choose frames for fit and comfort as well as appearance."
                    )
            ),

            "round",
            guide(
                    "Round",
                    "A round outline may have fuller-looking cheeks and a " +
                            "softly curved jaw.",
                    List.of(
                            "The cheeks may look like one of the widest areas.",
                            "The jaw may have a softer, curved outline.",
                            "A photo angle can change the apparent width-to-length ratio."
                    ),
                    List.of(
                            hair("Long layers with a side part",
                                    "An option for creating a different direction and movement.",
                                    List.of("medium", "long"),
                                    List.of("straight", "wavy", "curly"), "low"),
                            hair("Textured pixie",
                                    "A short cut with flexible texture and styling variations.",
                                    List.of("short"),
                                    List.of("straight", "wavy", "curly", "coily"),
                                    "medium")
                    ),
                    List.of(
                            frame("Angular frames",
                                    "A structured alternative to compare with rounded designs."),
                            frame("Rectangular frames",
                                    "A classic shape worth trying for fit and proportions.")
                    ),
                    List.of(
                            "Try different partings and lengths before deciding.",
                            "Use your comfort and preferred style as the final guide."
                    )
            )
    );

    public StyleGuideResponse getGuide(String shape) {
        if (shape == null || shape.isBlank()) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Face-shape guide not found"
            );
        }

        String slug = shape.trim().toLowerCase(Locale.ROOT);
        StyleGuideResponse response = guides.get(slug);

        if (response == null) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "No guide exists for the requested face shape"
            );
        }

        return response;
    }

    private static StyleGuideResponse guide(
            String faceShape,
            String summary,
            List<String> visualClues,
            List<HairStyleOption> hairstyles,
            List<FrameOption> glasses,
            List<String> stylingTips
    ) {
        return new StyleGuideResponse(
                faceShape,
                summary,
                visualClues,
                hairstyles,
                glasses,
                UNCERTAINTY_FACTORS,
                stylingTips,
                DISCLAIMER
        );
    }

    private static HairStyleOption hair(
            String name,
            String explanation,
            List<String> lengths,
            List<String> textures,
            String maintenanceLevel
    ) {
        return new HairStyleOption(
                name,
                explanation,
                lengths,
                textures,
                maintenanceLevel
        );
    }

    private static FrameOption frame(
            String name,
            String explanation
    ) {
        return new FrameOption(name, explanation);
    }
}