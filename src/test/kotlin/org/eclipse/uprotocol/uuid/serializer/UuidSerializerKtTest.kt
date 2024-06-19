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

package org.eclipse.uprotocol.uuid.serializer

import org.eclipse.uprotocol.v1.UUID
import org.eclipse.uprotocol.v1.uUID
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

class UuidSerializerTest {
    @Test
    @DisplayName("Test serializer with good uuid")
    fun test_serializer_with_good_uuid() {
        val uuidStr = "123e4567-e89b-12d3-a456-426614174000"
        val uuid: UUID = uuidStr.deserializeAsUUID()
        assertEquals(uuidStr, uuid.serialize())
    }

    @Test
    @DisplayName("Test serializer with empty uuid")
    fun test_serializer_with_empty_uuid() {
        val uuid = uUID {  }
        assertEquals("00000000-0000-0000-0000-000000000000", uuid.serialize())
    }

    @Test
    @DisplayName("Test deserializer with invalid uuid")
    fun test_deserializer_with_invalid_uuid() {
        val uuidStr = "sdsadfasdfsfgagASDfadasfgsdfgs"
        val uuid: UUID = uuidStr.deserializeAsUUID()
        assertEquals(uUID {  }, uuid)
    }
}