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
package org.eclipse.uprotocol.uuid.validator

import com.google.rpc.Code
import com.google.rpc.Status
import org.eclipse.uprotocol.uuid.factory.UuidFactory
import org.eclipse.uprotocol.uuid.factory.UuidUtils
import org.eclipse.uprotocol.uuid.serializer.LongUuidSerializer
import org.eclipse.uprotocol.uuid.validate.UuidValidator
import org.eclipse.uprotocol.v1.UUID
import org.eclipse.uprotocol.validation.ValidationResult
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import java.time.Instant
import org.junit.jupiter.api.Assertions.*

class UuidValidatorTest {
    @Test
    @DisplayName("Test validator with good uuid")
    fun test_validator_with_good_uuid() {
        //final UuidValidator validator = new UuidValidator();
        val uuid: UUID = UuidFactory.Factories.UPROTOCOL.factory().create()
        val status: Status = UuidValidator.getValidator(uuid).validate(uuid)
        assertEquals(ValidationResult.STATUS_SUCCESS, status)
    }

    @Test
    @DisplayName("Test Good uuid Check")
    fun test_good_uuid_string() {
        val status: Status = UuidValidator.Validators.UPROTOCOL.validator()
            .validate(UuidFactory.Factories.UPROTOCOL.factory().create())
        assertEquals(status, ValidationResult.STATUS_SUCCESS)
    }

    @Test
    @DisplayName("Test fetching the invalid Validator for when UUID passed is garbage")
    fun test_invalid_uuid() {
        val uuid: UUID = UUID.newBuilder().setMsb(0L).setLsb(0L).build()
        val status: Status = UuidValidator.getValidator(uuid).validate(uuid)
        assertEquals(Code.INVALID_ARGUMENT_VALUE, status.code)
        assertEquals("Invalid UUID Version,Invalid UUID Variant,Invalid UUID Time", status.message)
    }

    @Test
    @DisplayName("Test invalid time uuid")
    fun test_invalid_time_uuid() {
        val uuid: UUID = UuidFactory.Factories.UPROTOCOL.factory().create(Instant.ofEpochSecond(0))
        val status: Status = UuidValidator.Validators.UPROTOCOL.validator().validate(uuid)
        assertEquals(Code.INVALID_ARGUMENT_VALUE, status.code)
        assertEquals("Invalid UUID Time", status.message)
    }

    @Test
    @DisplayName("Test UUIDv8 validator for null UUID")
    fun test_uuidv8_with_invalid_uuids() {
        val validator: UuidValidator = UuidValidator.Validators.UPROTOCOL.validator()
        assertNotNull(validator)
        val status: Status = validator.validate(null)
        assertEquals(Code.INVALID_ARGUMENT_VALUE, status.code)
        assertEquals("Invalid UUIDv8 Version,Invalid UUID Time", status.message)
    }

    @Test
    @DisplayName("Test UUIDv8 validator for invalid types")
    fun test_uuidv8_with_invalid_types() {
        val uuidv6: UUID = UuidFactory.Factories.UUIDV6.factory().create()
        val uuid: UUID = UUID.newBuilder().setMsb(0L).setLsb(0L).build()
        val uuidJava: java.util.UUID = java.util.UUID.randomUUID()
        val uuidv4: UUID = UUID.newBuilder().setMsb(uuidJava.mostSignificantBits)
            .setLsb(uuidJava.leastSignificantBits).build()
        val validator: UuidValidator = UuidValidator.Validators.UPROTOCOL.validator()
        assertNotNull(validator)
        val status: Status = validator.validate(uuidv6)
        assertEquals(Code.INVALID_ARGUMENT_VALUE, status.code)
        assertEquals("Invalid UUIDv8 Version", status.message)
        val status1: Status = validator.validate(uuid)
        assertEquals(Code.INVALID_ARGUMENT_VALUE, status1.code)
        assertEquals("Invalid UUIDv8 Version,Invalid UUID Time", status1.message)
        val status2: Status = validator.validate(uuidv4)
        assertEquals(Code.INVALID_ARGUMENT_VALUE, status2.code)
        assertEquals("Invalid UUIDv8 Version,Invalid UUID Time", status2.message)
    }

    @Test
    @DisplayName("Test good UUIDv6")
    fun test_good_uuidv6() {
        val uuid: UUID = UuidFactory.Factories.UUIDV6.factory().create()
        val validator: UuidValidator = UuidValidator.getValidator(uuid)
        assertNotNull(validator)
        assertTrue(UuidUtils.isUuidv6(uuid))
        assertEquals(Code.OK_VALUE, validator.validate(uuid).code)
    }

    @Test
    @DisplayName("Test UUIDv6 with bad variant")
    fun test_uuidv6_with_bad_variant() {
        val uuid: UUID =LongUuidSerializer.instance().deserialize("1ee57e66-d33a-65e0-4a77-3c3f061c1e9e")
        assertFalse(uuid == UUID.getDefaultInstance())
        val validator: UuidValidator = UuidValidator.getValidator(uuid)
        assertNotNull(validator)
        val status: Status = validator.validate(uuid)
        assertEquals("Invalid UUID Version,Invalid UUID Variant,Invalid UUID Time", status.message)
        assertEquals(Code.INVALID_ARGUMENT_VALUE, status.code)
    }

    @Test
    @DisplayName("Test UUIDv6 with invalid UUID")
    fun test_uuidv6_with_invalid_uuid() {
        val uuid: UUID = UUID.newBuilder().setMsb(9 shl 12).setLsb(0L).build()
        val validator: UuidValidator = UuidValidator.Validators.UUIDV6.validator()
        assertNotNull(validator)
        val status: Status = validator.validate(uuid)
        assertEquals("Not a UUIDv6 Version,Invalid UUIDv6 variant,Invalid UUID Time", status.message)
        assertEquals(Code.INVALID_ARGUMENT_VALUE, status.code)
    }

    @Test
    @DisplayName("Test using UUIDv6 Validator to validate null UUID")
    fun test_uuidv6_with_null_uuid() {
        val validator: UuidValidator = UuidValidator.Validators.UUIDV6.validator()
        assertNotNull(validator)
        val status: Status = validator.validate(null)
        assertEquals("Not a UUIDv6 Version,Invalid UUIDv6 variant,Invalid UUID Time", status.message)
        assertEquals(Code.INVALID_ARGUMENT_VALUE, status.code)
    }

    @Test
    @DisplayName("Test using UUIDv6 Validator to validate a different types of UUIDs")
    fun test_uuidv6_with_uuidv8() {
        val uuid: UUID = UuidFactory.Factories.UPROTOCOL.factory().create()
        val validator: UuidValidator = UuidValidator.Validators.UUIDV6.validator()
        assertNotNull(validator)
        val status: Status = validator.validate(uuid)
        assertEquals("Not a UUIDv6 Version", status.message)
        assertEquals(Code.INVALID_ARGUMENT_VALUE, status.code)
    }
}
