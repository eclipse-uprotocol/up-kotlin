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

import com.google.protobuf.Any
import org.eclipse.uprotocol.v1.*
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

class UMessageKtExtKtTest {
    private val testSource = uUri {
        ueId = 2
        ueVersionMajor = 1
        resourceId = 0
    }
    private val testSink = uUri {
        authorityName = "vcu.someVin.veh.ultifi.gm.com"
        ueId = 1
        ueVersionMajor = 1
        resourceId = 0
    }

    private val testUUID = uUID {
        val uuidJava = java.util.UUID.randomUUID()
        msb = uuidJava.mostSignificantBits
        lsb = uuidJava.leastSignificantBits
    }

    @Test
    fun testPublish() {
        val publish: UMessage = uMessage {
            forPublication(testSource)
        }

        assertNotNull(publish)
        assertEquals(UMessageType.UMESSAGE_TYPE_PUBLISH, publish.attributes.type)
        assertEquals(UPriority.UPRIORITY_CS1, publish.attributes.priority)
    }

    @Test
    fun testNotification() {
        val notification: UMessage = uMessage {
            forNotification(testSource, testSink)
        }
        assertNotNull(notification)
        assertEquals(UMessageType.UMESSAGE_TYPE_NOTIFICATION, notification.attributes.type)
        assertEquals(UPriority.UPRIORITY_CS1, notification.attributes.priority)
        assertEquals(testSink, notification.attributes.sink)
    }

    @Test
    fun testRequest() {
        val ttl = 1000
        val request: UMessage = uMessage {
            forRequest(testSource, testSink, ttl)
        }
        assertNotNull(request)
        assertEquals(UMessageType.UMESSAGE_TYPE_REQUEST, request.attributes.type)
        assertEquals(UPriority.UPRIORITY_CS4, request.attributes.priority)
        assertEquals(testSink, request.attributes.sink)
        assertEquals(ttl, request.attributes.ttl)
    }

    @Test
    fun testResponse() {
        val reqId = testUUID
        val response: UMessage = uMessage {
            forResponse(testSource, testSink, reqId)
        }
        assertNotNull(response)
        assertEquals(UMessageType.UMESSAGE_TYPE_RESPONSE, response.attributes.type)
        assertEquals(UPriority.UPRIORITY_CS4, response.attributes.priority)
        assertEquals(testSink, response.attributes.sink)
        assertEquals(reqId, response.attributes.reqid)
    }

    @Test
    @DisplayName("Test response with existing request")
    fun testResponseWithExistingRequest() {
        val request: UMessage = uMessage {
            forRequest(testSource, testSink, 1000)

        }
        val response: UMessage = uMessage {
            forResponse(request.attributes)
        }
        assertNotNull(response)
        assertEquals(UMessageType.UMESSAGE_TYPE_RESPONSE, response.attributes.type)
        assertEquals(UPriority.UPRIORITY_CS4, request.attributes.priority)
        assertEquals(UPriority.UPRIORITY_CS4, response.attributes.priority)
        assertEquals(request.attributes.source, response.attributes.sink)
        assertEquals(request.attributes.sink, response.attributes.source)
        assertEquals(request.attributes.id, response.attributes.reqid)
    }

    @Test
    @DisplayName("Test building UMessage with google.protobuf.Message payload")
    fun testBuildWithPayload() {
        val message: UMessage = uMessage {
            forPublication(testSource)
            setPayload(testSink)
        }
        assertNotNull(message)
        assertNotNull(message.payload)
        assertEquals(UPayloadFormat.UPAYLOAD_FORMAT_PROTOBUF, message.attributes.payloadFormat)
        assertEquals(testSink.toByteString(), message.payload)
    }

    @Test
    @DisplayName("Test building UMessage with UPayload payload")
    fun testBuildWithUPayload() {
        val message: UMessage = uMessage {
            forPublication(testSource)
            setPayload(UPayloadFormat.UPAYLOAD_FORMAT_PROTOBUF, testSink.toByteString())
        }
        assertNotNull(message)
        assertNotNull(message.payload)
        assertEquals(UPayloadFormat.UPAYLOAD_FORMAT_PROTOBUF, message.attributes.payloadFormat)
        assertEquals(testSink.toByteString(), message.payload)
    }

    @Test
    @DisplayName("Test building UMessage with google.protobuf.Any payload")
    fun testBuildWithAnyPayload() {
        val message: UMessage = uMessage {
            forPublication(testSource)
            setPayload(Any.getDefaultInstance())
        }
        assertNotNull(message)
        assertNotNull(message.payload)
        assertEquals(
            UPayloadFormat.UPAYLOAD_FORMAT_PROTOBUF_WRAPPED_IN_ANY,
            message.attributes.payloadFormat
        )
        assertEquals(Any.getDefaultInstance().toByteString(), message.payload)
    }


    @Test
    @DisplayName("Test building response message with the wrong priority value of UPRIORITY_CS3")
    fun testBuildResponseWithWrongPriority() {
        val reqId = testUUID
        val response: UMessage = uMessage {
            forResponse(testSource, testSink, reqId)
            setPriority(UPriority.UPRIORITY_CS3)
        }
        assertNotNull(response)
        assertEquals(UMessageType.UMESSAGE_TYPE_RESPONSE, response.attributes.type)
        assertEquals(UPriority.UPRIORITY_CS4, response.attributes.priority)
        assertEquals(testSink, response.attributes.sink)
        assertEquals(reqId, response.attributes.reqid)
    }

    @Test
    @DisplayName("Test building request message with the wrong priority value of UPRIORITY_CS3")
    fun testBuildRequestWithWrongPriority() {
        val ttl = 1000
        val request: UMessage = uMessage {
            forRequest(testSource, testSink, ttl)
            setPriority(UPriority.UPRIORITY_CS3)
        }

        assertNotNull(request)
        assertEquals(UMessageType.UMESSAGE_TYPE_REQUEST, request.attributes.type)
        assertEquals(UPriority.UPRIORITY_CS4, request.attributes.priority)
        assertEquals(testSink, request.attributes.sink)
        assertEquals(ttl, request.attributes.ttl)
    }

    @Test
    @DisplayName("Test building notification message with the wrong priority value of UPRIORITY_CS0")
    fun testBuildNotificationWithWrongPriority() {
        val notification: UMessage = uMessage {
            forNotification(testSource, testSink)
            setPriority(UPriority.UPRIORITY_CS0)
        }
        assertNotNull(notification)
        assertEquals(UMessageType.UMESSAGE_TYPE_NOTIFICATION, notification.attributes.type)
        assertEquals(UPriority.UPRIORITY_CS1, notification.attributes.priority)
        assertEquals(testSink, notification.attributes.sink)
    }

    @Test
    @DisplayName("Test building publish message with the wrong priority value of UPRIORITY_CS0")
    fun testBuildPublishWithWrongPriority() {
        val publish: UMessage = uMessage {
            forPublication(testSource)
            setPriority(UPriority.UPRIORITY_CS0)
        }
        assertNotNull(publish)
        assertEquals(UMessageType.UMESSAGE_TYPE_PUBLISH, publish.attributes.type)
        assertEquals(UPriority.UPRIORITY_CS1, publish.attributes.priority)
    }

    @Test
    @DisplayName("Test building publish message with the priority value of UPRIORITY_CS4")
    fun testBuildPublishWithPriority() {
        val publish: UMessage = uMessage {
            forPublication(testSource)
            setPriority(UPriority.UPRIORITY_CS4)

        }
        assertNotNull(publish)
        assertEquals(UMessageType.UMESSAGE_TYPE_PUBLISH, publish.attributes.type)
        assertEquals(UPriority.UPRIORITY_CS4, publish.attributes.priority)
    }

    @Test
    @DisplayName("Test building publish message with a different Permission level")
    fun testSetPermissionLevel() {
        val message: UMessage = uMessage {
            forPublication(testSource)
            setPermissionLevel(5)
        }
        assertNotNull(message)
        assertEquals(5, message.attributes.permissionLevel)
    }

    @Test
    @DisplayName("Test building publish message with a different ttl")
    fun testSetTtl() {
        val message: UMessage = uMessage {
            forPublication(testSource)
            setTtl(1000)
        }
        assertNotNull(message)
        assertEquals(1000, message.attributes.ttl)
    }

    @Test
    @DisplayName("Test building publish message with a different commstatus")
    fun testSetCommStatus() {
        val message: UMessage = uMessage {
            forPublication(testSource)
            setCommStatus(UCode.OK)
        }
        assertNotNull(message)
        assertEquals(UCode.OK, message.attributes.commstatus)
    }

    @Test
    @DisplayName("Test building publish message with a different reqid")
    fun testSetReqid() {
        val message: UMessage = uMessage {
            forPublication(testSource)
            setReqid(testUUID)
        }
        assertNotNull(message)
        assertEquals(testUUID, message.attributes.reqid)
    }

    @Test
    @DisplayName("Test building publish message with a different token")
    fun testSetToken() {
        val message: UMessage = uMessage {
            forPublication(testSource)
            setToken("test_token")
        }
        assertNotNull(message)
        assertEquals("test_token", message.attributes.token)
    }

    @Test
    @DisplayName("Test building publish message with a different traceparent")
    fun testSetTraceparent() {
        val message: UMessage = uMessage {
            forPublication(testSource)
            setTraceparent("test_traceparent")
        }
        assertNotNull(message)
        assertEquals("test_traceparent", message.attributes.traceparent)
    }
}