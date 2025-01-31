package com.redhat.service.smartevents.processor.validators;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import javax.inject.Inject;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import com.redhat.service.smartevents.processor.resolvers.AbstractGatewayValidatorTest;
import com.redhat.service.smartevents.processor.sources.aws.AwsSqsSource;
import com.redhat.service.smartevents.processor.validators.custom.AwsSqsSourceValidator;

import io.quarkus.test.junit.QuarkusTest;

import static com.redhat.service.smartevents.processor.sources.aws.AwsSqsSource.AWS_ACCESS_KEY_ID_PARAM;
import static com.redhat.service.smartevents.processor.sources.aws.AwsSqsSource.AWS_QUEUE_URL_PARAM;
import static com.redhat.service.smartevents.processor.sources.aws.AwsSqsSource.AWS_REGION_PARAM;
import static com.redhat.service.smartevents.processor.sources.aws.AwsSqsSource.AWS_SECRET_ACCESS_KEY_PARAM;

@QuarkusTest
class AwsSqsSourceValidatorTest extends AbstractGatewayValidatorTest {

    @Inject
    AwsSqsSourceValidator validator;

    @Override
    protected GatewayValidator getValidator() {
        return validator;
    }

    static final String INVALID_QUEUE_URL = "invalid";

    static final String VALID_QUEUE_NAME = "test_queue";
    static final String VALID_AWS_QUEUE_URL = "https://sqs.us-east-1.amazonaws.com/123456789012/" + VALID_QUEUE_NAME;
    static final String VALID_GENERIC_ENDPOINT = "http://localstack.local:4566";
    static final String VALID_GENERIC_QUEUE_URL = VALID_GENERIC_ENDPOINT + "/000000000000/" + VALID_QUEUE_NAME;
    static final String INVALID_AWS_REGION = "123";
    static final String VALID_AWS_REGION = "us-east-1";
    static final String VALID_AWS_ACCESS_KEY_ID = "key";
    static final String VALID_AWS_SECRET_ACCESS_KEY = "secret";

    private static final Object[][] INVALID_PARAMS = {
            { paramMap(null, null, null, null),
                    List.of("$.aws_queue_name_or_arn: is missing but it is required",
                            "$.aws_region: is missing but it is required",
                            "$.aws_access_key: is missing but it is required",
                            "$.aws_secret_key: is missing but it is required") },
            { paramMap("", null, null, null),
                    List.of("$.aws_region: is missing but it is required",
                            "$.aws_access_key: is missing but it is required",
                            "$.aws_secret_key: is missing but it is required") },
            { paramMap(VALID_AWS_QUEUE_URL, null, null, null),
                    List.of("$.aws_region: is missing but it is required",
                            "$.aws_access_key: is missing but it is required",
                            "$.aws_secret_key: is missing but it is required") },
            { paramMap(VALID_AWS_QUEUE_URL, "", null, null),
                    List.of("$.aws_access_key: is missing but it is required",
                            "$.aws_secret_key: is missing but it is required",
                            "$.aws_region: does not have a value in the enumeration [af-south-1, ap-east-1, ap-northeast-1, ap-northeast-2, ap-northeast-3, ap-south-1, ap-southeast-1, ap-southeast-2, ap-southeast-3, ca-central-1, eu-central-1, eu-north-1, eu-south-1, eu-west-1, eu-west-2, eu-west-3, fips-us-east-1, fips-us-east-2, fips-us-west-1, fips-us-west-2, me-south-1, sa-east-1, us-east-1, us-east-2, us-west-1, us-west-2, cn-north-1, cn-northwest-1, us-gov-east-1, us-gov-west-1, us-iso-east-1, us-iso-west-1, us-isob-east-1]") },
            { paramMap(VALID_AWS_QUEUE_URL, VALID_AWS_REGION, null, null),
                    List.of("$.aws_access_key: is missing but it is required",
                            "$.aws_secret_key: is missing but it is required") },
            { paramMap(VALID_AWS_QUEUE_URL, VALID_AWS_REGION, "", null),
                    List.of("$.aws_secret_key: is missing but it is required") },
            { paramMap(VALID_AWS_QUEUE_URL, VALID_AWS_REGION, VALID_AWS_ACCESS_KEY_ID, null),
                    List.of("$.aws_secret_key: is missing but it is required") },
            { paramMap(INVALID_QUEUE_URL, VALID_AWS_REGION, VALID_AWS_ACCESS_KEY_ID, VALID_AWS_SECRET_ACCESS_KEY),
                    List.of("Malformed \"aws_queue_name_or_arn\" url: \"invalid\"") },
            { paramMap(VALID_AWS_QUEUE_URL, "", VALID_AWS_ACCESS_KEY_ID, VALID_AWS_SECRET_ACCESS_KEY),
                    List.of("$.aws_region: does not have a value in the enumeration [af-south-1, ap-east-1, ap-northeast-1, ap-northeast-2, ap-northeast-3, ap-south-1, ap-southeast-1, ap-southeast-2, ap-southeast-3, ca-central-1, eu-central-1, eu-north-1, eu-south-1, eu-west-1, eu-west-2, eu-west-3, fips-us-east-1, fips-us-east-2, fips-us-west-1, fips-us-west-2, me-south-1, sa-east-1, us-east-1, us-east-2, us-west-1, us-west-2, cn-north-1, cn-northwest-1, us-gov-east-1, us-gov-west-1, us-iso-east-1, us-iso-west-1, us-isob-east-1]") },
            { paramMap(VALID_AWS_QUEUE_URL, INVALID_AWS_REGION, VALID_AWS_ACCESS_KEY_ID, VALID_AWS_SECRET_ACCESS_KEY),
                    List.of("$.aws_region: does not have a value in the enumeration [af-south-1, ap-east-1, ap-northeast-1, ap-northeast-2, ap-northeast-3, ap-south-1, ap-southeast-1, ap-southeast-2, ap-southeast-3, ca-central-1, eu-central-1, eu-north-1, eu-south-1, eu-west-1, eu-west-2, eu-west-3, fips-us-east-1, fips-us-east-2, fips-us-west-1, fips-us-west-2, me-south-1, sa-east-1, us-east-1, us-east-2, us-west-1, us-west-2, cn-north-1, cn-northwest-1, us-gov-east-1, us-gov-west-1, us-iso-east-1, us-iso-west-1, us-isob-east-1]") }
    };

    private static final Object[] VALID_PARAMS = {
            paramMap(VALID_AWS_QUEUE_URL, VALID_AWS_REGION, VALID_AWS_ACCESS_KEY_ID, VALID_AWS_SECRET_ACCESS_KEY),
            paramMap(VALID_GENERIC_QUEUE_URL, VALID_AWS_REGION, VALID_AWS_ACCESS_KEY_ID, VALID_AWS_SECRET_ACCESS_KEY)
    };

    @ParameterizedTest
    @MethodSource("validParams")
    void isValid(Map<String, String> validParams) {
        assertValidationIsValid(sourceWith(AwsSqsSource.TYPE, validParams));
    }

    @ParameterizedTest
    @MethodSource("invalidParams")
    void isInvalid(Map<String, String> invalidParams, List<String> expectedErrorMessages) {
        assertValidationIsInvalid(sourceWith(AwsSqsSource.TYPE, invalidParams), expectedErrorMessages);
    }

    private static Stream<Arguments> invalidParams() {
        return Arrays.stream(INVALID_PARAMS).map(Arguments::of);
    }

    private static Stream<Arguments> validParams() {
        return Arrays.stream(VALID_PARAMS).map(Arguments::of);
    }

    static Map<String, String> paramMap(String queueUrl, String awsRegion, String awsAccessKeyId, String awsSecretAccessKey) {
        Map<String, String> params = new HashMap<>();
        if (queueUrl != null) {
            params.put(AWS_QUEUE_URL_PARAM, queueUrl);
        }
        if (awsRegion != null) {
            params.put(AWS_REGION_PARAM, awsRegion);
        }
        if (awsAccessKeyId != null) {
            params.put(AWS_ACCESS_KEY_ID_PARAM, awsAccessKeyId);
        }
        if (awsSecretAccessKey != null) {
            params.put(AWS_SECRET_ACCESS_KEY_PARAM, awsSecretAccessKey);
        }
        return params;
    }
}
