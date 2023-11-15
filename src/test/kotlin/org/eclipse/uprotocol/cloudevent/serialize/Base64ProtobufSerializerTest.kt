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
package org.eclipse.uprotocol.cloudevent.serialize

import io.cloudevents.CloudEvent
import io.cloudevents.core.builder.CloudEventBuilder
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import java.net.URI
import org.junit.jupiter.api.Assertions.*

internal class Base64ProtobufSerializerTest {
    @Test
    @DisplayName("Test deserialize a byte[] to a String")
    fun test_deserialize_bytes_to_string() {

        // build the payload as just another cloud event packed into an Any
        val datapayload: CloudEvent = CloudEventBuilder.v1()
            .withId("hello")
            .withType("example.vertx")
            .withSource(URI.create("http://localhost"))
            .build()
        val bytes: ByteArray = CloudEventSerializers.PROTOBUF.serializer().serialize(datapayload)
        val payload: String = Base64ProtobufSerializer.deserialize(bytes)
        assertEquals("CgVoZWxsbxIQaHR0cDovL2xvY2FsaG9zdBoDMS4wIg1leGFtcGxlLnZlcnR4", payload)
    }

    @Test
    @DisplayName("Test deserialize a byte[] to a String when byte[] is null")
    fun test_deserialize_bytes_to_string_when_bytes_is_null() {
        val payload: String = Base64ProtobufSerializer.deserialize(null)
        assertEquals("", payload)
    }

    @Test
    @DisplayName("Test deserialize a byte[] to a String when byte[] is empty")
    fun test_deserialize_bytes_to_string_when_bytes_is_empty() {
        val payload: String = Base64ProtobufSerializer.deserialize(ByteArray(0))
        assertEquals("", payload)
    }

    @Test
    @DisplayName("Test serialize a base64 String to bytes")
    fun test_serialize_string_into_bytes() {
        val base64String = "CgVoZWxsbxIQaHR0cDovL2xvY2FsaG9zdBoDMS4wIg1leGFtcGxlLnZlcnR4"
        val bytes: ByteArray = Base64ProtobufSerializer.serialize(base64String)
        val datapayload: CloudEvent = CloudEventBuilder.v1()
            .withId("hello")
            .withType("example.vertx")
            .withSource(URI.create("http://localhost"))
            .build()
        val ceBytes: ByteArray = CloudEventSerializers.PROTOBUF.serializer().serialize(datapayload)
        assertArrayEquals(ceBytes, bytes)
    }

    @Test
    @DisplayName("Test serialize a base64 String to bytes when string is null")
    fun test_serialize_string_into_bytes_when_string_is_null() {
        val bytes: ByteArray = Base64ProtobufSerializer.serialize(null)
        val ceBytes = ByteArray(0)
        assertArrayEquals(ceBytes, bytes)
    }

    @Test
    @DisplayName("Test serialize a base64 String to bytes when string is empty")
    fun test_serialize_string_into_bytes_when_string_is_empty() {
        val bytes: ByteArray = Base64ProtobufSerializer.serialize("")
        val ceBytes = ByteArray(0)
        assertArrayEquals(ceBytes, bytes)
    }
}