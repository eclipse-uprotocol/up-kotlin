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
*
* SPDX-FileType: SOURCE
* SPDX-FileCopyrightText: 2024 General Motors GTO LLC
* SPDX-License-Identifier: Apache-2.0
*/

package org.eclipse.uprotocol.uri.serializer

import com.google.protobuf.ByteString
import org.eclipse.uprotocol.uri.serializer.IpAddress.toBytes
import org.eclipse.uprotocol.v1.*
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test


class ShortUriSerializerTest {
    @Test
    @DisplayName("Test serialize with empty uri")
    fun testSerializeWithEmptyUri() {
        val strUri: String = ShortUriSerializer.INSTANCE.serialize(UUri.getDefaultInstance())
        assertEquals("", strUri)
    }


    @Test
    @DisplayName("Test Creating and using the ShortUriSerializer")
    fun testCreatingShortUriSerializer() {
        val uri: UUri = uUri {
            entity = uEntity{ id = 1; versionMajor = 1 }
            resource = uResource{ forRpcResponse() }
        }
        val strUri: String = ShortUriSerializer.INSTANCE.serialize(uri)
        assertEquals("/1/1/0", strUri)
        val uri2: UUri = ShortUriSerializer.INSTANCE.deserialize(strUri)
        assertEquals(uri, uri2)
    }

    @Test
    @DisplayName("Test Creating and using the ShortUriSerializer with a method")
    fun testCreatingShortUriSerializerWithMethod() {
        val uri: UUri = uUri {
            entity = uEntity{ id = 1; versionMajor = 1 }
            resource = uResource { forRpcRequest(id = 10) }
        }

        val strUri: String = ShortUriSerializer.INSTANCE.serialize(uri)
        assertEquals("/1/1/10", strUri)
        val uri2: UUri = ShortUriSerializer.INSTANCE.deserialize(strUri)
        assertEquals(uri, uri2)
    }

    @Test
    @DisplayName("Test Creating and using the ShortUriSerializer with a topic")
    fun testCreatingShortUriSerializerWithTopic() {
        val uri: UUri = uUri {
            entity = uEntity{ id = 1; versionMajor = 1 }
            resource = uResource { from(20000) }
        }
        val strUri: String = ShortUriSerializer.INSTANCE.serialize(uri)
        assertEquals("/1/1/20000", strUri)
        val uri2: UUri = ShortUriSerializer.INSTANCE.deserialize(strUri)
        assertEquals(uri, uri2)
    }

    @Test
    @DisplayName("Test Creating and using the ShortUriSerializer with id authority")
    fun testCreatingShortUriSerializerWithAuthority() {
        val uri: UUri = uUri {
            entity = uEntity{ id = 1; versionMajor = 1 }
            authority = uAuthority { id = ByteString.copyFromUtf8("19UYA31581L000000")}
            resource = uResource { from(20000) }
        }

        val strUri: String = ShortUriSerializer.INSTANCE.serialize(uri)
        assertEquals("//19UYA31581L000000/1/1/20000", strUri)
        val uri2: UUri = ShortUriSerializer.INSTANCE.deserialize(strUri)
        assertEquals(uri, uri2)
    }

    @Test
    @DisplayName("Test Creating and using the ShortUriSerializer with ip authority")
    fun testCreatingShortUriSerializerWithIpAuthority() {
        val uri: UUri = uUri {
            entity = uEntity{ id = 1; versionMajor = 1 }
            authority = uAuthority { ip = ByteString.copyFrom(toBytes("192.168.1.100")) }
            resource = uResource { from(20000) }
        }

        val strUri: String = ShortUriSerializer.INSTANCE.serialize(uri)
        assertEquals("//192.168.1.100/1/1/20000", strUri)
        val uri2: UUri = ShortUriSerializer.INSTANCE.deserialize(strUri)
        assertEquals(uri, uri2)
    }

    @Test
    @DisplayName("Test short serializing a URI that doesn't have a resource")
    fun testShortSerializingUriWithoutResource() {
        val uri = uUri {
            entity = uEntity { id = 1; versionMajor = 1 }
        }
        val strUri: String = ShortUriSerializer.INSTANCE.serialize(uri)
        assertEquals(strUri, "/1/1")
    }

    @Test
    @DisplayName("Test short serializing a URI that have a negative number for uEntity version major")
    fun testShortSerializingUriWithNegativeVersionMajor() {
        val uri: UUri = uUri {
            entity = uEntity { id = 1; versionMajor = -1 }
            resource = uResource { from(20000) }
        }
        val strUri: String = ShortUriSerializer.INSTANCE.serialize(uri)
        assertEquals(strUri, "/1//20000")
    }

    @Test
    @DisplayName("Test short deserialize an empty URI")
    fun testShortDeserializeEmptyUri() {
        val uri: UUri = ShortUriSerializer.INSTANCE.deserialize("")
        assertEquals(uri, UUri.getDefaultInstance())
    }

    @Test
    @DisplayName("Test short deserialize of a valid URI with scheme")
    fun testShortDeserializeUriWithSchemeAndAuthority() {
        val uri: UUri = ShortUriSerializer.INSTANCE.deserialize("up://mypc/1/1/1")
        assertTrue(uri.hasAuthority())
        assertEquals(uri.authority.id, ByteString.copyFromUtf8("mypc"))
        assertFalse(uri.authority.hasName())
        assertFalse(uri.authority.hasIp())
        assertTrue(uri.hasEntity())
        assertEquals(uri.entity.id, 1)
        assertEquals(uri.entity.versionMajor, 1)
        assertTrue(uri.hasResource())
        assertEquals(uri.resource.id, 1)
    }

    @Test
    @DisplayName("Test short deserialize of a valid URI without scheme")
    fun testShortDeserializeUriWithoutScheme() {
        val uri: UUri = ShortUriSerializer.INSTANCE.deserialize("//mypc/1/1/1")
        assertTrue(uri.hasAuthority())
        assertEquals(uri.authority.id, ByteString.copyFromUtf8("mypc"))
        assertFalse(uri.authority.hasName())
        assertFalse(uri.authority.hasIp())
        assertTrue(uri.hasEntity())
        assertEquals(uri.entity.id, 1)
        assertEquals(uri.entity.versionMajor, 1)
        assertTrue(uri.hasResource())
        assertEquals(uri.resource.id, 1)
    }

    @Test
    @DisplayName("Test short deserialize a uri that only contains //")
    fun testShortDeserializeUriWithOnlyAuthority() {
        val uri: UUri = ShortUriSerializer.INSTANCE.deserialize("//")
        assertEquals(uri, UUri.getDefaultInstance())
    }

    @Test
    @DisplayName("Test short deserialize a uri with scheme and only contains //")
    fun testShortDeserializeUriWithSchemeAndOnlyAuthority() {
        val uri: UUri = ShortUriSerializer.INSTANCE.deserialize("up://")
        assertEquals(uri, UUri.getDefaultInstance())
    }

    @Test
    @DisplayName("Test short serialize with UAuthority ip address that is invalid")
    fun testShortSerializeWithInvalidIpAddress() {
        val uri = UUri.newBuilder()
            .setEntity(UEntity.newBuilder().setId(1).setVersionMajor(1))
            .setAuthority(UAuthority.newBuilder().setIp(ByteString.copyFromUtf8("34823748273")))
            .build()
        val uriString: String = ShortUriSerializer.INSTANCE.serialize(uri)
        assertEquals(uriString, "")
    }

    @Test
    @DisplayName("Test short serialize with UAuthority that only have name and not ip or id")
    fun testShortSerializeWithAuthorityOnlyName() {
        val uri = UUri.newBuilder()
            .setEntity(UEntity.newBuilder().setId(1).setVersionMajor(1))
            .setAuthority(UAuthority.newBuilder().setName("mypc"))
            .build()
        val uriString: String = ShortUriSerializer.INSTANCE.serialize(uri)
        assertEquals(uriString, "")
    }

    @Test
    @DisplayName("Test short deserialize of a local URI that has too many parts")
    fun testShortDeserializeLocalUriWithTooManyParts() {
        val uri: UUri = ShortUriSerializer.INSTANCE.deserialize("/1/1/1/1")
        assertEquals(uri, UUri.getDefaultInstance())
    }

    @Test
    @DisplayName("Test short deserialize of a local URI that only has 2 parts")
    fun testShortDeserializeLocalUriWithOnlyTwoParts() {
        val uri: UUri = ShortUriSerializer.INSTANCE.deserialize("/1/1")
        assertTrue(uri.hasEntity())
        assertEquals(uri.entity.id, 1)
        assertEquals(uri.entity.versionMajor, 1)
    }

    @Test
    @DisplayName("Test short deserialize of a local URI that has 2 parts")
    fun testShortDeserializeLocalUriWithThreeParts() {
        val uri: UUri = ShortUriSerializer.INSTANCE.deserialize("/1")
        assertTrue(uri.hasEntity())
        assertEquals(uri.entity.id, 1)
        assertFalse(uri.hasResource())
    }

    @Test
    @DisplayName("Test short deserialize with a blank authority")
    fun testShortDeserializeWithBlankAuthority() {
        val uri: UUri = ShortUriSerializer.INSTANCE.deserialize("///1/1/1")
        assertEquals(uri, UUri.getDefaultInstance())
    }

    @Test
    @DisplayName("Test short deserialize with a remote authority that is an IP address and too many parts in the uri")
    fun testShortDeserializeWithRemoteAuthorityIpAndTooManyParts() {
        val uri: UUri = ShortUriSerializer.INSTANCE.deserialize("//192.168.1.100/1/1/1/1")
        assertEquals(uri, UUri.getDefaultInstance())
    }

    @Test
    @DisplayName("Test short deserialize with a remote authority that is an IP address and right number of parts")
    fun testShortDeserializeWithRemoteAuthorityIpAndRightNumberOfParts() {
        val uri: UUri = ShortUriSerializer.INSTANCE.deserialize("//192.168.1.100/1/1/1")
        assertTrue(uri.hasAuthority())
        assertTrue(uri.authority.hasIp())
        assertTrue(uri.authority.ip == ByteString.copyFrom(toBytes("192.168.1.100")))
        assertTrue(uri.hasEntity())
        assertEquals(uri.entity.id, 1)
        assertEquals(uri.entity.versionMajor, 1)
        assertTrue(uri.hasResource())
        assertEquals(uri.resource.id, 1)
    }


    @Test
    @DisplayName("Test short deserialize with a remote authority that is an IP address but missing resource")
    fun testShortDeserializeWithRemoteAuthorityIpAddressMissingResource() {
        val uri: UUri = ShortUriSerializer.INSTANCE.deserialize("//192.168.1.100/1/1")
        assertTrue(uri.hasAuthority())
        assertTrue(uri.authority.hasIp())
        assertTrue(uri.authority.ip == ByteString.copyFrom(toBytes("192.168.1.100")))
        assertTrue(uri.hasEntity())
        assertEquals(uri.entity.id, 1)
        assertEquals(uri.entity.versionMajor, 1)
        assertFalse(uri.hasResource())
    }

    @Test
    @DisplayName("Test short deserialize with a remote authority that is an IP address but missing resource and major version")
    fun testShortDeserializeWithRemoteAuthorityIpAddressMissingResourceAndVersionMajor() {
        val uri: UUri = ShortUriSerializer.INSTANCE.deserialize("//192.168.1.100/1")
        assertTrue(uri.hasAuthority())
        assertTrue(uri.authority.hasIp())
        assertTrue(uri.authority.ip == ByteString.copyFrom(toBytes("192.168.1.100")))
        assertTrue(uri.hasEntity())
        assertEquals(uri.entity.id, 1)
        assertFalse(uri.entity.hasVersionMajor())
    }

    @Test
    @DisplayName("Test short deserialize with a remote authority that is an IP address but missing resource and major version")
    fun testShortDeserializeWithRemoteAuthorityIpAddressMissingResourceAndVersionMajorAndUeId() {
        val uri: UUri = ShortUriSerializer.INSTANCE.deserialize("//192.168.1.100//")
        assertTrue(uri.hasAuthority())
        assertTrue(uri.authority.hasIp())
        assertTrue(uri.authority.ip == ByteString.copyFrom(toBytes("192.168.1.100")))
        assertFalse(uri.hasEntity())
    }

    @Test
    @DisplayName("Test short deserialize with a remote authority and blank ueversion and ueid")
    fun testShortDeserializeWithRemoteAuthorityAndBlankUeVersionAndUeId() {
        val uri: UUri = ShortUriSerializer.INSTANCE.deserialize("//mypc//1/")
        assertTrue(uri.hasAuthority())
        assertTrue(uri.authority.hasId())
        assertEquals(uri.authority.id, ByteString.copyFromUtf8("mypc"))
        assertTrue(uri.hasEntity())
    }

    @Test
    @DisplayName("Test short deserialize with a remote authority and missing the other parts")
    fun testShortDeserializeWithRemoteAuthorityAndMissingParts() {
        val uri: UUri = ShortUriSerializer.INSTANCE.deserialize("//mypc")
        assertTrue(uri.hasAuthority())
        assertTrue(uri.authority.hasId())
        assertEquals(uri.authority.id, ByteString.copyFromUtf8("mypc"))
    }

    @Test
    @DisplayName("Test short deserialize with a remote authority and invalid characters for entity id and version")
    fun testShortDeserializeWithRemoteAuthorityAndInvalidEntityIdAndVersion() {
        val uri: UUri = ShortUriSerializer.INSTANCE.deserialize("//mypc/abc/def")
        assertEquals(uri, UUri.getDefaultInstance())
    }

    @Test
    @DisplayName("Test short deserialize with a remote authority and invalid characters for resource id")
    fun testShortDeserializeWithRemoteAuthorityAndInvalidResourceId() {
        val uri: UUri = ShortUriSerializer.INSTANCE.deserialize("//mypc/1/1/abc")
        assertEquals(uri.resource, UResource.getDefaultInstance())
    }
}