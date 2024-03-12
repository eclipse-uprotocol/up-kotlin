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
 */

package org.eclipse.uprotocol.transport

import com.google.protobuf.Message
import org.junit.jupiter.api.Test

import org.eclipse.uprotocol.v1.*
import kotlin.test.assertEquals
import kotlin.test.assertNull

class UPayloadUtilsTest {

    @Test
    fun test_unpack_with_UPAYLOAD_FORMAT_PROTOBUF_WRAPPED_IN_ANY(){
        val message = uStatus {  }
        val payload = uPayload {
            packToAny(message)
        }
        val result = unpack(payload, UStatus::class.java)
        checkNotNull(result)
        assertEquals(message, result)
    }

    @Test
    fun test_unpack_with_UPAYLOAD_FORMAT_PROTOBUF(){
        val message = uStatus {  }
        val payload = uPayload {
            pack(message)
        }
        val result = unpack(payload, UStatus::class.java)
        checkNotNull(result)
        assertEquals(message, result)
    }

    @Test
    fun test_unpack_with_UPAYLOAD_FORMAT_RAW(){
        val message = uStatus {  }
        val payload = uPayload {
            format = UPayloadFormat.UPAYLOAD_FORMAT_RAW
            value = message.toByteString()
        }
        val result = unpack(payload, UStatus::class.java)
        assertNull(result)
    }

    @Test
    fun test_unpack_but_InvalidProtocolBufferException(){
        val message = uStatus {  }
        val payload = uPayload {
            packToAny(message)
        }
        val result = unpack(payload, UUri::class.java)
        assertNull(result)
    }

    @Test
    fun test_inline_unpack_with_UPAYLOAD_FORMAT_PROTOBUF_WRAPPED_IN_ANY(){
        val message = uStatus {  }
        val payload = uPayload {
            packToAny(message)
        }
        val result = payload.unpack<UStatus>()
        checkNotNull(result)
        assertEquals(message, result)
    }

    @Test
    fun test_inline_unpack_with_UPAYLOAD_FORMAT_PROTOBUF(){
        val message = uStatus {  }
        val payload = uPayload {
            pack(message)
        }
        val result = payload.unpack<UStatus>()
        checkNotNull(result)
        assertEquals(message, result)
    }

    @Test
    fun test_inline_unpack_with_UPAYLOAD_FORMAT_RAW(){
        val message = uStatus {  }
        val payload = uPayload {
            format = UPayloadFormat.UPAYLOAD_FORMAT_RAW
            value = message.toByteString()
        }
        val result = payload.unpack<UStatus>()
        assertNull(result)
    }

    @Test
    fun test_inline_unpack_but_InvalidProtocolBufferException(){
        val message = uStatus {  }
        val payload = uPayload {
            packToAny(message)
        }
        val result = payload.unpack<UUri>()
        assertNull(result)
    }
}