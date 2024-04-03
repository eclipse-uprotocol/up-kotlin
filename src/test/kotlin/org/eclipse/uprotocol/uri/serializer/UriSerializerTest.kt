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
import org.eclipse.uprotocol.UprotocolOptions
import org.eclipse.uprotocol.core.usubscription.v3.USubscriptionProto
import org.eclipse.uprotocol.uri.factory.UEntityFactory.fromProto
import org.eclipse.uprotocol.uri.validator.isEmpty
import org.eclipse.uprotocol.uri.validator.isLongForm
import org.eclipse.uprotocol.uri.validator.isMicroForm
import org.eclipse.uprotocol.uri.validator.isShortForm
import org.eclipse.uprotocol.v1.*
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import java.net.InetAddress


class UriSerializerTest {
    @Test
    @DisplayName("Test building uSubscription Update Notification topic and comparing long, short, and micro URIs")
    fun test_build_resolved_full_information_compare() {
        val descriptor = USubscriptionProto.getDescriptor().services[0]

        val entity = fromProto(descriptor)

        val options = descriptor.options

        val resource = options.getExtension(UprotocolOptions.notificationTopic).filter { topic ->
            topic.name.contains("SubscriptionChange")
        }.map { topic ->
            uResource {
                from(topic)
            }
        }.first()

        val uUri = uUri {
            this.entity = entity
            this.resource = resource
        }

        assertFalse(uUri.isEmpty())
        assertTrue(uUri.isMicroForm())
        assertTrue(uUri.isLongForm())
        assertTrue(uUri.isShortForm())
        val longUri: String = LongUriSerializer.INSTANCE.serialize(uUri)
        val microUri: ByteArray = MicroUriSerializer.INSTANCE.serialize(uUri)
        val shortUri: String = ShortUriSerializer.INSTANCE.serialize(uUri)

        assertEquals(longUri, "/core.usubscription/3/SubscriptionChange#Update")
        assertEquals(shortUri, "/0/3/32768")
        assertEquals(microUri.contentToString(), "[1, 0, -128, 0, 0, 0, 3, 0]")
    }


    @Test
    @DisplayName("Test building uSubscription Update Notification topic with IPv4 address UAuthority and comparing long, short, and micro URIs")
    fun test_build_resolved_full_information_compare_with_ipv4() {
        val descriptor = USubscriptionProto.getDescriptor().services[0]
        val entity = fromProto(descriptor)
        val options = descriptor.options
        val resource = options.getExtension(UprotocolOptions.notificationTopic).filter { topic ->
            topic.name.contains("SubscriptionChange")
        }.map { topic ->
            uResource {
                from(topic)
            }
        }.first()

        val uUri = uUri {
            this.entity = entity
            this.resource = resource
            authority = uAuthority {
                name = "vcu.veh.gm.com"
                ip = ByteString.copyFrom(InetAddress.getByName("192.168.1.100").address)
            }
        }

        assertFalse(uUri.isEmpty())
        assertTrue(uUri.isMicroForm())
        assertTrue(uUri.isLongForm())
        assertTrue(uUri.isShortForm())
        val longUri: String = LongUriSerializer.INSTANCE.serialize(uUri)
        val microUri: ByteArray = MicroUriSerializer.INSTANCE.serialize(uUri)
        val shortUri: String = ShortUriSerializer.INSTANCE.serialize(uUri)

        assertEquals(longUri, "//vcu.veh.gm.com/core.usubscription/3/SubscriptionChange#Update")
        assertEquals(shortUri, "//192.168.1.100/0/3/32768")
        assertEquals(microUri.contentToString(), "[1, 1, -128, 0, 0, 0, 3, 0, -64, -88, 1, 100]")
    }

    @Test
    @DisplayName("Test building uSubscription Update Notification topic with IPv6 address UAuthority and comparing long, short, and micro URIs")
    fun test_build_resolved_full_information_compare_with_ipv6() {
        val descriptor = USubscriptionProto.getDescriptor().services[0]
        val entity = fromProto(descriptor)
        val options = descriptor.options
        val resource = options.getExtension(UprotocolOptions.notificationTopic).filter { topic ->
            topic.name.contains("SubscriptionChange")
        }.map { topic ->
            uResource {
                from(topic)
            }
        }.first()

        val uUri = uUri {
            this.entity = entity
            this.resource = resource
            authority = uAuthority {
                name = "vcu.veh.gm.com"
                ip = ByteString.copyFrom(InetAddress.getByName("2001:db8:85a3:0:0:8a2e:370:7334").address)
            }
        }

        assertFalse(uUri.isEmpty())
        assertTrue(uUri.isMicroForm())
        assertTrue(uUri.isLongForm())
        assertTrue(uUri.isShortForm())
        val longUri: String = LongUriSerializer.INSTANCE.serialize(uUri)
        val microUri: ByteArray = MicroUriSerializer.INSTANCE.serialize(uUri)
        val shortUri: String = ShortUriSerializer.INSTANCE.serialize(uUri)

        assertEquals(longUri, "//vcu.veh.gm.com/core.usubscription/3/SubscriptionChange#Update")
        assertEquals(shortUri, "//2001:db8:85a3:0:0:8a2e:370:7334/0/3/32768")
        assertEquals(
            microUri.contentToString(),
            "[1, 2, -128, 0, 0, 0, 3, 0, 32, 1, 13, -72, -123, -93, 0, 0, 0, 0, -118, 46, 3, 112, 115, 52]"
        )
    }

    @Test
    @DisplayName("Test building uSubscription Update Notification topic with id address UAuthority and comparing long, short, and micro URIs")
    fun test_build_resolved_full_information_compare_with_id() {
        val descriptor = USubscriptionProto.getDescriptor().services[0]
        val entity = fromProto(descriptor)
        val options = descriptor.options

        val resource = options.getExtension(UprotocolOptions.notificationTopic).filter { topic ->
            topic.name.contains("SubscriptionChange")
        }.map { topic ->
            uResource {
                from(topic)
            }
        }.first()

        val uUri = uUri {
            this.entity = entity
            this.resource = resource
            authority = uAuthority {
                name = "1G1YZ23J9P5800001.veh.gm.com"
                id = ByteString.copyFromUtf8("1G1YZ23J9P5800001")
            }
        }

        assertFalse(uUri.isEmpty())
        assertTrue(uUri.isMicroForm())
        assertTrue(uUri.isLongForm())
        assertTrue(uUri.isShortForm())
        val longUri: String = LongUriSerializer.INSTANCE.serialize(uUri)
        val microUri: ByteArray = MicroUriSerializer.INSTANCE.serialize(uUri)
        val shortUri: String = ShortUriSerializer.INSTANCE.serialize(uUri)

        assertEquals(longUri, "//1G1YZ23J9P5800001.veh.gm.com/core.usubscription/3/SubscriptionChange#Update")
        assertEquals(shortUri, "//1G1YZ23J9P5800001/0/3/32768")
        assertEquals(
            microUri.contentToString(),
            "[1, 3, -128, 0, 0, 0, 3, 0, 17, 49, 71, 49, 89, 90, 50, 51, 74, 57, 80, 53, 56, 48, 48, 48, 48, 49]"
        )
    }
}
