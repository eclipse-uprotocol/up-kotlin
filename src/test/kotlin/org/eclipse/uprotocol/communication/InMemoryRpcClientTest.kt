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

import io.mockk.every
import io.mockk.mockkObject
import io.mockk.unmockkAll
import kotlinx.coroutines.*
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.eclipse.uprotocol.uuid.factory.UUIDV7
import org.eclipse.uprotocol.v1.*
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import java.time.Instant
import java.util.*

@ExperimentalCoroutinesApi
class InMemoryRpcClientTest {
    private val testDispatcher = UnconfinedTestDispatcher()
    private val testScope = TestScope(testDispatcher)


    private val mTransport = TestUTransport(dispatcher = testDispatcher)
    private val mRpcClient = InMemoryRpcClient(mTransport, testDispatcher)

    private val testMethodUri = uUri {
        authorityName = "hartley"
        ueId = 10
        ueVersionMajor = 1
        resourceId = 3
    }

    @AfterEach
    fun tearDown() {
        unmockkAll()
    }

    @Test
    @DisplayName("Test RegisterListener at init")
    fun testSendRequest() = testScope.runTest {
        assertEquals(1, mTransport.listeners.size)
    }

    @Test
    @DisplayName("Test RegisterListener at init with Default Dispatcher")
    fun testSendRequestWithDefaultDispatcher() = runBlocking {
        val transport = TestUTransport()
        InMemoryRpcClient(transport)
        delay(100)
        assertEquals(1, transport.listeners.size)
    }

    @Test
    @DisplayName("Test invokeMethod happy path")
    fun `test InvokeMethod Happy Path`() = testScope.runTest {
        assertEquals(1, mTransport.listeners.size)
        val payload = UPayload.packToAny(uUri { })
        val callOptions = CallOptions(token = "testToken", timeout = 1000)
        val result = mRpcClient.invokeMethod(testMethodUri, payload, callOptions)
        assertEquals(payload, result.getOrNull())

        assertEquals("testToken", mTransport.lastMessage.attributes.token)
    }

    @Test
    @DisplayName("Test invokeMethod happy path without token")
    fun `test InvokeMethod Happy Path without token`() = testScope.runTest {
        assertEquals(1, mTransport.listeners.size)
        val payload = UPayload.packToAny(uUri { })
        val callOptions = CallOptions(timeout = 1000)
        val result = mRpcClient.invokeMethod(testMethodUri, payload, callOptions)
        assertEquals(payload, result.getOrNull())

        assertEquals("", mTransport.lastMessage.attributes.token)
    }

    @Test
    @DisplayName("Test invokeMethod error to send")
    fun `test InvokeMethod Error to send`() = testScope.runTest {
        val transport = ErrorUTransport(testDispatcher)
        val rpcClient = InMemoryRpcClient(transport, testDispatcher)
        val payload = UPayload.packToAny(uUri { })
        val callOptions = CallOptions(timeout = 1000)
        val result = rpcClient.invokeMethod(testMethodUri, payload, callOptions)
        assertTrue(result.isFailure)
        val exception = result.exceptionOrNull() as UStatusException
        assertEquals(UCode.FAILED_PRECONDITION, exception.code)
    }

    @Test
    @DisplayName("Test invokeMethod response with CommStatus Error")
    fun `test InvokeMethod response with CommStatus error`() = testScope.runTest {
        val transport = CommStatusTransport(testDispatcher)
        val rpcClient = InMemoryRpcClient(transport, testDispatcher)
        val payload = UPayload.packToAny(uUri { })
        val result = rpcClient.invokeMethod(testMethodUri, payload)
        assertTrue(result.isFailure)
        val exception = result.exceptionOrNull() as UStatusException
        assertEquals(UCode.DATA_LOSS, exception.code)
        assertEquals("Communication error [DATA_LOSS]", exception.message)
    }

    @Test
    @DisplayName("Test invokeMethod response with CommStatus Error but status is ok")
    fun `test InvokeMethod response with CommStatus error but status is ok`() = testScope.runTest {
        val transport = CommStatusOkTransport(testDispatcher)
        val rpcClient = InMemoryRpcClient(transport, testDispatcher)
        val payload = UPayload.packToAny(uUri { })
        val result = rpcClient.invokeMethod(testMethodUri, payload)
        assertTrue(result.isSuccess)
        val unpack = result.getOrNull()!!.unpack<UStatus>()
        assertEquals(UCode.OK, unpack?.code)
        assertEquals("No Communication Error", unpack?.message)
    }

    @Test
    @DisplayName("Test invokeMethod Duplicate Request")
    fun `test InvokeMethod Duplicate Request`() = testScope.runTest {
        val time: Long = Instant.now().toEpochMilli()
        val randA = Random().nextInt() and 0xfff
        val randB = Random().nextLong() and 0x3fffffffffffffffL
        val testUUID = uUID {
            msb = (time shl 16) or (7L shl 12) or randA.toLong()
            lsb = randB or (1L shl 63)
        }
        mockkObject(UUIDV7)
        every { UUIDV7.invoke(any()) } returns testUUID
        assertEquals(1, mTransport.listeners.size)
        val payload = UPayload.packToAny(uUri { })
        val callOptions = CallOptions(token = "testToken", timeout = 1000)
        val result1 = async {
            mRpcClient.invokeMethod(testMethodUri, payload, callOptions)
        }
        val result2 = async {
            mRpcClient.invokeMethod(testMethodUri, payload, callOptions)
        }
        val results = awaitAll(result1, result2)

        val failed = results.first { it.isFailure }
        val exception = failed.exceptionOrNull() as UStatusException
        assertEquals(UCode.ALREADY_EXISTS, exception.code)
        assertEquals("Duplicated request found", exception.message)
        val success = results.first { it.isSuccess }
        assertEquals(payload, success.getOrNull())
    }

    @Test
    @DisplayName("Test invokeMethod with Multiple Responses")
    fun `test InvokeMethod with Multiple Responses`() = testScope.runTest {
        assertEquals(1, mTransport.listeners.size)
        val payload = UPayload.packToAny(uUri { })
        val callOptions = CallOptions(token = "testToken", timeout = 1000)
        val result = mRpcClient.invokeMethod(testMethodUri, payload, callOptions)
        assertEquals(payload, result.getOrNull())
        mTransport.listeners.last().onReceive(mTransport.lastMessage)
    }

    @Test
    @DisplayName("Test invokeMethod with Wrong Responses type")
    fun `test InvokeMethod with Responses but wrong type`() = testScope.runTest {
        val transport = InvalidResponseTransport(testDispatcher)
        val rpcClient = InMemoryRpcClient(transport, testDispatcher)
        val payload = UPayload.packToAny(uUri { })
        val callOptions = CallOptions(timeout = 1000)
        val result = rpcClient.invokeMethod(testMethodUri, payload, callOptions)
        assertTrue(result.isFailure)
        val exception = result.exceptionOrNull() as UStatusException
        assertEquals(UCode.DEADLINE_EXCEEDED, exception.code)
        assertEquals("Request timed out", exception.message)
    }

    @Test
    @DisplayName("Test invokeMethod with response arrives late")
    fun `test InvokeMethod with late Response`() = testScope.runTest {
        val transport = TimeoutUTransport(testDispatcher)
        val rpcClient = InMemoryRpcClient(transport, testDispatcher)
        val payload = UPayload.packToAny(uUri { })
        val callOptions = CallOptions(timeout = 1000)
        val result = rpcClient.invokeMethod(testMethodUri, payload, callOptions)
        assertTrue(result.isFailure)
        val exception = result.exceptionOrNull() as UStatusException
        assertEquals(UCode.DEADLINE_EXCEEDED, exception.code)
        assertEquals("Request timed out", exception.message)
    }

    @Test
    @DisplayName("Test invokeMethod Unknown failure")
    fun `test InvokeMethod Unknown failure`() = testScope.runTest {
        mockkObject(UUIDV7)
        every { UUIDV7.invoke(any()) } throws IllegalStateException("Unknown Error")

        val payload = UPayload.packToAny(uUri { })
        val callOptions = CallOptions(token = "testToken", timeout = 1000)

        val result = mRpcClient.invokeMethod(testMethodUri, payload, callOptions)

        val exception = result.exceptionOrNull() as UStatusException
        assertEquals(UCode.UNKNOWN, exception.code)
        assertEquals("Unknown Error", exception.message)

    }

    @Test
    fun `test close would remove listener`() = testScope.runTest {
        mRpcClient.close()
        assertEquals(0, mTransport.listeners.size)
    }
}
