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
package org.eclipse.uprotocol.cloudevent.factory

import com.google.protobuf.Any
import io.cloudevents.CloudEvent
import io.cloudevents.core.builder.CloudEventBuilder
import org.eclipse.uprotocol.cloudevent.datamodel.UCloudEventAttributes
import org.eclipse.uprotocol.uri.serializer.LongUriSerializer
import org.eclipse.uprotocol.v1.*
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import java.net.URI
import java.util.*

internal class CloudEventFactoryTest {
    @Test
    @DisplayName("Test create base CloudEvent")
    fun test_create_base_cloud_event() {
        val source = buildUriForTest()

        // fake payload
        val protoPayload = buildProtoPayloadForTest()

        // additional attributes
        val uCloudEventAttributes: UCloudEventAttributes =
            UCloudEventAttributes.UCloudEventAttributesBuilder().withHash("somehash")
                .withPriority(UPriority.UPRIORITY_CS1).withTtl(3).withToken("someOAuthToken").build()

        // build the cloud event
        val cloudEventBuilder: CloudEventBuilder = CloudEventFactory.buildBaseCloudEvent(
            "testme", source, protoPayload.toByteArray(), protoPayload.typeUrl, uCloudEventAttributes
        )
        cloudEventBuilder.withType(UCloudEvent.getEventType(UMessageType.UMESSAGE_TYPE_PUBLISH))
        val cloudEvent: CloudEvent = cloudEventBuilder.build()
        assertEquals("1.0", cloudEvent.specVersion.toString())
        assertEquals("testme", cloudEvent.id)
        assertEquals(source, cloudEvent.source.toString())
        assertEquals(UCloudEvent.getEventType(UMessageType.UMESSAGE_TYPE_PUBLISH), cloudEvent.type)
        assertFalse(cloudEvent.extensionNames.contains("sink"))
        assertEquals("somehash", cloudEvent.getExtension("hash"))
        assertEquals(UPriority.UPRIORITY_CS1.name, cloudEvent.getExtension("priority"))
        assertEquals(3, cloudEvent.getExtension("ttl"))
        assertEquals("someOAuthToken", cloudEvent.getExtension("token"))
        assertArrayEquals(protoPayload.toByteArray(), Objects.requireNonNull(cloudEvent.data).toBytes())
    }

    @Test
    @DisplayName("Test create base CloudEvent with datacontenttype and dataschema")
    fun test_create_base_cloud_event_with_datacontenttype_and_schema() {
        val source = buildUriForTest()

        // fake payload
        val protoPayload = buildProtoPayloadForTest()

        // additional attributes
        val uCloudEventAttributes: UCloudEventAttributes =
            UCloudEventAttributes.UCloudEventAttributesBuilder().withHash("somehash")
                .withPriority(UPriority.UPRIORITY_CS1).withTtl(3).withToken("someOAuthToken").build()

        // build the cloud event
        val cloudEventBuilder: CloudEventBuilder = CloudEventFactory.buildBaseCloudEvent(
            "testme", source, protoPayload.toByteArray(), protoPayload.typeUrl, uCloudEventAttributes
        )
        cloudEventBuilder.withType(UCloudEvent.getEventType(UMessageType.UMESSAGE_TYPE_PUBLISH))
            .withDataContentType(DATA_CONTENT_TYPE).withDataSchema(URI.create(protoPayload.typeUrl))
        val cloudEvent: CloudEvent = cloudEventBuilder.build()

        // test all attributes
        assertEquals("1.0", cloudEvent.specVersion.toString())
        assertEquals("testme", cloudEvent.id)
        assertEquals(source, cloudEvent.source.toString())
        assertEquals(UCloudEvent.getEventType(UMessageType.UMESSAGE_TYPE_PUBLISH), cloudEvent.type)
        assertEquals(DATA_CONTENT_TYPE, cloudEvent.dataContentType)
        assertEquals(
            "type.googleapis.com/io.cloudevents.v1.CloudEvent", Objects.requireNonNull(cloudEvent.dataSchema).toString()
        )
        assertFalse(cloudEvent.extensionNames.contains("sink"))
        assertEquals("somehash", cloudEvent.getExtension("hash"))
        assertEquals(UPriority.UPRIORITY_CS1.name, cloudEvent.getExtension("priority"))
        assertEquals(3, cloudEvent.getExtension("ttl"))
        assertEquals("someOAuthToken", cloudEvent.getExtension("token"))
        assertArrayEquals(protoPayload.toByteArray(), Objects.requireNonNull(cloudEvent.data).toBytes())
    }

    @Test
    @DisplayName("Test create base CloudEvent without attributes")
    fun test_create_base_cloud_event_without_attributes() {
        val source = buildUriForTest()

        // fake payload
        val protoPayload = buildProtoPayloadForTest()

        // no additional attributes
        val uCloudEventAttributes: UCloudEventAttributes = UCloudEventAttributes.empty()

        // build the cloud event
        val cloudEventBuilder: CloudEventBuilder = CloudEventFactory.buildBaseCloudEvent(
            "testme", source, protoPayload.toByteArray(), protoPayload.typeUrl, uCloudEventAttributes
        )
        cloudEventBuilder.withType(UCloudEvent.getEventType(UMessageType.UMESSAGE_TYPE_PUBLISH))
        val cloudEvent: CloudEvent = cloudEventBuilder.build()
        assertEquals("1.0", cloudEvent.specVersion.toString())
        assertEquals("testme", cloudEvent.id)
        assertEquals(source, cloudEvent.source.toString())
        assertEquals(UCloudEvent.getEventType(UMessageType.UMESSAGE_TYPE_PUBLISH), cloudEvent.type)
        assertFalse(cloudEvent.extensionNames.contains("sink"))
        assertFalse(cloudEvent.extensionNames.contains("hash"))
        assertFalse(cloudEvent.extensionNames.contains("priority"))
        assertFalse(cloudEvent.extensionNames.contains("ttl"))
        assertArrayEquals(protoPayload.toByteArray(), Objects.requireNonNull(cloudEvent.data).toBytes())
    }

    @Test
    @DisplayName("Test create publish CloudEvent")
    fun test_create_publish_cloud_event() {

        // source
        val source = buildUriForTest()

        // fake payload
        val protoPayload = buildProtoPayloadForTest()

        // additional attributes
        val uCloudEventAttributes: UCloudEventAttributes =
            UCloudEventAttributes.UCloudEventAttributesBuilder().withHash("somehash")
                .withPriority(UPriority.UPRIORITY_CS1).withTtl(3).build()
        val cloudEvent: CloudEvent = CloudEventFactory.publish(source, protoPayload, uCloudEventAttributes)
        assertEquals("1.0", cloudEvent.specVersion.toString())
        assertNotNull(cloudEvent.id)
        assertEquals(source, cloudEvent.source.toString())
        assertEquals(UCloudEvent.getEventType(UMessageType.UMESSAGE_TYPE_PUBLISH), cloudEvent.type)
        assertFalse(cloudEvent.extensionNames.contains("sink"))
        assertEquals("somehash", cloudEvent.getExtension("hash"))
        assertEquals(UPriority.UPRIORITY_CS1.name, cloudEvent.getExtension("priority"))
        assertEquals(3, cloudEvent.getExtension("ttl"))
        assertArrayEquals(protoPayload.toByteArray(), Objects.requireNonNull(cloudEvent.data).toBytes())
    }

    @Test
    @DisplayName("Test create notification CloudEvent")
    fun test_create_notification_cloud_event() {

        // source
        val source = buildUriForTest()

        // sink
        val sink = buildUriForTest()

        // fake payload
        val protoPayload = buildProtoPayloadForTest()

        // additional attributes
        val uCloudEventAttributes: UCloudEventAttributes =
            UCloudEventAttributes.UCloudEventAttributesBuilder().withHash("somehash")
                .withPriority(UPriority.UPRIORITY_CS2).withTtl(3).build()

        // build the cloud event of type publish with destination - a notification
        val cloudEvent: CloudEvent = CloudEventFactory.notification(source, sink, protoPayload, uCloudEventAttributes)
        assertEquals("1.0", cloudEvent.specVersion.toString())
        assertNotNull(cloudEvent.id)
        assertEquals(source, cloudEvent.source.toString())
        assertTrue(cloudEvent.extensionNames.contains("sink"))
        assertEquals(sink, Objects.requireNonNull(cloudEvent.getExtension("sink")).toString())
        assertEquals(UCloudEvent.getEventType(UMessageType.UMESSAGE_TYPE_PUBLISH), cloudEvent.type)
        assertEquals("somehash", cloudEvent.getExtension("hash"))
        assertEquals(UPriority.UPRIORITY_CS2.name, cloudEvent.getExtension("priority"))
        assertEquals(3, cloudEvent.getExtension("ttl"))
        assertArrayEquals(protoPayload.toByteArray(), Objects.requireNonNull(cloudEvent.data).toBytes())
    }

    @Test
    @DisplayName("Test create request RPC CloudEvent coming from a local USE")
    fun test_create_request_cloud_event_from_local_use() {

        // UriPart for the application requesting the RPC
        val applicationUriForRPC = buildUriForTest()

        // service Method UriPart
        val serviceMethodUri = buildUriForTest()

        // fake payload
        val protoPayload = buildProtoPayloadForTest()

        // additional attributes
        val uCloudEventAttributes: UCloudEventAttributes =
            UCloudEventAttributes.UCloudEventAttributesBuilder().withHash("somehash")
                .withPriority(UPriority.UPRIORITY_CS2).withTtl(3).withToken("someOAuthToken").build()
        val cloudEvent: CloudEvent = CloudEventFactory.request(
            applicationUriForRPC, serviceMethodUri, protoPayload, uCloudEventAttributes
        )
        assertEquals("1.0", cloudEvent.specVersion.toString())
        assertNotNull(cloudEvent.id)
        assertEquals(applicationUriForRPC, cloudEvent.source.toString())
        assertTrue(cloudEvent.extensionNames.contains("sink"))
        assertEquals(serviceMethodUri, Objects.requireNonNull(cloudEvent.getExtension("sink")).toString())
        assertEquals("req.v1", cloudEvent.type)
        assertEquals("somehash", cloudEvent.getExtension("hash"))
        assertEquals(UPriority.UPRIORITY_CS2.name, cloudEvent.getExtension("priority"))
        assertEquals(3, cloudEvent.getExtension("ttl"))
        assertEquals("someOAuthToken", cloudEvent.getExtension("token"))
        assertArrayEquals(protoPayload.toByteArray(), Objects.requireNonNull(cloudEvent.data).toBytes())
    }

    @Test
    @DisplayName("Test create response RPC CloudEvent originating from a local USE")
    fun test_create_response_cloud_event_originating_from_local_use() {

        // UriPart for the application requesting the RPC
        val applicationUriForRPC = buildUriForTest()

        // service Method UriPart
        val serviceMethodUri = buildUriForTest()

        // fake payload
        val protoPayload = buildProtoPayloadForTest()

        // additional attributes
        val uCloudEventAttributes: UCloudEventAttributes =
            UCloudEventAttributes.UCloudEventAttributesBuilder().withHash("somehash")
                .withPriority(UPriority.UPRIORITY_CS2).withTtl(3).build()
        val cloudEvent: CloudEvent = CloudEventFactory.response(
            applicationUriForRPC,
            serviceMethodUri,
            "requestIdFromRequestCloudEvent",
            protoPayload,
            uCloudEventAttributes
        )
        assertEquals("1.0", cloudEvent.specVersion.toString())
        assertNotNull(cloudEvent.id)
        assertEquals(serviceMethodUri, cloudEvent.source.toString())
        assertTrue(cloudEvent.extensionNames.contains("sink"))
        assertEquals(applicationUriForRPC, Objects.requireNonNull(cloudEvent.getExtension("sink")).toString())
        assertEquals("res.v1", cloudEvent.type)
        assertEquals("somehash", cloudEvent.getExtension("hash"))
        assertEquals(UPriority.UPRIORITY_CS2.name, cloudEvent.getExtension("priority"))
        assertEquals(3, cloudEvent.getExtension("ttl"))
        assertEquals("requestIdFromRequestCloudEvent", cloudEvent.getExtension("reqid"))
        assertArrayEquals(protoPayload.toByteArray(), Objects.requireNonNull(cloudEvent.data).toBytes())
    }

    @Test
    @DisplayName("Test create a failed response RPC CloudEvent originating from a local USE")
    fun test_create_a_failed_response_cloud_event_originating_from_local_use() {

        // UriPart for the application requesting the RPC
        val applicationUriForRPC = buildUriForTest()

        // service Method UriPart
        val serviceMethodUri = buildUriForTest()

        // additional attributes
        val uCloudEventAttributes: UCloudEventAttributes =
            UCloudEventAttributes.UCloudEventAttributesBuilder().withHash("somehash")
                .withPriority(UPriority.UPRIORITY_CS2).withTtl(3).build()
        val cloudEvent: CloudEvent = CloudEventFactory.failedResponse(
            applicationUriForRPC,
            serviceMethodUri,
            "requestIdFromRequestCloudEvent",
            UCode.INVALID_ARGUMENT_VALUE,
            uCloudEventAttributes
        )
        assertEquals("1.0", cloudEvent.specVersion.toString())
        assertNotNull(cloudEvent.id)
        assertEquals(serviceMethodUri, cloudEvent.source.toString())
        assertTrue(cloudEvent.extensionNames.contains("sink"))
        assertEquals(applicationUriForRPC, Objects.requireNonNull(cloudEvent.getExtension("sink")).toString())
        assertEquals("res.v1", cloudEvent.type)
        assertEquals("somehash", cloudEvent.getExtension("hash"))
        assertEquals(UPriority.UPRIORITY_CS2.name, cloudEvent.getExtension("priority"))
        assertEquals(3, cloudEvent.getExtension("ttl"))
        assertEquals(UCode.INVALID_ARGUMENT_VALUE, cloudEvent.getExtension("commstatus"))
        assertEquals("requestIdFromRequestCloudEvent", cloudEvent.getExtension("reqid"))
    }

    @Test
    @DisplayName("Test create a failed response RPC CloudEvent originating from a microRemote USE")
    fun test_create_a_failed_response_cloud_event_originating_from_remote_use() {

        // UriPart for the application requesting the RPC
        val applicationUriForRPC = buildUriForTest()

        // service Method UriPart
        val serviceMethodUri = buildUriForTest()


        // additional attributes
        val uCloudEventAttributes: UCloudEventAttributes =
            UCloudEventAttributes.UCloudEventAttributesBuilder().withHash("somehash")
                .withPriority(UPriority.UPRIORITY_CS2).withTtl(3).build()
        val cloudEvent: CloudEvent = CloudEventFactory.failedResponse(
            applicationUriForRPC,
            serviceMethodUri,
            "requestIdFromRequestCloudEvent",
            UCode.INVALID_ARGUMENT_VALUE,
            uCloudEventAttributes
        )
        assertEquals("1.0", cloudEvent.specVersion.toString())
        assertNotNull(cloudEvent.id)
        assertEquals(serviceMethodUri, cloudEvent.source.toString())
        assertTrue(cloudEvent.extensionNames.contains("sink"))
        assertEquals(applicationUriForRPC, Objects.requireNonNull(cloudEvent.getExtension("sink")).toString())
        assertEquals("res.v1", cloudEvent.type)
        assertEquals("somehash", cloudEvent.getExtension("hash"))
        assertEquals(UPriority.UPRIORITY_CS2.name, cloudEvent.getExtension("priority"))
        assertEquals(3, cloudEvent.getExtension("ttl"))
        assertEquals(UCode.INVALID_ARGUMENT_VALUE, cloudEvent.getExtension("commstatus"))
        assertEquals("requestIdFromRequestCloudEvent", cloudEvent.getExtension("reqid"))
    }

    private fun buildUriForTest(): String {
        val uri: UUri = uUri {
            entity = uEntity { name = "body.access" }
            resource = uResource {
                name = "door"
                instance = "front_left"
                message = "Door"
            }
        }

        return LongUriSerializer.instance().serialize(uri)
    }

    private fun buildProtoPayloadForTest(): Any {
        val cloudEventProto: io.cloudevents.v1.proto.CloudEvent =
            io.cloudevents.v1.proto.CloudEvent.newBuilder().setSpecVersion("1.0").setId("hello")
                .setSource("https://example.com").setType("example.demo").setProtoData(Any.newBuilder().build()).build()
        return Any.pack(cloudEventProto)
    }

    companion object {
        private const val DATA_CONTENT_TYPE: String = CloudEventFactory.PROTOBUF_CONTENT_TYPE
    }
}