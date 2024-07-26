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

import org.eclipse.uprotocol.communication.UPayload
import org.eclipse.uprotocol.uuid.factory.UUIDV7
import org.eclipse.uprotocol.uuid.factory.UuidFactory
import org.eclipse.uprotocol.v1.*
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

class UMessageKtExtKtTest {
    private val testSource = uUri {
        ueId = 2
        ueVersionMajor = 1
        resourceId = 0
    }

    private val testTopic = uUri {
        ueId = 2
        ueVersionMajor = 1
        resourceId = 0x8000
    }

    private val testSink = uUri {
        authorityName = "vcu.someVin.veh.ultifi.gm.com"
        ueId = 1
        ueVersionMajor = 1
        resourceId = 0
    }

    private val testMethod = uUri {
        ueId = 2
        ueVersionMajor = 1
        resourceId = 1
    }

    private val testUUID = uUID {
        val uuidJava = java.util.UUID.randomUUID()
        msb = uuidJava.mostSignificantBits
        lsb = uuidJava.leastSignificantBits
    }

    @Test
    fun testPublish() {
        val publish: UMessage = uMessage {
            forPublication(testTopic)
        }

        assertNotNull(publish)
        assertEquals(UMessageType.UMESSAGE_TYPE_PUBLISH, publish.attributes.type)
        assertEquals(UPriority.UPRIORITY_CS1, publish.attributes.priority)
    }

    @Test
    fun testNotification() {
        val notification: UMessage = uMessage {
            forNotification(testTopic, testSink)
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
            forRequest(testSource, testMethod, ttl)
        }
        assertNotNull(request)
        assertEquals(UMessageType.UMESSAGE_TYPE_REQUEST, request.attributes.type)
        assertEquals(UPriority.UPRIORITY_CS4, request.attributes.priority)
        assertEquals(testMethod, request.attributes.sink)
        assertEquals(ttl, request.attributes.ttl)
    }

    @Test
    fun testResponse() {
        val reqId = UUIDV7()
        val response: UMessage = uMessage {
            forResponse(testMethod, testSink, reqId)
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
            forRequest(testSource, testMethod, 1000)

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
    @DisplayName("Test building UMessage with UPayload payload")
    fun testBuildWithUPayload() {
        val message: UMessage = uMessage {
            forPublication(testTopic)
            setPayload(UPayload(testSink.toByteString(), UPayloadFormat.UPAYLOAD_FORMAT_PROTOBUF))
        }
        assertNotNull(message)
        assertNotNull(message.payload)
        assertEquals(UPayloadFormat.UPAYLOAD_FORMAT_PROTOBUF, message.attributes.payloadFormat)
        assertEquals(testSink.toByteString(), message.payload)
    }

    @Test
    @DisplayName("Test building UMessage with empty payload")
    fun testBuildWithAnyPayload() {
        val message: UMessage = uMessage {
            forPublication(testTopic)
        }
        assertNotNull(message)
        assertFalse(message.hasPayload())
    }


    @Test
    @DisplayName("Test building response message with the wrong priority value of UPRIORITY_CS3")
    fun testBuildResponseWithWrongPriority() {
        val reqId = UUIDV7()
        val response: UMessage = uMessage {
            forResponse(testMethod, testSink, reqId)
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
            forRequest(testSource, testMethod, ttl)
            setPriority(UPriority.UPRIORITY_CS3)
        }

        assertNotNull(request)
        assertEquals(UMessageType.UMESSAGE_TYPE_REQUEST, request.attributes.type)
        assertEquals(UPriority.UPRIORITY_CS4, request.attributes.priority)
        assertEquals(testMethod, request.attributes.sink)
        assertEquals(ttl, request.attributes.ttl)
    }

    @Test
    @DisplayName("Test building notification message with the wrong priority value of UPRIORITY_CS0")
    fun testBuildNotificationWithWrongPriority() {
        val notification: UMessage = uMessage {
            forNotification(testTopic, testSink)
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
            forPublication(testTopic)
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
            forPublication(testTopic)
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
            forPublication(testTopic)
            setPermissionLevel(5)
        }
        assertNotNull(message)
        assertEquals(5, message.attributes.permissionLevel)
    }

    @Test
    @DisplayName("Test building publish message with a different ttl")
    fun testSetTtl() {
        val message: UMessage = uMessage {
            forPublication(testTopic)
            setTtl(1000)
        }
        assertNotNull(message)
        assertEquals(1000, message.attributes.ttl)
    }

    @Test
    @DisplayName("Test building publish message with a different commstatus")
    fun testSetCommStatus() {
        val message: UMessage = uMessage {
            forPublication(testTopic)
            setCommStatus(UCode.OK)
        }
        assertNotNull(message)
        assertEquals(UCode.OK, message.attributes.commstatus)
    }

    @Test
    @DisplayName("Test building publish message with a different reqid")
    fun testSetReqid() {
        val message: UMessage = uMessage {
            forPublication(testTopic)
            setReqid(testUUID)
        }
        assertNotNull(message)
        assertEquals(testUUID, message.attributes.reqid)
    }

    @Test
    @DisplayName("Test building publish message with a different token")
    fun testSetToken() {
        val message: UMessage = uMessage {
            forPublication(testTopic)
            setToken("test_token")
        }
        assertNotNull(message)
        assertEquals("test_token", message.attributes.token)
    }

    @Test
    @DisplayName("Test building publish message with a different traceparent")
    fun testSetTraceparent() {
        val message: UMessage = uMessage {
            forPublication(testTopic)
            setTraceparent("test_traceparent")
        }
        assertNotNull(message)
        assertEquals("test_traceparent", message.attributes.traceparent)
    }

    @Test
    @DisplayName("Test publish when source is not a valid topic")
    fun testPublishWithInvalidSource() {
        assertThrows(
            IllegalArgumentException::class.java
        ) {
            uMessage {
                forPublication(testSource)
            }
        }
    }


    @Test
    @DisplayName("Test notification when source is not a valid topic")
    fun testNotificationWithInvalidSource() {
        assertThrows(
            IllegalArgumentException::class.java
        ) {
            uMessage {
                forNotification(testSource, testSink)
            }
        }
    }


    @Test
    @DisplayName("Test notification when sink is not a valid UUri")
    fun testNotificationWithInvalidSink() {
        assertThrows(
            IllegalArgumentException::class.java
        ) {
            uMessage {
                forNotification(testTopic, testTopic)
            }
        }
    }


    @Test
    @DisplayName("Test request when source is not valid")
    fun testRequestWithInvalidSource() {
        assertThrows(
            IllegalArgumentException::class.java
        ) {
            uMessage {
                forRequest(testMethod, testMethod, 1000)
            }
        }
    }

    @Test
    @DisplayName("Test request when sink is not valid")
    fun testRequestWithInvalidSink() {
        assertThrows(
            IllegalArgumentException::class.java
        ) {
            uMessage {
                forRequest(testSource, testSource, 1000)
            }
        }
    }

    @Test
    @DisplayName("Test request when source and sink are not valid")
    fun testRequestWithInvalidSourceAndSink() {
        assertThrows(
            IllegalArgumentException::class.java
        ) {
            uMessage {
                forRequest(testMethod, testSource, 1000)
            }
        }
    }

    @Test
    @DisplayName("Test request when ttl is negative")
    fun testRequestWithNegativeTtl() {
        assertThrows(
            IllegalArgumentException::class.java
        ) {
            uMessage {
                forRequest(testSource, testMethod, -1)
            }
        }
    }


    @Test
    @DisplayName("Test response when source is not valid")
    fun testResponseWithInvalidSource() {
        assertThrows(
            IllegalArgumentException::class.java
        ) {
            uMessage {
                forResponse(testSink, testSink, UUIDV7())
            }
        }
    }


    @Test
    @DisplayName("Test response when sink is not valid")
    fun testResponseWithInvalidSink() {
        assertThrows(
            IllegalArgumentException::class.java
        ) {
            uMessage {
                forResponse(testMethod, testMethod, UUIDV7())
            }
        }
    }

    @Test
    @DisplayName("Test response when source and sink are not valid")
    fun testResponseWithInvalidSourceAndSink() {
        assertThrows<IllegalArgumentException>(
            IllegalArgumentException::class.java
        ) {
            uMessage {
                forResponse(testSource, testSource, UUIDV7())
            }
        }
    }

    @Test
    @DisplayName("Test response when we pass an invalid reqid")
    fun testResponseWithInvalidReqId() {
        assertThrows<IllegalArgumentException>(
            IllegalArgumentException::class.java
        ) {
            uMessage {
                forResponse(testMethod, testSink, uUID { })
            }
        }
    }


    @Test
    @DisplayName("Test notification when source is not a valid topic and and sink is not valid")
    fun testNotificationWithInvalidSourceAndSink() {
        assertThrows(
            IllegalArgumentException::class.java
        ) {
            uMessage {
                forNotification(testSink, testSource)
            }
        }
    }


    @Test
    @DisplayName("Test response builder when we pass UAttributes that is not a valid request type")
    fun testResponseBuilderWithInvalidRequestType() {
        assertThrows(
            IllegalArgumentException::class.java
        ) {
            uMessage {
                forResponse(uAttributes { })
            }
        }
    }
}