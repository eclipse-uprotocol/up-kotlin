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
                UUIDV7Validator
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

data object UUIDV7Validator : UuidValidator() {
    override fun validateVersion(uuid: UUID): ValidationResult {
        return if (uuid.getVersion() == UUIDVersion.VERSION_UPROTOCOL) {
            ValidationResult.success()
        } else {
            ValidationResult.failure("Invalid UUIDV7 Version")
        }
    }

    override fun validateVariant(uuid: UUID): ValidationResult {
        return ValidationResult.success()
    }
}



