/**
 * SPDX-FileCopyrightText: 2024 Contributors to the Eclipse Foundation
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information regarding copyright ownership.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Apache License Version 2.0 which is available at
 * https://www.apache.org/licenses/LICENSE-2.0
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package org.eclipse.uprotocol.transport.validator

import org.eclipse.uprotocol.transport.*
import org.eclipse.uprotocol.transport.validator.UAttributesValidator.Companion.getValidator
import org.eclipse.uprotocol.v1.*
import org.eclipse.uprotocol.validation.ValidationResult
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test


internal class UAttributesValidatorTest {
    private val defaultUUri = uUri {
        ueId=1
        ueVersionMajor=1
        resourceId=0
    }

    private val methodUUri = uUri {
        ueId=1
        ueVersionMajor=1
        resourceId=1
    }

    private val topicUUri = uUri {
        ueId=1
        ueVersionMajor=1
        resourceId=0x8000
    }
    @Test
    @DisplayName("Test creating a UMessage of type publish then validating it using UAttributeValidator for the happy path")
    fun testUAttributeValidatorHappyPath() {
        val message = uMessage {
            forPublication(topicUUri)
        }
        val validator = message.attributes.getValidator()
        val status: ValidationResult = validator.validate(message.attributes)
        assertTrue(status.isSuccess())
        assertEquals(validator.toString(), "UAttributesValidator.Publish")
    }

    @Test
    @DisplayName("Test validation a notification message using UAttributeValidator")
    fun testUAttributeValidatorNotification() {
        val message: UMessage = uMessage { 
            forNotification(topicUUri, defaultUUri)
        }
        val validator: UAttributesValidator = message.attributes.getValidator()
        val result = validator.validate(message.attributes)
        assertTrue(result.isSuccess())
        assertEquals(validator.toString(), "UAttributesValidator.Notification")
    }

    @Test
    @DisplayName("Test validation a request message using UAttributeValidator")
    fun testUAttributeValidatorRequest() {
        val message: UMessage = uMessage { 
            forRequest(defaultUUri, methodUUri, 1000)
        }
        val validator: UAttributesValidator = message.attributes.getValidator()
        val result = validator.validate(message.attributes)
        assertTrue(result.isSuccess())
        assertEquals(validator.toString(), "UAttributesValidator.Request")
    }

    @Test
    @DisplayName("Test validation a response message using UAttributeValidator")
    fun testUAttributeValidatorResponse() {
        val request: UMessage =uMessage {
            forRequest(defaultUUri, methodUUri, 1000)
        }
        val response: UMessage = uMessage {
            forResponse(methodUUri, defaultUUri, request.attributes.id)
        }

        val validator: UAttributesValidator = response.attributes.getValidator()
        val result = validator.validate(response.attributes)
        assertTrue(result.isSuccess())
        assertEquals(validator.toString(), "UAttributesValidator.Response")
        assertEquals(result.getMessage(), "")
    }

    @Test
    @DisplayName("Test validation a response message using UAttributeValidator when passed request UAttributes")
    fun testUAttributeValidatorResponseWithRequestAttributes() {
        val request: UMessage =uMessage {
            forRequest(defaultUUri, methodUUri, 1000)
        }
        val response: UMessage = uMessage {
            forResponse(request.attributes)
        }

        val validator: UAttributesValidator = response.attributes.getValidator()
        val result = validator.validate(response.attributes)
        assertTrue(result.isSuccess())
        assertEquals(validator.toString(), "UAttributesValidator.Response")
    }

    @Test
    @DisplayName("Test validation failed when using the publish validator to test request messages")
    fun testUAttributeValidatorRequestWithPublishValidator() {
        val message: UMessage =uMessage {
            forRequest(defaultUUri, methodUUri, 1000)
        }

        val validator: UAttributesValidator = Publish
        val result = validator.validate(message.attributes)
        assertTrue(result.isFailure())
        assertEquals(validator.toString(), "UAttributesValidator.Publish")
        assertEquals(result.getMessage(), "Wrong Attribute Type [UMESSAGE_TYPE_REQUEST],Sink should not be present")
    }

    @Test
    @DisplayName("Test validation failed when using the notification validator to test publish messages")
    fun testUAttributeValidatorPublishWithNotificationValidator() {
        val message: UMessage = uMessage { 
            forPublication(topicUUri)
        }

        val validator: UAttributesValidator = Notification
        val result = validator.validate(message.attributes)
        assertTrue(result.isFailure())
        assertEquals(validator.toString(), "UAttributesValidator.Notification")
        assertEquals(result.getMessage(), "Wrong Attribute Type [UMESSAGE_TYPE_PUBLISH],Missing Sink")
    }

    @Test
    @DisplayName("Test validation failed when using the request validator to test response messages")
    fun testUAttributeValidatorResponseWithRequestValidator() {
        val request: UMessage =uMessage {
            forRequest(defaultUUri, methodUUri, 1000)
        }
        val response: UMessage = uMessage {
            forResponse(request.attributes)
        }

        val validator: UAttributesValidator = Request
        val result = validator.validate(response.attributes)
        assertTrue(result.isFailure())
        assertEquals(validator.toString(), "UAttributesValidator.Request")
        assertEquals(
            result.getMessage(),
            "Wrong Attribute Type [UMESSAGE_TYPE_RESPONSE],Missing TTL,Invalid Sink Uri,Message should not have a reqid"
        )
    }

    @Test
    @DisplayName("Test validation failed when using the response validator to test notification messages")
    fun testUAttributeValidatorNotificationWithResponseValidator() {
        val message: UMessage = uMessage { 
            forNotification(topicUUri, defaultUUri)
        }

        val validator: UAttributesValidator = Response
        val result = validator.validate(message.attributes)
        assertTrue(result.isFailure())
        assertEquals(validator.toString(), "UAttributesValidator.Response")
        assertEquals(
            result.getMessage(),
            "Wrong Attribute Type [UMESSAGE_TYPE_NOTIFICATION],Invalid UPriority [UPRIORITY_CS1],Missing correlationId"
        )
    }

    @Test
    @DisplayName("Test validation of request message has an invalid sink attribute")
    fun testUAttributeValidatorRequestMissingSink() {
        val message: UMessage = uMessage {
            forRequest(defaultUUri, defaultUUri, 1000)
        }

        val validator: UAttributesValidator = message.attributes.getValidator()
        val result = validator.validate(message.attributes)
        assertTrue(result.isFailure())
        assertEquals(validator.toString(), "UAttributesValidator.Request")
        assertEquals(result.getMessage(), "Invalid Sink Uri")
    }

    @Test
    @DisplayName("Test validation of request message that has a permission level that is less than 0")
    fun testUAttributeValidatorRequestInvalidPermissionLevel() {
        val message: UMessage = uMessage {
            forRequest(defaultUUri, methodUUri, 1000)
            setPermissionLevel(-1)
        }

        val validator: UAttributesValidator = message.attributes.getValidator()
        val result = validator.validate(message.attributes)
        assertTrue(result.isFailure())
        assertEquals(validator.toString(), "UAttributesValidator.Request")
        assertEquals(result.getMessage(), "Invalid Permission Level")
    }

    @Test
    @DisplayName("Test validation of request message that has a permission level that is greater than 0")
    fun testUAttributeValidatorRequestValidPermissionLevel() {
        val message: UMessage = uMessage {
            forRequest(defaultUUri, methodUUri, 1000)
            setPermissionLevel(1)
        }

        val validator: UAttributesValidator = message.attributes.getValidator()
        val result = validator.validate(message.attributes)
        assertTrue(result.isSuccess())
        assertFalse(validator.isExpired(message.attributes))
        assertEquals(validator.toString(), "UAttributesValidator.Request")
    }

    @Test
    @DisplayName("Test validation of request message that has TTL that is less than 0")
    fun testUAttributeValidatorRequestInvalidTTL() {
        val message: UMessage = uMessage {
            forPublication(topicUUri)
            setTtl(-1)
        }

        val validator: UAttributesValidator = message.attributes.getValidator()
        val result = validator.validate(message.attributes)
        assertTrue(result.isFailure())
        assertFalse(validator.isExpired(message.attributes))
        assertEquals(validator.toString(), "UAttributesValidator.Publish")
        assertEquals(result.getMessage(), "Invalid TTL [-1]")
    }

    @Test
    @DisplayName("Test validation of request message where the message has expired")
//    @Throws(
//        InterruptedException::class
//    )
    fun testUAttributeValidatorRequestExpired() {
        val message: UMessage = uMessage {
            forRequest(defaultUUri, methodUUri, 1)
        }
        Thread.sleep(100)
        val validator: UAttributesValidator = message.attributes.getValidator()
        assertTrue(validator.isExpired(message.attributes))
    }

    @Test
    @DisplayName("Test validator isExpired() for an ID that is mall formed and doesn't have the time")
    fun testUAttributeValidatorRequestExpiredMalformedId() {
        val attributes = uAttributes {
            id = uUID {  }
            type = UMessageType.UMESSAGE_TYPE_REQUEST
        }
        val validator: UAttributesValidator = attributes.getValidator()
        assertFalse(validator.isExpired(attributes))
    }

    @Test
    @DisplayName("Test validation fails when a publish messages has a reqid")
    fun testUAttributeValidatorPublishWithReqId() {
        val publish: UMessage = uMessage {
            forPublication(topicUUri)
        }

        val attributes = publish.attributes.copy {
            reqid = uUID {  }
        }

        val validator: UAttributesValidator = attributes.getValidator()
        val result = validator.validate(attributes)
        assertTrue(result.isFailure())
        assertEquals(validator.toString(), "UAttributesValidator.Publish")
        assertEquals(result.getMessage(), "Message should not have a reqid")
    }

    @Test
    @DisplayName("Test notification validation where the sink is missing")
    fun testUAttributeValidatorNotificationMissingSink() {
        val message: UMessage =uMessage {
            forNotification(topicUUri, defaultUUri)
        }
        val attributes = message.attributes.copy { clearSink() }
        val validator: UAttributesValidator = attributes.getValidator()
        val result = validator.validate(attributes)

        assertTrue(result.isFailure())
        assertEquals(validator.toString(), "UAttributesValidator.Notification")
        assertEquals(result.getMessage(), "Missing Sink")
    }

    @Test
    @DisplayName("Test notification validation where the sink the default instance")
    fun testUAttributeValidatorNotificationDefaultSink() {
        val message: UMessage =uMessage {
            forNotification(topicUUri, defaultUUri)
        }
        val attributes = message.attributes.copy {
            sink = UUri.getDefaultInstance()
        }
        val validator: UAttributesValidator = attributes.getValidator()
        val result = validator.validate(attributes)

        assertTrue(result.isFailure())
        assertEquals(validator.toString(), "UAttributesValidator.Notification")
        assertEquals(result.getMessage(), "Missing Sink")
    }

    @Test
    @DisplayName("Test notification validation where the sink is NOT the defaultResourceId")
    fun testUAttributeValidatorNotificationDefaultResourceId() {
        val message: UMessage = uMessage {
            forNotification(topicUUri, topicUUri)
        }
        val validator: UAttributesValidator = message.attributes.getValidator()
        val result = validator.validate(message.attributes)

        assertTrue(result.isFailure())
        assertEquals(validator.toString(), "UAttributesValidator.Notification")
        assertEquals(result.getMessage(), "Invalid Sink Uri")
    }

    @Test
    @DisplayName("Test validatePriority when priority is less than CS1")
    fun testUAttributeValidatorValidatePriorityLessThanCS1() {
        val message: UMessage = uMessage {
            forPublication(topicUUri)
        }
        val attributes = message.attributes.copy { priority = UPriority.UPRIORITY_CS0 }
        val validator: UAttributesValidator = attributes.getValidator()
        val result = validator.validate(attributes)

        assertTrue(result.isFailure())
        assertEquals(validator.toString(), "UAttributesValidator.Publish")
        assertEquals(result.getMessage(), "Invalid UPriority [UPRIORITY_CS0]")
    }

    @Test
    @DisplayName("Test validateId when id is missing")
    fun testUAttributeValidatorValidateIdMissing() {
        val message: UMessage = uMessage {
            forPublication(topicUUri)
        }
        val attributes = message.attributes.copy {  clearId() }
        val validator: UAttributesValidator = attributes.getValidator()
        val result = validator.validate(attributes)

        assertTrue(result.isFailure())
        assertEquals(validator.toString(), "UAttributesValidator.Publish")
        assertEquals(result.getMessage(), "Missing id")
    }

    @Test
    @DisplayName("Test validateId when id is the default instance")
    fun testUAttributeValidatorValidateIdDefault() {
        val message: UMessage = uMessage {
            forPublication(topicUUri)
        }
        val attributes = message.attributes.copy { id = UUID.getDefaultInstance() }
        val validator: UAttributesValidator = attributes.getValidator()
        val result = validator.validate(attributes)

        assertTrue(result.isFailure())
        assertEquals(validator.toString(), "UAttributesValidator.Publish")
        assertEquals(result.getMessage(), "Attributes must contain valid uProtocol UUID in id property")
    }

    @Test
    @DisplayName("Test publish validateSink when sink is not empty")
    fun testUAttributeValidatorValidateSinkNotEmpty() {
        val message: UMessage = uMessage {
            forPublication(topicUUri)
        }
        val attributes: UAttributes = message.attributes.copy { sink = defaultUUri }
        val validator: UAttributesValidator = attributes.getValidator()
        val result = validator.validate(attributes)

        assertTrue(result.isFailure())
        assertEquals(validator.toString(), "UAttributesValidator.Publish")
        assertEquals(result.getMessage(), "Sink should not be present")
    }

    @Test
    @DisplayName("Test validateSink of a request message that is missing a sink")
    fun testUAttributeValidatorValidateSinkMissing() {
        val message: UMessage = uMessage {
            forRequest(defaultUUri, methodUUri, 1000)
        }

        val attributes =  message.attributes.copy { clearSink() }
        val validator: UAttributesValidator = attributes.getValidator()
        val result = validator.validate(attributes)

        assertTrue(result.isFailure())
        assertEquals(validator.toString(), "UAttributesValidator.Request")
        assertEquals(result.getMessage(), "Missing Sink")
    }

    @Test
    @DisplayName("Test validateTtl of a request message where ttl is less than 0")
    fun testUAttributeValidatorValidateTtlLessThanZero() {
        val message: UMessage = uMessage {
            forRequest(defaultUUri, methodUUri, -1)
        }
        val validator: UAttributesValidator = message.attributes.getValidator()
        val result = validator.validate(message.attributes)

        assertTrue(result.isFailure())
        assertEquals(validator.toString(), "UAttributesValidator.Request")
        assertEquals(result.getMessage(), "Invalid TTL [-1]")
    }

    @Test
    @DisplayName("Test validatePriority of a request message where priority is less than CS4")
    fun testUAttributeValidatorValidatePriorityLessThanCS4() {
        val message: UMessage = uMessage {
            forRequest(defaultUUri, methodUUri, 1000)
        }
        val attributes = message.attributes.copy {
            priority = UPriority.UPRIORITY_CS3
        }
        val validator: UAttributesValidator = attributes.getValidator()
        val result = validator.validate(attributes)

        assertTrue(result.isFailure())
        assertEquals(validator.toString(), "UAttributesValidator.Request")
        assertEquals(result.getMessage(), "Invalid UPriority [UPRIORITY_CS3]")
    }

    @Test
    @DisplayName("Test validateSink for a response message where the sink is missing")
    fun testUAttributeValidatorValidateSinkResponseMissing() {
        val request: UMessage = uMessage {
            forRequest(defaultUUri, methodUUri, 1000)
        }
        val response: UMessage = uMessage {
            forResponse(request.attributes)
        }
        val attributes = response.attributes.copy {
            clearSink()
        }
        val validator: UAttributesValidator = attributes.getValidator()
        val result = validator.validate(attributes)

        assertTrue(result.isFailure())
        assertEquals(validator.toString(), "UAttributesValidator.Response")
        assertEquals(result.getMessage(), "Missing Sink")
    }

    @Test
    @DisplayName("Test validateSink for a response message where the sink is the default instance")
    fun testUAttributeValidatorValidateSinkResponseDefault() {
        val request: UMessage = uMessage {
            forRequest(defaultUUri, methodUUri, 1000)
        }
        val response: UMessage = uMessage {
            forResponse(request.attributes)
        }
        val attributes = response.attributes.copy {
            sink = UUri.getDefaultInstance()
        }
        val validator: UAttributesValidator = attributes.getValidator()
        val result = validator.validate(attributes)

        assertTrue(result.isFailure())
        assertEquals(validator.toString(), "UAttributesValidator.Response")
        assertEquals(result.getMessage(), "Missing Sink")
    }

    @Test
    @DisplayName("Test validateSink for a response message where the sink is NOT the defaultResourceId")
    fun testUAttributeValidatorValidateSinkResponseDefaultResourceId() {
        val request: UMessage = uMessage {
            forRequest(methodUUri, defaultUUri, 1000)
        }
        val response: UMessage = uMessage {
            forResponse(request.attributes)
        }
        val validator: UAttributesValidator = response.attributes.getValidator()
        val result = validator.validate(response.attributes)

        assertTrue(result.isFailure())
        assertEquals(validator.toString(), "UAttributesValidator.Response")
        assertEquals(result.getMessage(), "Invalid Sink Uri")
    }

    @Test
    @DisplayName("Test validateReqId for a response message when the reqid is missing")
    fun testUAttributeValidatorValidateReqIdMissing() {
        val request: UMessage = uMessage {
            forRequest(defaultUUri, methodUUri, 1000)
        }
        val response: UMessage = uMessage {
            forResponse(request.attributes)
        }
        val attributes = response.attributes.copy {
            clearReqid()
        }
        val validator: UAttributesValidator = attributes.getValidator()
        val result = validator.validate(attributes)

        assertTrue(result.isFailure())
        assertEquals(validator.toString(), "UAttributesValidator.Response")
        assertEquals(result.getMessage(), "Missing correlationId")
    }

    @Test
    @DisplayName("Test validateReqId for a response message when the reqid is the default instance")
    fun testUAttributeValidatorValidateReqIdDefault() {
        val request: UMessage = uMessage {
            forRequest(defaultUUri, methodUUri, 1000)
        }
        val response: UMessage = uMessage {
            forResponse(request.attributes)
        }
        val attributes = response.attributes.copy { reqid = UUID.getDefaultInstance() }
        val validator: UAttributesValidator = attributes.getValidator()
        val result = validator.validate(attributes)

        assertTrue(result.isFailure())
        assertEquals(validator.toString(), "UAttributesValidator.Response")
        assertEquals(result.getMessage(), "Missing correlationId")
    }

    @Test
    @DisplayName("Test validateReqId for a response message when the reqid not a valid uprotocol UUID")
    fun testUAttributeValidatorValidateReqIdInvalid() {
        val request: UMessage = uMessage {
            forRequest(defaultUUri, methodUUri, 1000)
        }
        val response: UMessage = uMessage {
            forResponse(request.attributes)
        }
        val attributes = response.attributes.copy { reqid = uUID {
            lsb = -0x41524111
            msb = -0x21524111
        } }
        val validator: UAttributesValidator = attributes.getValidator()
        val result = validator.validate(attributes)

        assertTrue(result.isFailure())
        assertEquals(validator.toString(), "UAttributesValidator.Response")
        assertEquals(result.getMessage(), "Invalid correlation UUID")
    }
}
