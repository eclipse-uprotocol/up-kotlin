/**
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

package org.eclipse.uprotocol.rpc

import org.eclipse.uprotocol.v1.callOptions
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue

class CallOptionsTest {
    @Test
    @DisplayName("Make sure the toString works")
    fun testToString() {
        val callOptions = callOptions {
            ttl = 30
            token = "someToken"
        }
        val expected = "ttl: 30\ntoken: \"someToken\"\n"
        assertEquals(expected, callOptions.toString())
    }

    @Test
    @DisplayName("Test creating CallOptions with only a token")
    fun testCreatingCallOptionsWithAToken() {
        val callOptions = callOptions {
            token = "someToken"
        }
        assertTrue(callOptions.token.isNotEmpty())
        assertEquals("someToken", callOptions.token)
    }


    @Test
    @DisplayName("Test creating CallOptions with only an empty string token")
    fun testCreatingCallOptionsWithAnEmptyStringToken() {
        val callOptions = callOptions {
            token = ""
        }
        assertTrue(callOptions.token.isEmpty())
    }

    @Test
    @DisplayName("Test creating CallOptions with only a token with only spaces")
    fun testCreatingCallOptionsWithATokenWithOnlySpaces() {
        val callOptions = callOptions {
            token = "   "
        }
        assertTrue(callOptions.token.isBlank())
    }

    @Test
    @DisplayName("Test creating CallOptions with only a timeout")
    fun testCreatingCallOptionsWithATimeout() {
        val callOptions = callOptions {
            ttl = 30
        }
        assertEquals(30, callOptions.ttl)
        assertTrue(callOptions.token.isEmpty())
    }
}
