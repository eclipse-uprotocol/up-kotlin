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

import org.eclipse.uprotocol.v1.*
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test


/**
 * Test implementing and using uTransport API
 */
class UTransportTest {
    @Test
    @DisplayName("Test happy path send message parts")
    fun test_happy_send_message_parts() {
        val transport: UTransport = HappyUTransport()
        val status = transport.send(UMessage.getDefaultInstance())
        assertEquals(status.code, UCode.OK)
    }

    @Test
    @DisplayName("Test happy path send message")
    fun test_happy_send_message() {
        val transport: UTransport = HappyUTransport()
        val status = transport.send(UMessage.getDefaultInstance())
        assertEquals(status.code, UCode.OK)
    }

    @Test
    @DisplayName("Test happy path register listener")
    fun test_happy_register_listener() {
        val transport: UTransport = HappyUTransport()
        val status = transport.registerListener(UUri.getDefaultInstance(), MyListener())
        assertEquals(status.code, UCode.OK)
    }

    @Test
    @DisplayName("Test happy path unregister listener")
    fun test_happy_register_unlistener() {
        val transport: UTransport = HappyUTransport()
        val status = transport.unregisterListener(UUri.getDefaultInstance(), MyListener())
        assertEquals(status.code, UCode.OK)
    }

    @Test
    @DisplayName("Test unhappy path send message parts")
    fun test_unhappy_send_message_parts() {
        val transport: UTransport = SadUTransport()
        val status = transport.send(UMessage.getDefaultInstance())

        assertEquals(status.code, UCode.INTERNAL)
    }

    @Test
    @DisplayName("Test unhappy path send message")
    fun test_unhappy_send_message() {
        val transport: UTransport = SadUTransport()
        val status = transport.send(UMessage.getDefaultInstance())
        assertEquals(status.code, UCode.INTERNAL)
    }

    @Test
    @DisplayName("Test unhappy path register listener")
    fun test_unhappy_register_listener() {
        val transport: UTransport = SadUTransport()
        val status = transport.registerListener(UUri.getDefaultInstance(), MyListener())
        assertEquals(status.code, UCode.INTERNAL)
    }

    @Test
    @DisplayName("Test unhappy path unregister listener")
    fun test_unhappy_register_unlistener() {
        val transport: UTransport = SadUTransport()
        val status = transport.unregisterListener(UUri.getDefaultInstance(), MyListener())
        assertEquals(status.code, UCode.INTERNAL)
    }

    internal inner class MyListener : UListener {
        override fun onReceive(message: UMessage) {}
    }

    private inner class HappyUTransport : UTransport {
        override fun send(message: UMessage): UStatus {
            return uStatus {
                code = UCode.OK
            }
        }

        override fun registerListener(topic: UUri, listener: UListener): UStatus {
            listener.onReceive(uMessage { })
            return uStatus {
                code = UCode.OK
            }
        }

        override fun unregisterListener(topic: UUri, listener: UListener): UStatus {
            return uStatus {
                code = UCode.OK
            }
        }
    }

    private inner class SadUTransport : UTransport {
        override fun send(message: UMessage): UStatus {
            return uStatus {
                code = UCode.INTERNAL
            }
        }

        override fun registerListener(topic: UUri, listener: UListener): UStatus {
            listener.onReceive(uMessage { })
            return uStatus {
                code = UCode.INTERNAL
            }
        }

        override fun unregisterListener(topic: UUri, listener: UListener): UStatus {
            return uStatus {
                code = UCode.INTERNAL
            }
        }
    }
}