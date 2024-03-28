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
import org.eclipse.uprotocol.uri.Uri
import org.eclipse.uprotocol.uri.serializer.LongUriSerializer
import org.eclipse.uprotocol.uri.toUri
import org.eclipse.uprotocol.uri.validator.isRpcMethod
import org.eclipse.uprotocol.uri.validator.validate
import org.eclipse.uprotocol.v1.*
import org.eclipse.uprotocol.validation.ValidationResult

/**
 * Validates a CloudEvent using google.grpc.Status<br></br>
 *
 * [google.grpc.Status](https://grpc.github.io/grpc/core/md_doc_statuscodes.html)
 */
sealed class CloudEventValidator {
    protected abstract val typeName: String

    //hardcode to prevent obfuscation
    protected abstract val className: String

    /**
     * Validate the CloudEvent. A CloudEventValidator instance is obtained according to the type attribute on the CloudEvent.
     * @param cloudEvent The CloudEvent to validate.
     * @return Returns a UStatus with success or a UUStatus with failure containing all the errors that were found.
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

    /**
     * Validate the type of a cloud event.
     * @param cloudEvent The cloud event containing the source to validate.
     * @return Returns the ValidationResult containing a success or a failure with the error message.
     */
    fun validateType(cloudEvent: CloudEvent): ValidationResult {
        return if ("$typeName.v1" == cloudEvent.type) ValidationResult.success() else ValidationResult.failure(
            "Invalid CloudEvent type [${cloudEvent.type}]. CloudEvent of type $className must have a type of '$typeName.v1'"
        )
    }

    override fun toString(): String {
        return "CloudEventValidator.$className"
    }

    /**
     * Validate the sink value of a cloud event in the default scenario where the sink attribute is optional.
     * @param cloudEvent The cloud event containing the sink to validate.
     * @return Returns the ValidationResult containing a success or a failure with the error message.
     */
    open fun validateSink(cloudEvent: CloudEvent): ValidationResult {
        return UCloudEvent.getSink(cloudEvent)?.let { sink ->
            val checkSink = sink.validateUEntityUri()
            if (checkSink.isFailure()) {
                ValidationResult.failure("Invalid CloudEvent sink [${sink}]. ${checkSink.getMessage()}")
            } else {
                ValidationResult.success()
            }
        } ?: ValidationResult.success()
    }

    companion object {
        /**
         * Obtain a CloudEventValidator according to the type attribute in the CloudEvent.
         * @return Returns a CloudEventValidator according to the type attribute in the CloudEvent.
         */
        fun CloudEvent.getValidator(): CloudEventValidator {
            val cloudEventType: String? = type
            if (cloudEventType.isNullOrEmpty()) {
                return Publish
            }
            val validator: CloudEventValidator = when (UCloudEvent.getMessageType(cloudEventType)) {
                UMessageType.UMESSAGE_TYPE_NOTIFICATION -> Notification
                UMessageType.UMESSAGE_TYPE_RESPONSE -> Response
                UMessageType.UMESSAGE_TYPE_REQUEST -> Request
                else -> Publish
            }
            return validator
        }

        fun validateVersion(cloudEvent: CloudEvent): ValidationResult {
            return validateVersion(cloudEvent.specVersion.toString())
        }

        fun validateId(cloudEvent: CloudEvent): ValidationResult {
            return if (UCloudEvent.isCloudEventId(cloudEvent)) ValidationResult.success() else ValidationResult.failure(
                "Invalid CloudEvent Id [${cloudEvent.id}]. CloudEvent Id must be of type UUIDv8."
            )
        }

        /**
         * Validate an UriPart for a Software Entity must have an authority in the case of a microRemote uri, and must contain
         * the name of the USE.
         * @return Returns the ValidationResult containing a success or a failure with the error message.
         */
        fun Uri.validateUEntityUri(): ValidationResult {
            return LongUriSerializer.INSTANCE.deserialize(this.get()).validateUEntityUri()
        }

        /**
         * Validate an UriPart for a Software Entity must have an authority in the case of a microRemote uri, and must contain
         * the name of the USE.
         * @return Returns the ValidationResult containing a success or a failure with the error message.
         */
        fun UUri.validateUEntityUri(): ValidationResult {
            return validate()
        }

        fun validateVersion(version: String): ValidationResult {
            return if (version == "1.0") ValidationResult.success() else ValidationResult.failure(
                "Invalid CloudEvent version [$version]. CloudEvent version must be 1.0."
            )
        }

        /**
         * Validate a UriPart that is to be used as a topic in publish scenarios for events such as publish, file and notification.
         * @return Returns the ValidationResult containing a success or a failure with the error message.
         */
        fun Uri.validateTopicUri(): ValidationResult {
            return LongUriSerializer.INSTANCE.deserialize(this.get()).validateTopicUri()
        }

        /**
         * Validate a UriPart that is to be used as a topic in publish scenarios for events such as publish, file and notification.
         * @return Returns the ValidationResult containing a success or a failure with the error message.
         */
        fun UUri.validateTopicUri(): ValidationResult {
            val validationResult: ValidationResult = validateUEntityUri()
            if (validationResult.isFailure()) {
                return validationResult
            }
            val uResource: UResource = resource
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
         * @return Returns the ValidationResult containing a success or a failure with the error message.
         */
        fun Uri.validateRpcTopicUri(): ValidationResult {
            return LongUriSerializer.INSTANCE.deserialize(this.get()).validateRpcTopicUri()
        }

        /**
         * Validate an UriPart that is meant to be used as the application response topic for rpc calls. <br></br>
         * @return Returns the ValidationResult containing a success or a failure with the error message.
         */
        fun UUri.validateRpcTopicUri(): ValidationResult {
            val validationResult: ValidationResult = validateUEntityUri()
            if (validationResult.isFailure()) {
                return ValidationResult.failure("Invalid RPC uri application response topic. ${validationResult.getMessage()}")
            }
            val uResource: UResource = resource
            val topic = "${uResource.name}.${uResource.instance}"
            return if ("rpc.response" != topic) {
                ValidationResult.failure("Invalid RPC uri application response topic. UriPart is missing rpc.response.")
            } else ValidationResult.success()
        }

        /**
         * Validate a UriPart that is meant to be used as an RPC method URI. Used in Request sink values and Response source values.
         * @return Returns the ValidationResult containing a success or a failure with the error message.
         */
        fun Uri.validateRpcMethod(): ValidationResult {
            val uuri: UUri = LongUriSerializer.INSTANCE.deserialize(this.get())
            val validationResult: ValidationResult = uuri.validateUEntityUri()
            if (validationResult.isFailure()) {
                return ValidationResult.failure("Invalid RPC method uri. ${validationResult.getMessage()}")
            }
            return if (!uuri.isRpcMethod()) {
                ValidationResult.failure("Invalid RPC method uri. UriPart should be the method to be called, or method from response.")
            } else ValidationResult.success()
        }
    }
}

/**
 * Implements Validations for a CloudEvent of type Publish.
 */
object Publish : CloudEventValidator() {
    override val typeName: String = "pub"
    override val className: String = "Publish"

    override fun validateSource(cloudEvent: CloudEvent): ValidationResult {
        val source: Uri = cloudEvent.source.toString().toUri()
        val checkSource = source.validateTopicUri()
        return if (checkSource.isFailure()) {
            ValidationResult.failure(
                "Invalid Publish type CloudEvent source [${source}]. ${checkSource.getMessage()}"
            )
        } else ValidationResult.success()
    }
}

/**
 * Implements Validations for a CloudEvent of type Publish that behaves as a Notification, meaning
 * it must have a sink.
 */
object Notification : CloudEventValidator() {
    override val typeName: String = "not"
    override val className: String = "Notification"
    override fun validateSource(cloudEvent: CloudEvent): ValidationResult {
        val source: Uri = cloudEvent.source.toString().toUri()
        val checkSource = source.validateTopicUri()
        return if (checkSource.isFailure()) {
            ValidationResult.failure(
                "Invalid Notification type CloudEvent source [${source}]. ${checkSource.getMessage()}"
            )
        } else ValidationResult.success()
    }

    override fun validateSink(cloudEvent: CloudEvent): ValidationResult {
        val sink = UCloudEvent.getSink(cloudEvent)
            ?: return ValidationResult.failure("Invalid CloudEvent sink. Notification CloudEvent sink must be an uri.")
        val checkSink: ValidationResult = sink.validateUEntityUri()
        if (checkSink.isFailure()) {
            return ValidationResult.failure(
                "Invalid Notification type CloudEvent sink [${sink}]. ${checkSink.getMessage()}"
            )
        }
        return ValidationResult.success()
    }
}

/**
 * Implements Validations for a CloudEvent for RPC Request.
 */
object Request : CloudEventValidator() {
    override val typeName: String = "req"
    override val className: String = "Request"

    override fun validateSource(cloudEvent: CloudEvent): ValidationResult {
        val source = cloudEvent.source.toString().toUri()
        val checkSource: ValidationResult = source.validateRpcTopicUri()
        return if (checkSource.isFailure()) {
            ValidationResult.failure(
                "Invalid RPC Request CloudEvent source [${source}]. ${checkSource.getMessage()}"
            )
        } else ValidationResult.success()
    }


    override fun validateSink(cloudEvent: CloudEvent): ValidationResult {
        val sink = UCloudEvent.getSink(cloudEvent)
            ?: return ValidationResult.failure("Invalid RPC Request CloudEvent sink. Request CloudEvent sink must be uri for the method to be called.")
        val checkSink: ValidationResult = sink.validateRpcMethod()
        if (checkSink.isFailure()) {
            return ValidationResult.failure("Invalid RPC Request CloudEvent sink [${sink}]. ${checkSink.getMessage()}")
        }
        return ValidationResult.success()
    }
}

/**
 * Implements Validations for a CloudEvent for RPC Response.
 */
object Response : CloudEventValidator() {
    override val typeName: String = "res"
    override val className: String = "Response"

    override fun validateSource(cloudEvent: CloudEvent): ValidationResult {
        val source = cloudEvent.source.toString().toUri()
        val checkSource: ValidationResult = source.validateRpcMethod()
        return if (checkSource.isFailure()) {
            ValidationResult.failure(
                "Invalid RPC Response CloudEvent source [$source]. ${checkSource.getMessage()}"
            )
        } else ValidationResult.success()
    }

    override fun validateSink(cloudEvent: CloudEvent): ValidationResult {
        val sink = UCloudEvent.getSink(cloudEvent)
            ?: return ValidationResult.failure("Invalid CloudEvent sink. Response CloudEvent sink must be uri the destination of the response.")
        val checkSink: ValidationResult = sink.validateRpcTopicUri()
        if (checkSink.isFailure()) {
            return ValidationResult.failure(
                "Invalid RPC Response CloudEvent sink [$sink]. ${checkSink.getMessage()}"
            )
        }
        return ValidationResult.success()
    }
}
