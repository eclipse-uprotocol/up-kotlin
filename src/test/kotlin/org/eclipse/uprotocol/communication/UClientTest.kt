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

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.eclipse.uprotocol.client.usubscription.v3.SubscriptionChangeHandler
import org.eclipse.uprotocol.transport.UListener
import org.eclipse.uprotocol.v1.*
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

@ExperimentalCoroutinesApi
class UClientTest {
    private val testDispatcher = UnconfinedTestDispatcher()
    private val testScope = TestScope(testDispatcher)

    // Main functionality is tested in the various individual implementations
    @Test
    @DisplayName("Test happy path for all APIs")
    fun test() = testScope.runTest {
        val client = UClient(TestUTransport(dispatcher = testDispatcher), testDispatcher)
        val listener = UListener {
            assertNotNull(it)
        }

        client.notify(createTopic(), createDestinationUri())

        client.publish(createTopic())

        client.invokeMethod(createMethodUri(), UPayload())

        client.invokeMethod(createMethodUri(), UPayload(), CallOptions())

        client.registerNotificationListener(createTopic(), listener)

        client.unregisterNotificationListener(createTopic(), listener)

        val handler = RequestHandler {
            throw UnsupportedOperationException("Unimplemented method 'handleRequest'")
        }

        client.registerRequestHandler(createMethodUri(), handler)

        client.unregisterRequestHandler(createMethodUri(), handler)

        client.close()
    }


    @Test
    @DisplayName("Test happy path for all APIs")
    fun test_sync() = runBlocking {
        val client = UClient(TestUTransport())
        val listener = UListener {
            assertNotNull(it)
        }
        client.notify(createTopic(), createDestinationUri())

        client.publish(createTopic())

        client.invokeMethod(createMethodUri(), UPayload())

        client.invokeMethod(createMethodUri(), UPayload(), CallOptions())

        client.registerNotificationListener(createTopic(), listener)

        client.unregisterNotificationListener(createTopic(), listener)

        val handler = RequestHandler {
            throw UnsupportedOperationException("Unimplemented method 'handleRequest'")
        }

        client.registerRequestHandler(createMethodUri(), handler)

        client.unregisterRequestHandler(createMethodUri(), handler)

        client.close()
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