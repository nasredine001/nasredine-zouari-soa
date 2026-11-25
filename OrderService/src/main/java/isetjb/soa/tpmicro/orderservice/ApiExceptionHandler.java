package isetjb.soa.tpmicro.orderservice;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import feign.FeignException;

@RestControllerAdvice
public class ApiExceptionHandler {

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ProblemDetail> handleBadRequest(IllegalArgumentException e) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, e.getMessage());
        return ResponseEntity.badRequest().body(problem);
    }

    @ExceptionHandler(FeignException.class)
    public ResponseEntity<ProblemDetail> handleProductServiceError(FeignException e) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_GATEWAY,
                "Le service Produits est indisponible ou a renvoyé une erreur.");
        return ResponseEntity.status(HttpStatus.BAD_GATEWAY).body(problem);
    }
}
