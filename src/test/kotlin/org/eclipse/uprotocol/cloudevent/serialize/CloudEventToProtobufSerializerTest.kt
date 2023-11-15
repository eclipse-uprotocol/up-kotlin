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
package org.eclipse.uprotocol.cloudevent.serialize

import com.google.protobuf.Any
import com.google.protobuf.InvalidProtocolBufferException
import com.google.protobuf.Message
import io.cloudevents.CloudEvent
import io.cloudevents.core.builder.CloudEventBuilder
import org.eclipse.uprotocol.cloudevent.datamodel.UCloudEventAttributes
import org.eclipse.uprotocol.cloudevent.factory.CloudEventFactory
import org.eclipse.uprotocol.cloudevent.factory.UCloudEvent
import org.eclipse.uprotocol.uri.serializer.LongUriSerializer
import org.eclipse.uprotocol.v1.*
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import java.net.URI
import java.util.Objects
import org.junit.jupiter.api.Assertions.*


internal class CloudEventToProtobufSerializerTest {
    private val serializer: CloudEventSerializer = CloudEventToProtobufSerializer()
    private val protoContentType: String = CloudEventFactory.PROTOBUF_CONTENT_TYPE
    @Test
    @DisplayName("Test serialize and deserialize a CloudEvent to protobuf")
    fun test_serialize_and_desirialize_cloud_event_to_protobuf() {

        // build the source
        val source = buildUriForTest()

        // fake payload
        val protoPayload = buildProtoPayloadForTest()

        // configure cloud event
        val uCloudEventAttributes: UCloudEventAttributes = UCloudEventAttributes.UCloudEventAttributesBuilder()
            .withHash("somehash")
            .withPriority(UPriority.UPRIORITY_CS0)
            .withTtl(3)
            .build()
        val cloudEventBuilder: CloudEventBuilder = CloudEventFactory.buildBaseCloudEvent(
            "hello", source,
            protoPayload.toByteArray(), protoPayload.typeUrl,
            uCloudEventAttributes
        )
        cloudEventBuilder.withType("pub.v1")
        val cloudEvent: CloudEvent = cloudEventBuilder.build()
        val bytes: ByteArray = serializer.serialize(cloudEvent)
        val deserialize: CloudEvent = serializer.deserialize(bytes)

        // data is not the same type, does not work -> expected data=BytesCloudEventData actual data=io.cloudevents.protobuf.ProtoDataWrapper
        //assertEquals(cloudEvent, deserialize);
        assertCloudEventsAreTheSame(cloudEvent, deserialize)
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
            .withDataContentType("application/protobuf")
            .withDataSchema(URI.create(protoPayload.typeUrl))
            .withData(protoPayload.toByteArray())
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
            .withDataContentType("application/protobuf")
            .withDataSchema(URI.create(protoPayload.typeUrl))
            .withData(protoPayload.toByteArray())
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
        val serializer: CloudEventSerializer = CloudEventSerializers.PROTOBUF.serializer()

        // source
        val source = buildUriForTest()

        // fake payload
        val protoPayload = buildProtoPayloadForTest1()

        // additional attributes
        val uCloudEventAttributes: UCloudEventAttributes = UCloudEventAttributes.UCloudEventAttributesBuilder()
            .withHash("somehash")
            .withPriority(UPriority.UPRIORITY_CS1)
            .withTtl(3)
            .withToken("someOAuthToken")
            .build()

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
        assertCloudEventsAreTheSame(cloudEvent2, cloudEvent1)
        val bytes2: ByteArray = serializer.serialize(cloudEvent2)
        assertArrayEquals(bytes1, bytes2)
        val cloudEvent3: CloudEvent = serializer.deserialize(bytes2)
        val cloudEvent3Payload: Any = UCloudEvent.getPayload(cloudEvent3)
        val clazz: Class<out Message?> = io.cloudevents.v1.proto.CloudEvent::class.java
        assertEquals(cloudEvent3Payload.unpack(clazz), protoPayload.unpack(clazz))
        assertEquals(cloudEvent2, cloudEvent3)
        assertCloudEventsAreTheSame(cloudEvent1, cloudEvent3)
    }

    @Test
    @DisplayName("test double serialization Protobuf")
    @Throws(InvalidProtocolBufferException::class)
    fun test_double_serialization_protobuf() {
        val serializer: CloudEventSerializer = CloudEventSerializers.PROTOBUF.serializer()
        val builder: CloudEventBuilder = buildCloudEventForTest()
        val cloudEventProto = buildProtoPayloadForTest1()
        builder.withDataContentType(protoContentType)
        builder.withData(cloudEventProto.toByteArray())
        builder.withDataSchema(URI.create(cloudEventProto.typeUrl))
        val cloudEvent1: CloudEvent = builder.build()
        val bytes1: ByteArray = serializer.serialize(cloudEvent1)
        val cloudEvent2: CloudEvent = serializer.deserialize(bytes1)
        assertCloudEventsAreTheSame(cloudEvent2, cloudEvent1)
        val bytes2: ByteArray = serializer.serialize(cloudEvent2)
        assertArrayEquals(bytes1, bytes2)
        val cloudEvent3: CloudEvent = serializer.deserialize(bytes2)
        val cloudEvent3Payload: Any = UCloudEvent.getPayload(cloudEvent3)
        val clazz: Class<out Message?> = io.cloudevents.v1.proto.CloudEvent::class.java
        assertEquals(cloudEvent3Payload.unpack(clazz), cloudEventProto.unpack(clazz))
        assertEquals(cloudEvent2, cloudEvent3)
        assertCloudEventsAreTheSame(cloudEvent1, cloudEvent3)
    }

    @Test
    @DisplayName("test double serialization proto to Json")
    fun test_double_serialization_proto_to_json() {
        val protoSerializer: CloudEventSerializer = CloudEventSerializers.PROTOBUF.serializer()
        val jsonSerializer: CloudEventSerializer = CloudEventSerializers.JSON.serializer()
        val builder: CloudEventBuilder = buildCloudEventForTest()
        val cloudEventProto = buildProtoPayloadForTest1()
        builder.withDataContentType(protoContentType)
        builder.withData(cloudEventProto.toByteArray())
        builder.withDataSchema(URI.create(cloudEventProto.typeUrl))
        val cloudEvent1: CloudEvent = builder.build()
        val bytes1: ByteArray = protoSerializer.serialize(cloudEvent1)
        val cloudEvent2: CloudEvent = protoSerializer.deserialize(bytes1)
        assertCloudEventsAreTheSame(cloudEvent2, cloudEvent1)
        val bytes2: ByteArray = protoSerializer.serialize(cloudEvent2)
        assertArrayEquals(bytes1, bytes2)
        val bytes3: ByteArray = jsonSerializer.serialize(cloudEvent2)
        val cloudEvent3: CloudEvent = jsonSerializer.deserialize(bytes3)
        assertCloudEventsAreTheSame(cloudEvent2, cloudEvent3)
        assertEquals(cloudEvent1, cloudEvent3)
    }

    @Test
    @DisplayName("test double serialization json to proto")
    fun test_double_serialization_json_to_proto() {
        val protoSerializer: CloudEventSerializer = CloudEventSerializers.PROTOBUF.serializer()
        val jsonSerializer: CloudEventSerializer = CloudEventSerializers.JSON.serializer()
        val builder: CloudEventBuilder = buildCloudEventForTest()
        val cloudEventProto = buildProtoPayloadForTest1()
        builder.withDataContentType(protoContentType)
        builder.withData(cloudEventProto.toByteArray())
        builder.withDataSchema(URI.create(cloudEventProto.typeUrl))
        val cloudEvent1: CloudEvent = builder.build()
        val bytes1: ByteArray = jsonSerializer.serialize(cloudEvent1)
        val cloudEvent2: CloudEvent = jsonSerializer.deserialize(bytes1)
        assertEquals(cloudEvent2, cloudEvent1)
        val bytes2: ByteArray = jsonSerializer.serialize(cloudEvent2)
        assertArrayEquals(bytes1, bytes2)
        val bytes3: ByteArray = protoSerializer.serialize(cloudEvent2)
        val cloudEvent3: CloudEvent = protoSerializer.deserialize(bytes3)
        assertCloudEventsAreTheSame(cloudEvent2, cloudEvent3)
        assertCloudEventsAreTheSame(cloudEvent1, cloudEvent3)
    }

    private fun assertCloudEventsAreTheSame(cloudEvent1: CloudEvent, cloudEvent2: CloudEvent) {
        assertNotNull(cloudEvent1)
        assertNotNull(cloudEvent2)
        assertEquals(cloudEvent1.specVersion.toString(), cloudEvent2.specVersion.toString())
        assertEquals(cloudEvent1.id, cloudEvent2.id)
        assertEquals(cloudEvent1.source, cloudEvent2.source)
        assertEquals(cloudEvent1.type, cloudEvent2.type)
        assertEquals(cloudEvent1.dataContentType, cloudEvent2.dataContentType)
        assertEquals(cloudEvent1.dataSchema, cloudEvent2.dataSchema)
        val ce1ExtensionNames: Set<String> = cloudEvent1.extensionNames
        val ce2ExtensionNames: Set<String> = cloudEvent2.extensionNames
        assertEquals(ce1ExtensionNames.joinToString(","), ce2ExtensionNames.joinToString(","))
        assertArrayEquals(
            Objects.requireNonNull(cloudEvent1.data).toBytes(),
            Objects.requireNonNull(cloudEvent2.data).toBytes()
        )
        assertEquals(cloudEvent1, cloudEvent2)
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

    private fun buildUriForTest(): String {
        val uri: UUri = UUri.newBuilder()
            .setEntity(UEntity.newBuilder().setName("body.access"))
            .setResource(
                UResource.newBuilder()
                    .setName("door")
                    .setInstance("front_left")
                    .setMessage("Door")
            )
            .build()
        return LongUriSerializer.instance().serialize(uri)
    }

    private fun buildProtoPayloadForTest(): Any {
        val cloudEventProto: io.cloudevents.v1.proto.CloudEvent = io.cloudevents.v1.proto.CloudEvent.newBuilder()
            .setSpecVersion("1.0")
            .setId("hello")
            .setSource("http://example.com")
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