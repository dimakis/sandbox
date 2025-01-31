package com.redhat.service.smartevents.infra.exceptions.mappers;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;

import org.hibernate.validator.engine.HibernateConstraintViolation;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.redhat.service.smartevents.infra.api.models.responses.BaseResponse;
import com.redhat.service.smartevents.infra.api.models.responses.ErrorResponse;
import com.redhat.service.smartevents.infra.api.models.responses.ErrorsResponse;
import com.redhat.service.smartevents.infra.exceptions.BridgeError;
import com.redhat.service.smartevents.infra.exceptions.BridgeErrorService;
import com.redhat.service.smartevents.infra.exceptions.BridgeErrorType;
import com.redhat.service.smartevents.infra.exceptions.definitions.platform.UnclassifiedConstraintViolationException;
import com.redhat.service.smartevents.infra.exceptions.definitions.user.ExternalUserException;
import com.redhat.service.smartevents.infra.exceptions.definitions.user.ItemNotFoundException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ConstraintViolationExceptionMapperTest {

    private static final BridgeError BRIDGE_ERROR = new BridgeError(1, "code", "reason", BridgeErrorType.USER);

    private static final BridgeError MAPPED_ERROR = new BridgeError(2, "mapped-code", "mapped-reason", BridgeErrorType.USER);

    @Mock
    private ConstraintViolation<?> constraintViolation;

    @Mock
    private HibernateConstraintViolation<?> hibernateConstraintViolation;

    @Mock
    private BridgeErrorService bridgeErrorService;

    private ConstraintViolationExceptionMapper mapper;

    @BeforeEach
    void setup() {
        this.mapper = new ConstraintViolationExceptionMapper();
        this.mapper.bridgeErrorService = bridgeErrorService;

        lenient().when(constraintViolation.getMessage()).thenReturn("message");
        lenient().when(hibernateConstraintViolation.getMessage()).thenReturn("message");
    }

    @Test
    void testSingleDefaultViolationUnmapped() {
        when(bridgeErrorService.getError(UnclassifiedConstraintViolationException.class)).thenReturn(Optional.of(BRIDGE_ERROR));

        ConstraintViolationException violation = new ConstraintViolationException(Set.of(constraintViolation));

        ErrorResponse response = mapper.toResponse(violation).readEntity(ErrorResponse.class);

        assertThat(response.getId()).isEqualTo("1");
        assertThat(response.getCode()).isEqualTo("code");
        assertThat(response.getReason()).isEqualTo("message");
    }

    @Test
    void testSingleHibernateViolationWithoutDynamicPayload() {
        when(bridgeErrorService.getError(UnclassifiedConstraintViolationException.class)).thenReturn(Optional.of(BRIDGE_ERROR));

        ConstraintViolationException violation = new ConstraintViolationException(Set.of(hibernateConstraintViolation));

        ErrorResponse response = mapper.toResponse(violation).readEntity(ErrorResponse.class);

        assertThat(response.getId()).isEqualTo("1");
        assertThat(response.getCode()).isEqualTo("code");
        assertThat(response.getReason()).isEqualTo("message");
    }

    @Test
    void testSingleHibernateViolationWithDynamicPayloadUnmapped() {
        when(hibernateConstraintViolation.getDynamicPayload(ExternalUserException.class)).thenReturn(new ItemNotFoundException("not-found"));
        when(bridgeErrorService.getError(ItemNotFoundException.class)).thenReturn(Optional.empty());
        when(bridgeErrorService.getError(UnclassifiedConstraintViolationException.class)).thenReturn(Optional.of(BRIDGE_ERROR));

        ConstraintViolationException violation = new ConstraintViolationException(Set.of(hibernateConstraintViolation));

        ErrorResponse response = mapper.toResponse(violation).readEntity(ErrorResponse.class);

        assertThat(response.getId()).isEqualTo("1");
        assertThat(response.getCode()).isEqualTo("code");
        assertThat(response.getReason()).isEqualTo("message");
    }

    @Test
    void testSingleHibernateViolationWithDynamicPayloadMapped() {
        when(hibernateConstraintViolation.getDynamicPayload(ExternalUserException.class)).thenReturn(new ItemNotFoundException("not-found"));
        when(bridgeErrorService.getError(ItemNotFoundException.class)).thenReturn(Optional.of(MAPPED_ERROR));

        ConstraintViolationException violation = new ConstraintViolationException(Set.of(hibernateConstraintViolation));

        ErrorResponse response = mapper.toResponse(violation).readEntity(ErrorResponse.class);

        assertThat(response.getId()).isEqualTo("2");
        assertThat(response.getCode()).isEqualTo("mapped-code");
        assertThat(response.getReason()).isEqualTo("not-found");
    }

    @Test
    void testMultipleAllScenarios() {
        when(hibernateConstraintViolation.getDynamicPayload(ExternalUserException.class)).thenReturn(new ItemNotFoundException("not-found"));
        when(bridgeErrorService.getError(ItemNotFoundException.class)).thenReturn(Optional.of(MAPPED_ERROR));
        when(bridgeErrorService.getError(UnclassifiedConstraintViolationException.class)).thenReturn(Optional.of(BRIDGE_ERROR));

        ConstraintViolationException violation = new ConstraintViolationException(Set.of(constraintViolation, hibernateConstraintViolation));

        ErrorsResponse response = mapper.toResponse(violation).readEntity(ErrorsResponse.class);
        List<ErrorResponse> errors = response.getItems();
        assertThat(errors).hasSize(2);

        // Sort responses as ConstraintViolationException's use of Set does not guarantee ordering
        List<ErrorResponse> sortedErrors = errors.stream().sorted(Comparator.comparing(BaseResponse::getId)).collect(Collectors.toList());

        ErrorResponse error1 = sortedErrors.get(0);
        assertThat(error1.getId()).isEqualTo("1");
        assertThat(error1.getCode()).isEqualTo("code");
        assertThat(error1.getReason()).isEqualTo("message");

        ErrorResponse error2 = sortedErrors.get(1);
        assertThat(error2.getId()).isEqualTo("2");
        assertThat(error2.getCode()).isEqualTo("mapped-code");
        assertThat(error2.getReason()).isEqualTo("not-found");
    }

}
