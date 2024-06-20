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
    @DisplayName("Test happy path send message")
    fun test_happy_send_message() {
        val transport: UTransport = HappyUTransport()
        val uri = uUri {
            ueId = 1
            ueVersionMajor = 1
            resourceId = 0x8000

        }

        val status = transport.send(uMessage { forPublication(uri) })
        assertEquals(status.code, UCode.OK)
    }

    @Test
    @DisplayName("Test happy path register listener")
    fun test_happy_register_listener() {
        val transport: UTransport = HappyUTransport()
        val status = transport.registerListener(UUri.getDefaultInstance(), listener = MyListener())
        assertEquals(status.code, UCode.OK)
    }

    @Test
    @DisplayName("Test happy path unregister listener")
    fun test_happy_register_unlistener() {
        val transport: UTransport = HappyUTransport()
        val status = transport.unregisterListener(UUri.getDefaultInstance(), listener = MyListener())
        assertEquals(status.code, UCode.OK)
    }

    @Test
    @DisplayName("Test unhappy path send message")
    fun test_unhappy_send_message() {
        val transport: UTransport = SadUTransport()
        val status = transport.send(uMessage { })
        assertEquals(status.code, UCode.INTERNAL)
    }

    @Test
    @DisplayName("Test unhappy path register listener")
    fun test_unhappy_register_listener() {
        val transport: UTransport = SadUTransport()
        val status = transport.registerListener(UUri.getDefaultInstance(), listener = MyListener())
        assertEquals(status.code, UCode.INTERNAL)
    }

    @Test
    @DisplayName("Test unhappy path unregister listener")
    fun test_unhappy_register_unlistener() {
        val transport: UTransport = SadUTransport()
        val status = transport.unregisterListener(UUri.getDefaultInstance(), listener = MyListener())
        assertEquals(status.code, UCode.INTERNAL)
    }

    @Test
    @DisplayName("Test happy path registerlistener with source filter only")
    fun test_happy_register_listener_source_filter() {
        val transport: UTransport = HappyUTransport()
        val status = transport.registerListener(UUri.getDefaultInstance(), listener = MyListener())
        assertEquals(status.code, UCode.OK)
    }

    @Test
    @DisplayName("Test happy path unregisterlistener with source filter only")
    fun test_happy_unregister_listener_source_filter() {
        val transport: UTransport = HappyUTransport()
        val status = transport.unregisterListener(UUri.getDefaultInstance(), listener = MyListener())
        assertEquals(status.code, UCode.OK)
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

        override fun registerListener(sourceFilter: UUri, sinkFilter: UUri?, listener: UListener): UStatus {
            listener.onReceive(uMessage { })
            return uStatus {
                code = UCode.OK
            }
        }

        override fun unregisterListener(sourceFilter: UUri, sinkFilter: UUri?, listener: UListener): UStatus {
            return uStatus {
                code = UCode.OK
            }
        }

        override fun getSource(): UUri {
            return uUri { }
        }
    }

    private inner class SadUTransport : UTransport {
        override fun send(message: UMessage): UStatus {
            return uStatus {
                code = UCode.INTERNAL
            }
        }

        override fun registerListener(sourceFilter: UUri, sinkFilter: UUri?, listener: UListener): UStatus {
            listener.onReceive(uMessage { })
            return uStatus {
                code = UCode.INTERNAL
            }
        }

        override fun unregisterListener(sourceFilter: UUri, sinkFilter: UUri?, listener: UListener): UStatus {
            return uStatus {
                code = UCode.INTERNAL
            }
        }

        override fun getSource(): UUri {
            return uUri { }
        }
    }


}