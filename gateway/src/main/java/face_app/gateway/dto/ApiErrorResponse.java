package face_app.gateway.dto;



public record ApiErrorResponse(
        String error,
        String message
) {}