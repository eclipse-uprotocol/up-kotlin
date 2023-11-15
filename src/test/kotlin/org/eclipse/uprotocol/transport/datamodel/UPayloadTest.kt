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
package org.eclipse.uprotocol.transport.datamodel

import nl.jqno.equalsverifier.EqualsVerifier
import org.eclipse.uprotocol.v1.UPayloadFormat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import java.nio.charset.StandardCharsets
import org.junit.jupiter.api.Assertions.*

internal class UPayloadTest {
    @Test
    @DisplayName("Make sure the equals and hash code works")
    fun testHashCodeEquals() {
        EqualsVerifier.forClass(UPayload::class.java).usingGetClass().verify()
    }

    @Test
    @DisplayName("Make sure the toString works on empty")
    fun testToString_with_empty() {
        val uPayload: UPayload = UPayload.empty()
        assertEquals("UPayload{data=[], hint=UPAYLOAD_FORMAT_UNSPECIFIED}", uPayload.toString())
        assertEquals(UPayloadFormat.UPAYLOAD_FORMAT_UNSPECIFIED, uPayload.hint())
    }

    @Test
    @DisplayName("Create an empty UPayload")
    fun create_an_empty_upayload() {
        val uPayload: UPayload = UPayload.empty()
        assertEquals(0, uPayload.data().size)
        assertTrue(uPayload.isEmpty)
    }

    @Test
    @DisplayName("Create a UPayload with null")
    fun create_upayload_with_null() {
        val uPayload = UPayload(null, null)
        assertEquals(0, uPayload.data().size)
        assertTrue(uPayload.isEmpty)
        assertEquals(UPayloadFormat.UPAYLOAD_FORMAT_UNSPECIFIED, uPayload.hint())
    }

    @Test
    @DisplayName("Create a UPayload from string with hint")
    fun create_upayload_from_string_with_hint() {
        val stringData = "hello"
        val uPayload = UPayload(stringData.toByteArray(StandardCharsets.UTF_8), UPayloadFormat.UPAYLOAD_FORMAT_TEXT)
        assertEquals(stringData.length, uPayload.data().size)
        assertFalse(uPayload.isEmpty)
        assertEquals(UPayloadFormat.UPAYLOAD_FORMAT_TEXT, uPayload.hint())
        assertEquals(stringData, String(uPayload.data()))
    }

    @Test
    @DisplayName("Create a UPayload from some string without hint")
    fun create_upayload_from_string_without_hint() {
        val stringData = "hello"
        val uPayload = UPayload(stringData.toByteArray(StandardCharsets.UTF_8), null)
        assertEquals(stringData.length, uPayload.data().size)
        assertFalse(uPayload.isEmpty)
        assertEquals(UPayloadFormat.UPAYLOAD_FORMAT_UNSPECIFIED, uPayload.hint())
    }

    @Test
    @DisplayName("Create a UPayload without a byte array but with some weird hint")
    fun create_upayload_without_byte_array_but_with_weird_hint() {
        val uPayload = UPayload(null, UPayloadFormat.UPAYLOAD_FORMAT_PROTOBUF)
        assertEquals(0, uPayload.data().size)
        assertTrue(uPayload.isEmpty)
        assertEquals(UPayloadFormat.UPAYLOAD_FORMAT_PROTOBUF, uPayload.hint())
        assertFalse(UPayload.empty() == uPayload)
    }
}