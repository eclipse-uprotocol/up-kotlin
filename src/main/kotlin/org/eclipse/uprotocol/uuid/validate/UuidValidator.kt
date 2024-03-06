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

package org.eclipse.uprotocol.uuid.validate

import com.github.f4b6a3.uuid.enums.UuidVariant
import org.eclipse.uprotocol.uuid.factory.*
import org.eclipse.uprotocol.v1.UCode
import org.eclipse.uprotocol.v1.UStatus
import org.eclipse.uprotocol.v1.UUID
import org.eclipse.uprotocol.v1.uStatus
import org.eclipse.uprotocol.validation.ValidationResult


/**
 * UUID Validator class that validates UUIDs
 */
sealed class UuidValidator {
    abstract fun validateVersion(uuid: UUID): ValidationResult
    abstract fun validateVariant(uuid: UUID): ValidationResult

    fun validate(uuid: UUID): UStatus {
        val errorMessages = listOf(
            validateVersion(uuid), validateVariant(uuid), validateTime(uuid)
        ).filter { it.isFailure() }.joinToString(",") { it.getMessage() }

        return if (errorMessages.isBlank()) ValidationResult.success().toStatus()
        else uStatus {
            code = UCode.INVALID_ARGUMENT
            message = errorMessages
        }
    }

    private fun validateTime(uuid: UUID): ValidationResult {
        val time = uuid.getTime()
        return if (time != null && time > 0) {
            ValidationResult.success()
        } else {
            ValidationResult.failure("Invalid UUID Time")
        }
    }

    companion object {
        fun getValidator(uuid: UUID): UuidValidator {
            return if (uuid.isUuidv6()) {
                UUIDv6Validator
            } else if (uuid.isUProtocol()) {
                UUIDv8Validator
            } else {
                InvalidValidator
            }
        }
    }
}

data object InvalidValidator : UuidValidator() {
    override fun validateVersion(uuid: UUID): ValidationResult {
        return ValidationResult.failure("Invalid UUID Version")
    }

    override fun validateVariant(uuid: UUID): ValidationResult {
        return ValidationResult.failure("Invalid UUID Variant")
    }
}

data object UUIDv6Validator : UuidValidator() {
    override fun validateVersion(uuid: UUID): ValidationResult {
        return if (uuid.getVersion() == UUIDVersion.VERSION_TIME_ORDERED) {
            ValidationResult.success()
        } else {
            ValidationResult.failure("Not a UUIDv6 Version")
        }
    }

    override fun validateVariant(uuid: UUID): ValidationResult {
        return if (uuid.getVariant() == UuidVariant.VARIANT_RFC_4122.value) {
            ValidationResult.success()
        } else {
            ValidationResult.failure("Invalid UUIDv6 variant")
        }
    }
}

data object UUIDv8Validator : UuidValidator() {
    override fun validateVersion(uuid: UUID): ValidationResult {
        return if (uuid.getVersion() == UUIDVersion.VERSION_UPROTOCOL) {
            ValidationResult.success()
        } else {
            ValidationResult.failure("Invalid UUIDv8 Version")
        }
    }

    override fun validateVariant(uuid: UUID): ValidationResult {
        return ValidationResult.success()
    }
}



