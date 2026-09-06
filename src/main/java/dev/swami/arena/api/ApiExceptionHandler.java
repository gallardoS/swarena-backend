package dev.swami.arena.api;

import dev.swami.arena.account.AccountAlreadyExistsException;
import dev.swami.arena.account.RegistrationDisabledException;
import dev.swami.arena.leaderboard.InvalidLeaderboardRequestException;
import dev.swami.arena.turnstile.TurnstileVerificationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.LinkedHashMap;
import java.util.Map;

@RestControllerAdvice
public class ApiExceptionHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(ApiExceptionHandler.class);

    @ExceptionHandler(AccountAlreadyExistsException.class)
    ProblemDetail handleAccountAlreadyExists(AccountAlreadyExistsException exception) {
        return problem(HttpStatus.CONFLICT, "Account already exists", exception.getMessage(), "ACCOUNT_ALREADY_EXISTS");
    }

    @ExceptionHandler(RegistrationDisabledException.class)
    ProblemDetail handleRegistrationDisabled(RegistrationDisabledException exception) {
        return problem(HttpStatus.SERVICE_UNAVAILABLE, "Registration disabled", exception.getMessage(), "REGISTRATION_DISABLED");
    }

    @ExceptionHandler(TurnstileVerificationException.class)
    ProblemDetail handleTurnstileVerification() {
        return problem(
                HttpStatus.FORBIDDEN,
                "Verification failed",
                "The human verification could not be completed",
                "TURNSTILE_VERIFICATION_FAILED"
        );
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    ProblemDetail handleValidation(MethodArgumentNotValidException exception) {
        ProblemDetail problem = problem(
                HttpStatus.BAD_REQUEST,
                "Invalid registration data",
                "One or more fields are invalid",
                "VALIDATION_ERROR"
        );

        Map<String, String> errors = new LinkedHashMap<>();
        exception.getBindingResult().getFieldErrors().forEach(error ->
                errors.putIfAbsent(error.getField(), error.getDefaultMessage())
        );
        problem.setProperty("errors", errors);
        return problem;
    }

    @ExceptionHandler(InvalidLeaderboardRequestException.class)
    ProblemDetail handleInvalidLeaderboardRequest(InvalidLeaderboardRequestException exception) {
        return problem(
                HttpStatus.BAD_REQUEST,
                "Invalid leaderboard request",
                exception.getMessage(),
                "INVALID_LEADERBOARD_REQUEST"
        );
    }

    @ExceptionHandler(DataAccessException.class)
    ProblemDetail handleDatabaseUnavailable(DataAccessException exception) {
        LOGGER.error("Database operation failed", exception);
        return problem(
                HttpStatus.SERVICE_UNAVAILABLE,
                "Service temporarily unavailable",
                "The requested operation could not be completed right now",
                "DATABASE_UNAVAILABLE"
        );
    }

    private static ProblemDetail problem(HttpStatus status, String title, String detail, String code) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(status, detail);
        problem.setTitle(title);
        problem.setProperty("code", code);
        return problem;
    }
}
