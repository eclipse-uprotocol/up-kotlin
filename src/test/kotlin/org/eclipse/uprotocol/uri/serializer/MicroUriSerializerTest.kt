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

import com.google.protobuf.ByteString
import org.eclipse.uprotocol.uri.serializer.IpAddress.toBytes
import org.eclipse.uprotocol.uri.validator.isEmpty
import org.eclipse.uprotocol.uri.validator.isMicroForm
import org.eclipse.uprotocol.v1.*
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import java.net.InetAddress
import java.net.UnknownHostException


class MicroUriSerializerTest {
    @Test
    @DisplayName("Test serialize and deserialize empty content")
    fun test_empty() {
        val bytes: ByteArray = MicroUriSerializer.INSTANCE.serialize(UUri.getDefaultInstance())
        assertEquals(bytes.size, 0)
        val uri2: UUri = MicroUriSerializer.INSTANCE.deserialize(bytes)
        assertTrue(uri2.isEmpty())
    }

    @Test
    @DisplayName("Test happy path Byte serialization of local UUri")
    fun test_serialize_uri() {
        val uri: UUri = uUri {
            entity = uEntity {
                id = 29999
                versionMajor = 254
            }
            resource = uResource { from(19999) }
        }

        val bytes: ByteArray = MicroUriSerializer.INSTANCE.serialize(uri)
        val uri2: UUri = MicroUriSerializer.INSTANCE.deserialize(bytes)
        assertTrue(uri.isMicroForm())
        assertTrue(bytes.isNotEmpty())
        assertEquals(uri, uri2)
    }

    @Test
    @DisplayName("Test Serialize a remote UUri to micro without the address")
    fun test_serialize_remote_uri_without_address() {
        val uri: UUri = uUri {
            authority = uAuthority { name = "vcu.vin" }
            entity = uEntity {
                id = 29999
                versionMajor = 254
            }
            resource = uResource { id = 19999 }
        }

        val bytes: ByteArray = MicroUriSerializer.INSTANCE.serialize(uri)
        assertTrue(bytes.isEmpty())
    }

    @Test
    @DisplayName("Test serialize Uri missing uE ID")
    fun test_serialize_uri_missing_ids() {
        val uri: UUri = uUri {
            entity = uEntity { name = "hartley" }
            resource = uResource { forRpcResponse() }
        }


        val bytes: ByteArray = MicroUriSerializer.INSTANCE.serialize(uri)
        assertTrue(bytes.isEmpty())
    }

    @Test
    @DisplayName("Test serialize Uri missing resource")
    fun test_serialize_uri_missing_resource_id() {
        val uri: UUri = uUri { entity = uEntity { name = "hartley" } }

        val bytes: ByteArray = MicroUriSerializer.INSTANCE.serialize(uri)
        assertTrue(bytes.isEmpty())
    }

    @Test
    @DisplayName("Test deserialize bad micro uri - length")
    fun test_deserialize_bad_microuri_length() {
        var badMicroUUri = byteArrayOf(0x1, 0x0, 0x0, 0x0, 0x0)
        var uuri: UUri = MicroUriSerializer.INSTANCE.deserialize(badMicroUUri)
        assertTrue(uuri.isEmpty())
        badMicroUUri = byteArrayOf(0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0)
        uuri = MicroUriSerializer.INSTANCE.deserialize(badMicroUUri)
        assertTrue(uuri.isEmpty())
    }

    @Test
    @DisplayName("Test deserialize bad micro uri - not version 1")
    fun test_deserialize_bad_microuri_not_version_1() {
        val badMicroUUri = byteArrayOf(0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0)
        val uuri: UUri = MicroUriSerializer.INSTANCE.deserialize(badMicroUUri)
        assertTrue(uuri.isEmpty())
    }

    @Test
    @DisplayName("Test deserialize bad micro uri - not valid address type")
    fun test_deserialize_bad_microuri_not_valid_address_type() {
        val badMicroUUri = byteArrayOf(0x1, 0x5, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0)
        val uuri: UUri = MicroUriSerializer.INSTANCE.deserialize(badMicroUUri)
        assertTrue(uuri.isEmpty())
    }

    @Test
    @DisplayName("Test deserialize bad micro uri - valid address type and invalid length")
    fun test_deserialize_bad_microuri_valid_address_type_invalid_length() {
        var badMicroUUri = byteArrayOf(0x1, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0)
        var uuri: UUri = MicroUriSerializer.INSTANCE.deserialize(badMicroUUri)
        assertTrue(uuri.isEmpty())
        badMicroUUri = byteArrayOf(0x1, 0x1, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0)
        uuri = MicroUriSerializer.INSTANCE.deserialize(badMicroUUri)
        assertTrue(uuri.isEmpty())
        badMicroUUri = byteArrayOf(0x1, 0x2, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0)
        uuri = MicroUriSerializer.INSTANCE.deserialize(badMicroUUri)
        assertTrue(uuri.isEmpty())
    }

    @Test
    @DisplayName("Test serialize with good IPv4 based authority")
    @Throws(UnknownHostException::class)
    fun test_serialize_good_ipv4_based_authority() {
        val uri: UUri = uUri {
            authority = uAuthority { ip = ByteString.copyFrom(InetAddress.getByName("10.0.3.3").address) }
            entity = uEntity {
                id = 29999
                versionMajor = 254
            }
            resource = uResource { forRpcRequest( id = 99) }
        }


        val bytes: ByteArray = MicroUriSerializer.INSTANCE.serialize(uri)
        val uri2: UUri = MicroUriSerializer.INSTANCE.deserialize(bytes)
        assertTrue(bytes.isNotEmpty())
        assertTrue(uri.isMicroForm())
        assertTrue(uri2.isMicroForm())
        assertEquals(uri.toString(), uri2.toString())
        assertTrue(uri == uri2)
    }

    @Test
    @DisplayName("Test serialize with good IPv4 based authority and the uEntity major version is missing")
    fun test_serialize_good_ipv4_based_authority_missing_major_version() {
        val uri: UUri = uUri {
            authority = uAuthority { ip = ByteString.copyFrom(toBytes("192.168.1.100")) }
            entity = uEntity { id = 29999 }
            resource = uResource { forRpcResponse() }
        }
        val bytes: ByteArray = MicroUriSerializer.INSTANCE.serialize(uri)
        val uri2: UUri = MicroUriSerializer.INSTANCE.deserialize(bytes)
        assertEquals(uri2.entity.versionMajor, 0)
    }

    @Test
    @DisplayName("Test serialize without uauthority ip or id")
    fun test_serialize_without_uauthority_ip_or_id() {
        val uri: UUri = uUri {
            authority = uAuthority { name = "vcu.vin" }
            entity = uEntity {
                id = 29999
                versionMajor = 254
            }
            resource = uResource { id = 19999 }
        }
        val bytes: ByteArray = MicroUriSerializer.INSTANCE.serialize(uri)
        assertTrue(bytes.isEmpty())
    }

    @Test
    @DisplayName("Test serialize with id that is out of range")
    fun test_serialize_id_out_of_range() {
        val byteArray = ByteArray(258)
        for (i in 0..255) {
            byteArray[i] = i.toByte()
        }
        val uri: UUri = uUri {
            authority = uAuthority { ip = ByteString.copyFrom(byteArray) }
            entity = uEntity {
                id = 29999
                versionMajor = 254
            }
            resource = uResource { from(19999) }
        }
        val bytes: ByteArray = MicroUriSerializer.INSTANCE.serialize(uri)
        assertTrue(bytes.isEmpty())
    }

    @Test
    @DisplayName("Test serialize with good IPv6 based authority")
    @Throws(UnknownHostException::class)
    fun test_serialize_good_ipv6_based_authority() {
        val uri: UUri = uUri {
            authority = uAuthority {
                ip = ByteString.copyFrom(
                    InetAddress.getByName("2001:0db8:85a3:0000:0000:8a2e:0370:7334").address
                )
            }
            entity = uEntity {
                id = 29999
                versionMajor = 254
            }

            resource = uResource { from(19999) }
        }

        val bytes: ByteArray = MicroUriSerializer.INSTANCE.serialize(uri)
        val uri2: UUri = MicroUriSerializer.INSTANCE.deserialize(bytes)
        assertTrue(uri.isMicroForm())
        assertTrue(bytes.isNotEmpty())
        assertTrue(uri == uri2)
    }

    @Test
    @DisplayName("Test serialize with ID based authority")
    fun test_serialize_id_based_authority() {
        val size = 13
        val byteArray = ByteArray(size)
        // Assign values to the elements of the byte array
        for (i in 0 until size) {
            byteArray[i] = i.toByte()
        }
        val uri: UUri = uUri {
            authority = uAuthority { id = ByteString.copyFrom(byteArray) }
            entity = uEntity {
                id = 29999
                versionMajor = 254
            }
            resource = uResource { from(19999) }
        }

        val bytes: ByteArray = MicroUriSerializer.INSTANCE.serialize(uri)
        val uri2: UUri = MicroUriSerializer.INSTANCE.deserialize(bytes)
        assertTrue(uri.isMicroForm())
        assertTrue(bytes.isNotEmpty())
        assertTrue(uri == uri2)
    }

    @Test
    @DisplayName("Test serialize with bad length IP based authority")
    @Throws(UnknownHostException::class)
    fun test_serialize_bad_length_ip_based_authority() {
        val byteArray = byteArrayOf(127, 1, 23, 123, 12, 6)
        val uri: UUri = uUri {
            authority = uAuthority {
                ip = ByteString.copyFrom(byteArray)
            }
            entity = uEntity {
                id = 29999
                versionMajor = 254
            }
            resource = uResource { from(19999) }
        }

        val bytes: ByteArray = MicroUriSerializer.INSTANCE.serialize(uri)
        assertTrue(bytes.isEmpty())
    }

    @Test
    @DisplayName("Test serialize with ID based authority")
    fun test_serialize_id_size_255_based_authority() {
        val size = 129
        val byteArray = ByteArray(size)
        // Assign values to the elements of the byte array
        for (i in 0 until size) {
            byteArray[i] = i.toByte()
        }
        val uri: UUri = uUri {
            authority = uAuthority { id = ByteString.copyFrom(byteArray) }
            entity = uEntity {
                id = 29999
                versionMajor = 254
            }
            resource = uResource { from(19999) }
        }

        val bytes: ByteArray = MicroUriSerializer.INSTANCE.serialize(uri)
        assertEquals(bytes.size, 9 + size)
        val uri2: UUri = MicroUriSerializer.INSTANCE.deserialize(bytes)
        assertTrue(uri.isMicroForm())
        assertTrue(uri == uri2)
    }
}
