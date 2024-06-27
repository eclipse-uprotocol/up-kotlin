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

import org.eclipse.uprotocol.v1.*
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test


class UAttributesKtExtTest {
    @Test
    fun testPublish() {
        val attributes = uAttributes {
            forPublication(testSource, UPriority.UPRIORITY_CS1)
        }
        assertNotNull(attributes)
        assertEquals(UMessageType.UMESSAGE_TYPE_PUBLISH, attributes.type)
        assertEquals(UPriority.UPRIORITY_CS1, attributes.priority)
    }

    @Test
    fun testNotification() {
        val attributes = uAttributes {
            forNotification(testSource, testSink, UPriority.UPRIORITY_CS1)
        }
        assertNotNull(attributes)
        assertEquals(UMessageType.UMESSAGE_TYPE_NOTIFICATION, attributes.type)
        assertEquals(UPriority.UPRIORITY_CS1, attributes.priority)
        assertEquals(testSink, attributes.sink)
    }

    @Test
    fun testRequest() {
        val ttl = 1000
        val attributes: UAttributes = uAttributes {
            forRequest(testSource, testSink, UPriority.UPRIORITY_CS4, ttl)
        }
        assertNotNull(attributes)
        assertEquals(UMessageType.UMESSAGE_TYPE_REQUEST, attributes.type)
        assertEquals(UPriority.UPRIORITY_CS4, attributes.priority)
        assertEquals(testSink, attributes.sink)
        assertEquals(ttl, attributes.ttl)
    }

    @Test
    fun testResponse() {
        val attributes: UAttributes = uAttributes {
            forResponse(testSource, testSink, UPriority.UPRIORITY_CS6, uUID)
        }
        assertNotNull(attributes)
        assertEquals(UMessageType.UMESSAGE_TYPE_RESPONSE, attributes.type)
        assertEquals(UPriority.UPRIORITY_CS6, attributes.priority)
        assertEquals(testSink, attributes.sink)
        assertEquals(uUID, attributes.reqid)
    }

    @Test
    fun testResponseWithExistingRequest() {
        val request: UAttributes = uAttributes{
            forRequest(testSource, testSink, UPriority.UPRIORITY_CS6, 1000)
        }
        val response: UAttributes = uAttributes {
            forResponse(request)
        }
        assertEquals(UMessageType.UMESSAGE_TYPE_RESPONSE, response.type)
        assertEquals(UPriority.UPRIORITY_CS6, response.priority)
        assertEquals(request.source, response.sink)
        assertEquals(request.sink, response.source)
        assertEquals(request.id, response.reqid)
    }

    @Test
    fun testBuild() {
        val reqId: UUID = uUID
        val attributes: UAttributes = uAttributes {
            forPublication(testSource, UPriority.UPRIORITY_CS1)
            ttl = 1000
            token = "test_token"
            sink = testSink
            permissionLevel = 2
            commstatus = UCode.CANCELLED
            reqid = reqId
            traceparent = "test_traceparent"
        }
        assertNotNull(attributes)
        assertEquals(UMessageType.UMESSAGE_TYPE_PUBLISH, attributes.type)
        assertEquals(UPriority.UPRIORITY_CS1, attributes.priority)
        assertEquals(1000, attributes.ttl)
        assertEquals("test_token", attributes.token)
        assertEquals(testSink, attributes.sink)
        assertEquals(2, attributes.permissionLevel)
        assertEquals(UCode.CANCELLED, attributes.commstatus)
        assertEquals(reqId, attributes.reqid)
        assertEquals("test_traceparent",attributes.traceparent)
    }

    private val testSink = uUri {
    }


    private val uUID: UUID = uUID {
        val uuidJava: java.util.UUID = java.util.UUID.randomUUID()
        msb = uuidJava.mostSignificantBits
        lsb = uuidJava.leastSignificantBits

    }

    private val testSource: UUri = uUri {
    }
}
