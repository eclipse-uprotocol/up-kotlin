/*
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

package org.eclipse.uprotocol.client.usubscription.v3

import io.mockk.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.eclipse.uprotocol.communication.*
import org.eclipse.uprotocol.core.usubscription.v3.*
import org.eclipse.uprotocol.transport.*
import org.eclipse.uprotocol.v1.*
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

@ExperimentalCoroutinesApi
class InMemoryUSubscriptionClientTest {
    private val testDispatcher = UnconfinedTestDispatcher()
    private val testScope = TestScope(testDispatcher)

    private val mockClient = mockk<RpcClient>()

    private val mockNotifier = mockk<Notifier>()

    private val successSubscriptionResult =
        subscriptionResponse { status = subscriptionStatus { state = SubscriptionStatus.State.SUBSCRIBED } }
    private val successUnsubscriptionResult = unsubscribeResponse { }

    private val testSink = uUri {
        authorityName = "vcu.someVin.veh.ultifi.gm.com"
        ueId = 1
        ueVersionMajor = 1
        resourceId = 0
    }

    @BeforeEach
    fun setup() {
        coEvery { mockNotifier.registerNotificationListener(any(), any()) } returns OK_STATUS
        coEvery { mockNotifier.unregisterNotificationListener(any(), any()) } returns OK_STATUS
    }

    @Test
    @DisplayName("Testing creation of InMemoryUSubscriptionClient passing only the transport")
    fun test_creation_of_InMemoryUSubscriptionClient_passing_only_the_transport() {
        val transport: UTransport = TestUTransport()

        val subscriber = InMemoryUSubscriptionClient(transport)
        subscriber.close()
    }

    @Test
    @DisplayName("Test registerNotification at init and unregister when close")
    fun test_registerNotification_at_init_and_unregister_when_close() = testScope.runTest {
        val transport: UTransport = TestUTransport()
        val subscriber = InMemoryUSubscriptionClient(transport, mockClient, mockNotifier, testDispatcher)
        coVerify { mockNotifier.registerNotificationListener(any(), any()) }

        subscriber.close()

        coVerify { mockNotifier.unregisterNotificationListener(any(), any()) }
    }


    @Test
    @DisplayName("Test subscribe happy path without handler")
    fun test_subscribe_happy_path_without_handler() = testScope.runTest {
        val topic = testTopic
        val transport = TestUTransport()
        val subscriber: USubscriptionClient = InMemoryUSubscriptionClient(transport, mockClient, mockNotifier)

        coEvery { mockClient.invokeMethod(any(), any(), any()) } returns Result.success(
            UPayload.pack(
                successSubscriptionResult
            )
        )

        val result = subscriber.subscribe(topic, {})
        val payloadSlot = slot<UPayload>()
        coVerify { mockClient.invokeMethod(any(), capture(payloadSlot), CallOptions()) }
        assertEquals(1, transport.listeners.size)
        assertEquals(SubscriptionStatus.State.SUBSCRIBED, result.getOrNull()?.status?.state)
    }

    @Test
    @DisplayName("Test subscribe happy path with null handler")
    fun test_subscribe_happy_path_with_NULL_handler() = testScope.runTest {
        val topic = testTopic
        val transport = TestUTransport()
        val subscriber: USubscriptionClient = InMemoryUSubscriptionClient(transport, mockClient, mockNotifier)

        coEvery { mockClient.invokeMethod(any(), any(), any()) } returns Result.success(
            UPayload.pack(
                successSubscriptionResult
            )
        )

        val result = subscriber.subscribe(topic, {}, CallOptions(100), handler = null)
        val payloadSlot = slot<UPayload>()
        coVerify { mockClient.invokeMethod(any(), capture(payloadSlot), any()) }
        assertEquals(1, transport.listeners.size)
        assertEquals(SubscriptionStatus.State.SUBSCRIBED, result.getOrNull()?.status?.state)
    }

    @Test
    @DisplayName("Test subscribe happy path with handler")
    fun test_subscribe_happy_path_with_handler() = testScope.runTest {
        val topic = testTopic
        val transport: UTransport = TestUTransport()
        val handler = SubscriptionChangeHandler { _, _ ->
        }
        val subscriber: USubscriptionClient = InMemoryUSubscriptionClient(transport, mockClient, mockNotifier)

        coEvery { mockClient.invokeMethod(any(), any(), any()) } returns Result.success(
            UPayload.pack(
                successSubscriptionResult
            )
        )

        val result = subscriber.subscribe(topic, {}, handler = handler)
        val payloadSlot = slot<UPayload>()
        coVerify { mockClient.invokeMethod(any(), capture(payloadSlot), CallOptions()) }
        assertEquals(SubscriptionStatus.State.SUBSCRIBED, result.getOrNull()?.status?.state)
    }

    @Test
    @DisplayName("Test subscribe but state is not subscribed")
    fun test_subscribe_state_not_subscribed() = testScope.runTest {
        val topic = testTopic
        val transport = TestUTransport()
        val handler = SubscriptionChangeHandler { _, _ ->
        }
        val subscriber: USubscriptionClient = InMemoryUSubscriptionClient(transport, mockClient, mockNotifier)

        val subscriptionResult =
            subscriptionResponse { status = subscriptionStatus { state = SubscriptionStatus.State.UNSUBSCRIBED } }

        coEvery { mockClient.invokeMethod(any(), any(), any()) } returns Result.success(
            UPayload.pack(
                subscriptionResult
            )
        )

        val result = subscriber.subscribe(topic, {}, handler = handler)
        val payloadSlot = slot<UPayload>()
        coVerify { mockClient.invokeMethod(any(), capture(payloadSlot), CallOptions()) }
        assertEquals(0, transport.listeners.size)
        assertEquals(SubscriptionStatus.State.UNSUBSCRIBED, result.getOrNull()?.status?.state)
    }

    @Test
    @DisplayName("Test subscribe pending subscribe")
    fun test_subscribe_pending_subscribe() = testScope.runTest {
        val topic = testTopic
        val transport = TestUTransport()
        val handler = SubscriptionChangeHandler { _, _ ->
        }
        val subscriber: USubscriptionClient = InMemoryUSubscriptionClient(transport, mockClient, mockNotifier)
        val subscriptionResult =
            subscriptionResponse { status = subscriptionStatus { state = SubscriptionStatus.State.SUBSCRIBE_PENDING } }

        coEvery { mockClient.invokeMethod(any(), any(), any()) } returns Result.success(
            UPayload.pack(
                subscriptionResult
            )
        )

        val result = subscriber.subscribe(topic, {}, handler = handler)
        val payloadSlot = slot<UPayload>()
        coVerify { mockClient.invokeMethod(any(), capture(payloadSlot), CallOptions()) }
        assertEquals(1, transport.listeners.size)
        assertEquals(SubscriptionStatus.State.SUBSCRIBE_PENDING, result.getOrNull()?.status?.state)
    }


    @Test
    @DisplayName("Test subscribe transport failure")
    fun test_subscribe_transport_failure() = testScope.runTest {
        val topic = testTopic
        val transport: UTransport = TestUTransport()
        val handler = SubscriptionChangeHandler { _, _ ->
        }
        val subscriber: USubscriptionClient = InMemoryUSubscriptionClient(transport, mockClient, mockNotifier)

        coEvery { mockClient.invokeMethod(any(), any(), any()) } returns Result.failure(
            UStatusException(UCode.FAILED_PRECONDITION, "CommStatus Error")
        )

        val result = subscriber.subscribe(topic, {}, handler = handler)
        val payloadSlot = slot<UPayload>()
        coVerify { mockClient.invokeMethod(any(), capture(payloadSlot), CallOptions()) }
        assertTrue(result.exceptionOrNull() is UStatusException)
        assertEquals(UCode.FAILED_PRECONDITION, (result.exceptionOrNull() as UStatusException).code)
    }

    @Test
    @DisplayName("Test subscribe, then send notification")
    fun test_subscribe_then_notification() = testScope.runTest {
        val topic = testTopic
        val transport: UTransport = TestUTransport()
        var isHandlerCalled = false
        val handler = SubscriptionChangeHandler { uri, status ->
            assertEquals(topic, uri)
            assertEquals(SubscriptionStatus.State.SUBSCRIBED, status.state)
            isHandlerCalled = true
        }
        val notificationListenerSlot = slot<UListener>()
        coEvery {
            mockNotifier.registerNotificationListener(
                any(),
                capture(notificationListenerSlot)
            )
        } returns OK_STATUS
        val subscriber: USubscriptionClient = InMemoryUSubscriptionClient(transport, mockClient, mockNotifier)

        coEvery { mockClient.invokeMethod(any(), any(), any()) } returns Result.success(
            UPayload.pack(
                successSubscriptionResult
            )
        )

        subscriber.subscribe(topic, {}, handler = handler)
        val testMessage = uMessage {
            forNotification(topic, testSink)
            setPayload(
                UPayload.pack(update {
                    this.topic = topic
                    status = subscriptionStatus { state = SubscriptionStatus.State.SUBSCRIBED }
                })
            )
        }
        notificationListenerSlot.captured.onReceive(testMessage)
        assertTrue(isHandlerCalled)
    }

    @Test
    @DisplayName("Test subscribe, then send notification but exception")
    fun test_subscribe_then_notification_but_exception() = testScope.runTest {
        val topic = testTopic
        val transport: UTransport = TestUTransport()
        val handler = SubscriptionChangeHandler { _, _ ->
            throw Exception("test Error")
        }
        val notificationListenerSlot = slot<UListener>()
        coEvery {
            mockNotifier.registerNotificationListener(
                any(),
                capture(notificationListenerSlot)
            )
        } returns OK_STATUS
        val subscriber: USubscriptionClient = InMemoryUSubscriptionClient(transport, mockClient, mockNotifier)

        coEvery { mockClient.invokeMethod(any(), any(), any()) } returns Result.success(
            UPayload.pack(
                successSubscriptionResult
            )
        )

        subscriber.subscribe(topic, {}, handler = handler)
        val testMessage = uMessage {
            forNotification(topic, testSink)
            setPayload(
                UPayload.pack(update {
                    this.topic = topic
                    status = subscriptionStatus { state = SubscriptionStatus.State.SUBSCRIBED }
                })
            )
        }
        notificationListenerSlot.captured.onReceive(testMessage)
    }

    @Test
    @DisplayName("Test subscribe, then send empty notification")
    fun test_subscribe_then_empty_notification() = testScope.runTest {
        val topic = testTopic
        val transport: UTransport = TestUTransport()
        var isHandlerCalled = false
        val handler = SubscriptionChangeHandler { _, _ ->
            isHandlerCalled = true
        }
        val notificationListenerSlot = slot<UListener>()
        coEvery {
            mockNotifier.registerNotificationListener(
                any(),
                capture(notificationListenerSlot)
            )
        } returns OK_STATUS
        val subscriber: USubscriptionClient = InMemoryUSubscriptionClient(transport, mockClient, mockNotifier)

        coEvery { mockClient.invokeMethod(any(), any(), any()) } returns Result.success(
            UPayload.pack(
                successSubscriptionResult
            )
        )

        subscriber.subscribe(topic, {}, handler = handler)
        val testMessage = uMessage {
            forNotification(topic, testSink)
            setPayload(
                UPayload.pack(update {
                })
            )
        }
        notificationListenerSlot.captured.onReceive(testMessage)
        Assertions.assertFalse(isHandlerCalled)
    }

    @Test
    @DisplayName("Test subscribe, then send other messages not for notification")
    fun test_subscribe_then_not_notification() = testScope.runTest {
        val topic = testTopic
        val transport: UTransport = TestUTransport()
        var isHandlerCalled = false
        val handler = SubscriptionChangeHandler { _, _ ->
            isHandlerCalled = true
        }
        val notificationListenerSlot = slot<UListener>()
        coEvery {
            mockNotifier.registerNotificationListener(
                any(),
                capture(notificationListenerSlot)
            )
        } returns OK_STATUS
        val subscriber: USubscriptionClient = InMemoryUSubscriptionClient(transport, mockClient, mockNotifier)

        coEvery { mockClient.invokeMethod(any(), any(), any()) } returns Result.success(
            UPayload.pack(
                successSubscriptionResult
            )
        )
        subscriber.subscribe(topic, {}, handler = handler)
        val testMessage = uMessage {
            forPublication(topic)
            setPayload(
                UPayload.pack(update {
                    this.topic = topic
                    status = subscriptionStatus { state = SubscriptionStatus.State.SUBSCRIBED }
                })
            )
        }
        notificationListenerSlot.captured.onReceive(testMessage)
        Assertions.assertFalse(isHandlerCalled)
    }

    @Test
    @DisplayName("Test subscribe, then send notification but wrong topic")
    fun test_subscribe_then_notification_but_wrong_topic() = testScope.runTest {
        val topic = testTopic
        val transport: UTransport = TestUTransport()
        var isHandlerCalled = false
        val handler = SubscriptionChangeHandler { _, _ ->
            isHandlerCalled = true
        }
        val notificationListenerSlot = slot<UListener>()
        coEvery {
            mockNotifier.registerNotificationListener(
                any(),
                capture(notificationListenerSlot)
            )
        } returns OK_STATUS
        val subscriber: USubscriptionClient = InMemoryUSubscriptionClient(transport, mockClient, mockNotifier)

        coEvery { mockClient.invokeMethod(any(), any(), any()) } returns Result.success(
            UPayload.pack(
                successSubscriptionResult
            )
        )

        subscriber.subscribe(topic, {}, handler = handler)
        val testMessage = uMessage {
            forNotification(topic, testSink)
            setPayload(
                UPayload.pack(update {
                    this.topic = testSink
                    status = subscriptionStatus { state = SubscriptionStatus.State.SUBSCRIBED }
                })
            )
        }
        notificationListenerSlot.captured.onReceive(testMessage)
        Assertions.assertFalse(isHandlerCalled)
    }

    @Test
    @DisplayName("Test subscribe twice with same handler")
    fun test_subscribe_twice_with_same_handler() = testScope.runTest {
        val topic = testTopic
        val transport: UTransport = TestUTransport()
        val handler = SubscriptionChangeHandler { _, _ ->
        }
        val subscriber: USubscriptionClient = InMemoryUSubscriptionClient(transport, mockClient, mockNotifier)

        coEvery { mockClient.invokeMethod(any(), any(), any()) } returns Result.success(
            UPayload.pack(
                successSubscriptionResult
            )
        )

        subscriber.subscribe(topic, {}, handler = handler)
        val result = subscriber.subscribe(topic, {}, handler = handler)
        assertEquals(SubscriptionStatus.State.SUBSCRIBED, result.getOrNull()?.status?.state)
    }

    @Test
    @DisplayName("Test subscribe twice with new handler")
    fun test_subscribe_twice_with_new_handler() = testScope.runTest {
        val topic = testTopic
        val transport: UTransport = TestUTransport()
        val handler1 = SubscriptionChangeHandler { _, _ ->
        }
        val handler2 = SubscriptionChangeHandler { _, _ ->
        }
        val subscriber: USubscriptionClient = InMemoryUSubscriptionClient(transport, mockClient, mockNotifier)

        coEvery { mockClient.invokeMethod(any(), any(), any()) } returns Result.success(
            UPayload.pack(
                successSubscriptionResult
            )
        )

        subscriber.subscribe(topic, {}, handler = handler1)
        val result = subscriber.subscribe(topic, {}, handler = handler2)
        assertTrue(result.exceptionOrNull() is UStatusException)
        assertEquals(UCode.ALREADY_EXISTS, (result.exceptionOrNull() as UStatusException).code)
    }

    @Test
    @DisplayName("Test subscribe and then unsubscribe happy path")
    fun test_subscribe_and_then_unsubscribe_happy_path() = testScope.runTest {
        val topic = testTopic
        val transport: UTransport = TestUTransport()
        val listener = UListener { }
        val handler = SubscriptionChangeHandler { _, _ ->
        }
        val subscriber: USubscriptionClient = InMemoryUSubscriptionClient(transport, mockClient, mockNotifier)
        coEvery { mockClient.invokeMethod(any(), any(), any()) } returnsMany
                listOf(
                    Result.success(UPayload.pack(successSubscriptionResult)),
                    Result.success(UPayload.pack(successUnsubscriptionResult))
                )

        subscriber.subscribe(topic, listener, handler = handler)
        val result = subscriber.unsubscribe(topic, listener)
        assertEquals(UCode.OK, result.code)
    }

    @Test
    @DisplayName("Test subscribe and then unsubscribe happy path with handler")
    fun test_subscribe_and_then_unsubscribe_happy_path_with_handler() = testScope.runTest {
        val topic = testTopic
        val transport: UTransport = TestUTransport()
        val listener = UListener { }
        val subscriber: USubscriptionClient = InMemoryUSubscriptionClient(transport, mockClient, mockNotifier)
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
    fun test_unsubscribe_happy_path() = testScope.runTest {
        val topic = testTopic
        val transport: UTransport = TestUTransport()
        val subscriber: USubscriptionClient = InMemoryUSubscriptionClient(transport, mockClient, mockNotifier)
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
    @DisplayName("Test unsubscribe happy path with CallOption")
    fun test_unsubscribe_happy_path_with_callOption() = testScope.runTest {
        val topic = testTopic
        val transport: UTransport = TestUTransport()
        val subscriber: USubscriptionClient = InMemoryUSubscriptionClient(transport, mockClient, mockNotifier)
        coEvery { mockClient.invokeMethod(any(), any(), any()) } returns Result.success(
            UPayload.pack(
                successUnsubscriptionResult
            )
        )

        val result = subscriber.unsubscribe(topic, {
            throw UnsupportedOperationException("should not execute")
        }, CallOptions())
        assertEquals(UCode.NOT_FOUND, result.code)
    }

    @Test
    @DisplayName("Test unregisterListener after we successfully subscribed to a topic")
    fun testUnregisterListener() = testScope.runTest {
        val topic = testTopic
        val myListener = UListener { }
        val transport: UTransport = TestUTransport()
        val subscriber: USubscriptionClient = InMemoryUSubscriptionClient(transport, mockClient, mockNotifier)

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
    fun testUnsubscribeWithUStatusException() = testScope.runTest {
        val topic = testTopic
        val transport: UTransport = CommStatusTransport()
        val subscriber: USubscriptionClient = InMemoryUSubscriptionClient(transport, mockClient, mockNotifier)

        coEvery { mockClient.invokeMethod(any(), any(), any()) } returns Result.failure(
            UStatusException(UCode.FAILED_PRECONDITION, "CommStatus Error")
        )

        val result = subscriber.unsubscribe(topic, {})

        assertEquals(UCode.FAILED_PRECONDITION, result.code)
        assertEquals("CommStatus Error", result.message)
    }

    @Test
    @DisplayName("Test unsubscribe with OtherException")
    fun testUnsubscribeWithOtherException() = testScope.runTest {
        val topic = testTopic
        val transport: UTransport = CommStatusTransport()
        val subscriber: USubscriptionClient = InMemoryUSubscriptionClient(transport, mockClient, mockNotifier)

        coEvery { mockClient.invokeMethod(any(), any(), any()) } returns Result.failure(
            IllegalStateException("Some Error")
        )

        val result = subscriber.unsubscribe(topic, {})

        assertEquals(UCode.INVALID_ARGUMENT, result.code)
        assertEquals("Some Error", result.message)
    }

    @Test
    @DisplayName("Test unsubscribe with OtherException No Message")
    fun testUnsubscribeWithOtherExceptionNoMessage() = testScope.runTest {
        val topic = testTopic
        val transport: UTransport = CommStatusTransport()
        val subscriber: USubscriptionClient = InMemoryUSubscriptionClient(transport, mockClient, mockNotifier)

        coEvery { mockClient.invokeMethod(any(), any(), any()) } returns Result.failure(
            IllegalStateException()
        )

        val result = subscriber.unsubscribe(topic, {})

        assertEquals(UCode.INVALID_ARGUMENT, result.code)
        assertEquals("Invalid argument", result.message)
    }

    @Test
    @DisplayName("Test register for Notifications without Handler")
    fun testRegisterForNotificationsWithoutHandler() = testScope.runTest {
        val topic = testTopic
        val transport = TestUTransport()
        val subscriber: USubscriptionClient = InMemoryUSubscriptionClient(transport, mockClient, mockNotifier)
        val response = notificationsResponse { }
        coEvery { mockClient.invokeMethod(any(), any(), any()) } returns Result.success(
            UPayload.pack(response)
        )
        val result = subscriber.registerForNotifications(topic)
        val uriSlot = slot<UUri>()
        coVerify { mockClient.invokeMethod(capture(uriSlot), any(), any()) }
        assertEquals(6, uriSlot.captured.resourceId)
        assertTrue(result.isSuccess)
        assertEquals(response, result.getOrNull())
    }

    @Test
    @DisplayName("Test register for Notifications Exception")
    fun testRegisterForNotificationsException() = testScope.runTest {
        val topic = testTopic
        val transport = TestUTransport()
        val exception = IllegalStateException("Some Error")
        val subscriber: USubscriptionClient = InMemoryUSubscriptionClient(transport, mockClient, mockNotifier)
        coEvery { mockClient.invokeMethod(any(), any(), any()) } returns Result.failure(exception)
        val result = subscriber.registerForNotifications(topic)
        val uriSlot = slot<UUri>()
        coVerify { mockClient.invokeMethod(capture(uriSlot), any(), any()) }
        assertEquals(6, uriSlot.captured.resourceId)
        assertTrue(result.isFailure)
        assertEquals(exception, result.exceptionOrNull())
    }

    @Test
    @DisplayName("Test register for Notifications with CallOption")
    fun testRegisterForNotificationsWithCallOption() = testScope.runTest {
        val topic = testTopic
        val transport = TestUTransport()
        val subscriber: USubscriptionClient = InMemoryUSubscriptionClient(transport, mockClient, mockNotifier)
        val response = notificationsResponse { }
        coEvery { mockClient.invokeMethod(any(), any(), any()) } returns Result.success(
            UPayload.pack(response)
        )
        val callOptions = CallOptions(100, token = "testToken")
        val result = subscriber.registerForNotifications(topic, callOptions)
        coVerify { mockClient.invokeMethod(any(), any(), callOptions) }
        assertTrue(result.isSuccess)
        assertEquals(response, result.getOrNull())
    }

    @Test
    @DisplayName("Test register for Notifications with Handler")
    fun testRegisterForNotificationsWithHandler() = testScope.runTest {
        val topic = testTopic
        val transport = TestUTransport()

        val response = notificationsResponse { }
        coEvery { mockClient.invokeMethod(any(), any(), any()) } returns Result.success(
            UPayload.pack(response)
        )
        val notificationListenerSlot = slot<UListener>()
        coEvery {
            mockNotifier.registerNotificationListener(
                any(),
                capture(notificationListenerSlot)
            )
        } returns OK_STATUS
        var counter = 0
        val handler = SubscriptionChangeHandler { t, status ->
            counter += 1
            assertEquals(topic, t)
            assertEquals(SubscriptionStatus.State.SUBSCRIBED, status.state)
        }

        val subscriber: USubscriptionClient = InMemoryUSubscriptionClient(transport, mockClient, mockNotifier)
        val result = subscriber.registerForNotifications(topic, handler = handler)
        coVerify { mockClient.invokeMethod(any(), any(), any()) }
        assertTrue(result.isSuccess)
        assertEquals(response, result.getOrNull())
        notificationListenerSlot.captured.onReceive(uMessage {
            forNotification(testTopic, testSink)
            setPayload(
                UPayload.pack(update {
                    this.topic = testTopic
                    status = subscriptionStatus { state = SubscriptionStatus.State.SUBSCRIBED }
                })
            )
        })
        assertEquals(1, counter)
    }

    @Test
    @DisplayName("Test register for Notifications twice with diff Handler")
    fun testRegisterForNotificationsTwiceWithDiffHandler() = testScope.runTest {
        val topic = testTopic
        val transport = TestUTransport()
        val response = notificationsResponse { }
        coEvery { mockClient.invokeMethod(any(), any(), any()) } returns Result.success(
            UPayload.pack(response)
        )
        val handler1 = SubscriptionChangeHandler { _, _ ->
        }

        val handler2 = SubscriptionChangeHandler { _, _ ->
        }

        val subscriber: USubscriptionClient = InMemoryUSubscriptionClient(transport, mockClient, mockNotifier)
        val result1 = subscriber.registerForNotifications(topic, handler = handler1)
        val result2 = subscriber.registerForNotifications(topic, handler = handler2)
        coVerify(exactly = 2) { mockClient.invokeMethod(any(), any(), any()) }
        assertTrue(result1.isSuccess)
        assertTrue(result2.isFailure)
        assertEquals(response, result1.getOrNull())
        assertEquals(UCode.ALREADY_EXISTS, (result2.exceptionOrNull() as UStatusException).code)
        assertEquals("Handler already registered", (result2.exceptionOrNull() as UStatusException).message)
    }

    @Test
    @DisplayName("Test register for Notifications twice with same Handler")
    fun testRegisterForNotificationsTwiceWithSameHandler() = testScope.runTest {
        val topic = testTopic
        val transport = TestUTransport()
        val response = notificationsResponse { }
        coEvery { mockClient.invokeMethod(any(), any(), any()) } returns Result.success(
            UPayload.pack(response)
        )
        val handler = SubscriptionChangeHandler { _, _ ->
        }

        val subscriber: USubscriptionClient = InMemoryUSubscriptionClient(transport, mockClient, mockNotifier)
        val result1 = subscriber.registerForNotifications(topic, handler = handler)
        val result2 = subscriber.registerForNotifications(topic, handler = handler)
        coVerify(exactly = 2) { mockClient.invokeMethod(any(), any(), any()) }
        assertTrue(result1.isSuccess)
        assertTrue(result2.isSuccess)
        assertEquals(response, result1.getOrNull())
        assertEquals(response, result2.getOrNull())
    }

    @Test
    @DisplayName("Test unregister for Notifications")
    fun testUnregisterForNotifications() = testScope.runTest {
        val topic = testTopic
        val transport = TestUTransport()

        val response = notificationsResponse { }
        coEvery { mockClient.invokeMethod(any(), any(), any()) } returns Result.success(
            UPayload.pack(response)
        )
        val subscriber: USubscriptionClient = InMemoryUSubscriptionClient(transport, mockClient, mockNotifier)
        val result = subscriber.unregisterForNotifications(topic)

        val uriSlot = slot<UUri>()
        coVerify { mockClient.invokeMethod(capture(uriSlot), any(), any()) }
        assertEquals(7, uriSlot.captured.resourceId)
        assertTrue(result.isSuccess)
        assertEquals(response, result.getOrNull())
    }

    @Test
    @DisplayName("Test unregister for Notifications Exception")
    fun testUnregisterForNotificationsException() = testScope.runTest {
        val topic = testTopic
        val transport = TestUTransport()
        val exception = IllegalStateException("Some Error")
        coEvery { mockClient.invokeMethod(any(), any(), any()) } returns Result.failure(exception)
        val subscriber: USubscriptionClient = InMemoryUSubscriptionClient(transport, mockClient, mockNotifier)
        val result = subscriber.unregisterForNotifications(topic)

        val uriSlot = slot<UUri>()
        coVerify { mockClient.invokeMethod(capture(uriSlot), any(), any()) }
        assertEquals(7, uriSlot.captured.resourceId)
        assertTrue(result.isFailure)
        assertEquals(exception, result.exceptionOrNull())
    }

    @Test
    @DisplayName("Test unregister for Notifications with CallOption")
    fun testUnregisterForNotificationsWithCallOption() = testScope.runTest {
        val topic = testTopic
        val transport = TestUTransport()

        val response = notificationsResponse { }
        coEvery { mockClient.invokeMethod(any(), any(), any()) } returns Result.success(
            UPayload.pack(response)
        )

        val callOptions = CallOptions(100, token = "testToken")
        val subscriber: USubscriptionClient = InMemoryUSubscriptionClient(transport, mockClient, mockNotifier)
        val result = subscriber.unregisterForNotifications(topic, callOptions)

        val uriSlot = slot<UUri>()
        coVerify { mockClient.invokeMethod(capture(uriSlot), any(), callOptions) }
        assertEquals(7, uriSlot.captured.resourceId)
        assertTrue(result.isSuccess)
        assertEquals(response, result.getOrNull())
    }

    @Test
    @DisplayName("Test FetchSubscribers")
    fun testFetchSubscribers() = testScope.runTest {
        val topic = testTopic
        val transport = TestUTransport()

        val response = fetchSubscribersResponse { }
        coEvery { mockClient.invokeMethod(any(), any(), any()) } returns Result.success(
            UPayload.pack(response)
        )

        val subscriber: USubscriptionClient = InMemoryUSubscriptionClient(transport, mockClient, mockNotifier)
        val result = subscriber.fetchSubscribers(topic)

        val uriSlot = slot<UUri>()
        coVerify { mockClient.invokeMethod(capture(uriSlot), any(), any()) }
        assertEquals(8, uriSlot.captured.resourceId)
        assertTrue(result.isSuccess)
        assertEquals(response, result.getOrNull())
    }

    @Test
    @DisplayName("Test FetchSubscribers Exception")
    fun testFetchSubscribersException() = testScope.runTest {
        val topic = testTopic
        val transport = TestUTransport()
        val exception = IllegalStateException("Some Error")
        coEvery { mockClient.invokeMethod(any(), any(), any()) } returns Result.failure(
            exception
        )

        val subscriber: USubscriptionClient = InMemoryUSubscriptionClient(transport, mockClient, mockNotifier)
        val result = subscriber.fetchSubscribers(topic)

        val uriSlot = slot<UUri>()
        coVerify { mockClient.invokeMethod(capture(uriSlot), any(), any()) }
        assertEquals(8, uriSlot.captured.resourceId)
        assertTrue(result.isFailure)
        assertEquals(exception, result.exceptionOrNull())
    }

    @Test
    @DisplayName("Test FetchSubscribers with CallOption")
    fun testFetchSubscribersWithCallOption() = testScope.runTest {
        val topic = testTopic
        val transport = TestUTransport()

        val response = fetchSubscribersResponse { }
        coEvery { mockClient.invokeMethod(any(), any(), any()) } returns Result.success(
            UPayload.pack(response)
        )

        val callOptions = CallOptions(100, token = "testToken")
        val subscriber: USubscriptionClient = InMemoryUSubscriptionClient(transport, mockClient, mockNotifier)
        val result = subscriber.fetchSubscribers(topic, callOptions)

        val uriSlot = slot<UUri>()
        coVerify { mockClient.invokeMethod(capture(uriSlot), any(), callOptions) }
        assertEquals(8, uriSlot.captured.resourceId)
        assertTrue(result.isSuccess)
        assertEquals(response, result.getOrNull())
    }

    @Test
    @DisplayName("Test FetchSubscriptions")
    fun testFetchSubscriptions() = testScope.runTest {
        val transport = TestUTransport()

        val response = fetchSubscriptionsResponse { }
        coEvery { mockClient.invokeMethod(any(), any(), any()) } returns Result.success(
            UPayload.pack(response)
        )

        val subscriber: USubscriptionClient = InMemoryUSubscriptionClient(transport, mockClient, mockNotifier)
        val result = subscriber.fetchSubscriptions(fetchSubscriptionsRequest {  })

        val uriSlot = slot<UUri>()
        coVerify { mockClient.invokeMethod(capture(uriSlot), any(), any()) }
        assertEquals(3, uriSlot.captured.resourceId)
        assertTrue(result.isSuccess)
        assertEquals(response, result.getOrNull())
    }

    @Test
    @DisplayName("Test FetchSubscriptions with CallOption")
    fun testFetchSubscriptionsWithCallOption() = testScope.runTest {
        val transport = TestUTransport()

        val response = fetchSubscriptionsResponse { }
        coEvery { mockClient.invokeMethod(any(), any(), any()) } returns Result.success(
            UPayload.pack(response)
        )

        val callOptions = CallOptions(100, token = "testToken")
        val subscriber: USubscriptionClient = InMemoryUSubscriptionClient(transport, mockClient, mockNotifier)
        val result = subscriber.fetchSubscriptions(fetchSubscriptionsRequest {  }, callOptions)

        val uriSlot = slot<UUri>()
        coVerify { mockClient.invokeMethod(capture(uriSlot), any(), callOptions) }
        assertEquals(3, uriSlot.captured.resourceId)
        assertTrue(result.isSuccess)
        assertEquals(response, result.getOrNull())
    }

    @Test
    @DisplayName("Test FetchSubscriptions Exception")
    fun testFetchSubscriptionsException() = testScope.runTest {
        val transport = TestUTransport()
        val exception = IllegalStateException("Some Error")
        coEvery { mockClient.invokeMethod(any(), any(), any()) } returns Result.failure(
            exception
        )

        val subscriber: USubscriptionClient = InMemoryUSubscriptionClient(transport, mockClient, mockNotifier)
        val result = subscriber.fetchSubscriptions(fetchSubscriptionsRequest {  })

        val uriSlot = slot<UUri>()
        coVerify { mockClient.invokeMethod(capture(uriSlot), any(), any()) }
        assertEquals(3, uriSlot.captured.resourceId)
        assertTrue(result.isFailure)
        assertEquals(exception, result.exceptionOrNull())
    }

    private val testTopic = uUri {
        authorityName = "hartley"
        ueId = 3
        ueVersionMajor = 1
        resourceId = 0x8000
    }
}