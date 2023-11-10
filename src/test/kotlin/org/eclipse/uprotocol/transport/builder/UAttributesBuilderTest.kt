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
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull


class UAttributesBuilderTest {
    @Test
    fun testPublish() {
        val builder: UAttributesBuilder = UAttributesBuilder.publish(UPriority.UPRIORITY_CS1)
        assertNotNull(builder)
        val attributes: UAttributes = builder.build()
        assertNotNull(attributes)
        assertEquals(UMessageType.UMESSAGE_TYPE_PUBLISH, attributes.getType())
        assertEquals(UPriority.UPRIORITY_CS1, attributes.getPriority())
    }

    @Test
    fun testNotification() {
        val sink: UUri = buildSink()
        val builder: UAttributesBuilder = UAttributesBuilder.notification(UPriority.UPRIORITY_CS1, sink)
        assertNotNull(builder)
        val attributes: UAttributes = builder.build()
        assertNotNull(attributes)
        assertEquals(UMessageType.UMESSAGE_TYPE_PUBLISH, attributes.getType())
        assertEquals(UPriority.UPRIORITY_CS1, attributes.getPriority())
        assertEquals(sink, attributes.getSink())
    }

    @Test
    fun testRequest() {
        val sink: UUri = buildSink()
        val ttl: Int = 1000
        val builder: UAttributesBuilder = UAttributesBuilder.request(UPriority.UPRIORITY_CS4, sink, ttl)
        assertNotNull(builder)
        val attributes: UAttributes = builder.build()
        assertNotNull(attributes)
        assertEquals(UMessageType.UMESSAGE_TYPE_REQUEST, attributes.getType())
        assertEquals(UPriority.UPRIORITY_CS4, attributes.getPriority())
        assertEquals(sink, attributes.getSink())
        assertEquals(ttl, attributes.getTtl())
    }

    @Test
    fun testResponse() {
        val sink: UUri = buildSink()
        val reqId: UUID = uUID
        val builder: UAttributesBuilder = UAttributesBuilder.response(UPriority.UPRIORITY_CS6, sink, reqId)
        assertNotNull(builder)
        val attributes: UAttributes = builder.build()
        assertNotNull(attributes)
        assertEquals(UMessageType.UMESSAGE_TYPE_RESPONSE, attributes.getType())
        assertEquals(UPriority.UPRIORITY_CS6, attributes.getPriority())
        assertEquals(sink, attributes.getSink())
        assertEquals(reqId, attributes.getReqid())
    }

    @Test
    fun testBuild() {
        val reqId: UUID = uUID
        val builder: UAttributesBuilder =
            UAttributesBuilder.publish(UPriority.UPRIORITY_CS1).withTtl(1000).withToken("test_token")
                .withSink(buildSink()).withPermissionLevel(2).withCommStatus(1).withReqId(reqId)
        val attributes: UAttributes = builder.build()
        assertNotNull(attributes)
        assertEquals(UMessageType.UMESSAGE_TYPE_PUBLISH, attributes.getType())
        assertEquals(UPriority.UPRIORITY_CS1, attributes.getPriority())
        assertEquals(1000, attributes.getTtl())
        assertEquals("test_token", attributes.getToken())
        assertEquals(buildSink(), attributes.getSink())
        assertEquals(2, attributes.getPermissionLevel())
        assertEquals(1, attributes.getCommstatus())
        assertEquals(reqId, attributes.getReqid())
    }

    private fun buildSink(): UUri {
        return UUri.newBuilder().setAuthority(UAuthority.newBuilder().setName("vcu.someVin.veh.ultifi.gm.com"))
            .setEntity(UEntity.newBuilder().setName("petapp.ultifi.gm.com").setVersionMajor(1))
            .setResource(UResourceBuilder.forRpcResponse()).build()
    }

    private val uUID: UUID
        private get() {
            val uuid_java: java.util.UUID = java.util.UUID.randomUUID()
            return UUID.newBuilder().setMsb(uuid_java.getMostSignificantBits())
                .setLsb(uuid_java.getLeastSignificantBits())
                .build()
        }
}
