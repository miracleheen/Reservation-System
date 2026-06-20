package sverdlov.dev.reservation_system.web;

import java.time.LocalDateTime;

public record ErrorResponsesDto(
        String message,
        String detailedMessage,
        LocalDateTime errorTime
) {
}
