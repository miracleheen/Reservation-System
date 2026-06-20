package sverdlov.dev.reservation_system.web;

import jakarta.persistence.EntityNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import java.time.LocalDateTime;


@ControllerAdvice
public class GlobalExceptionHandler {
    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponsesDto> handlerGenericException(
            Exception ex
    ) {
        log.error("Handle exception: ", ex);

        var errorDto = new ErrorResponsesDto(
                "Internal Server Error",
                ex.getMessage(),
                LocalDateTime.now()
        );

        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(errorDto);
    }

    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<ErrorResponsesDto> handlerEntityNotFoundException(
            EntityNotFoundException ex
    ) {
        log.error("Handle entity not found exception: ", ex);

        var errorDto = new ErrorResponsesDto(
                "Entity not found",
                ex.getMessage(),
                LocalDateTime.now()
        );

        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(errorDto);
    }

    @ExceptionHandler(exception = {
            IllegalArgumentException.class,
            IllegalStateException.class,
            MethodArgumentNotValidException.class,
    })
    public ResponseEntity<ErrorResponsesDto> handlerBadRequestException(
            Exception ex
    ) {
        log.error("Handle handle bad request: ", ex);

        var errorDto = new ErrorResponsesDto(
                "Bad request",
                ex.getMessage(),
                LocalDateTime.now()
        );

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(errorDto);

    }
}
