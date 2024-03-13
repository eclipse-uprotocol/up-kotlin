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

package org.eclipse.uprotocol.cloudevent.validate

import io.cloudevents.CloudEvent
import org.eclipse.uprotocol.cloudevent.factory.UCloudEvent
import org.eclipse.uprotocol.uri.serializer.LongUriSerializer
import org.eclipse.uprotocol.uri.validator.isRpcMethod
import org.eclipse.uprotocol.uri.validator.validate
import org.eclipse.uprotocol.v1.*
import org.eclipse.uprotocol.validation.ValidationResult


/**
 * Validates a CloudEvent using google.grpc.Status<br></br>
 *
 * [google.grpc.Status](https://grpc.github.io/grpc/core/md_doc_statuscodes.html)
 */
abstract class CloudEventValidator {
    /**
     * Enum that hold the implementations of CloudEventValidator according to type.
     */
    enum class Validators(private val cloudEventValidator: CloudEventValidator) {
        PUBLISH(Publish()),
        NOTIFICATION(Notification()),
        REQUEST(Request()),
        RESPONSE(Response());

        fun validator(): CloudEventValidator {
            return cloudEventValidator
        }
    }

    /**
     * Validate the CloudEvent. A CloudEventValidator instance is obtained according to the type attribute on the CloudEvent.
     * @param cloudEvent The CloudEvent to validate.
     * @return Returns a UStatus with success or a UUStatuswith failure containing all the errors that were found.
     */
    fun validate(cloudEvent: CloudEvent): ValidationResult {
        val errorMessage = listOf(
            validateVersion(cloudEvent),
            validateId(cloudEvent),
            validateSource(cloudEvent),
            validateType(cloudEvent),
            validateSink(cloudEvent)
        ).filter { it.isFailure() }.joinToString(",") { it.getMessage() }
        return if (errorMessage.isBlank()) {
            ValidationResult.success()
        } else {
            ValidationResult.failure(errorMessage)

        }
    }

    /**
     * Validate the source value of a cloud event.
     * @param cloudEvent The cloud event containing the source to validate.
     * @return Returns the ValidationResult containing a success or a failure with the error message.
     */
    abstract fun validateSource(cloudEvent: CloudEvent): ValidationResult
    abstract fun validateType(cloudEvent: CloudEvent): ValidationResult

    /**
     * Validate the sink value of a cloud event in the default scenario where the sink attribute is optional.
     * @param cloudEvent The cloud event containing the sink to validate.
     * @return Returns the ValidationResult containing a success or a failure with the error message.
     */
    open fun validateSink(cloudEvent: CloudEvent): ValidationResult {
        return UCloudEvent.getSink(cloudEvent)?.let { sink ->
            val checkSink = validateUEntityUri(sink)
            if (checkSink.isFailure()) {
                ValidationResult.failure("Invalid CloudEvent sink [${sink}]. ${checkSink.getMessage()}")
            } else {
                ValidationResult.success()
            }
        } ?: ValidationResult.success()
    }


    /**
     * Implements Validations for a CloudEvent of type Publish.
     */
    private open class Publish : CloudEventValidator() {
        override fun validateSource(cloudEvent: CloudEvent): ValidationResult {
            val source: String = cloudEvent.source.toString()
            val checkSource = validateTopicUri(source)
            return if (checkSource.isFailure()) {
                ValidationResult.failure(
                    "Invalid Publish type CloudEvent source [${source}]. ${checkSource.getMessage()}"
                )
            } else ValidationResult.success()
        }

        override fun validateType(cloudEvent: CloudEvent): ValidationResult {
            return if ("pub.v1" == cloudEvent.type) ValidationResult.success() else ValidationResult.failure(
                "Invalid CloudEvent type [${cloudEvent.type}]. CloudEvent of type Publish must have a type of 'pub.v1'"
            )
        }

        override fun toString(): String {
            return "CloudEventValidator.Publish"
        }
    }

    /**
     * Implements Validations for a CloudEvent of type Publish that behaves as a Notification, meaning
     * it must have a sink.
     */
    private class Notification : Publish() {
        override fun validateSink(cloudEvent: CloudEvent): ValidationResult {
            val sink: String = UCloudEvent.getSink(cloudEvent)
                ?: return ValidationResult.failure("Invalid CloudEvent sink. Notification CloudEvent sink must be an uri.")
            val checkSink: ValidationResult = validateUEntityUri(sink)
            if (checkSink.isFailure()) {
                return ValidationResult.failure(
                    "Invalid Notification type CloudEvent sink [${sink}]. ${checkSink.getMessage()}"
                )
            }
            return ValidationResult.success()
        }

        override fun toString(): String {
            return "CloudEventValidator.Notification"
        }
    }

    /**
     * Implements Validations for a CloudEvent for RPC Request.
     */
    private class Request : CloudEventValidator() {
        override fun validateSource(cloudEvent: CloudEvent): ValidationResult {
            val source: String = cloudEvent.source.toString()
            val checkSource: ValidationResult = validateRpcTopicUri(source)
            return if (checkSource.isFailure()) {
                ValidationResult.failure(
                    "Invalid RPC Request CloudEvent source [${source}]. ${checkSource.getMessage()}"
                )
            } else ValidationResult.success()
        }


        override fun validateSink(cloudEvent: CloudEvent): ValidationResult {
            val sink: String = UCloudEvent.getSink(cloudEvent)
                ?: return ValidationResult.failure("Invalid RPC Request CloudEvent sink. Request CloudEvent sink must be uri for the method to be called.")
            val checkSink: ValidationResult = validateRpcMethod(sink)
            if (checkSink.isFailure()) {
                return ValidationResult.failure("Invalid RPC Request CloudEvent sink [${sink}]. ${checkSink.getMessage()}")
            }
            return ValidationResult.success()
        }

        override fun validateType(cloudEvent: CloudEvent): ValidationResult {
            return if ("req.v1" == cloudEvent.type) ValidationResult.success() else ValidationResult.failure(
                "Invalid CloudEvent type [${cloudEvent.type}]. CloudEvent of type Request must have a type of 'req.v1'"
            )
        }

        override fun toString(): String {
            return "CloudEventValidator.Request"
        }
    }

    /**
     * Implements Validations for a CloudEvent for RPC Response.
     */
    private class Response : CloudEventValidator() {
        override fun validateSource(cloudEvent: CloudEvent): ValidationResult {
            val source: String = cloudEvent.source.toString()
            val checkSource: ValidationResult = validateRpcMethod(source)
            return if (checkSource.isFailure()) {
                ValidationResult.failure(
                    "Invalid RPC Response CloudEvent source [$source]. ${checkSource.getMessage()}"
                )
            } else ValidationResult.success()
        }

        override fun validateSink(cloudEvent: CloudEvent): ValidationResult {
            val sink: String = UCloudEvent.getSink(cloudEvent)
                ?: return ValidationResult.failure("Invalid CloudEvent sink. Response CloudEvent sink must be uri the destination of the response.")
            val checkSink: ValidationResult = validateRpcTopicUri(sink)
            if (checkSink.isFailure()) {
                return ValidationResult.failure(
                    "Invalid RPC Response CloudEvent sink [${sink}]. ${checkSink.getMessage()}"
                )

            }
            return ValidationResult.success()
        }

        override fun validateType(cloudEvent: CloudEvent): ValidationResult {
            return if ("res.v1" == cloudEvent.type) ValidationResult.success() else ValidationResult.failure(
                "Invalid CloudEvent type [${cloudEvent.type}]. CloudEvent of type Response must have a type of 'res.v1'"
            )
        }

        override fun toString(): String {
            return "CloudEventValidator.Response"
        }
    }

    companion object {
        /**
         * Obtain a CloudEventValidator according to the type attribute in the CloudEvent.
         * @param cloudEvent The CloudEvent with the type attribute.
         * @return Returns a CloudEventValidator according to the type attribute in the CloudEvent.
         */
        fun getValidator(cloudEvent: CloudEvent): CloudEventValidator {
            val cloudEventType: String? = cloudEvent.type
            if (cloudEventType.isNullOrEmpty()) {
                return Validators.PUBLISH.validator()
            }
            val validator: CloudEventValidator = when (UCloudEvent.getMessageType(cloudEventType)) {
                UMessageType.UMESSAGE_TYPE_RESPONSE -> Validators.RESPONSE.validator()
                UMessageType.UMESSAGE_TYPE_REQUEST -> Validators.REQUEST.validator()
                else -> Validators.PUBLISH.validator()
            }
            return validator
        }

        fun validateVersion(cloudEvent: CloudEvent): ValidationResult {
            return validateVersion(cloudEvent.specVersion.toString())
        }

        private fun validateVersion(version: String): ValidationResult {
            return if (version == "1.0") ValidationResult.success() else ValidationResult.failure(
                "Invalid CloudEvent version [$version]. CloudEvent version must be 1.0."
            )
        }

        fun validateId(cloudEvent: CloudEvent): ValidationResult {
            return if (UCloudEvent.isCloudEventId(cloudEvent)) ValidationResult.success() else ValidationResult.failure(
                "Invalid CloudEvent Id [${cloudEvent.id}]. CloudEvent Id must be of type UUIDv8."
            )
        }

        /**
         * Validate an UriPart for a Software Entity must have an authority in the case of a microRemote uri, and must contain
         * the name of the USE.
         * @param uri uri string to validate.
         * @return Returns the ValidationResult containing a success or a failure with the error message.
         */
        fun validateUEntityUri(uri: String): ValidationResult {
            return validateUEntityUri(LongUriSerializer.INSTANCE.deserialize(uri))
        }

        /**
         * Validate an UriPart for a Software Entity must have an authority in the case of a microRemote uri, and must contain
         * the name of the USE.
         * @param uri UUri to validate.
         * @return Returns the ValidationResult containing a success or a failure with the error message.
         */
        fun validateUEntityUri(uri: UUri): ValidationResult {
            return uri.validate()
        }

        /**
         * Validate a UriPart that is to be used as a topic in publish scenarios for events such as publish, file and notification.
         * @param uri String UriPart to validate.
         * @return Returns the ValidationResult containing a success or a failure with the error message.
         */
        fun validateTopicUri(uri: String): ValidationResult {
            return validateTopicUri(LongUriSerializer.INSTANCE.deserialize(uri))
        }

        /**
         * Validate a UriPart that is to be used as a topic in publish scenarios for events such as publish, file and notification.
         * @param uri UUri UriPart to validate.
         * @return Returns the ValidationResult containing a success or a failure with the error message.
         */
        fun validateTopicUri(uri: UUri): ValidationResult {
            val validationResult: ValidationResult = validateUEntityUri(uri)
            if (validationResult.isFailure()) {
                return validationResult
            }
            val uResource: UResource = uri.resource
            if (uResource.name.isBlank()) {
                return ValidationResult.failure("UriPart is missing uResource name.")
            }
            return if (uResource.message.isEmpty()) {
                ValidationResult.failure("UriPart is missing Message information.")
            } else ValidationResult.success()
        }

        /**
         * Validate a UriPart that is meant to be used as the application response topic for rpc calls. <br></br>
         * Used in Request source values and Response sink values.
         * @param uri String UriPart to validate.
         * @return Returns the ValidationResult containing a success or a failure with the error message.
         */
        fun validateRpcTopicUri(uri: String): ValidationResult {
            return validateRpcTopicUri(LongUriSerializer.INSTANCE.deserialize(uri))
        }

        /**
         * Validate an UriPart that is meant to be used as the application response topic for rpc calls. <br></br>
         * @param uri UUri UriPart to validate.
         * @return Returns the ValidationResult containing a success or a failure with the error message.
         */
        fun validateRpcTopicUri(uri: UUri): ValidationResult {
            val validationResult: ValidationResult = validateUEntityUri(uri)
            if (validationResult.isFailure()) {
                return ValidationResult.failure("Invalid RPC uri application response topic. ${validationResult.getMessage()}")
            }
            val uResource: UResource = uri.resource
            val topic = "${uResource.name}.${uResource.instance}"
            return if ("rpc.response" != topic) {
                ValidationResult.failure("Invalid RPC uri application response topic. UriPart is missing rpc.response.")
            } else ValidationResult.success()
        }

        /**
         * Validate a UriPart that is meant to be used as an RPC method URI. Used in Request sink values and Response source values.
         * @param uri String UriPart to validate.
         * @return Returns the ValidationResult containing a success or a failure with the error message.
         */
        fun validateRpcMethod(uri: String): ValidationResult {
            val uuri: UUri = LongUriSerializer.INSTANCE.deserialize(uri)
            val validationResult: ValidationResult = validateUEntityUri(uuri)
            if (validationResult.isFailure()) {
                return ValidationResult.failure("Invalid RPC method uri. ${validationResult.getMessage()}")
            }
            return if (!uuri.isRpcMethod()) {
                ValidationResult.failure("Invalid RPC method uri. UriPart should be the method to be called, or method from response.")
            } else ValidationResult.success()
        }
    }
}
