/*
 * Copyright (c) 2023 General Motors GTO LLC
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
package org.eclipse.uprotocol.transport.builder

import org.eclipse.uprotocol.uri.builder.UResourceBuilder
import org.eclipse.uprotocol.v1.*
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test


class UAttributesBuilderTest {
    @Test
    fun testPublish() {
        val builder: UAttributesBuilder = UAttributesBuilder.publish(UPriority.UPRIORITY_CS1)
        assertNotNull(builder)
        val attributes: UAttributes = builder.build()
        assertNotNull(attributes)
        assertEquals(UMessageType.UMESSAGE_TYPE_PUBLISH, attributes.type)
        assertEquals(UPriority.UPRIORITY_CS1, attributes.priority)
    }

    @Test
    fun testNotification() {
        val sink: UUri = buildSink()
        val builder: UAttributesBuilder = UAttributesBuilder.notification(UPriority.UPRIORITY_CS1, sink)
        assertNotNull(builder)
        val attributes: UAttributes = builder.build()
        assertNotNull(attributes)
        assertEquals(UMessageType.UMESSAGE_TYPE_PUBLISH, attributes.type)
        assertEquals(UPriority.UPRIORITY_CS1, attributes.priority)
        assertEquals(sink, attributes.sink)
    }

    @Test
    fun testRequest() {
        val sink: UUri = buildSink()
        val ttl = 1000
        val builder: UAttributesBuilder = UAttributesBuilder.request(UPriority.UPRIORITY_CS4, sink, ttl)
        assertNotNull(builder)
        val attributes: UAttributes = builder.build()
        assertNotNull(attributes)
        assertEquals(UMessageType.UMESSAGE_TYPE_REQUEST, attributes.type)
        assertEquals(UPriority.UPRIORITY_CS4, attributes.priority)
        assertEquals(sink, attributes.sink)
        assertEquals(ttl, attributes.ttl)
    }

    @Test
    fun testResponse() {
        val sink: UUri = buildSink()
        val reqId: UUID = uUID
        val builder: UAttributesBuilder = UAttributesBuilder.response(UPriority.UPRIORITY_CS6, sink, reqId)
        assertNotNull(builder)
        val attributes: UAttributes = builder.build()
        assertNotNull(attributes)
        assertEquals(UMessageType.UMESSAGE_TYPE_RESPONSE, attributes.type)
        assertEquals(UPriority.UPRIORITY_CS6, attributes.priority)
        assertEquals(sink, attributes.sink)
        assertEquals(reqId, attributes.reqid)
    }

    @Test
    fun testBuild() {
        val reqId: UUID = uUID
        val builder: UAttributesBuilder =
            UAttributesBuilder.publish(UPriority.UPRIORITY_CS1).withTtl(1000).withToken("test_token")
                .withSink(buildSink()).withPermissionLevel(2).withCommStatus(1).withReqId(reqId)
        val attributes: UAttributes = builder.build()
        assertNotNull(attributes)
        assertEquals(UMessageType.UMESSAGE_TYPE_PUBLISH, attributes.type)
        assertEquals(UPriority.UPRIORITY_CS1, attributes.priority)
        assertEquals(1000, attributes.ttl)
        assertEquals("test_token", attributes.token)
        assertEquals(buildSink(), attributes.sink)
        assertEquals(2, attributes.permissionLevel)
        assertEquals(1, attributes.commstatus)
        assertEquals(reqId, attributes.reqid)
    }

    private fun buildSink(): UUri {
        return uUri {
            authority = uAuthority { name = "vcu.someVin.veh.ultifi.gm.com" }
            entity = uEntity {
                name = "petapp.ultifi.gm.com"
                versionMajor = 1
            }
            resource = UResourceBuilder.forRpcResponse()
        }

    }

    private val uUID: UUID
        get() {
            val uuidJava: java.util.UUID = java.util.UUID.randomUUID()
            return uUID {
                msb = uuidJava.mostSignificantBits
                lsb = uuidJava.leastSignificantBits
            }
        }
}
