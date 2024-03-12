/*
 * Copyright (c) 2024 General Motors GTO LLC
 *
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.eclipse.uprotocol.v1

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
        assertEquals(UMessageType.UMESSAGE_TYPE_PUBLISH, attributes.type)
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
    fun testBuild() {
        val reqId: UUID = uUID
        val attributes: UAttributes = uAttributes {
            forPublication(testSource, UPriority.UPRIORITY_CS1)
            ttl = 1000
            token = "test_token"
            sink = testSink
            permissionLevel = 2
            commstatus = 1
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
        assertEquals(1, attributes.commstatus)
        assertEquals(reqId, attributes.reqid)
        assertEquals("test_traceparent",attributes.traceparent)
    }

    private val testSink = uUri {
        authority = uAuthority { name = "vcu.someVin.veh.ultifi.gm.com" }
        entity = uEntity {
            name = "petapp.ultifi.gm.com"
            versionMajor = 1
        }
        resource = uResource { forRpcResponse() }
    }


    private val uUID: UUID = uUID {
        val uuidJava: java.util.UUID = java.util.UUID.randomUUID()
        msb = uuidJava.mostSignificantBits
        lsb = uuidJava.leastSignificantBits

    }

    private val testSource: UUri = uUri {
        entity = uEntity {
            name = "hartley_app"
            versionMajor = 1
        }
        resource = uResource { forRpcResponse() }
    }
}
