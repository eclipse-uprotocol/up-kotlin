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

package org.eclipse.uprotocol.uuid.validator


import org.eclipse.uprotocol.uuid.factory.UUIDV6
import org.eclipse.uprotocol.uuid.factory.UUIDV7
import org.eclipse.uprotocol.uuid.factory.isUuidv6
import org.eclipse.uprotocol.uuid.serializer.deserializeAsUUID
import org.eclipse.uprotocol.uuid.validate.UUIDv6Validator
import org.eclipse.uprotocol.uuid.validate.UUIDV7Validator
import org.eclipse.uprotocol.uuid.validate.UuidValidator
import org.eclipse.uprotocol.v1.UCode
import org.eclipse.uprotocol.v1.UStatus
import org.eclipse.uprotocol.v1.UUID
import org.eclipse.uprotocol.v1.uUID
import org.eclipse.uprotocol.validation.ValidationResult
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import java.time.Instant

class UuidValidatorTest {
    @Test
    @DisplayName("Test validator with good uuid")
    fun test_validator_with_good_uuid() {
        val uuid: UUID = UUIDV7()
        val status: UStatus = UuidValidator.getValidator(uuid).validate(uuid)
        assertEquals(ValidationResult.STATUS_SUCCESS, status)
    }

    @Test
    @DisplayName("Test Good uuid Check")
    fun test_good_uuid_string() {
        val status: UStatus =
            UUIDV7Validator.validate(UUIDV7())
        assertEquals(status, ValidationResult.STATUS_SUCCESS)
    }

    @Test
    @DisplayName("Test fetching the invalid Validator for when UUID passed is garbage")
    fun test_invalid_uuid() {
        val uuid: UUID = uUID {
            msb = 0L
            lsb = 0L
        }
        val status: UStatus = UuidValidator.getValidator(uuid).validate(uuid)
        assertEquals(UCode.INVALID_ARGUMENT, status.code)
        assertEquals("Invalid UUID Version,Invalid UUID Variant,Invalid UUID Time", status.message)
    }

    @Test
    @DisplayName("Test invalid time uuid")
    fun test_invalid_time_uuid() {
        val uuid: UUID = UUIDV7(Instant.ofEpochSecond(0))
        val status = UUIDV7Validator.validate(uuid)
        assertEquals(UCode.INVALID_ARGUMENT, status.code)
        assertEquals("Invalid UUID Time", status.message)
    }

    @Test
    @DisplayName("Test UUIDV7 validator for invalid types")
    fun test_UUIDV7_with_invalid_types() {
        val uuidv6: UUID = UUIDV6()
        val uuid: UUID = uUID {
            msb = 0L
            lsb = 0L
        }
        val uuidJava: java.util.UUID = java.util.UUID.randomUUID()
        val uuidv4: UUID = uUID {
            msb = uuidJava.mostSignificantBits
            lsb = uuidJava.leastSignificantBits
        }
        val validator: UuidValidator = UUIDV7Validator
        assertNotNull(validator)
        val status: UStatus = validator.validate(uuidv6)
        assertEquals(UCode.INVALID_ARGUMENT, status.code)
        assertEquals("Invalid UUIDV7 Version", status.message)
        val status1: UStatus = validator.validate(uuid)
        assertEquals(UCode.INVALID_ARGUMENT, status1.code)
        assertEquals("Invalid UUIDV7 Version,Invalid UUID Time", status1.message)
        val status2: UStatus = validator.validate(uuidv4)
        assertEquals(UCode.INVALID_ARGUMENT, status2.code)
        assertEquals("Invalid UUIDV7 Version,Invalid UUID Time", status2.message)
    }

    @Test
    @DisplayName("Test good UUIDv6")
    fun test_good_uuidv6() {
        val uuid: UUID = UUIDV6()
        val validator: UuidValidator = UuidValidator.getValidator(uuid)
        assertNotNull(validator)
        assertTrue(uuid.isUuidv6())
        assertEquals(UCode.OK, validator.validate(uuid).code)
    }

    @Test
    @DisplayName("Test UUIDv6 with bad variant")
    fun test_uuidv6_with_bad_variant() {
        val uuid: UUID = "1ee57e66-d33a-65e0-4a77-3c3f061c1e9e".deserializeAsUUID()
        assertFalse(uuid == UUID.getDefaultInstance())
        val validator: UuidValidator = UuidValidator.getValidator(uuid)
        assertNotNull(validator)
        val status: UStatus = validator.validate(uuid)
        assertEquals("Invalid UUID Version,Invalid UUID Variant,Invalid UUID Time", status.message)
        assertEquals(UCode.INVALID_ARGUMENT, status.code)
    }

    @Test
    @DisplayName("Test UUIDv6 with invalid UUID")
    fun test_uuidv6_with_invalid_uuid() {
        val uuid: UUID = uUID {
            msb = 9 shl 12
            lsb = 0L
        }
        val validator: UuidValidator = UUIDv6Validator
        assertNotNull(validator)
        val status: UStatus = validator.validate(uuid)
        assertEquals("Not a UUIDv6 Version,Invalid UUIDv6 variant,Invalid UUID Time", status.message)
        assertEquals(UCode.INVALID_ARGUMENT, status.code)
    }

    @Test
    @DisplayName("Test using UUIDv6 Validator to validate a different types of UUIDs")
    fun test_uuidv6_with_UUIDV7() {
        val uuid: UUID = UUIDV7()
        val validator: UuidValidator = UUIDv6Validator
        assertNotNull(validator)
        val status: UStatus = validator.validate(uuid)
        assertEquals("Not a UUIDv6 Version", status.message)
        assertEquals(UCode.INVALID_ARGUMENT, status.code)
    }
}
