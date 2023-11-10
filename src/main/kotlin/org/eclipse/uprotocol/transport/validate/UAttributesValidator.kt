/*
 * Copyright (c) 2023 General Motors GTO LLC
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

package org.eclipse.uprotocol.transport.validate

import org.eclipse.uprotocol.transport.datamodel.UStatus
import org.eclipse.uprotocol.uri.validator.UriValidator
import org.eclipse.uprotocol.uuid.factory.UUIDUtils
import org.eclipse.uprotocol.v1.UAttributes
import org.eclipse.uprotocol.v1.UMessageType
import org.eclipse.uprotocol.v1.UUID
import org.eclipse.uprotocol.validation.ValidationResult
import java.util.*
import java.util.stream.Collectors
import java.util.stream.Stream

/**
 * [UAttributes] is the class that defines the Payload. It is the place for configuring time to live, priority,
 * security tokens and more.
 * Each UAttributes class defines a different type of message payload. The payload can represent a simple published
 * payload with some state change,
 * Payload representing an RPC request or Payload representing an RPC response.
 * UAttributesValidator is a base class for all UAttribute validators, that can help validate that the
 * [UAttributes] object is correctly defined
 * to define the Payload correctly.
 */
abstract class UAttributesValidator {
    /**
     * Take a [UAttributes] object and run validations.
     *
     * @param attributes The UAttriubes to validate.
     * @return Returns a [ValidationResult] that is success or failed with a message containing all validation
     * errors for
     * invalid configurations.
     */
    fun validate(attributes: UAttributes): ValidationResult {
        val errorMessage = Stream.of<ValidationResult?>(validateType(attributes),
                validateTtl(attributes), validateSink(attributes),
                validateCommStatus(attributes), validatePermissionLevel(attributes), validateReqId(attributes))
                .filter(ValidationResult::isFailure).map<String> { obj: ValidationResult? -> obj!!.getMessage() }.collect(Collectors.joining(","))
        return if (errorMessage.isBlank()) ValidationResult.success() else ValidationResult.failure(errorMessage)
    }


    /**
     * Indication if the Payload with these UAttributes is expired.
     *
     * @param uAttributes UAttributes with time to live value.
     * @return Returns a [ValidationResult] that is success meaning not expired or failed with a validation
     * message or expiration.
     */
    fun isExpired(uAttributes: UAttributes): ValidationResult {
        val ttl = uAttributes.ttl
        val maybeTime = UUIDUtils.getTime(uAttributes.id)
        //        if (maybeTime.isEmpty()) {
//            return ValidationResult.failure("Invalid Time");
//        }
        if (ttl <= 0) {
            return ValidationResult.success()
        }
        val delta = System.currentTimeMillis() - maybeTime.get()
        return if (delta >= ttl) ValidationResult.failure("Payload is expired") else ValidationResult.success()
    }

    /**
     * Validate the time to live configuration. If the UAttributes does not contain a time to live then the
     * ValidationResult is ok.
     *
     * @param attributes UAttributes object containing the message time to live configuration to validate.
     * @return Returns a [ValidationResult] that is success or failed with a failure message.
     */
    open fun validateTtl(attributes: UAttributes): ValidationResult? {
        val ttl = attributes.ttl
        return if (attributes.hasTtl() && ttl <= 0) {
            ValidationResult.failure(String.format("Invalid TTL [%s]", ttl))
        } else {
            ValidationResult.success()
        }
    }

    /**
     * Validate the sink UriPart for the default case. If the UAttributes does not contain a sink then the
     * ValidationResult is ok.
     *
     * @param attributes UAttributes object containing the sink to validate.
     * @return Returns a [ValidationResult] that is success or failed with a failure message.
     */
    open fun validateSink(attributes: UAttributes): ValidationResult? {
        return if (attributes.hasSink()) {
            UriValidator.validate(attributes.sink)
        } else {
            ValidationResult.success()
        }
    }

    /**
     * Validate the permissionLevel for the default case. If the UAttributes does not contain a permission level then
     * the ValidationResult is ok.
     *
     * @param attributes UAttributes object containing the permission level to validate.
     * @return Returns a ValidationResult indicating if the permissionLevel is valid or not.
     */
    fun validatePermissionLevel(attributes: UAttributes): ValidationResult {
        return if (!attributes.hasPermissionLevel() || attributes.permissionLevel > 0) {
            ValidationResult.success()
        } else {
            ValidationResult.failure("Invalid Permission Level")
        }
    }

    /**
     * Validate the commStatus for the default case. If the UAttributes does not contain a comm status then the
     * ValidationResult is ok.
     *
     * @param attributes UAttributes object containing the comm status to validate.
     * @return Returns a [ValidationResult] that is success or failed with a failure message.
     */
    fun validateCommStatus(attributes: UAttributes): ValidationResult {
        run {
            if (attributes.hasCommstatus()) {
                val enumValue = UStatus.Code.from(attributes.commstatus)
                return if (enumValue.isPresent) {
                    ValidationResult.success()
                } else {
                    ValidationResult.failure("Invalid Communication Status Code")
                }
            }
            return ValidationResult.success()
        }
    }

    /**
     * Validate the correlationId for the default case. If the UAttributes does not contain a request id then the
     * ValidationResult is ok.
     *
     * @param attributes Attributes object containing the request id to validate.
     * @return Returns a [ValidationResult] that is success or failed with a failure message.
     */
    open fun validateReqId(attributes: UAttributes): ValidationResult? {
        return if (attributes.hasReqid() && !UUIDUtils.isUuid(attributes.reqid)) {
            ValidationResult.failure("Invalid UUID")
        } else {
            ValidationResult.success()
        }
    }

    /**
     * Validate the [UMessageType] attribute, it is required.
     *
     * @param attributes UAttributes object containing the message type to validate.
     * @return Returns a [ValidationResult] that is success or failed with a failure message.
     */
    abstract fun validateType(attributes: UAttributes): ValidationResult?

    /**
     * Validators Factory. Example:
     * UAttributesValidator validateForPublishMessageType = UAttributesValidator.Validators.PUBLISH.validator()
     */
    enum class Validators(private val uattributesValidator: UAttributesValidator) {
        PUBLISH(Publish()),
        REQUEST(Request()),
        RESPONSE(Response());

        fun validator(): UAttributesValidator {
            return uattributesValidator
        }
    }

    /**
     * Implements validations for UAttributes that define a message that is meant for publishing state changes.
     */
    private class Publish : UAttributesValidator() {
        /**
         * Validates that attributes for a message meant to publish state changes has the correct type.
         *
         * @param attributes UAttributes object containing the message type to validate.
         * @return Returns a [ValidationResult] that is success or failed with a failure message.
         */
        override fun validateType(attributes: UAttributes): ValidationResult {
            return if (UMessageType.UMESSAGE_TYPE_PUBLISH == attributes.type) ValidationResult.success() else ValidationResult.failure(String.format("Wrong Attribute Type [%s]", attributes.type))
        }

        override fun toString(): String {
            return "UAttributesValidator.Publish"
        }
    }

    /**
     * Implements validations for UAttributes that define a message that is meant for an RPC request.
     */
    private class Request : UAttributesValidator() {
        /**
         * Validates that attributes for a message meant for an RPC request has the correct type.
         *
         * @param attributes UAttributes object containing the message type to validate.
         * @return Returns a [ValidationResult] that is success or failed with a failure message.
         */
        override fun validateType(attributes: UAttributes): ValidationResult {
            return if (UMessageType.UMESSAGE_TYPE_REQUEST == attributes.type) ValidationResult.success() else ValidationResult.failure(String.format("Wrong Attribute Type [%s]", attributes.type))
        }

        /**
         * Validates that attributes for a message meant for an RPC request has a destination sink.
         * In the case of an RPC request, the sink is required.
         *
         * @param attributes UAttributes object containing the sink to validate.
         * @return Returns a [ValidationResult] that is success or failed with a failure message.
         */
        override fun validateSink(attributes: UAttributes): ValidationResult? {
            return if (!attributes.hasSink()) {
                ValidationResult.failure("Missing Sink")
            } else UriValidator.validateRpcResponse(attributes.sink)
        }

        /**
         * Validate the time to live configuration.
         * In the case of an RPC request, the time to live is required.
         *
         * @param attributes UAttributes object containing the time to live to validate.
         * @return Returns a [ValidationResult] that is success or failed with a failure message.
         */
        override fun validateTtl(attributes: UAttributes): ValidationResult {
            if (!attributes.hasTtl()) {
                return ValidationResult.failure("Missing TTL")
            }
            val ttl = attributes.ttl
            return if (ttl <= 0) {
                ValidationResult.failure(String.format("Invalid TTL [%s]", ttl))
            } else {
                ValidationResult.success()
            }
        }

        override fun toString(): String {
            return "UAttributesValidator.Request"
        }
    }

    /**
     * Implements validations for UAttributes that define a message that is meant for an RPC response.
     */
    private class Response : UAttributesValidator() {
        /**
         * Validates that attributes for a message meant for an RPC response has the correct type.
         *
         * @param attributes UAttributes object containing the message type to validate.
         * @return Returns a [ValidationResult] that is success or failed with a failure message.
         */
        override fun validateType(attributes: UAttributes): ValidationResult {
            return if (UMessageType.UMESSAGE_TYPE_RESPONSE == attributes.type) ValidationResult.success() else ValidationResult.failure(String.format("Wrong Attribute Type [%s]", attributes.type))
        }

        /**
         * Validates that attributes for a message meant for an RPC response has a destination sink.
         * In the case of an RPC response, the sink is required.
         *
         * @param attributes UAttributes object containing the sink to validate.
         * @return Returns a [ValidationResult] that is success or failed with a failure message.
         */
        override fun validateSink(attributes: UAttributes): ValidationResult {
            Objects.requireNonNull(attributes, "UAttributes cannot be null.")
            val result = UriValidator.validateRpcMethod(attributes.sink)
            return if (result.isSuccess()) {
                result
            } else {
                ValidationResult.failure("Missing Sink")
            }
        }

        /**
         * Validate the correlationId. n the case of an RPC response, the correlation id is required.
         *
         * @param attributes UAttributes object containing the correlation id to validate.
         * @return Returns a [ValidationResult] that is success or failed with a failure message.
         */
        override fun validateReqId(attributes: UAttributes): ValidationResult {
            if (!attributes.hasReqid() || attributes.reqid === UUID.getDefaultInstance()) {
                return ValidationResult.failure("Missing correlationId")
            }
            return if (!UUIDUtils.isUuid(attributes.reqid)) {
                ValidationResult.failure(String.format("Invalid correlationId [%s]", attributes.reqid))
            } else {
                ValidationResult.success()
            }
        }

        override fun toString(): String {
            return "UAttributesValidator.Response"
        }
    }

    companion object {
        /**
         * Static factory method for getting a validator according to the [UMessageType] defined in the
         * [UAttributes].
         *
         * @param attribute UAttributes containing the UMessageType.
         * @return returns a UAttributesValidator according to the [UMessageType] defined in the [UAttributes].
         */
        fun getValidator(attribute: UAttributes): UAttributesValidator {
            return when (attribute.type) {
                UMessageType.UMESSAGE_TYPE_RESPONSE -> Validators.RESPONSE.validator()
                UMessageType.UMESSAGE_TYPE_REQUEST -> Validators.REQUEST.validator()
                else -> Validators.PUBLISH.validator()
            }
        }
    }
}
