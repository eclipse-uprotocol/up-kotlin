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
package org.eclipse.uprotocol.cloudevent.serialize

import com.google.protobuf.Any
import com.google.protobuf.InvalidProtocolBufferException
import com.google.protobuf.Message
import io.cloudevents.CloudEvent
import io.cloudevents.core.builder.CloudEventBuilder
import org.eclipse.uprotocol.cloudevent.datamodel.UCloudEventAttributes
import org.eclipse.uprotocol.cloudevent.factory.CloudEventFactory
import org.eclipse.uprotocol.cloudevent.factory.UCloudEvent
import org.eclipse.uprotocol.uri.Uri
import org.eclipse.uprotocol.v1.UMessageType
import org.eclipse.uprotocol.v1.UPriority
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import java.net.URI
import java.nio.charset.StandardCharsets
import org.junit.jupiter.api.Assertions.*


internal class CloudEventToJsonSerializerTest {
    private val serializer: CloudEventSerializer = CloudEventToJsonSerializer()
    private val protoContentType: String = CloudEventFactory.PROTOBUF_CONTENT_TYPE
    @Test
    @DisplayName("Test serialize a CloudEvent to JSON")
    fun test_serialize_cloud_event_to_json() {
        // fake payload
        val protoPayload = buildProtoPayloadForTest()

        // cloudevent
        val cloudEventBuilder: CloudEventBuilder = CloudEventBuilder.v1()
            .withId("hello")
            .withType("pub.v1")
            .withSource(URI.create("/body.access/1/door.front_left"))
            .withDataContentType(protoContentType)
            .withDataSchema(URI.create(protoPayload.typeUrl))
            .withData(protoPayload.toByteArray())
            .withExtension("ttl", 3)
            .withExtension("priority", "CS1")
        val cloudEvent: CloudEvent = cloudEventBuilder.build()
        val bytes: ByteArray = serializer.serialize(cloudEvent)
        val jsonString = String(bytes, StandardCharsets.UTF_8)
        val expected =
            "{\"specversion\":\"1.0\",\"id\":\"hello\",\"source\":\"/body.access/1/door.front_left\",\"type\":\"pub.v1\"," +
                    "\"datacontenttype\":\"application/x-protobuf\",\"dataschema\":\"type.googleapis.com/io.cloudevents.v1.CloudEvent\"," +
                    "\"priority\":\"CS1\",\"ttl\":3," +
                    "\"data_base64\":\"CjB0eXBlLmdvb2dsZWFwaXMuY29tL2lvLmNsb3VkZXZlbnRzLnYxLkNsb3VkRXZlbnQSPQoFaGVsbG8SE2h0dHBzOi8vZXhhbXBsZS5jb20aAzEuMCIMZXhhbXBsZS5kZW1vKgoKA3R0bBIDGgEzQgA=\"}"
        assertEquals(expected, jsonString)
    }

    @Test
    @DisplayName("Test serialize and deserialize a CloudEvent to JSON")
    fun test_serialize_and_desirialize_cloud_event_to_json() {

        // fake payload
        val protoPayload = buildProtoPayloadForTest()

        // cloudevent
        val cloudEventBuilder: CloudEventBuilder = CloudEventBuilder.v1()
            .withId("hello")
            .withType("pub.v1")
            .withSource(URI.create("/body.access/1/door.front_left"))
            .withDataContentType(protoContentType)
            .withDataSchema(URI.create(protoPayload.typeUrl))
            .withData(protoPayload.toByteArray())
            .withExtension("ttl", 3)
            .withExtension("priority", "CS1")
        val cloudEvent: CloudEvent = cloudEventBuilder.build()
        val bytes: ByteArray = serializer.serialize(cloudEvent)
        val deserialize: CloudEvent = serializer.deserialize(bytes)
        assertEquals(cloudEvent, deserialize)
    }

    @Test
    @DisplayName("Test serialize 2 different cloud events are not the same serialized elements")
    fun test_serialize_two_different_cloud_event_are_not_the_same() {

        // fake payload
        val protoPayload = buildProtoPayloadForTest()

        // cloudevent
        val cloudEventBuilder: CloudEventBuilder = CloudEventBuilder.v1()
            .withId("hello")
            .withType("pub.v1")
            .withSource(URI.create("/body.access/1/door.front_left"))
            .withDataContentType(protoContentType)
            .withDataSchema(URI.create(protoPayload.typeUrl))
            .withData(protoPayload.toByteArray())
            .withExtension("ttl", 3)
            .withExtension("priority", "CS1")
        val cloudEvent: CloudEvent = cloudEventBuilder.build()

        // another cloudevent
        val anotherCloudEvent: CloudEvent = cloudEventBuilder
            .withType("file.v1")
            .build()
        val bytesCloudEvent: ByteArray = serializer.serialize(cloudEvent)
        val bytesAnotherCloudEvent: ByteArray = serializer.serialize(anotherCloudEvent)
        assertNotEquals(bytesCloudEvent, bytesAnotherCloudEvent)
    }

    @Test
    @DisplayName("Test serialize 2 equal cloud events are the same serialized elements")
    fun test_serialize_two_same_cloud_event_are_the_same() {

        // fake payload
        val protoPayload = buildProtoPayloadForTest()

        // cloudevent
        val cloudEventBuilder: CloudEventBuilder = CloudEventBuilder.v1()
            .withId("hello")
            .withType("pub.v1")
            .withSource(URI.create("/body.access/1/door.front_left"))
            .withDataContentType(protoContentType)
            .withDataSchema(URI.create(protoPayload.typeUrl))
            .withData(protoPayload.toByteArray())
            .withExtension("ttl", 3)
            .withExtension("priority", "CS1")
        val cloudEvent: CloudEvent = cloudEventBuilder.build()

        // another cloudevent
        val anotherCloudEvent: CloudEvent = cloudEventBuilder.build()
        val bytesCloudEvent: ByteArray = serializer.serialize(cloudEvent)
        val bytesAnotherCloudEvent: ByteArray = serializer.serialize(anotherCloudEvent)
        assertArrayEquals(bytesCloudEvent, bytesAnotherCloudEvent)
    }

    @Test
    @DisplayName("test double serialization Protobuf when creating CloudEvent with factory methods")
    @Throws(
        InvalidProtocolBufferException::class
    )
    fun test_double_serialization_protobuf_when_creating_cloud_event_with_factory_methods() {
        val serializer: CloudEventSerializer = CloudEventSerializers.JSON.serializer()
        val source = Uri("/body.access//door.front_left#Door")

        // fake payload
        val protoPayload = buildProtoPayloadForTest1()

        // additional attributes
        val uCloudEventAttributes = UCloudEventAttributes.uCloudEventAttributes {
            hash = "somehash"
            priority = UPriority.UPRIORITY_CS1
            ttl = 3
            token = "someOAuthToken"
        }

        // build the cloud event
        val cloudEventBuilder: CloudEventBuilder = CloudEventFactory.buildBaseCloudEvent(
            "testme", source,
            protoPayload.toByteArray(), protoPayload.typeUrl,
            uCloudEventAttributes
        )
        cloudEventBuilder.withType(UCloudEvent.getEventType(UMessageType.UMESSAGE_TYPE_PUBLISH))
        val cloudEvent1: CloudEvent = cloudEventBuilder.build()
        val bytes1: ByteArray = serializer.serialize(cloudEvent1)
        val cloudEvent2: CloudEvent = serializer.deserialize(bytes1)
        assertEquals(cloudEvent2, cloudEvent1)
        val bytes2: ByteArray = serializer.serialize(cloudEvent2)
        assertArrayEquals(bytes1, bytes2)
        val cloudEvent3: CloudEvent = serializer.deserialize(bytes2)
        val cloudEvent3Payload: Any = UCloudEvent.getPayload(cloudEvent3)
        val clazz: Class<out Message?> = io.cloudevents.v1.proto.CloudEvent::class.java
        assertEquals(cloudEvent3Payload.unpack(clazz), protoPayload.unpack(clazz))
        assertEquals(cloudEvent2, cloudEvent3)
        assertEquals(cloudEvent1, cloudEvent3)
    }

    @Test
    @DisplayName("test double serialization Json")
    @Throws(InvalidProtocolBufferException::class)
    fun test_double_serialization_json() {
        val serializer: CloudEventSerializer = CloudEventSerializers.JSON.serializer()
        val builder: CloudEventBuilder = buildCloudEventForTest()
        val cloudEventProto = buildProtoPayloadForTest1()
        builder.withDataContentType(protoContentType)
        builder.withData(cloudEventProto.toByteArray())
        builder.withDataSchema(URI.create(cloudEventProto.typeUrl))
        val cloudEvent1: CloudEvent = builder.build()
        val bytes1: ByteArray = serializer.serialize(cloudEvent1)
        val cloudEvent2: CloudEvent = serializer.deserialize(bytes1)
        assertEquals(cloudEvent2, cloudEvent1)
        val bytes2: ByteArray = serializer.serialize(cloudEvent2)
        assertArrayEquals(bytes1, bytes2)
        val cloudEvent3: CloudEvent = serializer.deserialize(bytes2)
        val cloudEvent3Payload: Any = UCloudEvent.getPayload(cloudEvent3)
        val clazz: Class<out Message?> = io.cloudevents.v1.proto.CloudEvent::class.java
        assertEquals(cloudEvent3Payload.unpack(clazz), cloudEventProto.unpack(clazz))
        assertEquals(cloudEvent2, cloudEvent3)
        assertEquals(cloudEvent1, cloudEvent3)
    }

    private fun buildCloudEventForTest(): CloudEventBuilder {
        return CloudEventBuilder.v1()
            .withId("hello")
            .withType("pub.v1")
            .withSource(URI.create("//VCU.VIN/body.access"))
    }

    private fun buildProtoPayloadForTest1(): Any {
        val cloudEventProto: io.cloudevents.v1.proto.CloudEvent = io.cloudevents.v1.proto.CloudEvent.newBuilder()
            .setSpecVersion("1.0")
            .setId("hello")
            .setSource("//VCU.VIN/body.access")
            .setType("pub.v1")
            .setProtoData(Any.newBuilder().build())
            .build()
        return Any.pack(cloudEventProto)
    }

    private fun buildProtoPayloadForTest(): Any {
        val cloudEventProto: io.cloudevents.v1.proto.CloudEvent = io.cloudevents.v1.proto.CloudEvent.newBuilder()
            .setSpecVersion("1.0")
            .setId("hello")
            .setSource("https://example.com")
            .setType("example.demo")
            .setProtoData(Any.newBuilder().build())
            .putAttributes(
                "ttl", io.cloudevents.v1.proto.CloudEvent.CloudEventAttributeValue.newBuilder()
                    .setCeString("3").build()
            )
            .build()
        return Any.pack(cloudEventProto)
    }
}