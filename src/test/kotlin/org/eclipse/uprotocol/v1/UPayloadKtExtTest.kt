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
package org.eclipse.uprotocol.v1

import com.google.protobuf.Any
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test


class UPayloadKtExtTest {
    @Test
    fun testPackToAny() {
        val uStatus = uStatus { }
        val uPayload = uPayload {
            packToAny(uStatus)
        }
        assertNotNull(uPayload)
        assertEquals(UPayloadFormat.UPAYLOAD_FORMAT_PROTOBUF_WRAPPED_IN_ANY, uPayload.format)
        assertEquals(
            Any.pack(uStatus).toByteString(),
            uPayload.value
        )
    }

    @Test
    fun testPack() {
        val uStatus = uStatus { }
        val uPayload = uPayload {
            pack(uStatus)
        }
        assertNotNull(uPayload)
        assertEquals(UPayloadFormat.UPAYLOAD_FORMAT_PROTOBUF, uPayload.format)
        assertEquals(uStatus.toByteString(), uPayload.value)
    }
}
