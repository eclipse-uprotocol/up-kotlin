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

import io.mockk.*
import kotlinx.coroutines.test.runTest
import org.eclipse.uprotocol.core.usubscription.v3.*
import org.eclipse.uprotocol.transport.UListener
import org.eclipse.uprotocol.transport.UTransport
import org.eclipse.uprotocol.v1.*
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import java.util.concurrent.CompletionException

class InMemorySubscriberTest {

    private val mockClient = mockk<RpcClient>()

    private val successSubscriptionResult =
        subscriptionResponse { status = subscriptionStatus { state = SubscriptionStatus.State.SUBSCRIBED } }
    private val successUnsubscriptionResult = unsubscribeResponse { }

    @Test
    @DisplayName("Test subscribe happy path")
    fun test_subscribe_happy_path() = runTest {
        val topic = createTopic()
        val transport: UTransport = TestUTransport()
        val subscriber: Subscriber = InMemorySubscriber(transport, mockClient)

        coEvery { mockClient.invokeMethod(any(), any(), any()) } returns Result.success(
            UPayload.pack(
                successSubscriptionResult
            )
        )

        val result = subscriber.subscribe(topic, {})
        val payloadSlot = slot<UPayload>()
        coVerify { mockClient.invokeMethod(any(), capture(payloadSlot), CallOptions()) }
        assertEquals(SubscriptionStatus.State.SUBSCRIBED, result.getOrNull()?.status?.state)
    }

    @Test
    @DisplayName("Test subscribe and then unsubscribe happy path")
    fun test_subscribe_and_then_unsubscribe_happy_path() = runTest {
        val topic = createTopic()
        val transport: UTransport = TestUTransport()
        val listener = UListener { }
        val subscriber: Subscriber = InMemorySubscriber(transport, mockClient)
        coEvery { mockClient.invokeMethod(any(), any(), any()) } returnsMany
                listOf(
                    Result.success(UPayload.pack(successSubscriptionResult)),
                    Result.success(UPayload.pack(successUnsubscriptionResult))
                )

        subscriber.subscribe(topic, listener)
        val result = subscriber.unsubscribe(topic, listener)
        assertEquals(UCode.OK, result.code)
    }

    @Test
    @DisplayName("Test unsubscribe happy path")
    fun test_unsubscribe_happy_path() = runTest {
        val topic = createTopic()
        val transport: UTransport = TestUTransport()
        val subscriber: Subscriber = InMemorySubscriber(transport, mockClient)
        coEvery { mockClient.invokeMethod(any(), any(), any()) } returns Result.success(
            UPayload.pack(
                successUnsubscriptionResult
            )
        )

        val result = subscriber.unsubscribe(topic, {
            throw UnsupportedOperationException("Unimplemented method 'onReceive'")
        })
        assertEquals(UCode.NOT_FOUND, result.code)
    }

    @Test
    @DisplayName("Test unregisterListener after we successfully subscribed to a topic")
    fun testUnregisterListener() = runTest {
        val topic = createTopic()
        val myListener = UListener { }
        val transport: UTransport = TestUTransport()
        val subscriber: Subscriber = InMemorySubscriber(transport, mockClient)

        coEvery { mockClient.invokeMethod(any(), any(), any()) } returns Result.success(
            UPayload.pack(
                successSubscriptionResult
            )
        )

        subscriber.subscribe(topic, myListener, CallOptions(100))
        val result = subscriber.unregisterListener(topic, myListener)
        assertEquals(UCode.OK, result.code)
    }

    @Test
    @DisplayName("Test unsubscribe with UStatusException")
    fun testUnsubscribeWithUStatusException() = runTest {
        val topic = createTopic()
        val transport: UTransport = CommStatusTransport()
        val subscriber: Subscriber = InMemorySubscriber(transport, mockClient)

        coEvery { mockClient.invokeMethod(any(), any(), any()) } returns Result.failure(
            UStatusException(UCode.FAILED_PRECONDITION, "CommStatus Error")
        )

        val result = subscriber.unsubscribe(topic, {})

        assertEquals(UCode.FAILED_PRECONDITION, result.code)
        assertEquals("CommStatus Error", result.message)
    }

    @Test
    @DisplayName("Test unsubscribe with UStatusException wrapped in CompletionException")
    fun testUnsubscribeWithUStatusExceptionInCompletionException() = runTest {
        val topic = createTopic()
        val transport: UTransport = CommStatusTransport()
        val subscriber: Subscriber = InMemorySubscriber(transport, mockClient)

        coEvery { mockClient.invokeMethod(any(), any(), any()) } returns Result.failure(
            CompletionException(UStatusException(UCode.FAILED_PRECONDITION, "CommStatus Error"))
        )

        val result = subscriber.unsubscribe(topic, {})

        assertEquals(UCode.FAILED_PRECONDITION, result.code)
        assertEquals("CommStatus Error", result.message)
    }

    @Test
    @DisplayName("Test unsubscribe with Empty CompletionException")
    fun testUnsubscribeWithEmptyCompletionException() = runTest {
        val topic = createTopic()
        val transport: UTransport = CommStatusTransport()
        val subscriber: Subscriber = InMemorySubscriber(transport, mockClient)

        coEvery { mockClient.invokeMethod(any(), any(), any()) } returns Result.failure(
            CompletionException(null)
        )

        val result = subscriber.unsubscribe(topic, {})

        assertEquals(UCode.INVALID_ARGUMENT, result.code)
    }

    @Test
    @DisplayName("Test unsubscribe with OtherException")
    fun testUnsubscribeWithOtherException() = runTest {
        val topic = createTopic()
        val transport: UTransport = CommStatusTransport()
        val subscriber: Subscriber = InMemorySubscriber(transport, mockClient)

        coEvery { mockClient.invokeMethod(any(), any(), any()) } returns Result.failure(
            IllegalStateException("Some Error")
        )

        val result = subscriber.unsubscribe(topic, {})

        assertEquals(UCode.INVALID_ARGUMENT, result.code)
        assertEquals("Some Error", result.message)
    }

    @Test
    @DisplayName("Test unsubscribe with OtherException No Message")
    fun testUnsubscribeWithOtherExceptionNoMessage() = runTest {
        val topic = createTopic()
        val transport: UTransport = CommStatusTransport()
        val subscriber: Subscriber = InMemorySubscriber(transport, mockClient)

        coEvery { mockClient.invokeMethod(any(), any(), any()) } returns Result.failure(
            IllegalStateException()
        )

        val result = subscriber.unsubscribe(topic, {})

        assertEquals(UCode.INVALID_ARGUMENT, result.code)
        assertEquals("Invalid argument", result.message)
    }

    private fun createTopic(): UUri {
        return uUri {
            authorityName = "hartley"
            ueId = 3
            ueVersionMajor = 1
            resourceId = 0x8000
        }
    }
}