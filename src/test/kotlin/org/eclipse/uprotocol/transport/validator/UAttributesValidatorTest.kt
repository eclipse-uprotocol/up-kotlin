/*
 * Copyright (c) 2024 General Motors GTO LLC
 *
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.eclipse.uprotocol.transport.validator

import org.eclipse.uprotocol.transport.builder.UAttributesBuilder
import org.eclipse.uprotocol.transport.validate.UAttributesValidator
import org.eclipse.uprotocol.uri.factory.UResourceBuilder.forRpcResponse
import org.eclipse.uprotocol.uri.serializer.LongUriSerializer
import org.eclipse.uprotocol.uuid.factory.UuidFactory
import org.eclipse.uprotocol.v1.*
import org.eclipse.uprotocol.validation.ValidationResult
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.eclipse.uprotocol.v1.UPriority
import org.eclipse.uprotocol.v1.UAttributes


internal class UAttributesValidatorTest {
    @Test
    @DisplayName("test fetching validator for valid types")
    fun test_fetching_validator_for_valid_types() {
        val publish: UAttributesValidator = UAttributesValidator.getValidator(
            UAttributesBuilder.publish(source, UPriority.UPRIORITY_CS0).build()
        )
        assertEquals("UAttributesValidator.Publish", publish.toString())
        val request: UAttributesValidator = UAttributesValidator.getValidator(
            UAttributesBuilder.request(source, sink, UPriority.UPRIORITY_CS4, 1000).build()
        )
        assertEquals("UAttributesValidator.Request", request.toString())
        val response: UAttributesValidator = UAttributesValidator.getValidator(
            UAttributesBuilder.response(
                source, sink,
                UPriority.UPRIORITY_CS4, UuidFactory.Factories.UPROTOCOL.factory().create()
            ).build()
        )
        assertEquals("UAttributesValidator.Response", response.toString())
    }

    @Test
    @DisplayName("Validate a UAttributes for payload that is meant to be published")
    fun test_validate_uAttributes_for_publish_message_payload() {
        val attributes: UAttributes = UAttributesBuilder.publish(source, UPriority.UPRIORITY_CS0).build()
        val validator: UAttributesValidator = UAttributesValidator.Validators.PUBLISH.validator()
        val status: ValidationResult = validator.validate(attributes)
        assertTrue(status.isSuccess())
        assertEquals("", status.getMessage())
    }

    @Test
    @DisplayName("Validate a UAttributes for payload that is meant to be published with all values")
    fun test_validate_uAttributes_for_publish_message_payload_all_values() {
        val attributes: UAttributes =
            UAttributesBuilder.publish(source, UPriority.UPRIORITY_CS0).withTtl(1000).withSink(sink)
                .withPermissionLevel(2).withCommStatus(3).withReqId(UuidFactory.Factories.UPROTOCOL.factory().create())
                .build()
        val validator: UAttributesValidator = UAttributesValidator.Validators.PUBLISH.validator()
        val status: ValidationResult = validator.validate(attributes)
        assertTrue(status.isSuccess())
        assertEquals("", status.getMessage())
    }

    @Test
    @DisplayName("Validate a UAttributes for payload that is meant to be published with invalid type")
    fun test_validate_uAttributes_for_publish_message_payload_invalid_type() {
        val attributes: UAttributes = UAttributesBuilder.response(
            source, sink,
            UPriority.UPRIORITY_CS0, UuidFactory.Factories.UPROTOCOL.factory().create()
        ).build()
        val validator: UAttributesValidator = UAttributesValidator.Validators.PUBLISH.validator()
        val status: ValidationResult = validator.validate(attributes)
        assertTrue(status.isFailure())
        assertEquals("Wrong Attribute Type [UMESSAGE_TYPE_RESPONSE]", status.getMessage())
    }

    @Test
    @DisplayName("Validate a UAttributes for payload that is meant to be published with invalid time to live")
    fun test_validate_uAttributes_for_publish_message_payload_invalid_ttl() {
        val attributes: UAttributes =
            UAttributesBuilder.publish(source, UPriority.UPRIORITY_CS0).withTtl(-1).build()
        val validator: UAttributesValidator = UAttributesValidator.Validators.PUBLISH.validator()
        val status: ValidationResult = validator.validate(attributes)
        assertTrue(status.isFailure())
        assertEquals("Invalid TTL [-1]", status.getMessage())
    }

    @Test
    @DisplayName("Validate a UAttributes for payload that is meant to be published with invalid sink")
    fun test_validate_uAttributes_for_publish_message_payload_invalid_sink() {
        val attributes: UAttributes =
            UAttributesBuilder.publish(source, UPriority.UPRIORITY_CS0).withSink(UUri.getDefaultInstance())
                .build()
        val validator: UAttributesValidator = UAttributesValidator.Validators.PUBLISH.validator()
        val status: ValidationResult = validator.validate(attributes)
        assertTrue(status.isFailure())
        assertEquals("Uri is empty.", status.getMessage())
    }

    @Test
    @DisplayName("Validate a UAttributes for payload that is meant to be published with invalid permission level")
    fun test_validate_uAttributes_for_publish_message_payload_invalid_permission_level() {
        val attributes: UAttributes =
            UAttributesBuilder.publish(source, UPriority.UPRIORITY_CS0).withPermissionLevel(-42).build()
        val validator: UAttributesValidator = UAttributesValidator.Validators.PUBLISH.validator()
        val status: ValidationResult = validator.validate(attributes)
        assertTrue(status.isFailure())
        assertEquals("Invalid Permission Level", status.getMessage())
    }

    @Test
    @DisplayName("Validate a UAttributes for payload that is meant to be published with invalid communication status")
    fun test_validate_uAttributes_for_publish_message_payload_invalid_communication_status() {
        val attributes: UAttributes =
            UAttributesBuilder.publish(source, UPriority.UPRIORITY_CS0).withCommStatus(-42).build()
        val validator: UAttributesValidator = UAttributesValidator.Validators.PUBLISH.validator()
        val status: ValidationResult = validator.validate(attributes)
        assertTrue(status.isFailure())
        assertEquals("Invalid Communication Status Code", status.getMessage())
    }

    @Test
    @DisplayName("Validate a UAttributes for payload that is meant to be published with invalid request id")
    fun test_validate_uAttributes_for_publish_message_payload_invalid_request_id() {
        val uuidJava: java.util.UUID = java.util.UUID.randomUUID()
        val attributes: UAttributes =
            UAttributesBuilder.publish(source, UPriority.UPRIORITY_CS0).withReqId(uUID {
                msb = uuidJava.mostSignificantBits
                lsb = uuidJava.leastSignificantBits
            }).build()
        val validator: UAttributesValidator = UAttributesValidator.Validators.PUBLISH.validator()
        val status: ValidationResult = validator.validate(attributes)
        assertTrue(status.isFailure())
        assertEquals("Invalid UUID", status.getMessage())
    }

    // ----
    @Test
    @DisplayName("Validate a UAttributes for payload that is meant to be an RPC request")
    fun test_validate_uAttributes_for_rpc_request_message_payload() {
        val attributes: UAttributes =
            UAttributesBuilder.request(source, sink, UPriority.UPRIORITY_CS4, 1000).build()
        val validator: UAttributesValidator = UAttributesValidator.Validators.REQUEST.validator()
        val status: ValidationResult = validator.validate(attributes)
        assertTrue(status.isSuccess())
        assertEquals("", status.getMessage())
    }

    @Test
    @DisplayName("Validate a UAttributes for payload that is meant to be an RPC request with all values")
    fun test_validate_uAttributes_for_rpc_request_message_payload_all_values() {
        val attributes: UAttributes =
            UAttributesBuilder.request(source, sink, UPriority.UPRIORITY_CS4, 1000).withPermissionLevel(2)
                .withCommStatus(3).withReqId(UuidFactory.Factories.UPROTOCOL.factory().create()).build()
        val validator: UAttributesValidator = UAttributesValidator.Validators.REQUEST.validator()
        val status: ValidationResult = validator.validate(attributes)
        assertTrue(status.isSuccess())
        assertEquals("", status.getMessage())
    }

    @Test
    @DisplayName("Validate a UAttributes for payload that is meant to be an RPC request with invalid type")
    fun test_validate_uAttributes_for_rpc_request_message_payload_invalid_type() {
        val attributes: UAttributes = UAttributesBuilder.response(
            source, sink,
            UPriority.UPRIORITY_CS4, UuidFactory.Factories.UPROTOCOL.factory().create()
        ).withTtl(1000).build()
        val validator: UAttributesValidator = UAttributesValidator.Validators.REQUEST.validator()
        val status: ValidationResult = validator.validate(attributes)
        assertTrue(status.isFailure())
        assertEquals("Wrong Attribute Type [UMESSAGE_TYPE_RESPONSE]", status.getMessage())
    }

    @Test
    @DisplayName("Validate a UAttributes for payload that is meant to be an RPC request with invalid time to live")
    fun test_validate_uAttributes_for_rpc_request_message_payload_invalid_ttl() {
        val attributes: UAttributes =
            UAttributesBuilder.request(source, sink, UPriority.UPRIORITY_CS4, -1).build()
        val validator: UAttributesValidator = UAttributesValidator.Validators.REQUEST.validator()
        val status: ValidationResult = validator.validate(attributes)
        assertTrue(status.isFailure())
        assertEquals("Invalid TTL [-1]", status.getMessage())
    }

    @Test
    @DisplayName("Validate a UAttributes for payload that is meant to be an RPC request with invalid sink")
    fun test_validate_uAttributes_for_rpc_request_message_payload_invalid_sink() {
        val attributes: UAttributes =
            UAttributesBuilder.request(source, UUri.getDefaultInstance(), UPriority.UPRIORITY_CS4, 1000).build()
        val validator: UAttributesValidator = UAttributesValidator.Validators.REQUEST.validator()
        val status: ValidationResult = validator.validate(attributes)
        assertTrue(status.isFailure())
        assertEquals("Uri is empty.", status.getMessage())
    }

    @Test
    @DisplayName("Validate a UAttributes for payload that is meant to be an RPC request with invalid permission level")
    fun test_validate_uAttributes_for_rpc_request_message_payload_invalid_permission_level() {
        val attributes: UAttributes =
            UAttributesBuilder.request(source, sink, UPriority.UPRIORITY_CS4, 1000)
                .withPermissionLevel(-42).build()
        val validator: UAttributesValidator = UAttributesValidator.Validators.REQUEST.validator()
        val status: ValidationResult = validator.validate(attributes)
        assertTrue(status.isFailure())
        assertEquals("Invalid Permission Level", status.getMessage())
    }

    @Test
    @DisplayName("Validate a UAttributes for payload that is meant to be an RPC request with invalid communication " + "status")
    fun test_validate_uAttributes_for_rpc_request_message_payload_invalid_communication_status() {
        val attributes: UAttributes =
            UAttributesBuilder.request(source, sink, UPriority.UPRIORITY_CS4, 1000).withCommStatus(-42)
                .build()
        val validator: UAttributesValidator = UAttributesValidator.Validators.REQUEST.validator()
        val status: ValidationResult = validator.validate(attributes)
        assertTrue(status.isFailure())
        assertEquals("Invalid Communication Status Code", status.getMessage())
    }

    @Test
    @DisplayName("Validate a UAttributes for payload that is meant to be an RPC request with invalid request id")
    fun test_validate_uAttributes_for_rpc_request_message_payload_invalid_request_id() {
        val uuidJava: java.util.UUID = java.util.UUID.randomUUID()
        val attributes: UAttributes =
            UAttributesBuilder.request(source, sink, UPriority.UPRIORITY_CS4, 1000).withReqId(uUID {
                msb = uuidJava.mostSignificantBits
                lsb = uuidJava.leastSignificantBits
            }

            ).build()
        val validator: UAttributesValidator = UAttributesValidator.Validators.REQUEST.validator()
        val status: ValidationResult = validator.validate(attributes)
        assertTrue(status.isFailure())
        assertEquals("Invalid UUID", status.getMessage())
    }

    // ----
    @Test
    @DisplayName("Validate a UAttributes for payload that is meant to be an RPC response")
    fun test_validate_uAttributes_for_rpc_response_message_payload() {
        val attributes: UAttributes = UAttributesBuilder.response(
            source, sink,
            UPriority.UPRIORITY_CS4, UuidFactory.Factories.UPROTOCOL.factory().create()
        ).build()
        val validator: UAttributesValidator = UAttributesValidator.Validators.RESPONSE.validator()
        val status: ValidationResult = validator.validate(attributes)
        assertTrue(status.isSuccess())
        assertEquals("", status.getMessage())
    }

    @Test
    @DisplayName("Validate a UAttributes for payload that is meant to be an RPC response with all values")
    fun test_validate_uAttributes_for_rpc_response_message_payload_all_values() {
        val attributes: UAttributes = UAttributesBuilder.response(
            source, sink,
            UPriority.UPRIORITY_CS4, UuidFactory.Factories.UPROTOCOL.factory().create()
        ).withPermissionLevel(2).withCommStatus(3).build()
        val validator: UAttributesValidator = UAttributesValidator.Validators.RESPONSE.validator()
        val status: ValidationResult = validator.validate(attributes)
        assertTrue(status.isSuccess())
        assertEquals("", status.getMessage())
    }

    @Test
    @DisplayName("Validate a UAttributes for payload that is meant to be an RPC response with invalid type")
    fun test_validate_uAttributes_for_rpc_response_message_payload_invalid_type() {
        val attributes: UAttributes =
            UAttributesBuilder.notification(source, sink, UPriority.UPRIORITY_CS4).build()
        val validator: UAttributesValidator = UAttributesValidator.Validators.RESPONSE.validator()
        val status: ValidationResult = validator.validate(attributes)
        assertTrue(status.isFailure())
        assertEquals("Wrong Attribute Type [UMESSAGE_TYPE_PUBLISH],Missing correlationId", status.getMessage())
    }

    @Test
    @DisplayName("Validate a UAttributes for payload that is meant to be an RPC response with invalid time to live")
    fun test_validate_uAttributes_for_rpc_response_message_payload_invalid_ttl() {
        val attributes: UAttributes = UAttributesBuilder.response(
            source, sink,
            UPriority.UPRIORITY_CS4, UuidFactory.Factories.UPROTOCOL.factory().create()
        ).withTtl(-1).build()
        val validator: UAttributesValidator = UAttributesValidator.Validators.RESPONSE.validator()
        val status: ValidationResult = validator.validate(attributes)
        assertTrue(status.isFailure())
        assertEquals("Invalid TTL [-1]", status.getMessage())
    }

    @Test
    @DisplayName(
        "Validate a UAttributes for payload that is meant to be an RPC response with missing sink and " + "missing request id"
    )
    fun test_validate_uAttributes_for_rpc_response_message_payload_missing_sink_and_missing_requestId() {
        val attributes: UAttributes =
            UAttributesBuilder.response(
                source,
                UUri.getDefaultInstance(),
                UPriority.UPRIORITY_CS4,
                UUID.getDefaultInstance()
            )
                .build()
        val validator: UAttributesValidator = UAttributesValidator.Validators.RESPONSE.validator()
        val status: ValidationResult = validator.validate(attributes)
        assertTrue(status.isFailure())
        assertEquals("Missing Sink,Missing correlationId", status.getMessage())
    }

    @Test
    @DisplayName("Validate a UAttributes for payload that is meant to be an RPC response with invalid permission level")
    fun test_validate_uAttributes_for_rpc_response_message_payload_invalid_permission_level() {
        val attributes: UAttributes = UAttributesBuilder.response(
            source, sink,
            UPriority.UPRIORITY_CS4, UuidFactory.Factories.UPROTOCOL.factory().create()
        ).withPermissionLevel(-42).build()
        val validator: UAttributesValidator = UAttributesValidator.Validators.RESPONSE.validator()
        val status: ValidationResult = validator.validate(attributes)
        assertTrue(status.isFailure())
        assertEquals("Invalid Permission Level", status.getMessage())
    }

    @Test
    @DisplayName("Validate a UAttributes for payload that is meant to be an RPC response with invalid communication " + "status")
    fun test_validate_uAttributes_for_rpc_response_message_payload_invalid_communication_status() {
        val attributes: UAttributes = UAttributesBuilder.response(
            source, sink,
            UPriority.UPRIORITY_CS4, UuidFactory.Factories.UPROTOCOL.factory().create()
        ).withCommStatus(-42).build()
        val validator: UAttributesValidator = UAttributesValidator.Validators.RESPONSE.validator()
        val status: ValidationResult = validator.validate(attributes)
        assertTrue(status.isFailure())
        assertEquals("Invalid Communication Status Code", status.getMessage())
    }

    @Test
    @DisplayName("Validate a UAttributes for payload that is meant to be an RPC response with missing request id")
    fun test_validate_uAttributes_for_rpc_response_message_payload_missing_request_id() {
        val attributes: UAttributes =
            UAttributesBuilder.response(source, sink, UPriority.UPRIORITY_CS4, UUID.getDefaultInstance())
                .build()
        val validator: UAttributesValidator = UAttributesValidator.Validators.RESPONSE.validator()
        val status: ValidationResult = validator.validate(attributes)
        assertTrue(status.isFailure())
        assertEquals("Missing correlationId", status.getMessage())
    }

    @Test
    @DisplayName("Validate a UAttributes for payload that is meant to be an RPC response with invalid request id")
    fun test_validate_uAttributes_for_rpc_response_message_payload_invalid_request_id() {
        val uuidJava: java.util.UUID = java.util.UUID.randomUUID()
        val reqid: UUID = uUID {
            msb = uuidJava.mostSignificantBits
            lsb = uuidJava.leastSignificantBits
        }
        val attributes: UAttributes =
            UAttributesBuilder.response(source, sink, UPriority.UPRIORITY_CS4, reqid).build()
        val validator: UAttributesValidator = UAttributesValidator.Validators.RESPONSE.validator()
        val status: ValidationResult = validator.validate(attributes)
        assertTrue(status.isFailure())
        assertEquals(String.format("Invalid correlationId [%s]", reqid), status.getMessage())
    }

    // ----
    @Test
    @DisplayName("Validate a UAttributes for payload that is meant to be published not expired")
    fun test_validate_uAttributes_for_publish_message_payload_not_expired() {
        val attributes: UAttributes = UAttributesBuilder.publish(source, UPriority.UPRIORITY_CS0).build()
        val validator: UAttributesValidator = UAttributesValidator.Validators.PUBLISH.validator()
        assertFalse(validator.isExpired(attributes))
    }

    @Test
    @DisplayName("Validate a UAttributes for payload that is meant to be published not expired with ttl zero")
    fun test_validate_uAttributes_for_publish_message_payload_not_expired_with_ttl_zero() {
        val attributes: UAttributes =
            UAttributesBuilder.publish(source, UPriority.UPRIORITY_CS0).withTtl(0).build()
        val validator: UAttributesValidator = UAttributesValidator.Validators.PUBLISH.validator()
        assertFalse(validator.isExpired(attributes))
    }

    @Test
    @DisplayName("Validate a UAttributes for payload that is meant to be published not expired with ttl")
    fun test_validate_uAttributes_for_publish_message_payload_not_expired_with_ttl() {
        val attributes: UAttributes =
            UAttributesBuilder.publish(source, UPriority.UPRIORITY_CS0).withTtl(10000).build()
        val validator: UAttributesValidator = UAttributesValidator.Validators.PUBLISH.validator()
        assertFalse(validator.isExpired(attributes))
    }

    @Test
    @DisplayName("Validate a UAttributes for payload that is meant to be published not expired with ttl")
    fun test_validate_uAttributes_for_publish_message_payload_with_negative_ttl() {
        val attributes: UAttributes =
            UAttributesBuilder.publish(source, UPriority.UPRIORITY_CS0).withTtl(-1).build()

        val validator: UAttributesValidator = UAttributesValidator.Validators.PUBLISH.validator()
        assertFalse(validator.isExpired(attributes))
    }

    @Test
    @DisplayName("Validate a UAttributes for payload that is meant to be published expired with ttl")
    @Throws(
        InterruptedException::class
    )
    fun test_validate_uAttributes_for_publish_message_payload_expired_with_ttl() {
        val attributes: UAttributes =
            UAttributesBuilder.publish(source, UPriority.UPRIORITY_CS0).withTtl(1).build()
        Thread.sleep(800)
        val validator: UAttributesValidator = UAttributesValidator.Validators.PUBLISH.validator()
        assertTrue(validator.isExpired(attributes))
    }

    // ----
    @Test
    @DisplayName("test validating publish invalid ttl attribute")
    fun test_validating_publish_invalid_ttl_attribute() {
        val attributes: UAttributes =
            UAttributesBuilder.publish(source, UPriority.UPRIORITY_CS0).withTtl(-1).build()
        val validator: UAttributesValidator = UAttributesValidator.Validators.PUBLISH.validator()
        val status: ValidationResult = validator.validateTtl(attributes)
        assertTrue(status.isFailure())
        assertEquals("Invalid TTL [-1]", status.getMessage())
    }

    @Test
    @DisplayName("test validating publish valid ttl attribute")
    fun test_validating_valid_ttl_attribute() {
        val attributes: UAttributes =
            UAttributesBuilder.publish(source, UPriority.UPRIORITY_CS0).withTtl(100).build()
        val validator: UAttributesValidator = UAttributesValidator.Validators.PUBLISH.validator()
        val status: ValidationResult = validator.validateTtl(attributes)
        assertEquals(ValidationResult.success(), status)
    }

    @Test
    @DisplayName("test validating invalid sink attribute")
    fun test_validating_invalid_sink_attribute() {
        val uri: UUri = LongUriSerializer.instance().deserialize("//")
        val attributes: UAttributes =
            UAttributesBuilder.publish(source, UPriority.UPRIORITY_CS0).withSink(uri).build()
        val validator: UAttributesValidator = UAttributesValidator.Validators.PUBLISH.validator()
        val status: ValidationResult = validator.validateSink(attributes)
        assertTrue(status.isFailure())
        assertEquals("Uri is empty.", status.getMessage())
    }

    @Test
    @DisplayName("test validating valid sink attribute")
    fun test_validating_valid_sink_attribute() {
        val uri: UUri = LongUriSerializer.instance().deserialize("/haartley/1")
        val attributes: UAttributes =
            UAttributesBuilder.publish(source, UPriority.UPRIORITY_CS0).withSink(uri).build()
        val validator: UAttributesValidator = UAttributesValidator.Validators.PUBLISH.validator()
        val status: ValidationResult = validator.validateSink(attributes)
        assertEquals(ValidationResult.success(), status)
    }

    @Test
    @DisplayName("test validating invalid ReqId attribute")
    fun test_validating_invalid_ReqId_attribute() {
        val uuidJava: java.util.UUID = java.util.UUID.randomUUID()
        val attributes: UAttributes =
            UAttributesBuilder.publish(source, UPriority.UPRIORITY_CS0).withReqId(uUID {
                msb = uuidJava.mostSignificantBits
                lsb = uuidJava.leastSignificantBits
            }).build()
        val validator: UAttributesValidator = UAttributesValidator.Validators.PUBLISH.validator()
        val status: ValidationResult = validator.validateReqId(attributes)
        assertTrue(status.isFailure())
        assertEquals("Invalid UUID", status.getMessage())
    }

    @Test
    @DisplayName("test validating valid ReqId attribute")
    fun test_validating_valid_ReqId_attribute() {
        val attributes: UAttributes = UAttributesBuilder.publish(source, UPriority.UPRIORITY_CS0)
            .withReqId(UuidFactory.Factories.UPROTOCOL.factory().create()).build()
        val validator: UAttributesValidator = UAttributesValidator.Validators.PUBLISH.validator()
        val status: ValidationResult = validator.validateReqId(attributes)
        assertEquals(ValidationResult.success(), status)
    }

    @Test
    @DisplayName("test validating invalid PermissionLevel attribute")
    fun test_validating_invalid_PermissionLevel_attribute() {
        val attributes: UAttributes =
            UAttributesBuilder.publish(source, UPriority.UPRIORITY_CS0).withPermissionLevel(-1).build()
        val validator: UAttributesValidator = UAttributesValidator.Validators.PUBLISH.validator()
        val status: ValidationResult = validator.validatePermissionLevel(attributes)
        assertTrue(status.isFailure())
        assertEquals("Invalid Permission Level", status.getMessage())
    }

    @Test
    @DisplayName("test validating valid PermissionLevel attribute")
    fun test_validating_valid_PermissionLevel_attribute() {
        val attributes: UAttributes =
            UAttributesBuilder.publish(source, UPriority.UPRIORITY_CS0).withPermissionLevel(3).build()
        val validator: UAttributesValidator = UAttributesValidator.Validators.PUBLISH.validator()
        val status: ValidationResult = validator.validatePermissionLevel(attributes)
        assertEquals(ValidationResult.success(), status)
    }

    @Test
    @DisplayName("test validating valid PermissionLevel attribute")
    fun test_validating_valid_PermissionLevel_attribute_invalid() {
        val attributes: UAttributes =
            UAttributesBuilder.publish(source, UPriority.UPRIORITY_CS0).withPermissionLevel(0).build()
        val validator: UAttributesValidator = UAttributesValidator.Validators.PUBLISH.validator()
        val status: ValidationResult = validator.validatePermissionLevel(attributes)
        assertTrue(status.isFailure())
        assertEquals("Invalid Permission Level", status.getMessage())
    }

    @Test
    @DisplayName("test validating invalid commstatus attribute")
    fun test_validating_invalid_commstatus_attribute() {
        val attributes: UAttributes =
            UAttributesBuilder.publish(source, UPriority.UPRIORITY_CS0).withCommStatus(100).build()
        val validator: UAttributesValidator = UAttributesValidator.Validators.PUBLISH.validator()
        val status: ValidationResult = validator.validateCommStatus(attributes)
        assertTrue(status.isFailure())
        assertEquals("Invalid Communication Status Code", status.getMessage())
    }

    @Test
    @DisplayName("test validating valid commstatus attribute")
    fun test_validating_valid_commstatus_attribute() {
        val attributes: UAttributes =
            UAttributesBuilder.publish(source, UPriority.UPRIORITY_CS0).withCommStatus(UCode.ABORTED_VALUE)
                .build()
        val validator: UAttributesValidator = UAttributesValidator.Validators.PUBLISH.validator()
        val status: ValidationResult = validator.validateCommStatus(attributes)
        assertEquals(ValidationResult.success(), status)
    }

    @Test
    @DisplayName("test validating request message types")
    fun test_validating_request_message_types() {
        val attributes: UAttributes =
            UAttributesBuilder.request(source, sink, UPriority.UPRIORITY_CS6, 100).build()
        val validator: UAttributesValidator = UAttributesValidator.getValidator(attributes)
        assertEquals("UAttributesValidator.Request", validator.toString())
        val status: ValidationResult = validator.validate(attributes)
        assertTrue(status.isSuccess())
        assertEquals("", status.getMessage())
    }

    @Test
    @DisplayName("test validating request validator using wrong messagetype")
    fun test_validating_request_validator_with_wrong_messagetype() {
        val attributes: UAttributes = UAttributesBuilder.publish(source, UPriority.UPRIORITY_CS6).build()
        val validator: UAttributesValidator = UAttributesValidator.Validators.REQUEST.validator()
        assertEquals("UAttributesValidator.Request", validator.toString())
        val status: ValidationResult = validator.validate(attributes)
        assertTrue(status.isFailure())
        assertEquals("Wrong Attribute Type [UMESSAGE_TYPE_PUBLISH],Missing TTL,Missing Sink", status.getMessage())
    }

    @Test
    @DisplayName("test validating request validator using bad ttl")
    fun test_validating_request_validator_with_wrong_bad_ttl() {
        val attributes: UAttributes = UAttributesBuilder.request(
            source, LongUriSerializer.instance().deserialize("/hartley/1/rpc.response"),
            UPriority.UPRIORITY_CS6, -1
        ).build()
        val validator: UAttributesValidator = UAttributesValidator.Validators.REQUEST.validator()
        assertEquals("UAttributesValidator.Request", validator.toString())
        val status: ValidationResult = validator.validate(attributes)
        assertTrue(status.isFailure())
        assertEquals("Invalid TTL [-1]", status.getMessage())
    }

    @Test
    @DisplayName("test validating response validator using bad ttl")
    fun test_validating_response_validator_with_wrong_bad_ttl() {
        val attributes: UAttributes = UAttributesBuilder.response(
            source,
            LongUriSerializer.instance().deserialize("/hartley/1/rpc.response"),
            UPriority.UPRIORITY_CS6,
            UuidFactory.Factories.UPROTOCOL.factory().create()
        ).withTtl(-1).build()
        val validator: UAttributesValidator = UAttributesValidator.Validators.RESPONSE.validator()
        assertEquals("UAttributesValidator.Response", validator.toString())
        val status: ValidationResult = validator.validate(attributes)
        assertTrue(status.isFailure())
        assertEquals("Invalid TTL [-1]", status.getMessage())
    }

    @Test
    @DisplayName("test validating publish validator with wrong messagetype")
    fun test_validating_publish_validator_with_wrong_messagetype() {
        val attributes: UAttributes =
            UAttributesBuilder.request(source, sink, UPriority.UPRIORITY_CS6, 1000).build()
        val validator: UAttributesValidator = UAttributesValidator.Validators.PUBLISH.validator()
        assertEquals("UAttributesValidator.Publish", validator.toString())
        val status: ValidationResult = validator.validate(attributes)
        assertTrue(status.isFailure())
        assertEquals("Wrong Attribute Type [UMESSAGE_TYPE_REQUEST]", status.getMessage())
    }

    @Test
    @DisplayName("test validating response validator with wrong messagetype")
    fun test_validating_response_validator_with_wrong_messagetype() {
        val attributes: UAttributes = UAttributesBuilder.publish(source, UPriority.UPRIORITY_CS6).build()
        val validator: UAttributesValidator = UAttributesValidator.Validators.RESPONSE.validator()
        assertEquals("UAttributesValidator.Response", validator.toString())
        val status: ValidationResult = validator.validate(attributes)
        assertTrue(status.isFailure())
        assertEquals(
            "Wrong Attribute Type [UMESSAGE_TYPE_PUBLISH],Missing Sink,Missing correlationId", status.getMessage()
        )
    }

    @Test
    @DisplayName("test validating request containing token")
    fun test_validating_request_containing_token() {
        val attributes: UAttributes =
            UAttributesBuilder.publish(source, UPriority.UPRIORITY_CS0).withToken("null").build()
        val validator: UAttributesValidator = UAttributesValidator.getValidator(attributes)
        assertEquals("UAttributesValidator.Publish", validator.toString())
        val status: ValidationResult = validator.validate(attributes)
        assertEquals(ValidationResult.success(), status)
    }

    @Test
    @DisplayName("test_valid_request_methoduri_in_sink")
    fun test_valid_request_methoduri_in_sink() {
        val sink = LongUriSerializer.instance().deserialize("/test.service/1/rpc.method")
        val attributes = UAttributesBuilder.request(source, sink, UPriority.UPRIORITY_CS0, 3000).build()
        val validator = UAttributesValidator.getValidator(attributes)
        assertEquals("UAttributesValidator.Request", validator.toString())
        val status = validator.validate(attributes)
        assertEquals(ValidationResult.success(), status)
    }

    @Test
    @DisplayName("test_invalid_request_methoduri_in_sink")
    fun test_invalid_request_methoduri_in_sink() {
        val sink = LongUriSerializer.instance().deserialize("/test.client/1/test.response")
        val attributes = UAttributesBuilder.request(source, sink, UPriority.UPRIORITY_CS0, 3000).build()
        val validator = UAttributesValidator.getValidator(attributes)
        assertEquals("UAttributesValidator.Request", validator.toString())
        val status = validator.validate(attributes)
        assertEquals(
            "Invalid RPC method uri. Uri should be the method to be called, or method from response.",
            status.getMessage()
        )
    }

    @Test
    @DisplayName("test_valid_response_uri_in_sink")
    fun test_valid_response_uri_in_sink() {
        val sink = LongUriSerializer.instance().deserialize("/test.client/1/rpc.response")
        val attributes = UAttributesBuilder.response(
            source,
            sink,
            UPriority.UPRIORITY_CS0,
            UuidFactory.Factories.UPROTOCOL.factory().create()
        ).build()
        val validator = UAttributesValidator.getValidator(attributes)
        assertEquals("UAttributesValidator.Response", validator.toString())
        val status = validator.validate(attributes)
        assertEquals(ValidationResult.success(), status)
    }

    @Test
    @DisplayName("test_invalid_response_uri_in_sink")
    fun test_invalid_response_uri_in_sink() {
        val sink = LongUriSerializer.instance().deserialize("/test.client/1/rpc.method")
        val attributes = UAttributesBuilder.response(
            source,
            sink,
            UPriority.UPRIORITY_CS0,
            UuidFactory.Factories.UPROTOCOL.factory().create()
        ).build()
        val validator = UAttributesValidator.getValidator(attributes)
        assertEquals("UAttributesValidator.Response", validator.toString())
        val status = validator.validate(attributes)
        assertEquals("Invalid RPC response type.", status.getMessage())
    }

    private val sink = uUri {
        authority = uAuthority { name = "vcu.someVin.veh.ultifi.gm.com" }
        entity = uEntity {
            name = "petapp.ultifi.gm.com"
            versionMajor = 1
        }
        resource = forRpcResponse()
    }

    private val source: UUri = uUri {
        entity = uEntity {
            name = "hartley_app"
            versionMajor = 1
        }
        resource = forRpcResponse()
    }
}
