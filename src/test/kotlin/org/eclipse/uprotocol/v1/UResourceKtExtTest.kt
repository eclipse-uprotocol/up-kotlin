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

import org.eclipse.uprotocol.uServiceTopic
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import kotlin.random.Random


class UResourceFactoryTest {
    @Test
    fun test_forRpcResponse() {
        val resource = uResource {
            forRpcResponse()
        }
        assertEquals("rpc", resource.name)
        assertEquals("response", resource.instance)
        assertEquals(0, resource.id)
    }

    @Test
    fun test_forRpcRequest_without_argu() {
        val resource = uResource {
            forRpcRequest()
        }
        assertEquals("rpc", resource.name)
        assertEquals("", resource.instance)
        assertEquals(0, resource.id)
    }

    @Test
    fun test_forRpcRequest_with_method() {
        val resource = uResource {
            forRpcRequest("test")
        }
        assertEquals("rpc", resource.name)
        assertEquals("test", resource.instance)
        assertEquals(0, resource.id)
    }

    @Test
    fun test_forRpcRequest_with_id() {
        val resource = uResource {
            forRpcRequest(id = 999)
        }
        assertEquals("rpc", resource.name)
        assertEquals("", resource.instance)
        assertEquals(999, resource.id)
    }

    @Test
    fun test_forRpcRequest() {
        val resource = uResource {
            forRpcRequest(method = "test", id = 999)
        }
        assertEquals("rpc", resource.name)
        assertEquals("test", resource.instance)
        assertEquals(999, resource.id)
    }

    @Test
    fun test_fromId_valid_id() {
        val idTest = Random.nextInt(0, 999)
        val resource = uResource {
            from(idTest)
        }
        assertEquals("rpc", resource.name)
        assertEquals("", resource.instance)
        assertEquals(idTest, resource.id)
    }

    @Test
    fun test_fromId_invalid_id() {
        val idTest = Random.nextInt(1000, Int.MAX_VALUE)
        val resource = uResource {
            from(idTest)
        }
        assertEquals("", resource.name)
        assertEquals("", resource.instance)
        assertEquals(idTest, resource.id)
    }

    @Test
    fun test_from_uservice_topic_valid_service_topic() {
        val topic = uServiceTopic {
            name = "SubscriptionChange"
            id = 0
            message = "Update"
        }
        val resource: UResource = uResource {
            from(topic)
        }
        assertEquals(resource.name, "SubscriptionChange")
        assertEquals(resource.instance, "")
        assertEquals(resource.id, 0)
        assertEquals(resource.message, "Update")
    }

    @Test
    fun test_from_uservice_topic_valid_service_topic_with_instance() {
        val topic = uServiceTopic {
            name = "door.front_left"
            id = 0x8000
            message = "Door"
        }

        val resource: UResource = uResource {
            from(topic)
        }
        assertEquals(resource.name, "door")
        assertEquals(resource.instance, "front_left")
        assertEquals(resource.id, 0x8000)
        assertEquals(resource.message, "Door")
    }
}