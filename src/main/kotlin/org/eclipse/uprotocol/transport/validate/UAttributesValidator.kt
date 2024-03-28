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
 * SPDX-FileType: SOURCE
 * SPDX-FileCopyrightText: 2023 General Motors GTO LLC
 * SPDX-License-Identifier: Apache-2.0
 */

package org.eclipse.uprotocol.transport.validate

import org.eclipse.uprotocol.uri.validator.validate
import org.eclipse.uprotocol.uri.validator.validateRpcMethod
import org.eclipse.uprotocol.uri.validator.validateRpcResponse
import org.eclipse.uprotocol.uuid.factory.getTime
import org.eclipse.uprotocol.uuid.factory.isUuid
import org.eclipse.uprotocol.v1.*
import org.eclipse.uprotocol.validation.ValidationResult


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
sealed class UAttributesValidator {
    //hardcode to prevent obfuscation
    protected abstract val className: String
    protected abstract val type: UMessageType
    /**
     * Take a [UAttributes] object and run validations.
     *
     * @param attributes The UAttributes to validate.
     * @return Returns a [ValidationResult] that is success or failed with a message containing all validation
     * errors for
     * invalid configurations.
     */
    fun validate(attributes: UAttributes): ValidationResult {
        val errorMessage = listOf(
            validateType(attributes),
            validateTtl(attributes), validateSink(attributes), validatePriority(attributes),
            validatePermissionLevel(attributes), validateReqId(attributes)
        ).filter {
            it.isFailure()
        }.joinToString(",") { obj -> obj.getMessage() }
        return if (errorMessage.isBlank()) ValidationResult.success() else ValidationResult.failure(errorMessage)
    }


    /**
     * Check the time-to-live attribute to see if it has expired. <br>
     * The message has expired when the current time is greater than the original UUID time
     * plus the ttl attribute.
     *
     * @param uAttributes UAttributes with time to live value.
     * @return Returns a true if the original time plus the ttl is less than the current time
     */
    fun isExpired(uAttributes: UAttributes): Boolean {
        val ttl = uAttributes.ttl
        val maybeTime = uAttributes.id.getTime()

        // if the original time is not present or the ttl is less than 0
        if (maybeTime == null || ttl <= 0) {
            return false
        }
        // the original time plus the ttl is less than the current time, the message has expired
        return (maybeTime + ttl) < System.currentTimeMillis()
    }

    /**
     * Validate the time to live configuration. If the UAttributes does not contain a time to live then the
     * ValidationResult is ok.
     *
     * @param attributes UAttributes object containing the message time to live configuration to validate.
     * @return Returns a [ValidationResult] that is success or failed with a failure message.
     */
    open fun validateTtl(attributes: UAttributes): ValidationResult {
        val ttl = attributes.ttl
        return if (attributes.hasTtl() && ttl <= 0) {
            ValidationResult.failure("Invalid TTL [$ttl]")
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
    open fun validateSink(attributes: UAttributes): ValidationResult {
        return if (attributes.hasSink()) {
            attributes.sink.validate()
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
     * Validate the correlationId for the default case. If the UAttributes does not contain a request id then the
     * ValidationResult is ok.
     *
     * @param attributes Attributes object containing the request id to validate.
     * @return Returns a [ValidationResult] that is success or failed with a failure message.
     */
    open fun validateReqId(attributes: UAttributes): ValidationResult {
        return if (attributes.hasReqid() && !attributes.reqid.isUuid()) {
            ValidationResult.failure("Invalid UUID")
        } else {
            ValidationResult.success()
        }
    }

    /**
     * Validate the priority value to ensure it is one of the known CS values
     *
     * @param attributes Attributes object containing the Priority to validate.
     * @return Returns a [ValidationResult] that is success or failed with a failure message.
     */
    open fun validatePriority(attributes: UAttributes): ValidationResult {
        return if (attributes.priority.number >= UPriority.UPRIORITY_CS0_VALUE) {
            ValidationResult.success()
        } else {
            ValidationResult.failure("Invalid UPriority [${attributes.priority.name}]")
        }
    }


    /**
     * Validate the [UMessageType] attribute, it is required.
     *
     * @param attributes UAttributes object containing the message type to validate.
     * @return Returns a [ValidationResult] that is success or failed with a failure message.
     */
    fun validateType(attributes: UAttributes): ValidationResult{
        return if (type == attributes.type) {
            ValidationResult.success()
        } else {
            ValidationResult.failure("Wrong Attribute Type [${attributes.type}]")
        }
    }

    override fun toString(): String {
        return "UAttributesValidator.$className"
    }

    companion object {
        /**
         * Static factory method for getting a validator according to the [UMessageType] defined in the
         * [UAttributes].
         *
         * @return returns a UAttributesValidator according to the [UMessageType] defined in the [UAttributes].
         */
        fun UAttributes.getValidator(): UAttributesValidator {
            return when (type) {
                UMessageType.UMESSAGE_TYPE_RESPONSE -> Response
                UMessageType.UMESSAGE_TYPE_REQUEST -> Request
                UMessageType.UMESSAGE_TYPE_NOTIFICATION -> Notification
                else -> Publish
            }
        }
    }
}


/**
 * Implements validations for UAttributes that define a message that is meant for publishing state changes.
 */
object Publish : UAttributesValidator() {
    override val className: String = "Publish"
    override val type: UMessageType = UMessageType.UMESSAGE_TYPE_PUBLISH
}

/**
 * Implements validations for UAttributes that define a message that is meant for notifications.
 */
object Notification: UAttributesValidator(){
    override val className: String = "Notification"
    override val type: UMessageType = UMessageType.UMESSAGE_TYPE_NOTIFICATION

    /**
     * Validates that attributes for a message meant for notifications has a destination sink.
     * In the case of a notification, the sink is required.
     *
     * @param attributes UAttributes object containing the sink to validate.
     * @return Returns a [ValidationResult] that is success or failed with a failure message.
     */
    override fun validateSink(attributes: UAttributes): ValidationResult {
        if (!attributes.hasSink() || attributes.sink == UUri.getDefaultInstance()) {
            return ValidationResult.failure("Missing Sink")
        }
        return ValidationResult.success()
    }
}
/**
 * Implements validations for UAttributes that define a message that is meant for an RPC request.
 */
object Request : UAttributesValidator() {
    override val className: String = "Request"
    override val type: UMessageType = UMessageType.UMESSAGE_TYPE_REQUEST

    /**
     * Validates that attributes for a message meant for an RPC request has a destination sink.
     * In the case of an RPC request, the sink is required.
     *
     * @param attributes UAttributes object containing the sink to validate.
     * @return Returns a [ValidationResult] that is success or failed with a failure message.
     */
    override fun validateSink(attributes: UAttributes): ValidationResult {
        return if (!attributes.hasSink()) {
            ValidationResult.failure("Missing Sink")
        } else {
            attributes.sink.validateRpcMethod()
        }
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
        return if (attributes.ttl <= 0) {
            ValidationResult.failure("Invalid TTL [${attributes.ttl}]")
        } else {
            ValidationResult.success()
        }
    }

    /**
     * Validate the priority value to ensure it is one of the known CS values
     *
     * @param attributes Attributes object containing the Priority to validate.
     * @return Returns a [ValidationResult] that is success or failed with a failure message.
     */
    override fun validatePriority(attributes: UAttributes): ValidationResult {
        return if (attributes.priority.number >= UPriority.UPRIORITY_CS4_VALUE) {
            ValidationResult.success()
        } else {
            ValidationResult.failure("Invalid UPriority [${attributes.priority.name}]")
        }
    }
}

/**
 * Implements validations for UAttributes that define a message that is meant for an RPC response.
 */
object Response : UAttributesValidator() {
    override val className: String = "Response"
    override val type: UMessageType = UMessageType.UMESSAGE_TYPE_RESPONSE
    /**
     * Validates that attributes for a message meant for an RPC response has a destination sink.
     * In the case of an RPC response, the sink is required.
     *
     * @param attributes UAttributes object containing the sink to validate.
     * @return Returns a [ValidationResult] that is success or failed with a failure message.
     */
    override fun validateSink(attributes: UAttributes): ValidationResult {
        if (!attributes.hasSink() || attributes.sink === UUri.getDefaultInstance()) {
            return ValidationResult.failure("Missing Sink")
        }
        return attributes.sink.validateRpcResponse()
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
        return if (!attributes.reqid.isUuid()) {
            ValidationResult.failure("Invalid correlationId [${attributes.reqid}]")
        } else {
            ValidationResult.success()
        }
    }

    /**
     * Validate the priority value to ensure it is one of the known CS values
     *
     * @param attributes Attributes object containing the Priority to validate.
     * @return Returns a [ValidationResult] that is success or failed with a failure message.
     */
    override fun validatePriority(attributes: UAttributes): ValidationResult {
        return if (attributes.priority.number >= UPriority.UPRIORITY_CS4_VALUE) {
            ValidationResult.success()
        } else ValidationResult.failure(
            "Invalid UPriority [${attributes.priority.name}]"
        )
    }
}
