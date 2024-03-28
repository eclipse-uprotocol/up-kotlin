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

import org.eclipse.uprotocol.transport.validate.*
import org.eclipse.uprotocol.transport.validate.UAttributesValidator.Companion.getValidator
import org.eclipse.uprotocol.uri.serializer.LongUriSerializer
import org.eclipse.uprotocol.uuid.factory.UUIDV8
import org.eclipse.uprotocol.v1.*
import org.eclipse.uprotocol.validation.ValidationResult
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test


internal class UAttributesValidatorTest {
    @Test
    @DisplayName("test fetching validator for valid types")
    fun test_fetching_validator_for_valid_types() {
        val publish: UAttributesValidator = uAttributes {
            forPublication(testSource, UPriority.UPRIORITY_CS0)
        }.getValidator()

        assertEquals("UAttributesValidator.Publish", publish.toString())
        val request: UAttributesValidator = uAttributes {
            forRequest(testSource, testSink, UPriority.UPRIORITY_CS4, 1000)
        }.getValidator()

        assertEquals("UAttributesValidator.Request", request.toString())
        val response: UAttributesValidator = uAttributes {
            forResponse(testSource, testSink, UPriority.UPRIORITY_CS4, UUIDV8())
        }.getValidator()

        assertEquals("UAttributesValidator.Response", response.toString())

        val notification: UAttributesValidator = uAttributes {
            forNotification(testSource, testSink, UPriority.UPRIORITY_CS4)
        }.getValidator()

        assertEquals("UAttributesValidator.Notification", notification.toString())
    }

    @Test
    @DisplayName("test using notification validator for publish type message")
    fun test_using_notification_validator_for_publish_type_message() {
        val attributes: UAttributes = uAttributes { forPublication(testSource, UPriority.UPRIORITY_CS0) }
        val validator: UAttributesValidator = Notification
        val status = validator.validate(attributes)
        assertTrue(status.isFailure())
        assertEquals("Wrong Attribute Type [UMESSAGE_TYPE_PUBLISH],Missing Sink", status.getMessage())
    }

    @Test
    @DisplayName("Validate a UAttributes for payload that is meant to be published")
    fun test_validate_uAttributes_for_publish_message_payload() {
        val attributes: UAttributes = uAttributes {
            forPublication(testSource, UPriority.UPRIORITY_CS0)
        }
        val validator: UAttributesValidator = Publish
        val status: ValidationResult = validator.validate(attributes)
        assertTrue(status.isSuccess())
        assertEquals("", status.getMessage())
    }

    @Test
    @DisplayName("Validate a UAttributes for payload that is meant to be published with all values")
    fun test_validate_uAttributes_for_publish_message_payload_all_values() {
        val attributes: UAttributes =
            uAttributes {
                forPublication(testSource, UPriority.UPRIORITY_CS0)
                ttl = 1000
                sink = testSink
                permissionLevel = 2
                commstatus = UCode.INVALID_ARGUMENT
                reqid = UUIDV8()
            }
        val validator: UAttributesValidator = Publish
        val status: ValidationResult = validator.validate(attributes)
        assertTrue(status.isSuccess())
        assertEquals("", status.getMessage())
    }

    @Test
    @DisplayName("Validate a UAttributes for payload that is meant to be published with invalid type")
    fun test_validate_uAttributes_for_publish_message_payload_invalid_type() {
        val attributes: UAttributes = uAttributes {
            forResponse(
                testSource, testSink,
                UPriority.UPRIORITY_CS0, UUIDV8()
            )
        }
        val validator: UAttributesValidator = Publish
        val status: ValidationResult = validator.validate(attributes)
        assertTrue(status.isFailure())
        assertEquals("Wrong Attribute Type [UMESSAGE_TYPE_RESPONSE]", status.getMessage())
    }

    @Test
    @DisplayName("Validate a UAttributes for payload that is meant to be published with invalid time to live")
    fun test_validate_uAttributes_for_publish_message_payload_invalid_ttl() {
        val attributes: UAttributes =
            uAttributes {
                forPublication(testSource, UPriority.UPRIORITY_CS0)
                ttl = -1
            }
        val validator: UAttributesValidator = Publish
        val status: ValidationResult = validator.validate(attributes)
        assertTrue(status.isFailure())
        assertEquals("Invalid TTL [-1]", status.getMessage())
    }

    @Test
    @DisplayName("Validate a UAttributes for payload that is meant to be published with invalid sink")
    fun test_validate_uAttributes_for_publish_message_payload_invalid_sink() {
        val attributes: UAttributes =
            uAttributes {
                forPublication(testSource, UPriority.UPRIORITY_CS0)
                sink = UUri.getDefaultInstance()
            }
        val validator: UAttributesValidator = Publish
        val status: ValidationResult = validator.validate(attributes)
        assertTrue(status.isFailure())
        assertEquals("Uri is empty.", status.getMessage())
    }

    @Test
    @DisplayName("Validate a UAttributes for payload that is meant to be published with invalid permission level")
    fun test_validate_uAttributes_for_publish_message_payload_invalid_permission_level() {
        val attributes: UAttributes =
            uAttributes {
                forPublication(testSource, UPriority.UPRIORITY_CS0)
                permissionLevel = -42
            }
        val validator: UAttributesValidator = Publish
        val status: ValidationResult = validator.validate(attributes)
        assertTrue(status.isFailure())
        assertEquals("Invalid Permission Level", status.getMessage())
    }

    @Test
    @DisplayName("Validate a UAttributes for payload that is meant to be published with invalid request id")
    fun test_validate_uAttributes_for_publish_message_payload_invalid_request_id() {
        val uuidJava: java.util.UUID = java.util.UUID.randomUUID()
        val attributes: UAttributes =
            uAttributes {
                forPublication(testSource, UPriority.UPRIORITY_CS0)
                reqid = uUID {
                    msb = uuidJava.mostSignificantBits
                    lsb = uuidJava.leastSignificantBits
                }
            }
        val validator: UAttributesValidator = Publish
        val status: ValidationResult = validator.validate(attributes)
        assertTrue(status.isFailure())
        assertEquals("Invalid UUID", status.getMessage())
    }

    // ----
    @Test
    @DisplayName("Validate a UAttributes for payload that is meant to be an RPC request")
    fun test_validate_uAttributes_for_rpc_request_message_payload() {
        val attributes: UAttributes =
            uAttributes {
                forRequest(testSource, testSink, UPriority.UPRIORITY_CS4, 1000)
            }

        val validator: UAttributesValidator = Request
        val status: ValidationResult = validator.validate(attributes)
        assertTrue(status.isSuccess())
        assertEquals("", status.getMessage())
    }

    @Test
    @DisplayName("Validate a UAttributes for payload that is meant to be an RPC request with all values")
    fun test_validate_uAttributes_for_rpc_request_message_payload_all_values() {
        val attributes: UAttributes =
            uAttributes {
                forRequest(testSource, testSink, UPriority.UPRIORITY_CS4, 1000)
                permissionLevel = 2
                commstatus = UCode.INVALID_ARGUMENT
                reqid = UUIDV8()

            }

        val validator: UAttributesValidator = Request
        val status: ValidationResult = validator.validate(attributes)
        assertTrue(status.isSuccess())
        assertEquals("", status.getMessage())
    }

    @Test
    @DisplayName("Validate a UAttributes for payload that is meant to be an RPC request with invalid type")
    fun test_validate_uAttributes_for_rpc_request_message_payload_invalid_type() {
        val attributes: UAttributes = uAttributes {
            forResponse(
                testSource, testSink,
                UPriority.UPRIORITY_CS4, UUIDV8()
            )
            ttl = 1000
        }
        val validator: UAttributesValidator = Request
        val status: ValidationResult = validator.validate(attributes)
        assertTrue(status.isFailure())
        assertEquals("Wrong Attribute Type [UMESSAGE_TYPE_RESPONSE]", status.getMessage())
    }

    @Test
    @DisplayName("Validate a UAttributes for payload that is meant to be an RPC request with invalid time to live")
    fun test_validate_uAttributes_for_rpc_request_message_payload_invalid_ttl() {
        val attributes: UAttributes =
            uAttributes {
                forRequest(testSource, testSink, UPriority.UPRIORITY_CS4, -1)
            }

        val validator: UAttributesValidator = Request
        val status: ValidationResult = validator.validate(attributes)
        assertTrue(status.isFailure())
        assertEquals("Invalid TTL [-1]", status.getMessage())
    }

    @Test
    @DisplayName("Validate a UAttributes for payload that is meant to be an RPC request with invalid sink")
    fun test_validate_uAttributes_for_rpc_request_message_payload_invalid_sink() {
        val attributes: UAttributes =
            uAttributes {
                forRequest(testSource, UUri.getDefaultInstance(), UPriority.UPRIORITY_CS4, 1000)
            }
        val validator: UAttributesValidator = Request
        val status: ValidationResult = validator.validate(attributes)
        assertTrue(status.isFailure())
        assertEquals("Uri is empty.", status.getMessage())
    }

    @Test
    @DisplayName("Validate a UAttributes for payload that is meant to be an RPC request with invalid permission level")
    fun test_validate_uAttributes_for_rpc_request_message_payload_invalid_permission_level() {
        val attributes: UAttributes =
            uAttributes {
                forRequest(testSource, testSink, UPriority.UPRIORITY_CS4, 1000)
                permissionLevel = -42
            }

        val validator: UAttributesValidator = Request
        val status: ValidationResult = validator.validate(attributes)
        assertTrue(status.isFailure())
        assertEquals("Invalid Permission Level", status.getMessage())
    }

    @Test
    @DisplayName("Validate a UAttributes for payload that is meant to be an RPC request with invalid request id")
    fun test_validate_uAttributes_for_rpc_request_message_payload_invalid_request_id() {
        val uuidJava: java.util.UUID = java.util.UUID.randomUUID()
        val attributes: UAttributes =
            uAttributes {
                forRequest(testSource, testSink, UPriority.UPRIORITY_CS4, 1000)
                reqid = uUID {
                    msb = uuidJava.mostSignificantBits
                    lsb = uuidJava.leastSignificantBits
                }
            }

        val validator: UAttributesValidator = Request
        val status: ValidationResult = validator.validate(attributes)
        assertTrue(status.isFailure())
        assertEquals("Invalid UUID", status.getMessage())
    }

    // ----
    @Test
    @DisplayName("Validate a UAttributes for payload that is meant to be an RPC response")
    fun test_validate_uAttributes_for_rpc_response_message_payload() {
        val attributes: UAttributes = uAttributes {
            forResponse(
                testSource, testSink,
                UPriority.UPRIORITY_CS4, UUIDV8()
            )
        }
        val validator: UAttributesValidator = Response
        val status: ValidationResult = validator.validate(attributes)
        assertTrue(status.isSuccess())
        assertEquals("", status.getMessage())
    }

    @Test
    @DisplayName("Validate a UAttributes for payload that is meant to be an RPC response with all values")
    fun test_validate_uAttributes_for_rpc_response_message_payload_all_values() {
        val attributes: UAttributes = uAttributes {
            forResponse(
                testSource, testSink,
                UPriority.UPRIORITY_CS4, UUIDV8()
            )
            permissionLevel = 2
            commstatus = UCode.INVALID_ARGUMENT

        }
        val validator: UAttributesValidator = Response
        val status: ValidationResult = validator.validate(attributes)
        assertTrue(status.isSuccess())
        assertEquals("", status.getMessage())
    }

    @Test
    @DisplayName("Validate a UAttributes for payload that is meant to be an RPC response with invalid type")
    fun test_validate_uAttributes_for_rpc_response_message_payload_invalid_type() {
        val attributes: UAttributes =
            uAttributes {
                forNotification(testSource, testSink, UPriority.UPRIORITY_CS4)
            }
        val validator: UAttributesValidator = Response
        val status: ValidationResult = validator.validate(attributes)
        assertTrue(status.isFailure())
        assertEquals("Wrong Attribute Type [UMESSAGE_TYPE_NOTIFICATION],Missing correlationId", status.getMessage())
    }

    @Test
    @DisplayName("Validate a UAttributes for payload that is meant to be an RPC response with invalid time to live")
    fun test_validate_uAttributes_for_rpc_response_message_payload_invalid_ttl() {
        val attributes: UAttributes = uAttributes {
            forResponse(
                testSource, testSink,
                UPriority.UPRIORITY_CS4, UUIDV8()
            )
            ttl = -1
        }
        val validator: UAttributesValidator = Response
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
            uAttributes {
                forResponse(
                    testSource,
                    UUri.getDefaultInstance(),
                    UPriority.UPRIORITY_CS4,
                    UUID.getDefaultInstance()
                )
            }
        val validator: UAttributesValidator = Response
        val status: ValidationResult = validator.validate(attributes)
        assertTrue(status.isFailure())
        assertEquals("Missing Sink,Missing correlationId", status.getMessage())
    }

    @Test
    @DisplayName("Validate a UAttributes for payload that is meant to be an RPC response with invalid permission level")
    fun test_validate_uAttributes_for_rpc_response_message_payload_invalid_permission_level() {
        val attributes: UAttributes = uAttributes {
            forResponse(
                testSource, testSink,
                UPriority.UPRIORITY_CS4, UUIDV8()
            )
            permissionLevel = -42
        }
        val validator: UAttributesValidator = Response
        val status: ValidationResult = validator.validate(attributes)
        assertTrue(status.isFailure())
        assertEquals("Invalid Permission Level", status.getMessage())
    }

    @Test
    @DisplayName("Validate a UAttributes for payload that is meant to be an RPC response with missing request id")
    fun test_validate_uAttributes_for_rpc_response_message_payload_missing_request_id() {
        val attributes: UAttributes =
            uAttributes {
                forResponse(testSource, testSink, UPriority.UPRIORITY_CS4, UUID.getDefaultInstance())
            }
        val validator: UAttributesValidator = Response
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
            uAttributes { forResponse(testSource, testSink, UPriority.UPRIORITY_CS4, reqid) }
        val validator: UAttributesValidator = Response
        val status: ValidationResult = validator.validate(attributes)
        assertTrue(status.isFailure())
        assertEquals("Invalid correlationId [$reqid]", status.getMessage())
    }

    // ----
    @Test
    @DisplayName("Validate a UAttributes for payload that is meant to be published not expired")
    fun test_validate_uAttributes_for_publish_message_payload_not_expired() {
        val attributes: UAttributes = uAttributes {
            forPublication(testSource, UPriority.UPRIORITY_CS0)
        }
        val validator: UAttributesValidator = Publish
        assertFalse(validator.isExpired(attributes))
    }

    @Test
    @DisplayName("Validate a UAttributes isExpired() for an invalid UUID of UAttributes")
    fun test_validate_uAttributes_isExpired_for_invalid_UUID() {
        val attributes = UAttributes.getDefaultInstance()
        val validator = Publish
        assertFalse(validator.isExpired(attributes))
    }

    @Test
    @DisplayName("Validate a UAttributes for payload that is meant to be published not expired with ttl zero")
    fun test_validate_uAttributes_for_publish_message_payload_not_expired_with_ttl_zero() {
        val attributes: UAttributes =
            uAttributes {
                forPublication(testSource, UPriority.UPRIORITY_CS0)
                ttl = 0
            }
        val validator: UAttributesValidator = Publish
        assertFalse(validator.isExpired(attributes))
    }

    @Test
    @DisplayName("Validate a UAttributes for payload that is meant to be published not expired with ttl")
    fun test_validate_uAttributes_for_publish_message_payload_not_expired_with_ttl() {
        val attributes: UAttributes =
            uAttributes {
                forPublication(testSource, UPriority.UPRIORITY_CS0)
                ttl = 10000
            }
        val validator: UAttributesValidator = Publish
        assertFalse(validator.isExpired(attributes))
    }

    @Test
    @DisplayName("Validate a UAttributes for payload that is meant to be published not expired with ttl")
    fun test_validate_uAttributes_for_publish_message_payload_with_negative_ttl() {
        val attributes: UAttributes =
            uAttributes {
                forPublication(testSource, UPriority.UPRIORITY_CS0)
                ttl = -1
            }

        val validator: UAttributesValidator = Publish
        assertFalse(validator.isExpired(attributes))
    }

    @Test
    @DisplayName("Validate a UAttributes for payload that is meant to be published expired with ttl")
    @Throws(
        InterruptedException::class
    )
    fun test_validate_uAttributes_for_publish_message_payload_expired_with_ttl() {
        val attributes: UAttributes =
            uAttributes {
                forPublication(testSource, UPriority.UPRIORITY_CS0)
                ttl = 1
            }
        Thread.sleep(800)
        val validator: UAttributesValidator = Publish
        assertTrue(validator.isExpired(attributes))
    }

    // ----
    @Test
    @DisplayName("test validating publish invalid ttl attribute")
    fun test_validating_publish_invalid_ttl_attribute() {
        val attributes: UAttributes =
            uAttributes {
                forPublication(testSource, UPriority.UPRIORITY_CS0)
                ttl = -1
            }
        val validator: UAttributesValidator = Publish
        val status: ValidationResult = validator.validateTtl(attributes)
        assertTrue(status.isFailure())
        assertEquals("Invalid TTL [-1]", status.getMessage())
    }

    @Test
    @DisplayName("test validating publish valid ttl attribute")
    fun test_validating_valid_ttl_attribute() {
        val attributes: UAttributes =
            uAttributes {
                forPublication(testSource, UPriority.UPRIORITY_CS0)
                ttl = 100
            }
        val validator: UAttributesValidator = Publish
        val status: ValidationResult = validator.validateTtl(attributes)
        assertEquals(ValidationResult.success(), status)
    }

    @Test
    @DisplayName("test validating invalid sink attribute")
    fun test_validating_invalid_sink_attribute() {
        val uri: UUri = LongUriSerializer.INSTANCE.deserialize("//")
        val attributes: UAttributes =
            uAttributes {
                forPublication(testSource, UPriority.UPRIORITY_CS0)
                sink = uri
            }
        val validator: UAttributesValidator = Publish
        val status: ValidationResult = validator.validateSink(attributes)
        assertTrue(status.isFailure())
        assertEquals("Uri is empty.", status.getMessage())
    }

    @Test
    @DisplayName("test validating valid sink attribute")
    fun test_validating_valid_sink_attribute() {
        val uri: UUri = LongUriSerializer.INSTANCE.deserialize("/haartley/1")
        val attributes: UAttributes =
            uAttributes {
                forPublication(testSource, UPriority.UPRIORITY_CS0)
                sink = uri
            }
        val validator: UAttributesValidator = Publish
        val status: ValidationResult = validator.validateSink(attributes)
        assertEquals(ValidationResult.success(), status)
    }

    @Test
    @DisplayName("test validating invalid ReqId attribute")
    fun test_validating_invalid_ReqId_attribute() {
        val uuidJava: java.util.UUID = java.util.UUID.randomUUID()
        val attributes: UAttributes =
            uAttributes {
                forPublication(testSource, UPriority.UPRIORITY_CS0)
                reqid = uUID {
                    msb = uuidJava.mostSignificantBits
                    lsb = uuidJava.leastSignificantBits
                }
            }
        val validator: UAttributesValidator = Publish
        val status: ValidationResult = validator.validateReqId(attributes)
        assertTrue(status.isFailure())
        assertEquals("Invalid UUID", status.getMessage())
    }

    @Test
    @DisplayName("test validating valid ReqId attribute")
    fun test_validating_valid_ReqId_attribute() {
        val attributes: UAttributes = uAttributes {
            forPublication(testSource, UPriority.UPRIORITY_CS0)
            reqid = UUIDV8()
        }
        val validator: UAttributesValidator = Publish
        val status: ValidationResult = validator.validateReqId(attributes)
        assertEquals(ValidationResult.success(), status)
    }

    @Test
    @DisplayName("test validating invalid PermissionLevel attribute")
    fun test_validating_invalid_PermissionLevel_attribute() {
        val attributes: UAttributes =
            uAttributes {
                forPublication(testSource, UPriority.UPRIORITY_CS0)
                permissionLevel = -1
            }
        val validator: UAttributesValidator = Publish
        val status: ValidationResult = validator.validatePermissionLevel(attributes)
        assertTrue(status.isFailure())
        assertEquals("Invalid Permission Level", status.getMessage())
    }

    @Test
    @DisplayName("test validating valid PermissionLevel attribute")
    fun test_validating_valid_PermissionLevel_attribute() {
        val attributes: UAttributes =
            uAttributes {
                forPublication(testSource, UPriority.UPRIORITY_CS0)
                permissionLevel = 3
            }
        val validator: UAttributesValidator = Publish
        val status: ValidationResult = validator.validatePermissionLevel(attributes)
        assertEquals(ValidationResult.success(), status)
    }

    @Test
    @DisplayName("test validating valid PermissionLevel attribute")
    fun test_validating_valid_PermissionLevel_attribute_invalid() {
        val attributes: UAttributes =
            uAttributes {
                forPublication(testSource, UPriority.UPRIORITY_CS0)
                permissionLevel = 0
            }
        val validator: UAttributesValidator = Publish
        val status: ValidationResult = validator.validatePermissionLevel(attributes)
        assertTrue(status.isFailure())
        assertEquals("Invalid Permission Level", status.getMessage())
    }

    @Test
    @DisplayName("test validating request message types")
    fun test_validating_request_message_types() {
        val attributes: UAttributes =
            uAttributes {
                forRequest(testSource, testSink, UPriority.UPRIORITY_CS6, 100)
            }

        val validator: UAttributesValidator = attributes.getValidator()
        assertEquals("UAttributesValidator.Request", validator.toString())
        val status: ValidationResult = validator.validate(attributes)
        assertTrue(status.isSuccess())
        assertEquals("", status.getMessage())
    }

    @Test
    @DisplayName("test validating request validator using wrong messagetype")
    fun test_validating_request_validator_with_wrong_messagetype() {
        val attributes: UAttributes = uAttributes {
            forPublication(testSource, UPriority.UPRIORITY_CS6)
        }
        val validator: UAttributesValidator = Request
        assertEquals("UAttributesValidator.Request", validator.toString())
        val status: ValidationResult = validator.validate(attributes)
        assertTrue(status.isFailure())
        assertEquals("Wrong Attribute Type [UMESSAGE_TYPE_PUBLISH],Missing TTL,Missing Sink", status.getMessage())
    }

    @Test
    @DisplayName("test validating request validator using bad ttl")
    fun test_validating_request_validator_with_wrong_bad_ttl() {
        val attributes: UAttributes = uAttributes {
            forRequest(
                testSource, LongUriSerializer.INSTANCE.deserialize("/hartley/1/rpc.response"),
                UPriority.UPRIORITY_CS6, -1
            )
        }

        val validator: UAttributesValidator = Request
        assertEquals("UAttributesValidator.Request", validator.toString())
        val status: ValidationResult = validator.validate(attributes)
        assertTrue(status.isFailure())
        assertEquals("Invalid TTL [-1]", status.getMessage())
    }

    @Test
    @DisplayName("test validating response validator using bad ttl")
    fun test_validating_response_validator_with_wrong_bad_ttl() {
        val attributes: UAttributes = uAttributes {
            forResponse(
                testSource,
                LongUriSerializer.INSTANCE.deserialize("/hartley/1/rpc.response"),
                UPriority.UPRIORITY_CS6,
                UUIDV8()
            )
            ttl = -1
        }
        val validator: UAttributesValidator = Response
        assertEquals("UAttributesValidator.Response", validator.toString())
        val status: ValidationResult = validator.validate(attributes)
        assertTrue(status.isFailure())
        assertEquals("Invalid TTL [-1]", status.getMessage())
    }

    @Test
    @DisplayName("test validating publish validator with wrong messagetype")
    fun test_validating_publish_validator_with_wrong_messagetype() {
        val attributes: UAttributes =
            uAttributes {
                forRequest(testSource, testSink, UPriority.UPRIORITY_CS6, 1000)
            }

        val validator: UAttributesValidator = Publish
        assertEquals("UAttributesValidator.Publish", validator.toString())
        val status: ValidationResult = validator.validate(attributes)
        assertTrue(status.isFailure())
        assertEquals("Wrong Attribute Type [UMESSAGE_TYPE_REQUEST]", status.getMessage())
    }

    @Test
    @DisplayName("test validating response validator with wrong messagetype")
    fun test_validating_response_validator_with_wrong_messagetype() {
        val attributes: UAttributes = uAttributes {
            forPublication(testSource, UPriority.UPRIORITY_CS6)
        }
        val validator: UAttributesValidator = Response
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
            uAttributes {
                forPublication(testSource, UPriority.UPRIORITY_CS0)
                token = "null"
            }
        val validator: UAttributesValidator = attributes.getValidator()
        assertEquals("UAttributesValidator.Publish", validator.toString())
        val status: ValidationResult = validator.validate(attributes)
        assertEquals(ValidationResult.success(), status)
    }

    @Test
    @DisplayName("test_valid_request_methoduri_in_sink")
    fun test_valid_request_methoduri_in_sink() {
        val testSink = LongUriSerializer.INSTANCE.deserialize("/test.service/1/rpc.method")
        val attributes = uAttributes {
            forRequest(testSource, testSink, UPriority.UPRIORITY_CS4, 3000)
        }

        val validator = attributes.getValidator()
        assertEquals("UAttributesValidator.Request", validator.toString())
        val status = validator.validate(attributes)
        assertEquals(ValidationResult.success(), status)
    }

    @Test
    @DisplayName("test_invalid_request_methoduri_in_sink")
    fun test_invalid_request_methoduri_in_sink() {
        val testSink = LongUriSerializer.INSTANCE.deserialize("/test.client/1/test.response")
        val attributes = uAttributes {
            forRequest(testSource, testSink, UPriority.UPRIORITY_CS4, 3000)
        }

        val validator = attributes.getValidator()
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
        val testSink = LongUriSerializer.INSTANCE.deserialize("/test.client/1/rpc.response")
        val attributes = uAttributes {
            forResponse(
                testSource,
                testSink,
                UPriority.UPRIORITY_CS4,
                UUIDV8()
            )
        }
        val validator = attributes.getValidator()
        assertEquals("UAttributesValidator.Response", validator.toString())
        val status = validator.validate(attributes)
        assertEquals(ValidationResult.success(), status)
    }

    @Test
    @DisplayName("test_invalid_response_uri_in_sink")
    fun test_invalid_response_uri_in_sink() {
        val testSink = LongUriSerializer.INSTANCE.deserialize("/test.client/1/rpc.method")
        val attributes = uAttributes {
            forResponse(
                testSource,
                testSink,
                UPriority.UPRIORITY_CS4,
                UUIDV8()
            )
        }
        val validator = attributes.getValidator()
        assertEquals("UAttributesValidator.Response", validator.toString())
        val status = validator.validate(attributes)
        assertEquals("Invalid RPC response type.", status.getMessage())
    }

    @Test
    @DisplayName("test notification validation with missing sink")
    fun test_notification_validation_with_missing_sink() {
        val attributes: UAttributes = uAttributes { 
            forNotification(testSource, UUri.getDefaultInstance(), UPriority.UPRIORITY_CS0)
        }
        val validator: UAttributesValidator = Notification
        val status = validator.validate(attributes)
        assertTrue(status.isFailure())
        assertEquals("Missing Sink", status.getMessage())
    }

    @Test
    @DisplayName("test notification validation using publish validator")
    fun test_notification_validation_using_publish_validator() {
        val attributes: UAttributes = uAttributes {
            forNotification(testSource, testSink, UPriority.UPRIORITY_CS0)
        }
        val validator: UAttributesValidator = Publish
        val status = validator.validate(attributes)
        assertTrue(status.isFailure())
        assertEquals("Wrong Attribute Type [UMESSAGE_TYPE_NOTIFICATION]", status.getMessage())
    }

    @Test
    @DisplayName("test notification validation when sink is missing")
    fun test_notification_validation_when_sink_is_missing() {
        val attributes: UAttributes = uAttributes {
            id = UUIDV8()
            source = testSource
            type = UMessageType.UMESSAGE_TYPE_NOTIFICATION
            priority = UPriority.UPRIORITY_CS0
        }

        val validator: UAttributesValidator = Notification
        val status = validator.validate(attributes)
        assertTrue(status.isFailure())
        assertEquals("Missing Sink", status.getMessage())
    }

    @Test
    @DisplayName("test notification validation with a valid notification UAttributes")
    fun test_notification_validation_with_a_valid_notification_UAttributes() {
        val attributes: UAttributes = uAttributes {
            forNotification(testSource, testSink, UPriority.UPRIORITY_CS0)
        }
        val validator: UAttributesValidator = Notification
        val status = validator.validate(attributes)
        assertTrue(status.isSuccess())
        assertEquals("", status.getMessage())
    }

    private val testSink = uUri {
        authority = uAuthority { name = "vcu.someVin.veh.ultifi.gm.com" }
        entity = uEntity {
            name = "petapp.ultifi.gm.com"
            versionMajor = 1
        }
        resource = uResource {
            forRpcResponse()
        }
    }

    private val testSource: UUri = uUri {
        entity = uEntity {
            name = "hartley_app"
            versionMajor = 1
        }
        resource = uResource {
            forRpcResponse()
        }
    }
}
