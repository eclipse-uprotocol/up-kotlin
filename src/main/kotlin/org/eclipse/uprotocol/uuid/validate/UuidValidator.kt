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
 * SPDX-FileType: SOURCE
 * SPDX-FileCopyrightText: 2023 General Motors GTO LLC
 * SPDX-License-Identifier: Apache-2.0
 */

package org.eclipse.uprotocol.uuid.validate

import com.github.f4b6a3.uuid.enums.UuidVariant
import com.google.rpc.Code
import com.google.rpc.Status
import org.eclipse.uprotocol.uuid.factory.UuidUtils
import org.eclipse.uprotocol.v1.UUID
import org.eclipse.uprotocol.validation.ValidationResult
import java.util.*

/**
 * UUID Validator class that validates UUIDs
 */
abstract class UuidValidator {
    enum class Validators(private val uuidValidator: UuidValidator) {
        UNKNOWN(InvalidValidator()),
        UUIDV6(UUIDv6Validator()),
        UPROTOCOL(UUIDv8Validator());

        fun validator(): UuidValidator {
            return uuidValidator
        }
    }

    fun validate(uuid: UUID?): Status {
        val errorMessages = listOf(
                validateVersion(uuid),
                validateVariant(uuid),
                validateTime(uuid)
        )
                .filter { it.isFailure() }.joinToString(",") { it.getMessage() }

        return if (errorMessages.isBlank()) ValidationResult.success().toStatus()
        else Status.newBuilder()
                .setCode(Code.INVALID_ARGUMENT_VALUE)
                .setMessage(errorMessages)
                .build()
    }


    abstract fun validateVersion(uuid: UUID?): ValidationResult
    private fun validateTime(uuid: UUID?): ValidationResult {
        val time: Optional<Long> = UuidUtils.getTime(uuid)
        return if (time.isPresent && time.get() > 0) ValidationResult.success() else ValidationResult.failure(
            String.format(
                "Invalid UUID Time"
            )
        )
    }

    abstract fun validateVariant(uuid: UUID?): ValidationResult
    private class InvalidValidator : UuidValidator() {
        override fun validateVersion(uuid: UUID?): ValidationResult {
            return ValidationResult.failure(String.format("Invalid UUID Version"))
        }

        override fun validateVariant(uuid: UUID?): ValidationResult {
            return ValidationResult.failure(String.format("Invalid UUID Variant"))
        }
    }

    private class UUIDv6Validator : UuidValidator() {
        override fun validateVersion(uuid: UUID?): ValidationResult {
            val version: Optional<UuidUtils.Version> = UuidUtils.getVersion(uuid)
            return if (version.isPresent && version.get() == UuidUtils.Version.VERSION_TIME_ORDERED) ValidationResult.success() else ValidationResult.failure(
                String.format("Not a UUIDv6 Version")
            )
        }

        override fun validateVariant(uuid: UUID?): ValidationResult {
            val variant: Optional<Int> = UuidUtils.getVariant(uuid)
            return if (variant.isPresent && variant.get() == UuidVariant.VARIANT_RFC_4122.value) ValidationResult.success() else ValidationResult.failure(
                String.format("Invalid UUIDv6 variant")
            )
        }
    }

    private class UUIDv8Validator : UuidValidator() {
        override fun validateVersion(uuid: UUID?): ValidationResult {
            val version: Optional<UuidUtils.Version> = UuidUtils.getVersion(uuid)
            return if (version.isPresent && version.get() == UuidUtils.Version.VERSION_UPROTOCOL) ValidationResult.success() else ValidationResult.failure(
                String.format("Invalid UUIDv8 Version")
            )
        }

        override fun validateVariant(uuid: UUID?): ValidationResult {
            return ValidationResult.success()
        }
    }

    companion object {
        fun getValidator(uuid: UUID?): UuidValidator {
            return if (UuidUtils.isUuidv6(uuid)) {
                Validators.UUIDV6.validator()
            } else if (UuidUtils.isUProtocol(uuid)) {
                Validators.UPROTOCOL.validator()
            } else {
                Validators.UNKNOWN.validator()
            }
        }
    }
}