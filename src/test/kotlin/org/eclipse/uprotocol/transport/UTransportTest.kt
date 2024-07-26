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

import kotlinx.coroutines.test.runTest
import org.eclipse.uprotocol.v1.*
import org.junit.jupiter.api.Assertions.assertDoesNotThrow
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

/**
 * Test implementing and using uTransport API
 */
class UTransportTest {
    @Test
    @DisplayName("Test happy path send message")
    fun test_happy_send_message() = runTest {
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
    fun test_happy_register_listener() = runTest {
        val transport: UTransport = HappyUTransport()
        val status = transport.registerListener(UUri.getDefaultInstance(), listener = MyListener())
        assertEquals(status.code, UCode.OK)
    }

    @Test
    @DisplayName("Test happy path unregister listener")
    fun test_happy_register_unlistener() = runTest {
        val transport: UTransport = HappyUTransport()
        val status = transport.unregisterListener(UUri.getDefaultInstance(), listener = MyListener())
        assertEquals(status.code, UCode.OK)
    }

    @Test
    @DisplayName("Test unhappy path send message")
    fun test_unhappy_send_message() = runTest {
        val transport: UTransport = SadUTransport()
        val status = transport.send(uMessage { })
        assertEquals(status.code, UCode.INTERNAL)
    }

    @Test
    @DisplayName("Test unhappy path register listener")
    fun test_unhappy_register_listener() = runTest {
        val transport: UTransport = SadUTransport()
        val status = transport.registerListener(UUri.getDefaultInstance(), listener = MyListener())
        assertEquals(status.code, UCode.INTERNAL)
    }

    @Test
    @DisplayName("Test unhappy path unregister listener")
    fun test_unhappy_register_unlistener() = runTest {
        val transport: UTransport = SadUTransport()
        val status = transport.unregisterListener(UUri.getDefaultInstance(), listener = MyListener())
        assertEquals(status.code, UCode.INTERNAL)
    }

    @Test
    @DisplayName("Test happy path registerlistener with source filter only")
    fun test_happy_register_listener_source_filter() = runTest {
        val transport: UTransport = HappyUTransport()
        val status = transport.registerListener(UUri.getDefaultInstance(), listener = MyListener())
        assertEquals(status.code, UCode.OK)
    }

    @Test
    @DisplayName("Test happy path unregisterlistener with source filter only")
    fun test_happy_unregister_listener_source_filter() = runTest {
        val transport: UTransport = HappyUTransport()
        val status = transport.unregisterListener(UUri.getDefaultInstance(), listener = MyListener())
        assertEquals(status.code, UCode.OK)
    }

    @Test
    @DisplayName("Test happy path close")
    fun test_happy_close() {
        val transport = HappyUTransport()
        assertEquals(0, transport.status)
        transport.close()
        assertEquals(1, transport.status)
    }

    @Test
    @DisplayName("Test happy path calling open() API")
    fun test_happy_open() = runTest {
        val transport: UTransport = HappyUTransport()
        assertEquals(UCode.OK, transport.open().code)
    }

    @Test
    @DisplayName("Test default oepn() and close() APIs")
    fun test_default_open_close() {
        val transport: UTransport = object : UTransport {
            override suspend fun send(message: UMessage): UStatus {
                return uStatus {
                    code = UCode.OK
                }
            }

            override suspend fun registerListener(sourceFilter: UUri, sinkFilter: UUri, listener: UListener): UStatus {
                return uStatus {
                    code = UCode.OK
                }
            }

            override suspend fun unregisterListener(
                sourceFilter: UUri,
                sinkFilter: UUri,
                listener: UListener
            ): UStatus {
                return uStatus {
                    code = UCode.OK
                }
            }

            override fun getSource(): UUri {
                return uUri {  }
            }
        }

        assertDoesNotThrow { transport.close() }
    }


    internal inner class MyListener : UListener {
        override suspend fun onReceive(message: UMessage) {}
    }

    private inner class HappyUTransport : UTransport {
        var status = 0

        override suspend fun send(message: UMessage): UStatus {
            return uStatus {
                code = UCode.OK
            }
        }

        override suspend fun registerListener(sourceFilter: UUri, sinkFilter: UUri, listener: UListener): UStatus {
            listener.onReceive(uMessage { })
            return uStatus {
                code = UCode.OK
            }
        }

        override suspend fun unregisterListener(sourceFilter: UUri, sinkFilter: UUri, listener: UListener): UStatus {
            return uStatus {
                code = UCode.OK
            }
        }

        override fun getSource(): UUri {
            return uUri { }
        }

        override fun close() {
            status = 1
        }
    }

    private inner class SadUTransport : UTransport {
        override suspend fun send(message: UMessage): UStatus {
            return uStatus {
                code = UCode.INTERNAL
            }
        }

        override suspend fun registerListener(sourceFilter: UUri, sinkFilter: UUri, listener: UListener): UStatus {
            listener.onReceive(uMessage { })
            return uStatus {
                code = UCode.INTERNAL
            }
        }

        override suspend fun unregisterListener(sourceFilter: UUri, sinkFilter: UUri, listener: UListener): UStatus {
            return uStatus {
                code = UCode.INTERNAL
            }
        }

        override fun getSource(): UUri {
            return uUri { }
        }

        override fun close() {
            // Do nothing
        }
    }


}