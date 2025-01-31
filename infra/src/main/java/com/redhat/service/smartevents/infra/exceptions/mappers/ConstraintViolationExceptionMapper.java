package com.redhat.service.smartevents.infra.exceptions.mappers;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;

import javax.inject.Inject;
import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.ext.ExceptionMapper;

import org.hibernate.validator.engine.HibernateConstraintViolation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.redhat.service.smartevents.infra.api.models.responses.ErrorResponse;
import com.redhat.service.smartevents.infra.api.models.responses.ErrorsResponse;
import com.redhat.service.smartevents.infra.exceptions.BridgeError;
import com.redhat.service.smartevents.infra.exceptions.BridgeErrorService;
import com.redhat.service.smartevents.infra.exceptions.definitions.platform.UnclassifiedConstraintViolationException;
import com.redhat.service.smartevents.infra.exceptions.definitions.user.ExternalUserException;
import com.redhat.service.smartevents.infra.models.ListResult;

public class ConstraintViolationExceptionMapper implements ExceptionMapper<ConstraintViolationException> {

    private static final Logger LOGGER = LoggerFactory.getLogger(ConstraintViolationExceptionMapper.class);

    private final ErrorResponseConverter converter = new ErrorResponseConverter();

    @Inject
    BridgeErrorService bridgeErrorService;

    @Override
    public Response toResponse(ConstraintViolationException e) {
        LOGGER.debug(String.format("ConstraintViolationException: %s", e.getMessage()), e);

        ResponseBuilder builder = Response.status(Response.Status.BAD_REQUEST.getStatusCode());
        List<ConstraintViolation<?>> violations = new ArrayList<>(e.getConstraintViolations());
        if (violations.size() == 1) {
            ErrorResponse response = converter.apply(violations.get(0));
            return builder.entity(response).build();
        }

        ErrorsResponse response = new ErrorsResponse();
        ErrorsResponse.fill(new ListResult<>(violations), response, converter);
        return builder.entity(response).build();
    }

    private class ErrorResponseConverter implements Function<ConstraintViolation<?>, ErrorResponse> {

        @Override
        public ErrorResponse apply(ConstraintViolation<?> cv) {
            if (!(cv instanceof HibernateConstraintViolation)) {
                return unmappedConstraintViolation(cv);
            }

            ExternalUserException eue = ((HibernateConstraintViolation<?>) cv).getDynamicPayload(ExternalUserException.class);
            if (Objects.isNull(eue)) {
                return unmappedConstraintViolation(cv);
            }

            Optional<BridgeError> error = bridgeErrorService.getError(eue.getClass());
            if (error.isEmpty()) {
                return unmappedConstraintViolation(cv);
            }

            ErrorResponse errorResponse = ErrorResponse.from(error.get());
            errorResponse.setReason(eue.getMessage());
            return errorResponse;
        }

        private ErrorResponse unmappedConstraintViolation(ConstraintViolation<?> cv) {
            Optional<BridgeError> error = bridgeErrorService.getError(UnclassifiedConstraintViolationException.class);
            if (error.isEmpty()) {
                throw new IllegalStateException("Something seriously wrong has happened!");
            }
            ErrorResponse errorResponse = ErrorResponse.from(error.get());
            errorResponse.setReason(cv.getMessage());
            return errorResponse;
        }

    }

}
