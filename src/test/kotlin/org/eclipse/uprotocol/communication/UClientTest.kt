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

import kotlinx.coroutines.test.runTest
import org.eclipse.uprotocol.transport.UListener
import org.eclipse.uprotocol.v1.*
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

class UClientTest {
    // Main functionality is tested in the various individual implementations
    @Test
    @DisplayName("Test happy path for all APIs")
    fun test() = runTest {
        val client = UClient(TestUTransport())
        val listener = UListener {
            assertNotNull(it)
        }
        client.notify(createTopic(), createDestinationUri(), null)

        client.publish(createTopic(), null)

        client.invokeMethod(createMethodUri(), UPayload())

        client.invokeMethod(createMethodUri(), UPayload(), CallOptions())

        client.subscribe(createTopic(), listener)

        client.subscribe(createTopic(), listener, CallOptions())

        client.unsubscribe(createTopic(), listener)

        val result: UStatus = client.unregisterListener(createTopic(), listener)
        assertEquals(UCode.OK, result.code)

        client.registerNotificationListener(createTopic(), listener)

        client.unregisterNotificationListener(createTopic(), listener)

        val handler = RequestHandler {
            throw UnsupportedOperationException("Unimplemented method 'handleRequest'")
        }

        client.registerRequestHandler(createMethodUri(), handler)

        client.unregisterRequestHandler(createMethodUri(), handler)
    }

    private fun createTopic(): UUri {
        return uUri {
            authorityName = "Hartley"
            ueId = 4
            ueVersionMajor = 1
            resourceId = 0x8000
        }
    }

    private fun createDestinationUri(): UUri {
        return uUri {
            ueId = 4
            ueVersionMajor = 1
        }
    }

    private fun createMethodUri(): UUri {
        return uUri {
            authorityName = "Hartley"
            ueId = 4
            ueVersionMajor = 1
            resourceId = 3
        }
    }
}