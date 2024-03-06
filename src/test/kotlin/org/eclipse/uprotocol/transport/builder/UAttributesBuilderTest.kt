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
package org.eclipse.uprotocol.transport.builder

import org.eclipse.uprotocol.uri.factory.UResourceFactory
import org.eclipse.uprotocol.v1.*
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test


class UAttributesBuilderTest {
    @Test
    fun testPublish() {
        val builder: UAttributesBuilder = UAttributesBuilder.publish(source, UPriority.UPRIORITY_CS1)
        assertNotNull(builder)
        val attributes: UAttributes = builder.build()
        assertNotNull(attributes)
        assertEquals(UMessageType.UMESSAGE_TYPE_PUBLISH, attributes.type)
        assertEquals(UPriority.UPRIORITY_CS1, attributes.priority)
    }

    @Test
    fun testNotification() {
        val builder: UAttributesBuilder = UAttributesBuilder.notification(source, sink, UPriority.UPRIORITY_CS1)
        assertNotNull(builder)
        val attributes: UAttributes = builder.build()
        assertNotNull(attributes)
        assertEquals(UMessageType.UMESSAGE_TYPE_PUBLISH, attributes.type)
        assertEquals(UPriority.UPRIORITY_CS1, attributes.priority)
        assertEquals(sink, attributes.sink)
    }

    @Test
    fun testRequest() {
        val ttl = 1000
        val builder: UAttributesBuilder = UAttributesBuilder.request(source, sink, UPriority.UPRIORITY_CS4, ttl)
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
        val builder: UAttributesBuilder = UAttributesBuilder.response(source, sink, UPriority.UPRIORITY_CS6, uUID)
        assertNotNull(builder)
        val attributes: UAttributes = builder.build()
        assertNotNull(attributes)
        assertEquals(UMessageType.UMESSAGE_TYPE_RESPONSE, attributes.type)
        assertEquals(UPriority.UPRIORITY_CS6, attributes.priority)
        assertEquals(sink, attributes.sink)
        assertEquals(uUID, attributes.reqid)
    }

    @Test
    fun testBuild() {
        val reqId: UUID = uUID
        val builder: UAttributesBuilder =
            UAttributesBuilder.publish(source, UPriority.UPRIORITY_CS1).withTtl(1000).withToken("test_token")
                .withSink(sink).withPermissionLevel(2).withCommStatus(1).withReqId(reqId)
        val attributes: UAttributes = builder.build()
        assertNotNull(attributes)
        assertEquals(UMessageType.UMESSAGE_TYPE_PUBLISH, attributes.type)
        assertEquals(UPriority.UPRIORITY_CS1, attributes.priority)
        assertEquals(1000, attributes.ttl)
        assertEquals("test_token", attributes.token)
        assertEquals(sink, attributes.sink)
        assertEquals(2, attributes.permissionLevel)
        assertEquals(1, attributes.commstatus)
        assertEquals(reqId, attributes.reqid)
    }

    private val sink = uUri {
        authority = uAuthority { name = "vcu.someVin.veh.ultifi.gm.com" }
        entity = uEntity {
            name = "petapp.ultifi.gm.com"
            versionMajor = 1
        }
        resource = UResourceFactory.createForRpcResponse()
    }


    private val uUID: UUID = uUID {
        val uuidJava: java.util.UUID = java.util.UUID.randomUUID()
        msb = uuidJava.mostSignificantBits
        lsb = uuidJava.leastSignificantBits

    }

    private val source: UUri = uUri {
        entity = uEntity {
            name = "hartley_app"
            versionMajor = 1
        }
        resource = UResourceFactory.createForRpcResponse()
    }
}
