package face_app.gateway.controller;

import face_app.gateway.dto.StyleGuideResponse;
import face_app.gateway.service.StyleGuideService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/style-guides")
public class StyleGuideController {

    private final StyleGuideService styleGuideService;

    public StyleGuideController(StyleGuideService styleGuideService) {
        this.styleGuideService = styleGuideService;
    }

    @GetMapping("/{shape}")
    public StyleGuideResponse getGuide(@PathVariable String shape) {
        return styleGuideService.getGuide(shape);
    }
}