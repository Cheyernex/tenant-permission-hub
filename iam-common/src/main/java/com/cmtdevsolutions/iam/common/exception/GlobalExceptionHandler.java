package com.cmtdevsolutions.iam.common.exception;

import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleResourceNotFound(ResourceNotFoundException ex, WebRequest request) {
        log.warn("Recurso no encontrado: {}", ex.getMessage());
        return buildErrorResponse(HttpStatus.NOT_FOUND, "NOT_FOUND", ex.getMessage(), request);
    }

    @ExceptionHandler(TenantIsolationException.class)
    public ResponseEntity<ErrorResponse> handleTenantIsolation(TenantIsolationException ex, WebRequest request) {
        log.warn("Violación de aislamiento de tenant: {}", ex.getMessage());
        return buildErrorResponse(HttpStatus.FORBIDDEN, "TENANT_ISOLATION_VIOLATION", ex.getMessage(), request);
    }

    @ExceptionHandler(PrivilegeEscalationException.class)
    public ResponseEntity<ErrorResponse> handlePrivilegeEscalation(PrivilegeEscalationException ex, WebRequest request) {
        log.warn("Intento de escalación de privilegios: {}", ex.getMessage());
        return buildErrorResponse(HttpStatus.FORBIDDEN, "PRIVILEGE_ESCALATION", ex.getMessage(), request);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgument(IllegalArgumentException ex, WebRequest request) {
        log.warn("Argumento inválido: {}", ex.getMessage());
        return buildErrorResponse(HttpStatus.BAD_REQUEST, "INVALID_ARGUMENT", ex.getMessage(), request);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException ex, WebRequest request) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(error ->
                errors.put(error.getField(), error.getDefaultMessage()));
        
        String message = "Validación fallida: " + errors;
        log.warn("Error de validación: {}", message);
        
        ErrorResponse response = ErrorResponse.builder()
                .timestamp(Instant.now())
                .status(HttpStatus.BAD_REQUEST.value())
                .error("VALIDATION_ERROR")
                .message(message)
                .path(request.getDescription(false).replace("uri=", ""))
                .build();
        return ResponseEntity.badRequest().body(response);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponse> handleConstraintViolation(ConstraintViolationException ex, WebRequest request) {
        String message = ex.getConstraintViolations().stream()
                .map(v -> v.getPropertyPath() + ": " + v.getMessage())
                .reduce((a, b) -> a + "; " + b)
                .orElse("Violación de constraint");
        log.warn("Violación de constraint: {}", message);
        return buildErrorResponse(HttpStatus.BAD_REQUEST, "CONSTRAINT_VIOLATION", message, request);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorResponse> handleDataIntegrity(DataIntegrityViolationException ex, WebRequest request) {
        String message = "Error de integridad de datos";
        if (ex.getCause() != null && ex.getCause().getMessage() != null) {
            String cause = ex.getCause().getMessage();
            if (cause.contains("uk_usuario_tenant_email")) {
                message = "Ya existe un usuario con ese email en este tenant";
            } else if (cause.contains("uk_plantilla_tenant_nombre")) {
                message = "Ya existe una plantilla con ese nombre en este tenant";
            } else if (cause.contains("uk_modulo_nombre")) {
                message = "Ya existe un módulo con ese nombre";
            } else if (cause.contains("uk_submodulo_modulo_nombre")) {
                message = "Ya existe un submódulo con ese nombre en este módulo";
            } else if (cause.contains("uk_accion_submodulo_nombre")) {
                message = "Ya existe una acción con ese nombre en este submódulo";
            } else if (cause.contains("uk_permiso_submodulo_accion")) {
                message = "Ya existe ese permiso (submódulo + acción)";
            } else if (cause.contains("tenant_id mismatch")) {
                message = "Usuario y plantilla deben pertenecer al mismo tenant";
            } else if (cause.contains("anti-escalación")) {
                message = "No puedes delegar permisos que no posees";
            }
        }
        log.warn("Error de integridad: {} - Cause: {}", message, ex.getCause() != null ? ex.getCause().getMessage() : "none");
        return buildErrorResponse(HttpStatus.CONFLICT, "DATA_INTEGRITY_VIOLATION", message, request);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleAccessDenied(AccessDeniedException ex, WebRequest request) {
        log.warn("Acceso denegado: {}", ex.getMessage());
        return buildErrorResponse(HttpStatus.FORBIDDEN, "ACCESS_DENIED", "No tienes permisos para realizar esta acción", request);
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ErrorResponse> handleAuthentication(AuthenticationException ex, WebRequest request) {
        log.warn("Error de autenticación: {}", ex.getMessage());
        return buildErrorResponse(HttpStatus.UNAUTHORIZED, "UNAUTHORIZED", "Token inválido o expirado", request);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneric(Exception ex, WebRequest request) {
        log.error("Error interno no manejado", ex);
        return buildErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, "INTERNAL_ERROR", "Error interno del servidor", request);
    }

    private ResponseEntity<ErrorResponse> buildErrorResponse(HttpStatus status, String error, String message, WebRequest request) {
        ErrorResponse response = ErrorResponse.builder()
                .timestamp(Instant.now())
                .status(status.value())
                .error(error)
                .message(message)
                .path(request.getDescription(false).replace("uri=", ""))
                .build();
        return ResponseEntity.status(status).body(response);
    }

    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class ErrorResponse {
        private Instant timestamp;
        private int status;
        private String error;
        private String message;
        private String path;
    }
}