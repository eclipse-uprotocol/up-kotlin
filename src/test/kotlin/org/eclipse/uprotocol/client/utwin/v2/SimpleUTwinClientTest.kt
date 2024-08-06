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
package org.eclipse.uprotocol.client.utwin.v2

import io.mockk.*
import kotlinx.coroutines.test.runTest
import org.eclipse.uprotocol.communication.CallOptions
import org.eclipse.uprotocol.communication.RpcClient
import org.eclipse.uprotocol.communication.UPayload
import org.eclipse.uprotocol.communication.UStatusException
import org.eclipse.uprotocol.core.udiscovery.v3.addNodesResponse
import org.eclipse.uprotocol.core.utwin.v2.GetLastMessagesRequest
import org.eclipse.uprotocol.core.utwin.v2.GetLastMessagesResponse
import org.eclipse.uprotocol.core.utwin.v2.getLastMessagesRequest
import org.eclipse.uprotocol.core.utwin.v2.getLastMessagesResponse
import org.eclipse.uprotocol.core.utwin.v2.messageResponse
import org.eclipse.uprotocol.transport.UTransport
import org.eclipse.uprotocol.v1.*
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import kotlin.test.assertTrue

/**
 * The uTwin client implementation using RpcClient uP-L2 communication layer interface.
 * This is the test code for said implementation.
 */
class SimpleUTwinClientTest {
    private val rpcClient: RpcClient = mockk(relaxed = true)

    private val topic: UUri = uUri {
        authorityName = "hartley"
        ueId = 3
        ueVersionMajor = 1
        resourceId = 0x8000
    }

    @BeforeEach
    fun setup() {
    }

    @AfterEach
    fun tearDown() {
        unmockkAll()
    }


    @Test
    @DisplayName("Test calling getLastMessages() with valid topics")
    fun testGetLastMessages() = runTest {
        val topics = UUriBatch.newBuilder().addUris(topic).build()
        val optionSlot = slot<CallOptions>()
        coEvery { rpcClient.invokeMethod(any(), any(), capture(optionSlot)) } returns Result.success(UPayload.pack(
            getLastMessagesResponse { }
        ))
        val client = SimpleUTwinClient(rpcClient)
        val response = client.getLastMessages(topics)
        assertTrue(response.isSuccess)
        assertEquals("", optionSlot.captured.token)
        assertEquals(CallOptions.TIMEOUT_DEFAULT, optionSlot.captured.timeout)
        assertEquals(UPriority.UPRIORITY_CS4, optionSlot.captured.priority)
    }

    @Test
    @DisplayName("Test calling getLastMessages() with valid topics and Data in response")
    fun testGetLastMessagesWithDataInResponse() = runTest {
        val topics = UUriBatch.newBuilder().addUris(topic).build()
        val optionSlot = slot<CallOptions>()
        coEvery { rpcClient.invokeMethod(any(), any(), capture(optionSlot)) } returns Result.success(UPayload.pack(
            getLastMessagesResponse {
               this.responses.add(messageResponse {
                   this.topic = uUri {  }
               })
            }
        ))
        val client = SimpleUTwinClient(rpcClient)
        val response = client.getLastMessages(topics)
        assertTrue(response.isSuccess)
        assertEquals("", optionSlot.captured.token)
        assertEquals(CallOptions.TIMEOUT_DEFAULT, optionSlot.captured.timeout)
        assertEquals(UPriority.UPRIORITY_CS4, optionSlot.captured.priority)
    }

    @Test
    @DisplayName("Test calling getLastMessages() with valid topics and CallOptions")
    fun testGetLastMessagesWithCallOptions() = runTest {
        val topics = UUriBatch.newBuilder().addUris(topic).build()
        val options = CallOptions(
            timeout = 1000,
            priority = UPriority.UPRIORITY_CS2,
            token = "testToken"
        )
        val optionSlot = slot<CallOptions>()
        coEvery { rpcClient.invokeMethod(any(), any(), capture(optionSlot)) } returns Result.success(UPayload.pack(
            getLastMessagesResponse { }
        ))
        val client = SimpleUTwinClient(rpcClient)
        val response = client.getLastMessages(topics, options)
        assertTrue(response.isSuccess)
        assertEquals("testToken", optionSlot.captured.token)
        assertEquals(1000, optionSlot.captured.timeout)
        assertEquals(UPriority.UPRIORITY_CS2, optionSlot.captured.priority)
    }


    @Test
    @DisplayName("Test calling getLastMessages() with empty topics")
    fun testGetLastMessagesEmptyTopics() = runTest {
        val topics = uUriBatch { }
        val client = SimpleUTwinClient(rpcClient)
        val response = client.getLastMessages(topics)
        assertTrue(response.isFailure)
        assertEquals(UCode.INVALID_ARGUMENT, (response.exceptionOrNull() as UStatusException).code)
        assertEquals("topics must not be empty", response.exceptionOrNull()?.message)
    }


    @Test
    @DisplayName("Test calling getLastMessages() when the RpcClient completes exceptionally")
    fun testGetLastMessagesException() = runTest {

        val topics = UUriBatch.newBuilder().addUris(topic).build()
        coEvery { rpcClient.invokeMethod(any(), any(), any()) } returns
                Result.failure(UStatusException(UCode.NOT_FOUND, "Not found"))

        val client = SimpleUTwinClient(rpcClient)
        val response = client.getLastMessages(topics)
        assertTrue(response.isFailure)
        assertEquals(UCode.NOT_FOUND, (response.exceptionOrNull() as UStatusException).code)
        assertEquals("Not found", response.exceptionOrNull()?.message)
    }
}