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
package org.eclipse.uprotocol.uri.serializer

import com.google.protobuf.ByteString
import org.eclipse.uprotocol.core.usubscription.v3.Update
import org.eclipse.uprotocol.uri.validator.isEmpty
import org.eclipse.uprotocol.uri.validator.isMicroForm
import org.eclipse.uprotocol.v1.*
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

class UriSerializerTest {
    @Test
    @DisplayName("Test build resolve with valid long and micro uri")
    fun test_build_resolved_valid_long_micro_uri() {
        val longUUri: UUri = uUri {
            authority = uAuthority { name = "testauth" }
            entity = uEntity { name = "neelam" }
            resource = uResource {
                name = "rpc"
                instance = "response"
            }
        }

        val microUUri: UUri = uUri {
            authority = uAuthority { id = ByteString.copyFromUtf8("abcdefg") }
            entity = uEntity {
                id = 29999
                versionMajor = 254
            }
            resource = uResource { id = 39999 }
        }


        val microuri: ByteArray = MicroUriSerializer.INSTANCE.serialize(microUUri)
        val longuri: String = LongUriSerializer.INSTANCE.serialize(longUUri)

        val resolvedUUri: UUri? = LongUriSerializer.INSTANCE.buildResolved(longuri, microuri)
        checkNotNull(resolvedUUri)
        assertFalse(resolvedUUri.isEmpty())
        assertEquals("testauth", resolvedUUri.authority.name)
        assertEquals("neelam", resolvedUUri.entity?.name)
        assertEquals(29999, resolvedUUri.entity?.id)
        assertEquals(254, resolvedUUri.entity?.versionMajor)
        assertEquals("rpc", resolvedUUri.resource?.name)
        assertEquals("response", resolvedUUri.resource?.instance)
        assertEquals(39999, resolvedUUri.resource?.id)
    }

    @Test
    @DisplayName("Test build resolve with null long and null micro uri")
    fun test_build_resolved_null_long_null_micro_uri() {

        // Test the buildResolved method with invalid input
        val result: UUri? = MicroUriSerializer.INSTANCE.buildResolved(null, null)
        checkNotNull(result)
        // Assert that the result is empty
        assertTrue(result.isEmpty())
    }

    @Test
    @DisplayName("Test build resolve with null long and empty micro uri")
    fun test_build_resolved_null_long_micro_uri() {

        // Test the buildResolved method with invalid input
        val result: UUri? = MicroUriSerializer.INSTANCE.buildResolved(null, ByteArray(0))
        checkNotNull(result)
        // Assert that the result is empty
        assertTrue(result.isEmpty())
    }

    @Test
    @DisplayName("Test build resolve with empty long and micro uri")
    fun test_build_resolved_valid_long_null_micro_uri() {

        // Test the buildResolved method with invalid input
        val result: UUri? = MicroUriSerializer.INSTANCE.buildResolved("", ByteArray(0))
        checkNotNull(result)
        // Assert that the result is not empty
        assertTrue(result.isEmpty())
    }

    @Test
    @DisplayName("Test building uSubscription Update message  Notification topic without using generated stubs")
    fun test_build_resolved_full_information() {
        val uUri = uUri {
            entity = uEntity { id = 0 }
            resource = uResource {
                from(Update.Resources.subscriptions)
            }
        }
        assertFalse(uUri.isEmpty())
        assertTrue(uUri.isMicroForm())
    }
}
