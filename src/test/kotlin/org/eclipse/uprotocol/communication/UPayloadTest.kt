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
package org.eclipse.uprotocol.communication

import com.google.protobuf.ByteString
import org.eclipse.uprotocol.v1.*
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import java.util.*

class UPayloadTest {
    @Test
    @DisplayName("Test iEmpty() when we build a valid UPayload that data is empty but format is not")
    fun test_EMPTY_UPayload() {
        val payload = UPayload.EMPTY
        assertEquals(ByteString.EMPTY, payload.data)
        assertEquals(UPayloadFormat.UPAYLOAD_FORMAT_UNSPECIFIED, payload.format)
        assertTrue(payload.isEmpty())
    }

    @Test
    @DisplayName("Test iEmpty() when we build a valid UPayload that data is empty but format is not")
    fun test_building_a_UPayload_calling_pack() {
        val payload = UPayload.pack(uUri {  })
        assertFalse(payload.isEmpty())
    }

    @Test
    @DisplayName("Test iEmpty() when we build a valid UPayload where both data and format are not empty")
    fun test_building_a_UPayload_calling_packToAny() {
        val payload = UPayload.packToAny( uUri { authorityName = "name" })
        assertFalse(payload.isEmpty())
    }

    @Test
    @DisplayName("Test iEmpty() when we build a UPayload where data is empty")
    fun test_building_a_UPayload_with_empty_data() {
        val payload = UPayload(ByteString.EMPTY, UPayloadFormat.UPAYLOAD_FORMAT_PROTOBUF)
        assertFalse(payload.isEmpty())
    }


    @Test
    @DisplayName("Test unpack() passing google.protobuf.Any packed UPayload")
    fun test_unpack_passing_a_google_protobuf_any_packed_UPayload() {
        val uri = uUri {
            authorityName = "Hartley"
        }
        val payload = UPayload.packToAny(uri)
        val unpacked: UUri? = payload.unpack()
        assertNotNull(unpacked)
        assertEquals(uri, unpacked)
    }

    @Test
    @DisplayName("Test unpack() passing an unsupported format in UPayload")
    fun test_unpack_passing_an_unsupported_format_in_UPayload() {
        val uri = uUri {
            authorityName = "Hartley"
        }
        val payload = UPayload.pack(uri.toByteString(), UPayloadFormat.UPAYLOAD_FORMAT_JSON)
        val unpacked: UUri? = payload.unpack()
        assertNull(unpacked)
    }

    @Test
    @DisplayName("Test unpack() to unpack a message in ANY")
    fun test_unpack_to_unpack_a_message_in_ANY() {
        val uri = uUri {
            authorityName = "Hartley"
        }
        val packed = com.google.protobuf.Any.pack(uri)
        val unpacked: UUri? = UPayload.unpack(
            packed.toByteString() , UPayloadFormat.UPAYLOAD_FORMAT_PROTOBUF_WRAPPED_IN_ANY
        )
        assertNotNull(unpacked)
        assertEquals(uri, unpacked)
    }

    @Test
    @DisplayName("Test unpack() to unpack a message in UNSPECIFIED")
    fun test_unpack_to_unpack_a_message_in_UNSPECIFIED() {
        val uri = uUri {
            authorityName = "Hartley"
        }
        val packed = com.google.protobuf.Any.pack(uri)
        val unpacked: UUri? = UPayload.unpack(
            packed.toByteString() , UPayloadFormat.UPAYLOAD_FORMAT_UNSPECIFIED
        )
        assertNotNull(unpacked)
        assertEquals(uri, unpacked)
    }

    @Test
    @DisplayName("Test unpack() to unpack a message in PROTOBUF")
    fun test_unpack_to_unpack_a_message_in_PROTOBUF() {
        val uri = uUri {
            authorityName = "Hartley"
        }
        val unpacked: UUri? = UPayload.unpack(
            uri.toByteString() , UPayloadFormat.UPAYLOAD_FORMAT_PROTOBUF
        )
        assertNotNull(unpacked)
        assertEquals(uri, unpacked)
    }

    @Test
    @DisplayName("Test unpack() to unpack a message of the wrong type")
    fun test_unpack_to_unpack_a_message_of_the_wrong_type() {
        val uri = uUri {
            authorityName = "Hartley"
        }
        val unpacked: UMessage? = UPayload.unpack(
            uri.toByteString(), UPayloadFormat.UPAYLOAD_FORMAT_PROTOBUF
        )
        assertNull(unpacked)
    }

    @Test
    @DisplayName("Test equals when they are equal")
    fun test_equals_when_they_are_equal() {
        val uri = uUri {
            authorityName = "Hartley"
        }
        val payload1 = UPayload.packToAny(uri)
        val payload2 = UPayload.packToAny(uri)
        assertEquals(payload1, payload2)
    }

    @Test
    @DisplayName("Test equals when they are not equal")
    fun test_equals_when_they_are_not_equal() {
        val uri1 = uUri {
            authorityName = "Hartley"
        }
        val uri2 = uUri {
            authorityName = "Hartley"
        }
        val payload1 = UPayload.packToAny(uri1)
        val payload2 = UPayload.pack(uri2)
        assertFalse(payload1 == payload2)
    }

    @Test
    @DisplayName("Test equals when object is null")
    fun test_equals_when_object_is_null() {
        val uri = uUri {
            authorityName = "Hartley"
        }
        val payload = UPayload.packToAny(uri)
        assertNotNull(payload)
    }

    @Test
    @DisplayName("Test equals when it is the same object")
    fun test_equals_when_it_is_the_same_object() {
        val uri = uUri {
            authorityName = "Hartley"
        }
        val payload1 = UPayload.packToAny(uri)
        val payload2 = UPayload.packToAny(uri)
        assertTrue(payload1 == payload2)
    }

    @Test
    @DisplayName("Test equals when the data is the same but the format is not")
    fun test_equals_when_the_data_is_the_same_but_the_format_is_not() {
        val uri = uUri {
            authorityName = "Hartley"
        }
        val payload1 = UPayload.pack(uri.toByteString(), UPayloadFormat.UPAYLOAD_FORMAT_PROTOBUF)
        val payload2 = UPayload.pack(uri.toByteString(), UPayloadFormat.UPAYLOAD_FORMAT_JSON)
        assertFalse(payload1 == payload2)
    }

    @Test
    fun test_mapToMessage_with_success_Result(){
        val uStatus = OK_STATUS
        val payload = UPayload.packToAny(uStatus)

        val result =  Result.success(payload).mapToMessage<UStatus>().getOrNull()
        assertEquals(uStatus, result)
    }

    @Test
    fun test_mapToMessage_with_success_Result_but_empty_data(){
        val payload = UPayload()
        val result =  Result.success(payload).mapToMessage<UStatus>().getOrNull()
        assertEquals(uStatus{}, result)
    }

    @Test
    fun test_mapToMessage_with_failure_Result(){
        val exception = UStatusException(UCode.NOT_FOUND,"error")
        val result : Result<UStatus> =  Result.failure(exception)
        assertTrue(result.isFailure)
        assertFalse(result.isSuccess)
        assertEquals(exception, result.exceptionOrNull())
    }

    @Test
    @DisplayName("Test unpack passing a valid UMessage")
    fun test_unpack_passing_a_valid_UMessage() {
        val uri = UUri.newBuilder().setAuthorityName("Hartley").build()
        val payload = UPayload.packToAny(uri)
        val unpacked : UUri? = uMessage {
            this.payload = payload.data
        }.unpack()
        assertEquals(uri, unpacked)
    }
}