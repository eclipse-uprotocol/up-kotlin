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
package org.eclipse.uprotocol.uri.serializer

import org.eclipse.uprotocol.uri.validator.isEmpty
import org.eclipse.uprotocol.uri.validator.isRemote
import org.eclipse.uprotocol.v1.*
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test


class LongUriSerializerTest {
    @Test
    @DisplayName("Test using the serializers")
    fun test_using_the_serializers() {
        val uri: UUri = uUri {
            entity = uEntity { name = "hartley" }
            resource = uResource { forRpcRequest("raise") }
        }

        val strUri: String = LongUriSerializer.INSTANCE.serialize(uri)
        assertEquals("/hartley//rpc.raise", strUri)
        val uri2: UUri = LongUriSerializer.INSTANCE.deserialize(strUri)
        assertEquals(uri, uri2)
    }

    @Test
    @DisplayName("Test parse uProtocol uri that is empty string")
    fun test_parse_protocol_uri_when_is_empty_string() {
        val uri = ""
        val uuri: UUri = LongUriSerializer.INSTANCE.deserialize(uri)
        assertTrue(uuri.isEmpty())
    }

    @Test
    @DisplayName("Test parse uProtocol uri with schema and slash")
    fun test_parse_protocol_uri_with_schema_and_slash() {
        val uri = "/"
        val uuri: UUri = LongUriSerializer.INSTANCE.deserialize(uri)
        assertFalse(uuri.hasAuthority())
        assertTrue(uuri.isEmpty())
        assertFalse(uuri.hasResource())
        assertFalse(uuri.hasEntity())
        val uri2: String = LongUriSerializer.INSTANCE.serialize(uUri { })
        assertTrue(uri2.isEmpty())
    }

    @Test
    @DisplayName("Test parse uProtocol uri with schema and double slash")
    fun test_parse_protocol_uri_with_schema_and_double_slash() {
        val uri = "//"
        val uuri: UUri = LongUriSerializer.INSTANCE.deserialize(uri)
        assertFalse(uuri.hasAuthority())
        assertFalse(uuri.hasResource())
        assertFalse(uuri.hasEntity())
        assertTrue(uuri.isEmpty())
    }

    @Test
    @DisplayName("Test parse uProtocol uri with schema and 3 slash and something")
    fun test_parse_protocol_uri_with_schema_and_3_slash_and_something() {
        val uri = "///body.access"
        val uuri: UUri = LongUriSerializer.INSTANCE.deserialize(uri)
        assertFalse(uuri.hasAuthority())
        assertFalse(uuri.hasResource())
        assertFalse(uuri.hasEntity())
        assertTrue(uuri.isEmpty())
        assertNotEquals("body.access", uuri.entity.name)
        assertEquals(0, uuri.entity.versionMajor)
    }

    @Test
    @DisplayName("Test parse uProtocol uri with schema and 4 slash and something")
    fun test_parse_protocol_uri_with_schema_and_4_slash_and_something() {
        val uri = "////body.access"
        val uuri: UUri = LongUriSerializer.INSTANCE.deserialize(uri)
        assertFalse(uuri.authority.isRemote())
        assertFalse(uuri.hasResource())
        assertFalse(uuri.hasEntity())
        assertTrue(uuri.entity.name.isBlank())
        assertEquals(0, uuri.entity.versionMajor)
    }

    @Test
    @DisplayName("Test parse uProtocol uri with schema and 5 slash and something")
    fun test_parse_protocol_uri_with_schema_and_5_slash_and_something() {
        val uri = "/////body.access"
        val uuri: UUri = LongUriSerializer.INSTANCE.deserialize(uri)
        assertFalse(uuri.authority.isRemote())
        assertFalse(uuri.hasResource())
        assertFalse(uuri.hasEntity())
        assertTrue(uuri.isEmpty())
    }

    @Test
    @DisplayName("Test parse uProtocol uri with schema and 6 slash and something")
    fun test_parse_protocol_uri_with_schema_and_6_slash_and_something() {
        val uri = "//////body.access"
        val uuri: UUri = LongUriSerializer.INSTANCE.deserialize(uri)
        assertFalse(uuri.authority.isRemote())
        assertTrue(uuri.isEmpty())
    }

    @Test
    @DisplayName("Test parse uProtocol uri with local service no version")
    fun test_parse_protocol_uri_with_local_service_no_version() {
        val uri = "/body.access"
        val uuri: UUri = LongUriSerializer.INSTANCE.deserialize(uri)
        assertFalse(uuri.authority.isRemote())
        assertEquals("body.access", uuri.entity.name)
        assertEquals(0, uuri.entity.versionMajor)
        assertEquals(0, uuri.entity.versionMinor)
        assertFalse(uuri.hasResource())
    }

    @Test
    @DisplayName("Test parse uProtocol uri with local service with version")
    fun test_parse_protocol_uri_with_local_service_with_version() {
        val uri = "/body.access/1"
        val uuri: UUri = LongUriSerializer.INSTANCE.deserialize(uri)
        assertFalse(uuri.authority.isRemote())
        assertEquals("body.access", uuri.entity.name)
        assertEquals(1, uuri.entity.versionMajor)
        assertFalse(uuri.hasResource())
    }

    @Test
    @DisplayName("Test parse uProtocol uri with local service no version with resource name only")
    fun test_parse_protocol_uri_with_local_service_no_version_with_resource_name_only() {
        val uri = "/body.access//door"
        val uuri: UUri = LongUriSerializer.INSTANCE.deserialize(uri)
        assertFalse(uuri.authority.isRemote())
        assertEquals("body.access", uuri.entity.name)
        assertEquals(0, uuri.entity.versionMajor)
        assertEquals(0, uuri.entity.versionMinor)
        assertEquals("door", uuri.resource.name)
        assertTrue(uuri.resource.instance.isEmpty())
        assertTrue(uuri.resource.message.isEmpty())
    }

    @Test
    @DisplayName("Test parse uProtocol uri with local service with version with resource name only")
    fun test_parse_protocol_uri_with_local_service_with_version_with_resource_name_only() {
        val uri = "/body.access/1/door"
        val uuri: UUri = LongUriSerializer.INSTANCE.deserialize(uri)
        assertFalse(uuri.authority.isRemote())
        assertEquals("body.access", uuri.entity.name)
        assertEquals(1, uuri.entity.versionMajor)
        assertEquals("door", uuri.resource.name)
        assertTrue(uuri.resource.instance.isEmpty())
        assertTrue(uuri.resource.message.isEmpty())
    }

    @Test
    @DisplayName("Test parse uProtocol uri with local service no version with resource and instance only")
    fun test_parse_protocol_uri_with_local_service_no_version_with_resource_with_instance() {
        val uri = "/body.access//door.front_left"
        val uuri: UUri = LongUriSerializer.INSTANCE.deserialize(uri)
        assertFalse(uuri.authority.isRemote())
        assertEquals("body.access", uuri.entity.name)
        assertEquals(0, uuri.entity.versionMajor)
        assertEquals("door", uuri.resource.name)
        assertFalse(uuri.resource.instance.isEmpty())
        assertEquals("front_left", uuri.resource.instance)
        assertTrue(uuri.resource.message.isEmpty())
    }

    @Test
    @DisplayName("Test parse uProtocol uri with local service with version with resource and instance only")
    fun test_parse_protocol_uri_with_local_service_with_version_with_resource_with_getMessage() {
        val uri = "/body.access/1/door.front_left"
        val uuri: UUri = LongUriSerializer.INSTANCE.deserialize(uri)
        assertFalse(uuri.authority.isRemote())
        assertEquals("body.access", uuri.entity.name)
        assertNotEquals(0, uuri.entity.versionMajor)
        assertEquals(1, uuri.entity.versionMajor)
        assertEquals("door", uuri.resource.name)
        assertFalse(uuri.resource.instance.isEmpty())
        assertEquals("front_left", uuri.resource.instance)
        assertTrue(uuri.resource.message.isEmpty())
    }

    @Test
    @DisplayName("Test parse uProtocol uri with local service no version with resource with instance and message")
    fun test_parse_protocol_uri_with_local_service_no_version_with_resource_with_instance_and_getMessage() {
        val uri = "/body.access//door.front_left#Door"
        val uuri: UUri = LongUriSerializer.INSTANCE.deserialize(uri)
        assertFalse(uuri.authority.isRemote())
        assertEquals("body.access", uuri.entity.name)
        assertEquals(0, uuri.entity.versionMajor)
        assertEquals("door", uuri.resource.name)
        assertFalse(uuri.resource.instance.isEmpty())
        assertEquals("front_left", uuri.resource.instance)
        assertFalse(uuri.resource.message.isEmpty())
        assertEquals("Door", uuri.resource.message)
    }

    @Test
    @DisplayName("Test parse uProtocol uri with local service with version with resource with instance and message")
    fun test_parse_protocol_uri_with_local_service_with_version_with_resource_with_instance_and_getMessage() {
        val uri = "/body.access/1/door.front_left#Door"
        val uuri: UUri = LongUriSerializer.INSTANCE.deserialize(uri)
        assertFalse(uuri.authority.isRemote())
        assertEquals("body.access", uuri.entity.name)
        assertNotEquals(0, uuri.entity.versionMajor)
        assertEquals(1, uuri.entity.versionMajor)
        assertEquals("door", uuri.resource.name)
        assertFalse(uuri.resource.instance.isEmpty())
        assertEquals("front_left", uuri.resource.instance)
        assertFalse(uuri.resource.message.isEmpty())
        assertEquals("Door", uuri.resource.message)
    }

    @Test
    @DisplayName("Test parse uProtocol RPC uri with local service no version")
    fun test_parse_protocol_rpc_uri_with_local_service_no_version() {
        val uri = "/petapp//rpc.response"
        val uuri: UUri = LongUriSerializer.INSTANCE.deserialize(uri)
        assertFalse(uuri.authority.isRemote())
        assertEquals("petapp", uuri.entity.name)
        assertEquals(0, uuri.entity.versionMajor)
        assertEquals("rpc", uuri.resource.name)
        assertFalse(uuri.resource.instance.isEmpty())
        assertEquals("response", uuri.resource.instance)
        assertTrue(uuri.resource.message.isEmpty())
    }

    @Test
    @DisplayName("Test parse uProtocol RPC uri with local service with version")
    fun test_parse_protocol_rpc_uri_with_local_service_with_version() {
        val uri = "/petapp/1/rpc.response"
        val uuri: UUri = LongUriSerializer.INSTANCE.deserialize(uri)
        assertFalse(uuri.authority.isRemote())
        assertEquals("petapp", uuri.entity.name)
        assertNotEquals(0, uuri.entity.versionMajor)
        assertEquals(1, uuri.entity.versionMajor)
        assertEquals("rpc", uuri.resource.name)
        assertFalse(uuri.resource.instance.isEmpty())
        assertEquals("response", uuri.resource.instance)
        assertTrue(uuri.resource.message.isEmpty())
    }

    @Test
    @DisplayName("Test parse uProtocol uri with microRemote service , name with device and domain")
    fun test_parse_protocol_uri_with_remote_service_only_device_and_domain() {
        val uri = "//VCU.MY_CAR_VIN"
        val uuri: UUri = LongUriSerializer.INSTANCE.deserialize(uri)
        assertTrue(uuri.authority.isRemote())
        assertFalse(uuri.authority.name.isBlank())
        assertEquals("VCU.MY_CAR_VIN", uuri.authority.name)
    }

    @Test
    @DisplayName("Test parse uProtocol uri with microRemote service only device and cloud domain")
    fun test_parse_protocol_uri_with_remote_service_only_device_and_cloud_domain() {
        val uri = "//cloud.uprotocol.example.com"
        val uuri: UUri = LongUriSerializer.INSTANCE.deserialize(uri)
        assertTrue(uuri.authority.isRemote())
        assertFalse(uuri.authority.name.isBlank())
        assertEquals("cloud.uprotocol.example.com", uuri.authority.name)
        assertFalse(uuri.hasEntity())
        assertFalse(uuri.hasResource())
    }

    @Test
    @DisplayName("Test parse uProtocol uri with microRemote service no version")
    fun test_parse_protocol_uri_with_remote_service_no_version() {
        val uri = "//VCU.MY_CAR_VIN/body.access"
        val uuri: UUri = LongUriSerializer.INSTANCE.deserialize(uri)
        assertTrue(uuri.authority.isRemote())
        assertFalse(uuri.authority.name.isBlank())
        assertEquals("VCU.MY_CAR_VIN", uuri.authority.name)
        assertFalse(uuri.authority.name.isBlank())
        assertEquals("body.access", uuri.entity.name)
        assertEquals(0, uuri.entity.versionMajor)
        assertFalse(uuri.hasResource())
    }

    @Test
    @DisplayName("Test parse uProtocol uri with microRemote cloud service no version")
    fun test_parse_protocol_uri_with_remote_cloud_service_no_version() {
        val uri = "//cloud.uprotocol.example.com/body.access"
        val uuri: UUri = LongUriSerializer.INSTANCE.deserialize(uri)
        assertTrue(uuri.authority.isRemote())
        assertFalse(uuri.authority.name.isBlank())
        assertEquals("cloud.uprotocol.example.com", uuri.authority.name)
        assertFalse(uuri.authority.name.isBlank())
        assertEquals("body.access", uuri.entity.name)
        assertEquals(0, uuri.entity.versionMajor)
        assertFalse(uuri.hasResource())
    }

    @Test
    @DisplayName("Test parse uProtocol uri with microRemote service with version")
    fun test_parse_protocol_uri_with_remote_service_with_version() {
        val uri = "//VCU.MY_CAR_VIN/body.access/1"
        val uuri: UUri = LongUriSerializer.INSTANCE.deserialize(uri)
        assertTrue(uuri.authority.isRemote())
        assertFalse(uuri.authority.name.isBlank())
        assertEquals("VCU.MY_CAR_VIN", uuri.authority.name)
        assertFalse(uuri.authority.name.isBlank())
        assertEquals("body.access", uuri.entity.name)
        assertNotEquals(0, uuri.entity.versionMajor)
        assertEquals(1, uuri.entity.versionMajor)
        assertFalse(uuri.hasResource())
    }

    @Test
    @DisplayName("Test parse uProtocol uri with microRemote cloud service with version")
    fun test_parse_protocol_uri_with_remote_cloud_service_with_version() {
        val uri = "//cloud.uprotocol.example.com/body.access/1"
        val uuri: UUri = LongUriSerializer.INSTANCE.deserialize(uri)
        assertTrue(uuri.authority.isRemote())
        assertFalse(uuri.authority.name.isBlank())
        assertEquals("cloud.uprotocol.example.com", uuri.authority.name)
        assertFalse(uuri.authority.name.isBlank())
        assertEquals("body.access", uuri.entity.name)
        assertNotEquals(0, uuri.entity.versionMajor)
        assertEquals(1, uuri.entity.versionMajor)
        assertFalse(uuri.hasResource())
    }

    @Test
    @DisplayName("Test parse uProtocol uri with microRemote service no version with resource name only")
    fun test_parse_protocol_uri_with_remote_service_no_version_with_resource_name_only() {
        val uri = "//VCU.MY_CAR_VIN/body.access//door"
        val uuri: UUri = LongUriSerializer.INSTANCE.deserialize(uri)
        assertTrue(uuri.authority.isRemote())
        assertFalse(uuri.authority.name.isBlank())
        assertEquals("VCU.MY_CAR_VIN", uuri.authority.name)
        assertFalse(uuri.authority.name.isBlank())
        assertEquals("body.access", uuri.entity.name)
        assertEquals(0, uuri.entity.versionMajor)
        assertEquals("door", uuri.resource.name)
        assertTrue(uuri.resource.instance.isEmpty())
        assertTrue(uuri.resource.message.isEmpty())
    }

    @Test
    @DisplayName("Test parse uProtocol uri with microRemote cloud service no version with resource name only")
    fun test_parse_protocol_uri_with_remote_cloud_service_no_version_with_resource_name_only() {
        val uri = "//cloud.uprotocol.example.com/body.access//door"
        val uuri: UUri = LongUriSerializer.INSTANCE.deserialize(uri)
        assertTrue(uuri.authority.isRemote())
        assertFalse(uuri.authority.name.isBlank())
        assertEquals("cloud.uprotocol.example.com", uuri.authority.name)
        assertFalse(uuri.authority.name.isBlank())
        assertEquals("body.access", uuri.entity.name)
        assertEquals(0, uuri.entity.versionMajor)
        assertEquals("door", uuri.resource.name)
        assertTrue(uuri.resource.instance.isEmpty())
        assertTrue(uuri.resource.message.isEmpty())
    }

    @Test
    @DisplayName("Test parse uProtocol uri with microRemote service with version with resource name only")
    fun test_parse_protocol_uri_with_remote_service_with_version_with_resource_name_only() {
        val uri = "//VCU.MY_CAR_VIN/body.access/1/door"
        val uuri: UUri = LongUriSerializer.INSTANCE.deserialize(uri)
        assertTrue(uuri.authority.isRemote())
        assertFalse(uuri.authority.name.isBlank())
        assertEquals("VCU.MY_CAR_VIN", uuri.authority.name)
        assertFalse(uuri.authority.name.isBlank())
        assertEquals("body.access", uuri.entity.name)
        assertNotEquals(0, uuri.entity.versionMajor)
        assertEquals(1, uuri.entity.versionMajor)
        assertEquals("door", uuri.resource.name)
        assertTrue(uuri.resource.instance.isEmpty())
        assertTrue(uuri.resource.message.isEmpty())
    }

    @Test
    @DisplayName("Test parse uProtocol uri with microRemote cloud service with version with resource name only")
    fun test_parse_protocol_uri_with_remote_service_cloud_with_version_with_resource_name_only() {
        val uri = "//cloud.uprotocol.example.com/body.access/1/door"
        val uuri: UUri = LongUriSerializer.INSTANCE.deserialize(uri)
        assertTrue(uuri.authority.isRemote())
        assertFalse(uuri.authority.name.isBlank())
        assertEquals("cloud.uprotocol.example.com", uuri.authority.name)
        assertFalse(uuri.authority.name.isBlank())
        assertEquals("body.access", uuri.entity.name)
        assertNotEquals(0, uuri.entity.versionMajor)
        assertEquals(1, uuri.entity.versionMajor)
        assertEquals("door", uuri.resource.name)
        assertTrue(uuri.resource.instance.isEmpty())
        assertTrue(uuri.resource.message.isEmpty())
    }

    @Test
    @DisplayName("Test parse uProtocol uri with microRemote service no version with resource and instance no message")
    fun test_parse_protocol_uri_with_remote_service_no_version_with_resource_and_instance_no_getMessage() {
        val uri = "//VCU.MY_CAR_VIN/body.access//door.front_left"
        val uuri: UUri = LongUriSerializer.INSTANCE.deserialize(uri)
        assertTrue(uuri.authority.isRemote())
        assertFalse(uuri.authority.name.isBlank())
        assertEquals("VCU.MY_CAR_VIN", uuri.authority.name)
        assertFalse(uuri.authority.name.isBlank())
        assertEquals("body.access", uuri.entity.name)
        assertEquals(0, uuri.entity.versionMajor)
        assertEquals("door", uuri.resource.name)
        assertFalse(uuri.resource.instance.isEmpty())
        assertEquals("front_left", uuri.resource.instance)
        assertTrue(uuri.resource.message.isEmpty())
    }

    @Test
    @DisplayName("Test parse uProtocol uri with microRemote service with version with resource and instance no message")
    fun test_parse_protocol_uri_with_remote_service_with_version_with_resource_and_instance_no_getMessage() {
        val uri = "//VCU.MY_CAR_VIN/body.access/1/door.front_left"
        val uuri: UUri = LongUriSerializer.INSTANCE.deserialize(uri)
        assertTrue(uuri.authority.isRemote())
        assertFalse(uuri.authority.name.isBlank())
        assertEquals("VCU.MY_CAR_VIN", uuri.authority.name)
        assertFalse(uuri.authority.name.isBlank())
        assertEquals("body.access", uuri.entity.name)
        assertNotEquals(0, uuri.entity.versionMajor)
        assertEquals(1, uuri.entity.versionMajor)
        assertEquals("door", uuri.resource.name)
        assertFalse(uuri.resource.instance.isEmpty())
        assertEquals("front_left", uuri.resource.instance)
        assertTrue(uuri.resource.message.isEmpty())
    }

    @Test
    @DisplayName("Test parse uProtocol uri with microRemote service no version with resource and instance and message")
    fun test_parse_protocol_uri_with_remote_service_no_version_with_resource_and_instance_and_getMessage() {
        val uri = "//VCU.MY_CAR_VIN/body.access//door.front_left#Door"
        val uuri: UUri = LongUriSerializer.INSTANCE.deserialize(uri)
        assertTrue(uuri.authority.isRemote())
        assertFalse(uuri.authority.name.isBlank())
        assertEquals("VCU.MY_CAR_VIN", uuri.authority.name)
        assertFalse(uuri.authority.name.isBlank())
        assertEquals("body.access", uuri.entity.name)
        assertEquals(0, uuri.entity.versionMajor)
        assertEquals("door", uuri.resource.name)
        assertFalse(uuri.resource.instance.isEmpty())
        assertEquals("front_left", uuri.resource.instance)
        assertFalse(uuri.resource.message.isEmpty())
        assertEquals("Door", uuri.resource.message)
    }

    @Test
    @DisplayName("Test parse uProtocol uri with microRemote cloud service no version with resource and instance and message")
    fun test_parse_protocol_uri_with_remote_cloud_service_no_version_with_resource_and_instance_and_getMessage() {
        val uri = "//cloud.uprotocol.example.com/body.access//door.front_left#Door"
        val uuri: UUri = LongUriSerializer.INSTANCE.deserialize(uri)
        assertTrue(uuri.authority.isRemote())
        assertFalse(uuri.authority.name.isBlank())
        assertEquals("cloud.uprotocol.example.com", uuri.authority.name)
        assertFalse(uuri.authority.name.isBlank())
        assertEquals("body.access", uuri.entity.name)
        assertEquals(0, uuri.entity.versionMajor)
        assertEquals("door", uuri.resource.name)
        assertFalse(uuri.resource.instance.isEmpty())
        assertEquals("front_left", uuri.resource.instance)
        assertFalse(uuri.resource.message.isEmpty())
        assertEquals("Door", uuri.resource.message)
    }

    @Test
    @DisplayName("Test parse uProtocol uri with microRemote service with version with resource and instance and message")
    fun test_parse_protocol_uri_with_remote_service_with_version_with_resource_and_instance_and_getMessage() {
        val uri = "//VCU.MY_CAR_VIN/body.access/1/door.front_left#Door"
        val uuri: UUri = LongUriSerializer.INSTANCE.deserialize(uri)
        assertTrue(uuri.authority.isRemote())
        assertFalse(uuri.authority.name.isBlank())
        assertEquals("VCU.MY_CAR_VIN", uuri.authority.name)
        assertFalse(uuri.authority.name.isBlank())
        assertEquals("body.access", uuri.entity.name)
        assertNotEquals(0, uuri.entity.versionMajor)
        assertEquals(1, uuri.entity.versionMajor)
        assertEquals("door", uuri.resource.name)
        assertFalse(uuri.resource.instance.isEmpty())
        assertEquals("front_left", uuri.resource.instance)
        assertFalse(uuri.resource.message.isEmpty())
        assertEquals("Door", uuri.resource.message)
    }

    @Test
    @DisplayName("Test parse uProtocol uri with microRemote cloud service with version with resource and instance and message")
    fun test_parse_protocol_uri_with_remote_cloud_service_with_version_with_resource_and_instance_and_getMessage() {
        val uri = "//cloud.uprotocol.example.com/body.access/1/door.front_left#Door"
        val uuri: UUri = LongUriSerializer.INSTANCE.deserialize(uri)
        assertTrue(uuri.authority.isRemote())
        assertFalse(uuri.authority.name.isBlank())
        assertEquals("cloud.uprotocol.example.com", uuri.authority.name)
        assertFalse(uuri.authority.name.isBlank())
        assertEquals("body.access", uuri.entity.name)
        assertNotEquals(0, uuri.entity.versionMajor)
        assertEquals(1, uuri.entity.versionMajor)
        assertEquals("door", uuri.resource.name)
        assertFalse(uuri.resource.instance.isEmpty())
        assertEquals("front_left", uuri.resource.instance)
        assertFalse(uuri.resource.message.isEmpty())
        assertEquals("Door", uuri.resource.message)
    }

    @Test
    @DisplayName("Test parse uProtocol uri with microRemote service with version with resource with message when there is only device, no domain")
    fun test_parse_protocol_uri_with_remote_service_with_version_with_resource_with_message_device_no_domain() {
        val uri = "//VCU/body.access/1/door.front_left"
        val uuri: UUri = LongUriSerializer.INSTANCE.deserialize(uri)
        assertTrue(uuri.authority.isRemote())
        assertFalse(uuri.authority.name.isBlank())
        assertEquals("VCU", uuri.authority.name)
        assertFalse(uuri.authority.name.isEmpty())
        assertEquals("body.access", uuri.entity.name)
        assertNotEquals(0, uuri.entity.versionMajor)
        assertEquals(1, uuri.entity.versionMajor)
        assertEquals("door", uuri.resource.name)
        assertFalse(uuri.resource.instance.isEmpty())
        assertEquals("front_left", uuri.resource.instance)
        assertTrue(uuri.resource.message.isEmpty())
    }

    @Test
    @DisplayName("Test parse uProtocol RPC uri with microRemote service no version")
    fun test_parse_protocol_rpc_uri_with_remote_service_no_version() {
        val uri = "//bo.cloud/petapp//rpc.response"
        val uuri: UUri = LongUriSerializer.INSTANCE.deserialize(uri)
        assertTrue(uuri.authority.isRemote())
        assertFalse(uuri.authority.name.isBlank())
        assertEquals("bo.cloud", uuri.authority.name)
        assertFalse(uuri.authority.name.isBlank())
        assertEquals("petapp", uuri.entity.name)
        assertEquals(0, uuri.entity.versionMajor)
        assertEquals("rpc", uuri.resource.name)
        assertFalse(uuri.resource.instance.isEmpty())
        assertEquals("response", uuri.resource.instance)
        assertTrue(uuri.resource.message.isEmpty())
    }

    @Test
    @DisplayName("Test parse uProtocol RPC uri with microRemote service with version")
    fun test_parse_protocol_rpc_uri_with_remote_service_with_version() {
        val uri = "//bo.cloud/petapp/1/rpc.response"
        val uuri: UUri = LongUriSerializer.INSTANCE.deserialize(uri)
        assertTrue(uuri.authority.isRemote())
        assertFalse(uuri.authority.name.isBlank())
        assertEquals("bo.cloud", uuri.authority.name)
        assertFalse(uuri.authority.name.isBlank())
        assertEquals("petapp", uuri.entity.name)
        assertNotEquals(0, uuri.entity.versionMajor)
        assertEquals(1, uuri.entity.versionMajor)
        assertEquals("rpc", uuri.resource.name)
        assertFalse(uuri.resource.instance.isEmpty())
        assertEquals("response", uuri.resource.instance)
        assertTrue(uuri.resource.message.isEmpty())
    }

    @Test
    @DisplayName("Test Create a uProtocol URI from an empty  URI Object")
    fun test_build_protocol_uri_from__uri_when__uri_isEmpty() {
        val uuri: UUri = uUri { }
        val uProtocolUri: String = LongUriSerializer.INSTANCE.serialize(uuri)
        assertEquals("", uProtocolUri)
    }

    @Test
    @DisplayName("Test Create a uProtocol URI from an  URI object with an empty USE")
    fun test_build_protocol_uri_from__uri_when__uri_has_empty_use() {
        val use: UEntity = uEntity { }
        val uuri: UUri = uUri {
            authority = uAuthority { }
            entity = use
            resource = uResource { name = "door" }
        }
        val uProtocolUri: String = LongUriSerializer.INSTANCE.serialize(uuri)
        assertEquals("/////door", uProtocolUri)
    }

    @Test
    @DisplayName("Test Create a uProtocol URI from an  URI Object with a local authority with service no version")
    fun test_build_protocol_uri_from__uri_when__uri_has_local_authority_service_no_version() {
        val uuri: UUri = uUri { entity = uEntity { name = "body.access" } }

        val uProtocolUri: String = LongUriSerializer.INSTANCE.serialize(uuri)
        assertEquals("/body.access", uProtocolUri)
    }

    @Test
    @DisplayName("Test Create a uProtocol URI from an  URI Object with a local authority with service and version")
    fun test_build_protocol_uri_from__uri_when__uri_has_local_authority_service_and_version() {
        val use: UEntity = uEntity {
            name = "body.access"
            versionMajor = 1
        }
        val uuri: UUri = uUri {
            entity = use
            resource = uResource { }
        }
        val uProtocolUri: String = LongUriSerializer.INSTANCE.serialize(uuri)
        assertEquals("/body.access/1", uProtocolUri)
    }

    @Test
    @DisplayName("Test Create a uProtocol URI from an  URI Object with a local authority with service no version with resource")
    fun test_build_protocol_uri_from__uri_when__uri_has_local_authority_service_no_version_with_resource() {
        val use: UEntity = uEntity {
            name = "body.access"
        }
        val uuri: UUri = uUri {
            entity = use
            resource = uResource { name = "door" }
        }

        val uProtocolUri: String = LongUriSerializer.INSTANCE.serialize(uuri)
        assertEquals("/body.access//door", uProtocolUri)
    }

    @Test
    @DisplayName("Test Create a uProtocol URI from an  URI Object with a local authority with service and version with resource")
    fun test_build_protocol_uri_from__uri_when__uri_has_local_authority_service_and_version_with_resource() {
        val use: UEntity = uEntity {
            name = "body.access"
            versionMajor = 1
        }
        val uuri: UUri = uUri {
            entity = use
            resource = uResource { name = "door" }
        }

        val uProtocolUri: String = LongUriSerializer.INSTANCE.serialize(uuri)
        assertEquals("/body.access/1/door", uProtocolUri)
    }

    @Test
    @DisplayName("Test Create a uProtocol URI from an  URI Object with a local authority with service no version with resource with instance no message")
    fun test_build_protocol_uri_from__uri_when__uri_has_local_authority_service_no_version_with_resource_with_instance_no_getMessage() {
        val use: UEntity = uEntity {
            name = "body.access"
        }
        val uuri: UUri = uUri {
            entity = use
            resource = uResource {
                name = "door"
                instance = "front_left"
            }
        }

        val uProtocolUri: String = LongUriSerializer.INSTANCE.serialize(uuri)
        assertEquals("/body.access//door.front_left", uProtocolUri)
    }

    @Test
    @DisplayName("Test Create a uProtocol URI from an  URI Object with a local authority with service and version with resource with instance no message")
    fun test_build_protocol_uri_from__uri_when__uri_has_local_authority_service_and_version_with_resource_with_instance_no_getMessage() {
        val use: UEntity = uEntity {
            name = "body.access"
            versionMajor = 1
        }
        val uuri: UUri = uUri {
            entity = use
            resource = uResource {
                name = "door"
                instance = "front_left"
            }
        }


        val uProtocolUri: String = LongUriSerializer.INSTANCE.serialize(uuri)
        assertEquals("/body.access/1/door.front_left", uProtocolUri)
    }

    @Test
    @DisplayName("Test Create a uProtocol URI from an  URI Object with a local authority with service no version with resource with instance and message")
    fun test_build_protocol_uri_from__uri_when__uri_has_local_authority_service_no_version_with_resource_with_instance_with_getMessage() {
        val use: UEntity = uEntity {
            name = "body.access"
        }
        val uuri: UUri = uUri {
            entity = use
            resource = uResource {
                name = "door"
                instance = "front_left"
                message = "Door"
            }
        }


        val uProtocolUri: String = LongUriSerializer.INSTANCE.serialize(uuri)
        assertEquals("/body.access//door.front_left#Door", uProtocolUri)
    }

    @Test
    @DisplayName("Test Create a uProtocol URI from an  URI Object with a local authority with service and version with resource with instance and message")
    fun test_build_protocol_uri_from__uri_when__uri_has_local_authority_service_and_version_with_resource_with_instance_with_getMessage() {
        val use: UEntity = uEntity {
            name = "body.access"
            versionMajor = 1
        }
        val uuri: UUri = uUri {
            entity = use
            resource = uResource {
                name = "door"
                instance = "front_left"
                message = "Door"
            }
        }
        val uProtocolUri: String = LongUriSerializer.INSTANCE.serialize(uuri)
        assertEquals("/body.access/1/door.front_left#Door", uProtocolUri)
    }

    @Test
    @DisplayName("Test Create a uProtocol URI from an  URI Object with a microRemote authority with service no version")
    fun test_build_protocol_uri_from__uri_when__uri_has_remote_authority_service_no_version() {
        val use: UEntity = uEntity { name = "body.access" }
        val uuri: UUri = uUri {
            authority = uAuthority { name = "vcu.my_car_vin" }
            entity = use
        }

        val uProtocolUri: String = LongUriSerializer.INSTANCE.serialize(uuri)
        assertEquals("//vcu.my_car_vin/body.access", uProtocolUri)
    }

    @Test
    @DisplayName("Test Create a uProtocol URI from an  URI Object with a microRemote authority with service and version")
    fun test_build_protocol_uri_from__uri_when__uri_has_remote_authority_service_and_version() {
        val use: UEntity = uEntity {
            name = "body.access"
            versionMajor = 1
        }
        val uuri: UUri = uUri {
            entity = use
            authority = uAuthority {
                name = "vcu.my_car_vin"

            }
        }

        val uProtocolUri: String = LongUriSerializer.INSTANCE.serialize(uuri)
        assertEquals("//vcu.my_car_vin/body.access/1", uProtocolUri)
    }

    @Test
    @DisplayName("Test Create a uProtocol URI from an  URI Object with a microRemote cloud authority with service and version")
    fun test_build_protocol_uri_from__uri_when__uri_has_remote_cloud_authority_service_and_version() {
        val use: UEntity = uEntity {
            name = "body.access"
            versionMajor = 1
        }
        val uuri: UUri = uUri {
            authority = uAuthority { name = "cloud.uprotocol.example.com" }
            entity = use
        }

        val uProtocolUri: String = LongUriSerializer.INSTANCE.serialize(uuri)
        assertEquals("//cloud.uprotocol.example.com/body.access/1", uProtocolUri)
    }

    @Test
    @DisplayName("Test Create a uProtocol URI from an  URI Object with a microRemote authority with service and version with resource")
    fun test_build_protocol_uri_from__uri_when__uri_has_remote_authority_service_and_version_with_resource() {
        val use: UEntity = uEntity {
            name = "body.access"
            versionMajor = 1
        }
        val uuri: UUri = uUri {
            authority = uAuthority { name = "vcu.my_car_vin" }
            entity = use
            resource = uResource { name = "door" }
        }

        val uProtocolUri: String = LongUriSerializer.INSTANCE.serialize(uuri)
        assertEquals("//vcu.my_car_vin/body.access/1/door", uProtocolUri)
    }

    @Test
    @DisplayName("Test Create a uProtocol URI from an  URI Object with a microRemote authority with service no version with resource")
    fun test_build_protocol_uri_from__uri_when__uri_has_remote_authority_service_no_version_with_resource() {
        val use: UEntity = uEntity {
            name = "body.access"

        }
        val uuri: UUri = uUri {
            authority = uAuthority { name = "vcu.my_car_vin" }
            entity = use
            resource = uResource { name = "door" }
        }

        val uProtocolUri: String = LongUriSerializer.INSTANCE.serialize(uuri)
        assertEquals("//vcu.my_car_vin/body.access//door", uProtocolUri)
    }

    @Test
    @DisplayName("Test Create a uProtocol URI from an  URI Object with a microRemote authority with service and version with resource with instance no message")
    fun test_build_protocol_uri_from__uri_when__uri_has_remote_authority_service_and_version_with_resource_with_instance_no_getMessage() {
        val use: UEntity = uEntity {
            name = "body.access"
            versionMajor = 1
        }
        val uuri: UUri = uUri {
            authority = uAuthority { name = "vcu.my_car_vin" }
            entity = use
            resource = uResource {
                name = "door"
                instance = "front_left"
            }
        }

        val uProtocolUri: String = LongUriSerializer.INSTANCE.serialize(uuri)
        assertEquals("//vcu.my_car_vin/body.access/1/door.front_left", uProtocolUri)
    }

    @Test
    @DisplayName("Test Create a uProtocol URI from an  URI Object with a microRemote cloud authority with service and version with resource with instance no message")
    fun test_build_protocol_uri_from__uri_when__uri_has_remote_cloud_authority_service_and_version_with_resource_with_instance_no_getMessage() {
        val use: UEntity = uEntity {
            name = "body.access"
            versionMajor = 1
        }
        val uuri: UUri = uUri {
            authority = uAuthority { name = "cloud.uprotocol.example.com" }
            entity = use
            resource = uResource {
                name = "door"
                instance = "front_left"
            }
        }

        val uProtocolUri: String = LongUriSerializer.INSTANCE.serialize(uuri)
        assertEquals("//cloud.uprotocol.example.com/body.access/1/door.front_left", uProtocolUri)
    }

    @Test
    @DisplayName("Test Create a uProtocol URI from an  URI Object with a microRemote authority with service no version with resource with instance no message")
    fun test_build_protocol_uri_from__uri_when__uri_has_remote_authority_service_no_version_with_resource_with_instance_no_getMessage() {
        val use: UEntity = uEntity {
            name = "body.access"
        }
        val uuri: UUri = uUri {
            authority = uAuthority { name = "vcu.my_car_vin" }
            entity = use
            resource = uResource {
                name = "door"
                instance = "front_left"
            }
        }

        val uProtocolUri: String = LongUriSerializer.INSTANCE.serialize(uuri)
        assertEquals("//vcu.my_car_vin/body.access//door.front_left", uProtocolUri)
    }

    @Test
    @DisplayName("Test Create a uProtocol URI from an  URI Object with a microRemote authority with service and version with resource with instance and message")
    fun test_build_protocol_uri_from__uri_when__uri_has_remote_authority_service_and_version_with_resource_with_instance_and_getMessage() {
        val use: UEntity = uEntity {
            name = "body.access"
            versionMajor = 1
        }
        val uuri: UUri = uUri {
            authority = uAuthority { name = "vcu.my_car_vin" }
            entity = use
            resource = uResource {
                name = "door"
                instance = "front_left"
                message = "Door"
            }
        }


        val uProtocolUri: String = LongUriSerializer.INSTANCE.serialize(uuri)
        assertEquals("//vcu.my_car_vin/body.access/1/door.front_left#Door", uProtocolUri)
    }

    @Test
    @DisplayName("Test Create a uProtocol URI from an  URI Object with a microRemote authority with service no version with resource with instance and message")
    fun test_build_protocol_uri_from__uri_when__uri_has_remote_authority_service_no_version_with_resource_with_instance_and_getMessage() {
        val use: UEntity = uEntity {
            name = "body.access"
        }
        val uuri: UUri = uUri {
            authority = uAuthority { name = "vcu.my_car_vin" }
            entity = use
            resource = uResource {
                name = "door"
                instance = "front_left"
                message = "Door"
            }
        }

        val uProtocolUri: String = LongUriSerializer.INSTANCE.serialize(uuri)
        assertEquals("//vcu.my_car_vin/body.access//door.front_left#Door", uProtocolUri)
    }

    @Test
    @DisplayName("Test Create a uProtocol URI for the source part of an RPC request, where the source is local")
    fun test_build_protocol_uri_for_source_part_of_rpc_request_where_source_is_local() {
        val use: UEntity = uEntity {
            name = "petapp"
            versionMajor = 1
        }
        val uResource: UResource = uResource {
            name = "rpc"
            instance = "response"
        }
        val uProtocolUri: String = LongUriSerializer.INSTANCE.serialize(uUri {
            entity = use
            resource = uResource
        })
        assertEquals("/petapp/1/rpc.response", uProtocolUri)
    }

    @Test
    @DisplayName("Test Create a uProtocol URI for the source part of an RPC request, where the source is microRemote")
    fun test_build_protocol_uri_for_source_part_of_rpc_request_where_source_is_remote() {
        val uAuthority: UAuthority = uAuthority { name = "cloud.uprotocol.example.com" }
        val use: UEntity = uEntity {
            name = "petapp"
        }
        val uResource: UResource = uResource {
            name = "rpc"
            instance = "response"
        }
        val uProtocolUri: String = LongUriSerializer.INSTANCE.serialize(uUri {
            authority = uAuthority
            entity = use
            resource = uResource
        })
        assertEquals("//cloud.uprotocol.example.com/petapp//rpc.response", uProtocolUri)
    }

    @Test
    @DisplayName("Test Create a uProtocol URI from the parts of  URI Object with a microRemote authority with service and version with resource")
    fun test_build_protocol_uri_from__uri_parts_when__uri_has_remote_authority_service_and_version_with_resource() {
        val uAuthority: UAuthority = uAuthority { name = "vcu.my_car_vin" }
        val use: UEntity = uEntity {
            name = "body.access"
            versionMajor = 1
        }
        val uResource: UResource = uResource { name = "door" }
        val uProtocolUri: String = LongUriSerializer.INSTANCE.serialize(uUri {
            authority = uAuthority
            entity = use
            resource = uResource
        })
        assertEquals("//vcu.my_car_vin/body.access/1/door", uProtocolUri)
    }

    @Test
    @DisplayName("Test Create a custom URI using no scheme")
    fun test_custom_scheme_no_scheme() {
        val uAuthority: UAuthority = uAuthority { name = "vcu.my_car_vin" }
        val use: UEntity = uEntity {
            name = "body.access"
            versionMajor = 1
        }
        val uResource: UResource = uResource { name = "door" }
        val ucustomUri: String = LongUriSerializer.INSTANCE.serialize(uUri {
            authority = uAuthority
            entity = use
            resource = uResource
        }

        )
        assertEquals("//vcu.my_car_vin/body.access/1/door", ucustomUri)
    }

    @Test
    @DisplayName("Test parse local uProtocol uri with custom scheme")
    fun test_parse_local_protocol_uri_with_custom_scheme() {
        val uri = "custom:/body.access//door.front_left#Door"
        val uuri: UUri = LongUriSerializer.INSTANCE.deserialize(uri)
        assertFalse(uuri.authority.isRemote())
        assertEquals("body.access", uuri.entity.name)
        assertEquals(0, uuri.entity.versionMajor)
        assertEquals("door", uuri.resource.name)
        assertFalse(uuri.resource.instance.isEmpty())
        assertEquals("front_left", uuri.resource.instance)
        assertFalse(uuri.resource.message.isEmpty())
        assertEquals("Door", uuri.resource.message)
    }

    @Test
    @DisplayName("Test parse microRemote uProtocol uri with custom scheme")
    fun test_parse_remote_protocol_uri_with_custom_scheme() {
        val uri = "custom://vcu.vin/body.access//door.front_left#Door"
        val uri2 = "//vcu.vin/body.access//door.front_left#Door"
        val uuri: UUri = LongUriSerializer.INSTANCE.deserialize(uri)
        assertTrue(uuri.authority.isRemote())
        assertEquals("vcu.vin", uuri.authority.name)
        assertEquals("body.access", uuri.entity.name)
        assertEquals(0, uuri.entity.versionMajor)
        assertEquals("door", uuri.resource.name)
        assertEquals("front_left", uuri.resource.instance)
        assertEquals("Door", uuri.resource.message)
        assertEquals(uri2, LongUriSerializer.INSTANCE.serialize(uuri))
    }
}
