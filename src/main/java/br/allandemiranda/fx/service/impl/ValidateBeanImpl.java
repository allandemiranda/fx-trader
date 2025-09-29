package br.allandemiranda.fx.service.impl;

import br.allandemiranda.fx.service.ValidateBean;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Collection;

@Slf4j
@RequiredArgsConstructor
@Getter(AccessLevel.PRIVATE)
@Transactional
@Service
public class ValidateBeanImpl implements ValidateBean {

    private final Validator validator;

    private static <T> String getDetails(@NotNull Collection<ConstraintViolation<T>> violations) {
        return violations.stream()
                .map(v -> v.getPropertyPath() + ": " + v.getMessage())
                .reduce((a, b) -> a + "; " + b)
                .orElse("unknown violation");
    }

    @Override
    public <T> void validate(@NotNull T bean, @NotNull LocalDateTime timestamp) {
        Collection<ConstraintViolation<T>> violations = this.getValidator().validate(bean);
        if (!violations.isEmpty()) {
            String name = bean.getClass().getName();
            String details = ValidateBeanImpl.getDetails(violations);
            log.error("Validation failed for {} {} at {} -> {}", name, bean, timestamp, details);
            throw new IllegalStateException("Validation failed: " + details);
        }
    }

    @Override
    public <T> boolean isValidate(@NotNull T bean, @NotNull LocalDateTime timestamp) {
        Collection<ConstraintViolation<T>> violations = this.getValidator().validate(bean);
        if (!violations.isEmpty()) {
            String name = bean.getClass().getName();
            String details = ValidateBeanImpl.getDetails(violations);
            log.warn("Validation failed for {} {} at {} -> {}", name, bean, timestamp, details);
            return false;
        } else {
            return true;
        }
    }
}
