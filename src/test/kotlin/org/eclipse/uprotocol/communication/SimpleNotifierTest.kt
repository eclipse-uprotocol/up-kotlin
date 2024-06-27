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
import kotlinx.coroutines.delay
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.eclipse.uprotocol.transport.UListener
import org.eclipse.uprotocol.v1.*
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

@ExperimentalCoroutinesApi
class SimpleNotifierTest {
    private val testDispatcher = UnconfinedTestDispatcher()
    private val testScope = TestScope(testDispatcher)
    @Test
    @DisplayName("Test sending a simple notification")
    fun testSendNotification() = testScope.runTest {
        val notifier: Notifier = SimpleNotifier(TestUTransport(dispatcher = testDispatcher))
        val result = notifier.notify(createTopic(), createDestinationUri(), null)
        assertEquals(UCode.OK, result.code)
    }


    @Test
    @DisplayName("Test sending a simple notification passing a google.protobuf.Message payload")
    fun testSendNotificationWithPayload() = testScope.runTest {
        val uri = uUri {
            authorityName = "Hartley"
        }
        val notifier: Notifier = SimpleNotifier(TestUTransport(dispatcher = testDispatcher))
        val result = notifier.notify(
            createTopic(), createDestinationUri(),
            UPayload.pack(uri)
        )
        assertEquals(UCode.OK, result.code)
    }


    @Test
    @DisplayName("Test registering and unregistering a listener for a notification topic")
    fun testRegisterListener() = testScope.runTest {
        val listener = UListener { message -> assertNotNull(message) }

        val notifier: Notifier = SimpleNotifier(TestUTransport(dispatcher = testDispatcher))
        var result = notifier.registerNotificationListener(createTopic(), listener)
        assertEquals(UCode.OK, result.code)

        result = notifier.unregisterNotificationListener(createTopic(), listener)
        assertEquals(UCode.OK, result.code)
    }


    @Test
    @DisplayName("Test unregistering a listener that was not registered")
    fun testUnregisterListenerNotRegistered() = testScope.runTest {
        val listener = UListener { message -> assertNotNull(message) }
        val notifier: Notifier = SimpleNotifier(TestUTransport(dispatcher = testDispatcher))
        val result = notifier.unregisterNotificationListener(createTopic(), listener)
        assertEquals(UCode.NOT_FOUND, result.code)
    }

    @Test
    @DisplayName("Test happy path for a notification topic")
    fun testHappyPath() = testScope.runTest {
        var listenerCallBackCount = 0
        val listener = UListener { message ->
            assertNotNull(message)
            listenerCallBackCount += 1
        }

        val topic = createTopic()
        val notifier: Notifier = SimpleNotifier(TestUTransport(dispatcher = testDispatcher))
        val regResult = notifier.registerNotificationListener(topic, listener)
        assertEquals(UCode.OK, regResult.code)

        val payload = uUri {
            authorityName = "Hartley"
        }

        val notifyResult = notifier.notify(
            topic, createDestinationUri(),
            UPayload.pack(payload)
        )
        assertEquals(UCode.OK, notifyResult.code)

        delay(100)
        assertEquals(1, listenerCallBackCount)


        val unRegResult = notifier.unregisterNotificationListener(topic, listener)
        assertEquals(UCode.OK, unRegResult.code)

        val notifyResult2 = notifier.notify(
            topic, createDestinationUri(),
            UPayload.pack(payload)
        )
        assertEquals(UCode.OK, notifyResult2.code)

        delay(100)
        assertEquals(1, listenerCallBackCount)
    }

    private fun createTopic(): UUri {
        return uUri {
            authorityName = "hartley"
            ueId = 3
            ueVersionMajor = 1
            resourceId = 0x8000
        }
    }


    private fun createDestinationUri(): UUri {
        return uUri {
            authorityName = "hartley"
            ueId = 4
            ueVersionMajor = 1

        }
    }
}