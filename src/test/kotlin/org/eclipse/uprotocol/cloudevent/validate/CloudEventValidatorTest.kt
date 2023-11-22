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
package org.eclipse.uprotocol.cloudevent.validate

import com.google.protobuf.Any
import io.cloudevents.CloudEvent
import io.cloudevents.core.builder.CloudEventBuilder
import org.eclipse.uprotocol.cloudevent.datamodel.UCloudEventAttributes
import org.eclipse.uprotocol.cloudevent.factory.CloudEventFactory
import org.eclipse.uprotocol.cloudevent.factory.UCloudEvent
import org.eclipse.uprotocol.uri.serializer.LongUriSerializer
import org.eclipse.uprotocol.uuid.factory.UuidFactory
import org.eclipse.uprotocol.uuid.serializer.LongUuidSerializer
import org.eclipse.uprotocol.v1.*
import org.eclipse.uprotocol.validation.ValidationResult
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import java.net.URI
import java.time.Instant
import org.junit.jupiter.api.Assertions.*

internal class CloudEventValidatorTest {
    @Test
    @DisplayName("Test get a publish cloud event validator")
    fun test_get_a_publish_cloud_event_validator() {
        val builder: CloudEventBuilder = buildBaseCloudEventBuilderForTest().withType("pub.v1")
        val cloudEvent: CloudEvent = builder.build()
        val validator: CloudEventValidator = CloudEventValidator.getValidator(cloudEvent)
        val status: UStatus = validator.validateType(cloudEvent).toStatus()
        assertEquals(status, ValidationResult.STATUS_SUCCESS)
        assertEquals("CloudEventValidator.Publish", validator.toString())
    }

    @Test
    @DisplayName("Test get a notification cloud event validator")
    fun test_get_a_notification_cloud_event_validator() {
        val builder: CloudEventBuilder = buildBaseCloudEventBuilderForTest().withExtension("sink", "//bo.cloud/petapp")
            .withType("pub.v1")
        val cloudEvent: CloudEvent = builder.build()
        val validator: CloudEventValidator = CloudEventValidator.Validators.NOTIFICATION.validator()
        val status: UStatus = validator.validateType(cloudEvent).toStatus()
        assertEquals(status, ValidationResult.STATUS_SUCCESS)
        assertEquals("CloudEventValidator.Notification", validator.toString())
    }

    @Test
    @DisplayName("Test publish cloud event type")
    fun test_publish_cloud_event_type() {
        val builder: CloudEventBuilder = buildBaseCloudEventBuilderForTest().withType("res.v1")
        val cloudEvent: CloudEvent = builder.build()
        val validator: CloudEventValidator = CloudEventValidator.Validators.PUBLISH.validator()
        val status: UStatus = validator.validateType(cloudEvent).toStatus()
        assertEquals(UCode.INVALID_ARGUMENT, status.code)
        assertEquals(
            "Invalid CloudEvent type [res.v1]. CloudEvent of type Publish must have a type of 'pub.v1'",
            status.message
        )
    }

    @Test
    @DisplayName("Test notification cloud event type")
    fun test_notification_cloud_event_type() {
        val builder: CloudEventBuilder = buildBaseCloudEventBuilderForTest().withType("res.v1")
        val cloudEvent: CloudEvent = builder.build()
        val validator: CloudEventValidator = CloudEventValidator.Validators.NOTIFICATION.validator()
        val status: UStatus = validator.validateType(cloudEvent).toStatus()
        assertEquals(UCode.INVALID_ARGUMENT, status.code)
        assertEquals(
            "Invalid CloudEvent type [res.v1]. CloudEvent of type Publish must have a type of 'pub.v1'",
            status.message
        )
    }

    @Test
    @DisplayName("Test get a request cloud event validator")
    fun test_get_a_request_cloud_event_validator() {
        val builder: CloudEventBuilder = buildBaseCloudEventBuilderForTest().withType("req.v1")
        val cloudEvent: CloudEvent = builder.build()
        val validator: CloudEventValidator = CloudEventValidator.getValidator(cloudEvent)
        val status: UStatus = validator.validateType(cloudEvent).toStatus()
        assertEquals(status, ValidationResult.STATUS_SUCCESS)
        assertEquals("CloudEventValidator.Request", validator.toString())
    }

    @Test
    @DisplayName("Test request cloud event type")
    fun test_request_cloud_event_type() {
        val builder: CloudEventBuilder = buildBaseCloudEventBuilderForTest().withType("pub.v1")
        val cloudEvent: CloudEvent = builder.build()
        val validator: CloudEventValidator = CloudEventValidator.Validators.REQUEST.validator()
        val status: UStatus = validator.validateType(cloudEvent).toStatus()
        assertEquals(UCode.INVALID_ARGUMENT, status.code)
        assertEquals(
            "Invalid CloudEvent type [pub.v1]. CloudEvent of type Request must have a type of 'req.v1'",
            status.message
        )
    }

    @Test
    @DisplayName("Test get a response cloud event validator")
    fun test_get_a_response_cloud_event_validator() {
        val builder: CloudEventBuilder = buildBaseCloudEventBuilderForTest().withType("res.v1")
        val cloudEvent: CloudEvent = builder.build()
        val validator: CloudEventValidator = CloudEventValidator.getValidator(cloudEvent)
        val status: UStatus = validator.validateType(cloudEvent).toStatus()
        assertEquals(status, ValidationResult.STATUS_SUCCESS)
        assertEquals("CloudEventValidator.Response", validator.toString())
    }

    @Test
    @DisplayName("Test response cloud event type")
    fun test_response_cloud_event_type() {
        val builder: CloudEventBuilder = buildBaseCloudEventBuilderForTest().withType("pub.v1")
        val cloudEvent: CloudEvent = builder.build()
        val validator: CloudEventValidator = CloudEventValidator.Validators.RESPONSE.validator()
        val status: UStatus = validator.validateType(cloudEvent).toStatus()
        assertEquals(UCode.INVALID_ARGUMENT, status.code)
        assertEquals(
            "Invalid CloudEvent type [pub.v1]. CloudEvent of type Response must have a type of 'res.v1'",
            status.message
        )
    }

    @Test
    @DisplayName("Test get a publish cloud event validator when cloud event type is unknown")
    fun test_get_a_publish_cloud_event_validator_when_cloud_event_type_is_unknown() {
        val builder: CloudEventBuilder = buildBaseCloudEventBuilderForTest().withType("lala.v1")
        val cloudEvent: CloudEvent = builder.build()
        val validator: CloudEventValidator = CloudEventValidator.getValidator(cloudEvent)
        assertEquals("CloudEventValidator.Publish", validator.toString())
    }

    @Test
    @DisplayName("Test validate version")
    fun validate_cloud_event_version_when_valid() {
        val uuid: UUID = UuidFactory.Factories.UPROTOCOL.factory().create()
        val strUuid = LongUuidSerializer.instance().serialize(uuid)
        val builder: CloudEventBuilder = buildBaseCloudEventBuilderForTest().withType(UCloudEvent.getEventType(UMessageType.UMESSAGE_TYPE_PUBLISH))
            .withId(strUuid)
        val cloudEvent: CloudEvent = builder.build()
        val status: UStatus = CloudEventValidator.validateVersion(cloudEvent).toStatus()
        assertEquals(status, ValidationResult.STATUS_SUCCESS)
    }

    @Test
    @DisplayName("Test validate version when not valid")
    fun validate_cloud_event_version_when_not_valid() {
        val payloadForTest = buildProtoPayloadForTest()
        val builder: CloudEventBuilder = CloudEventBuilder.v03().withId("id").withType("pub.v1")
            .withSource(URI.create("/body.access")).withDataContentType("application/protobuf")
            .withDataSchema(URI.create(payloadForTest.typeUrl)).withData(payloadForTest.toByteArray())
        val cloudEvent: CloudEvent = builder.build()
        val status: UStatus = CloudEventValidator.validateVersion(cloudEvent).toStatus()
        assertEquals(UCode.INVALID_ARGUMENT, status.code)
        assertEquals("Invalid CloudEvent version [0.3]. CloudEvent version must be 1.0.", status.message)
    }

    @Test
    @DisplayName("Test validate cloudevent id when valid")
    fun validate_cloud_event_id_when_valid() {
        val uuid: UUID = UuidFactory.Factories.UPROTOCOL.factory().create()
        val strUuid = LongUuidSerializer.instance().serialize(uuid)
        val builder: CloudEventBuilder = buildBaseCloudEventBuilderForTest().withType(UCloudEvent.getEventType(UMessageType.UMESSAGE_TYPE_PUBLISH))
            .withId(strUuid)
        val cloudEvent: CloudEvent = builder.build()
        val status: UStatus = CloudEventValidator.validateId(cloudEvent).toStatus()
        assertEquals(status, ValidationResult.STATUS_SUCCESS)
    }

    @Test
    @DisplayName("Test validate cloudevent id when not UUIDv8 type id")
    fun validate_cloud_event_id_when_not_uuidv6_type_id() {
        val uuidJava: java.util.UUID = java.util.UUID.randomUUID()
        val uuid: UUID = UUID.newBuilder().setMsb(uuidJava.mostSignificantBits)
            .setLsb(uuidJava.leastSignificantBits).build()
        val strUuid = LongUuidSerializer.instance().serialize(uuid)
        val builder: CloudEventBuilder = buildBaseCloudEventBuilderForTest().withType(UCloudEvent.getEventType(UMessageType.UMESSAGE_TYPE_PUBLISH))
            .withId(strUuid)
        val cloudEvent: CloudEvent = builder.build()
        val status: UStatus = CloudEventValidator.validateId(cloudEvent).toStatus()
        assertEquals(UCode.INVALID_ARGUMENT, status.code)
        assertEquals(
            "Invalid CloudEvent Id [$strUuid]. CloudEvent Id must be of type UUIDv8.",
            status.message
        )
    }

    @Test
    @DisplayName("Test validate cloudevent id when not valid")
    fun validate_cloud_event_id_when_not_valid() {
        val builder: CloudEventBuilder = buildBaseCloudEventBuilderForTest().withType(UCloudEvent.getEventType(UMessageType.UMESSAGE_TYPE_PUBLISH))
            .withId("testme")
        val cloudEvent: CloudEvent = builder.build()
        val status: UStatus = CloudEventValidator.validateId(cloudEvent).toStatus()
        assertEquals(UCode.INVALID_ARGUMENT, status.code)
        assertEquals("Invalid CloudEvent Id [testme]. CloudEvent Id must be of type UUIDv8.", status.message)
    }

    @Test
    @DisplayName("Test local Publish type CloudEvent is valid everything is valid")
    fun test_publish_type_cloudevent_is_valid_when_everything_is_valid_local() {
        val uuid: UUID = UuidFactory.Factories.UPROTOCOL.factory().create()
        val strUuid = LongUuidSerializer.instance().serialize(uuid)
        val builder: CloudEventBuilder = buildBaseCloudEventBuilderForTest().withId(strUuid)
            .withSource(URI.create("/body.access/1/door.front_left#Door")).withType(UCloudEvent.getEventType(UMessageType.UMESSAGE_TYPE_PUBLISH))
        val cloudEvent: CloudEvent = builder.build()
        val validator: CloudEventValidator = CloudEventValidator.Validators.PUBLISH.validator()
        val status: UStatus = validator.validate(cloudEvent)
        assertEquals(ValidationResult.STATUS_SUCCESS, status)
    }

    @Test
    @DisplayName("Test microRemote Publish type CloudEvent is valid everything is valid")
    fun test_publish_type_cloudevent_is_valid_when_everything_is_valid_remote() {
        val uuid: UUID = UuidFactory.Factories.UPROTOCOL.factory().create()
        val strUuid = LongUuidSerializer.instance().serialize(uuid)
        val builder: CloudEventBuilder = buildBaseCloudEventBuilderForTest().withId(strUuid)
            .withSource(URI.create("//VCU.myvin/body.access/1/door.front_left#Door"))
            .withType(UCloudEvent.getEventType(UMessageType.UMESSAGE_TYPE_PUBLISH))
        val cloudEvent: CloudEvent = builder.build()
        val validator: CloudEventValidator = CloudEventValidator.Validators.PUBLISH.validator()
        val status: UStatus = validator.validate(cloudEvent)
        assertEquals(ValidationResult.STATUS_SUCCESS, status)
    }

    @Test
    @DisplayName("Test microRemote Publish type CloudEvent is valid everything is valid with a sink")
    fun test_publish_type_cloudevent_is_valid_when_everything_is_valid_remote_with_a_sink() {
        val uuid: UUID = UuidFactory.Factories.UPROTOCOL.factory().create()
        val strUuid = LongUuidSerializer.instance().serialize(uuid)
        val builder: CloudEventBuilder = buildBaseCloudEventBuilderForTest().withId(strUuid)
            .withSource(URI.create("//VCU.myvin/body.access/1/door.front_left#Door"))
            .withExtension("sink", "//bo.cloud/petapp").withType(UCloudEvent.getEventType(UMessageType.UMESSAGE_TYPE_PUBLISH))
        val cloudEvent: CloudEvent = builder.build()
        val validator: CloudEventValidator = CloudEventValidator.Validators.PUBLISH.validator()
        val status: UStatus = validator.validate(cloudEvent)
        assertEquals(ValidationResult.STATUS_SUCCESS, status)
    }

    @Test
    @DisplayName("Test microRemote Publish type CloudEvent is not valid everything is valid with invalid sink")
    fun test_publish_type_cloudevent_is_not_valid_when_remote_with_invalid_sink() {
        val uuid: UUID = UuidFactory.Factories.UPROTOCOL.factory().create()
        val strUuid = LongUuidSerializer.instance().serialize(uuid)
        val builder: CloudEventBuilder = buildBaseCloudEventBuilderForTest().withId(strUuid)
            .withSource(URI.create("//VCU.myvin/body.access/1/door.front_left#Door"))
            .withExtension("sink", "//bo.cloud").withType(UCloudEvent.getEventType(UMessageType.UMESSAGE_TYPE_PUBLISH))
        val cloudEvent: CloudEvent = builder.build()
        val validator: CloudEventValidator = CloudEventValidator.Validators.PUBLISH.validator()
        val status: UStatus = validator.validate(cloudEvent)
        assertEquals(UCode.INVALID_ARGUMENT, status.code)
        assertEquals(
            "Invalid CloudEvent sink [//bo.cloud]. Uri is missing uSoftware Entity name.",
            status.message
        )
    }

    @Test
    @DisplayName("Test Publish type CloudEvent is not valid when source is empty")
    fun test_publish_type_cloudevent_is_not_valid_when_source_is_empty() {
        val uuid: UUID = UuidFactory.Factories.UPROTOCOL.factory().create()
        val strUuid = LongUuidSerializer.instance().serialize(uuid)
        val builder: CloudEventBuilder = buildBaseCloudEventBuilderForTest().withId(strUuid)
            .withSource(URI.create("/")).withType(UCloudEvent.getEventType(UMessageType.UMESSAGE_TYPE_PUBLISH))
        val cloudEvent: CloudEvent = builder.build()
        val validator: CloudEventValidator = CloudEventValidator.Validators.PUBLISH.validator()
        val status: UStatus = validator.validate(cloudEvent)
        assertEquals(UCode.INVALID_ARGUMENT, status.code)
        assertEquals("Invalid Publish type CloudEvent source [/]. Uri is empty.", status.message)
    }

    @Test
    @DisplayName("Test Publish type CloudEvent is not valid when source is invalid and id invalid")
    fun test_publish_type_cloudevent_is_not_valid_when_source_is_missing_authority() {
        val builder: CloudEventBuilder = buildBaseCloudEventBuilderForTest().withId("testme")
            .withSource(URI.create("/body.access")).withType(UCloudEvent.getEventType(UMessageType.UMESSAGE_TYPE_PUBLISH))
        val cloudEvent: CloudEvent = builder.build()
        val validator: CloudEventValidator = CloudEventValidator.Validators.PUBLISH.validator()
        val status: UStatus = validator.validate(cloudEvent)
        assertEquals(UCode.INVALID_ARGUMENT, status.code)
        assertEquals(
            "Invalid CloudEvent Id [testme]. CloudEvent Id must be of type UUIDv8.," + "Invalid Publish type " +
                    "CloudEvent source [/body.access]. UriPart is missing uResource name.",
            status.message
        )
    }

    @Test
    @DisplayName("Test Publish type CloudEvent is not valid when source is invalid missing message information")
    fun test_publish_type_cloudevent_is_not_valid_when_source_is_missing_message_info() {
        val builder: CloudEventBuilder = buildBaseCloudEventBuilderForTest().withId("testme")
            .withSource(URI.create("/body.access/1/door.front_left")).withType(UCloudEvent.getEventType(UMessageType.UMESSAGE_TYPE_PUBLISH))
        val cloudEvent: CloudEvent = builder.build()
        val validator: CloudEventValidator = CloudEventValidator.Validators.PUBLISH.validator()
        val status: UStatus = validator.validate(cloudEvent)
        assertEquals(UCode.INVALID_ARGUMENT, status.code)
        assertEquals(
            "Invalid CloudEvent Id [testme]. CloudEvent Id must be of type UUIDv8.," + "Invalid Publish type " +
                    "CloudEvent source [/body.access/1/door.front_left]. UriPart is missing Message information.",
            status.message
        )
    }

    @Test
    @DisplayName("Test Notification type CloudEvent is valid everything is valid")
    fun test_notification_type_cloudevent_is_valid_when_everything_is_valid() {
        val uuid: UUID = UuidFactory.Factories.UPROTOCOL.factory().create()
        val strUuid = LongUuidSerializer.instance().serialize(uuid)
        val builder: CloudEventBuilder = buildBaseCloudEventBuilderForTest().withId(strUuid)
            .withSource(URI.create("/body.access/1/door.front_left#Door")).withType(UCloudEvent.getEventType(UMessageType.UMESSAGE_TYPE_PUBLISH))
            .withExtension("sink", "//bo.cloud/petapp")
        val cloudEvent: CloudEvent = builder.build()
        val validator: CloudEventValidator = CloudEventValidator.Validators.NOTIFICATION.validator()
        val status: UStatus = validator.validate(cloudEvent)
        assertEquals(ValidationResult.STATUS_SUCCESS, status)
    }

    @Test
    @DisplayName("Test Notification type CloudEvent is not valid missing sink")
    fun test_notification_type_cloudevent_is_not_valid_missing_sink() {
        val uuid: UUID = UuidFactory.Factories.UPROTOCOL.factory().create()
        val strUuid = LongUuidSerializer.instance().serialize(uuid)
        val builder: CloudEventBuilder = buildBaseCloudEventBuilderForTest().withId(strUuid)
            .withSource(URI.create("/body.access/1/door.front_left#Door")).withType(UCloudEvent.getEventType(UMessageType.UMESSAGE_TYPE_PUBLISH))
        val cloudEvent: CloudEvent = builder.build()
        val validator: CloudEventValidator = CloudEventValidator.Validators.NOTIFICATION.validator()
        val status: UStatus = validator.validate(cloudEvent)
        assertEquals(UCode.INVALID_ARGUMENT, status.code)
        assertEquals("Invalid CloudEvent sink. Notification CloudEvent sink must be an  uri.", status.message)
    }

    @Test
    @DisplayName("Test Notification type CloudEvent is not valid invalid sink")
    fun test_notification_type_cloudevent_is_not_valid_invalid_sink() {
        val uuid: UUID = UuidFactory.Factories.UPROTOCOL.factory().create()
        val strUuid = LongUuidSerializer.instance().serialize(uuid)
        val builder: CloudEventBuilder = buildBaseCloudEventBuilderForTest().withId(strUuid)
            .withSource(URI.create("/body.access/1/door.front_left#Door")).withType(UCloudEvent.getEventType(UMessageType.UMESSAGE_TYPE_PUBLISH))
            .withExtension("sink", "//bo.cloud")
        val cloudEvent: CloudEvent = builder.build()
        val validator: CloudEventValidator = CloudEventValidator.Validators.NOTIFICATION.validator()
        val status: UStatus = validator.validate(cloudEvent)
        assertEquals(UCode.INVALID_ARGUMENT, status.code)
        assertEquals(
            "Invalid Notification type CloudEvent sink [//bo.cloud]. Uri is missing uSoftware Entity name.",
            status.message
        )
    }

    @Test
    @DisplayName("Test Request type CloudEvent is valid everything is valid")
    fun test_request_type_cloudevent_is_valid_when_everything_is_valid() {
        val uuid: UUID = UuidFactory.Factories.UPROTOCOL.factory().create()
        val strUuid = LongUuidSerializer.instance().serialize(uuid)
        val builder: CloudEventBuilder = buildBaseCloudEventBuilderForTest().withId(strUuid)
            .withSource(URI.create("//bo.cloud/petapp//rpc.response")).withType(UCloudEvent.getEventType(UMessageType.UMESSAGE_TYPE_REQUEST))
            .withExtension("sink", "//VCU.myvin/body.access/1/rpc.UpdateDoor")
        val cloudEvent: CloudEvent = builder.build()
        val validator: CloudEventValidator = CloudEventValidator.Validators.REQUEST.validator()
        val status: UStatus = validator.validate(cloudEvent)
        assertEquals(ValidationResult.STATUS_SUCCESS, status)
    }

    @Test
    @DisplayName("Test Request type CloudEvent is not valid invalid source")
    fun test_request_type_cloudevent_is_not_valid_invalid_source() {
        val uuid: UUID = UuidFactory.Factories.UPROTOCOL.factory().create()
        val strUuid = LongUuidSerializer.instance().serialize(uuid)
        val builder: CloudEventBuilder = buildBaseCloudEventBuilderForTest().withId(strUuid)
            .withSource(URI.create("//bo.cloud/petapp//dog"))
            .withExtension("sink", "//VCU.myvin/body.access/1/rpc.UpdateDoor")
            .withType(UCloudEvent.getEventType(UMessageType.UMESSAGE_TYPE_REQUEST))
        val cloudEvent: CloudEvent = builder.build()
        val validator: CloudEventValidator = CloudEventValidator.Validators.REQUEST.validator()
        val status: UStatus = validator.validate(cloudEvent)
        assertEquals(UCode.INVALID_ARGUMENT, status.code)
        assertEquals(
            "Invalid RPC Request CloudEvent source [//bo.cloud/petapp//dog]. " + "Invalid RPC uri application " +
                    "response topic. UriPart is missing rpc.response.",
            status.message
        )
    }

    @Test
    @DisplayName("Test Request type CloudEvent is not valid missing sink")
    fun test_request_type_cloudevent_is_not_valid_missing_sink() {
        val uuid: UUID = UuidFactory.Factories.UPROTOCOL.factory().create()
        val strUuid = LongUuidSerializer.instance().serialize(uuid)
        val builder: CloudEventBuilder = buildBaseCloudEventBuilderForTest().withId(strUuid)
            .withSource(URI.create("//bo.cloud/petapp//rpc.response")).withType(UCloudEvent.getEventType(UMessageType.UMESSAGE_TYPE_REQUEST))
        val cloudEvent: CloudEvent = builder.build()
        val validator: CloudEventValidator = CloudEventValidator.Validators.REQUEST.validator()
        val status: UStatus = validator.validate(cloudEvent)
        assertEquals(UCode.INVALID_ARGUMENT, status.code)
        assertEquals(
            "Invalid RPC Request CloudEvent sink. Request CloudEvent sink must be uri for the method to be called.",
            status.message
        )
    }

    @Test
    @DisplayName("Test Request type CloudEvent is not valid sink not rpc command")
    fun test_request_type_cloudevent_is_not_valid_invalid_sink_not_rpc_command() {
        val uuid: UUID = UuidFactory.Factories.UPROTOCOL.factory().create()
        val strUuid = LongUuidSerializer.instance().serialize(uuid)
        val builder: CloudEventBuilder = buildBaseCloudEventBuilderForTest().withId(strUuid)
            .withSource(URI.create("//bo.cloud/petapp//rpc.response")).withType(UCloudEvent.getEventType(UMessageType.UMESSAGE_TYPE_REQUEST))
            .withExtension("sink", "//VCU.myvin/body.access/1/UpdateDoor")
        val cloudEvent: CloudEvent = builder.build()
        val validator: CloudEventValidator = CloudEventValidator.Validators.REQUEST.validator()
        val status: UStatus = validator.validate(cloudEvent)
        assertEquals(UCode.INVALID_ARGUMENT, status.code)
        assertEquals(
            "Invalid RPC Request CloudEvent sink [//VCU.myvin/body.access/1/UpdateDoor]. " + "Invalid RPC method " +
                    "uri. UriPart should be the method to be called, or method from response.",
            status.message
        )
    }

    @Test
    @DisplayName("Test Response type CloudEvent is valid everything is valid")
    fun test_response_type_cloudevent_is_valid_when_everything_is_valid() {
        val uuid: UUID = UuidFactory.Factories.UPROTOCOL.factory().create()
        val strUuid = LongUuidSerializer.instance().serialize(uuid)
        val builder: CloudEventBuilder = buildBaseCloudEventBuilderForTest().withId(strUuid)
            .withSource(URI.create("//VCU.myvin/body.access/1/rpc.UpdateDoor"))
            .withType(UCloudEvent.getEventType(UMessageType.UMESSAGE_TYPE_RESPONSE)).withExtension("sink", "//bo.cloud/petapp//rpc.response")
        val cloudEvent: CloudEvent = builder.build()
        val validator: CloudEventValidator = CloudEventValidator.Validators.RESPONSE.validator()
        val status: UStatus = validator.validate(cloudEvent)
        assertEquals(ValidationResult.STATUS_SUCCESS, status)
    }

    @Test
    @DisplayName("Test Response type CloudEvent is not valid invalid source")
    fun test_response_type_cloudevent_is_not_valid_invalid_source() {
        val uuid: UUID = UuidFactory.Factories.UPROTOCOL.factory().create()
        val strUuid = LongUuidSerializer.instance().serialize(uuid)
        val builder: CloudEventBuilder = buildBaseCloudEventBuilderForTest().withId(strUuid)
            .withSource(URI.create("//VCU.myvin/body.access/1/UpdateDoor"))
            .withExtension("sink", "//bo.cloud/petapp//rpc.response").withType(UCloudEvent.getEventType(UMessageType.UMESSAGE_TYPE_RESPONSE))
        val cloudEvent: CloudEvent = builder.build()
        val validator: CloudEventValidator = CloudEventValidator.Validators.RESPONSE.validator()
        val status: UStatus = validator.validate(cloudEvent)
        assertEquals(UCode.INVALID_ARGUMENT, status.code)
        assertEquals(
            "Invalid RPC Response CloudEvent source [//VCU.myvin/body.access/1/UpdateDoor]. " + "Invalid RPC " +
                    "method uri. UriPart should be the method to be called, or method from response.",
            status.message
        )
    }

    @Test
    @DisplayName("Test Response type CloudEvent is not valid missing sink and invalid source")
    fun test_response_type_cloudevent_is_not_valid_missing_sink_and_invalid_source() {
        val uuid: UUID = UuidFactory.Factories.UPROTOCOL.factory().create()
        val strUuid = LongUuidSerializer.instance().serialize(uuid)
        val builder: CloudEventBuilder = buildBaseCloudEventBuilderForTest().withId(strUuid)
            .withSource(URI.create("//VCU.myvin/body.access/1/UpdateDoor"))
            .withType(UCloudEvent.getEventType(UMessageType.UMESSAGE_TYPE_RESPONSE))
        val cloudEvent: CloudEvent = builder.build()
        val validator: CloudEventValidator = CloudEventValidator.Validators.RESPONSE.validator()
        val status: UStatus = validator.validate(cloudEvent)
        assertEquals(UCode.INVALID_ARGUMENT, status.code)
        assertEquals(
            "Invalid RPC Response CloudEvent source [//VCU.myvin/body.access/1/UpdateDoor]. " + "Invalid RPC " +
                    "method uri. UriPart should be the method to be called, or method from response.," + "Invalid" +
                    " CloudEvent sink. Response CloudEvent sink must be uri the destination of the response.",
            status.message
        )
    }

    @Test
    @DisplayName("Test Response type CloudEvent is not valid sink and source, missing entity name.")
    fun test_response_type_cloudevent_is_not_valid_invalid_sink() {
        val uuid: UUID = UuidFactory.Factories.UPROTOCOL.factory().create()
        val strUuid = LongUuidSerializer.instance().serialize(uuid)
        val builder: CloudEventBuilder = buildBaseCloudEventBuilderForTest().withId(strUuid)
            .withType(UCloudEvent.getEventType(UMessageType.UMESSAGE_TYPE_RESPONSE)).withSource(URI.create("//VCU.myvin"))
            .withExtension("sink", "//bo.cloud")
        val cloudEvent: CloudEvent = builder.build()
        val validator: CloudEventValidator = CloudEventValidator.Validators.RESPONSE.validator()
        val status: UStatus = validator.validate(cloudEvent)
        assertEquals(UCode.INVALID_ARGUMENT, status.code)
        assertEquals(
            "Invalid RPC Response CloudEvent source [//VCU.myvin]. Invalid RPC method uri. Uri is missing " +
                    "uSoftware Entity name.,Invalid RPC Response CloudEvent sink [//bo.cloud]. Invalid RPC uri " +
                    "application response topic. Uri is missing uSoftware Entity name.",
            status.message
        )
    }

    @Test
    @DisplayName("Test Response type CloudEvent is not valid source not rpc command")
    fun test_response_type_cloudevent_is_not_valid_invalid_source_not_rpc_command() {
        val uuid: UUID = UuidFactory.Factories.UPROTOCOL.factory().create()
        val strUuid = LongUuidSerializer.instance().serialize(uuid)
        val builder: CloudEventBuilder = buildBaseCloudEventBuilderForTest().withId(strUuid)
            .withSource(URI.create("//bo.cloud/petapp/1/dog")).withType(UCloudEvent.getEventType(UMessageType.UMESSAGE_TYPE_RESPONSE))
            .withExtension("sink", "//VCU.myvin/body.access/1/UpdateDoor")
        val cloudEvent: CloudEvent = builder.build()
        val validator: CloudEventValidator = CloudEventValidator.Validators.RESPONSE.validator()
        val status: UStatus = validator.validate(cloudEvent)
        assertEquals(UCode.INVALID_ARGUMENT, status.code)
        assertEquals(
            "Invalid RPC Response CloudEvent source [//bo.cloud/petapp/1/dog]. Invalid RPC method uri. UriPart " +
                    "should be the method to be called, or method from response.," + "Invalid RPC Response " +
                    "CloudEvent sink [//VCU.myvin/body.access/1/UpdateDoor]. " + "Invalid RPC uri application " +
                    "response topic. UriPart is missing rpc.response.",
            status.message
        )
    }

    private fun buildBaseCloudEventBuilderForTest(): CloudEventBuilder {
        // source
        val source = buildLongUriForTest()

        // fake payload
        val protoPayload = buildProtoPayloadForTest()

        // additional attributes
        val uCloudEventAttributes: UCloudEventAttributes = UCloudEventAttributes.UCloudEventAttributesBuilder().withHash(
            "somehash"
        ).withPriority(UPriority.UPRIORITY_CS1).withTtl(3).withToken(
            "someOAuthToken"
        )
            .build()

        // build the cloud event
        val cloudEventBuilder: CloudEventBuilder = CloudEventFactory.buildBaseCloudEvent(
            "testme", source,
            protoPayload.toByteArray(), protoPayload.typeUrl, uCloudEventAttributes
        )
        cloudEventBuilder.withType(UCloudEvent.getEventType(UMessageType.UMESSAGE_TYPE_PUBLISH))
        return cloudEventBuilder
    }

    private fun buildProtoPayloadForTest(): Any {
        val cloudEventProto: io.cloudevents.v1.proto.CloudEvent = io.cloudevents.v1.proto.CloudEvent.newBuilder()
            .setSpecVersion("1.0").setId("hello").setSource("/body.access").setType("example.demo")
            .setProtoData(Any.newBuilder().build()).build()
        return Any.pack(cloudEventProto)
    }

    @Test
    @DisplayName("Test create a v6 Cloudevent and validate it works with this SDK")
    fun test_create_a_v6_cloudevent_and_validate_it_against_sdk() {

        // source
        val source = buildLongUriForTest()
        val uuid: UUID = UuidFactory.Factories.UUIDV6.factory().create()
        val id = LongUuidSerializer.instance().serialize(uuid)

        // fake payload
        val protoPayload = buildProtoPayloadForTest()
        val attributes: UCloudEventAttributes = UCloudEventAttributes.UCloudEventAttributesBuilder().withPriority(
            UPriority.UPRIORITY_CS0
        ).withTtl(1000) // live for 1 second
            .build()

        // build the cloud event
        val cloudEvent: CloudEvent = CloudEventFactory.buildBaseCloudEvent(
            id, source, protoPayload.toByteArray(),
            protoPayload.typeUrl, attributes
        ).withType(UCloudEvent.getEventType(UMessageType.UMESSAGE_TYPE_PUBLISH)).build()
        val validator: CloudEventValidator = CloudEventValidator.Validators.PUBLISH.validator()
        val status: UStatus = validator.validate(cloudEvent)
        assertEquals(UCode.OK, status.code)
        assertFalse(UCloudEvent.isExpired(cloudEvent))
    }

    @Test
    @DisplayName("Test create an expired v6 Cloudevent to ensure we report the expiration")
    fun test_create_an_expired_v6_cloudevent() {

        // source
        val source = buildLongUriForTest()
        val uuid: UUID = UuidFactory.Factories.UUIDV6.factory().create(Instant.now().minusSeconds(100))
        val id = LongUuidSerializer.instance().serialize(uuid)

        // fake payload
        val protoPayload = buildProtoPayloadForTest()
        val attributes: UCloudEventAttributes = UCloudEventAttributes.UCloudEventAttributesBuilder().withPriority(
            UPriority.UPRIORITY_CS0
        ).withTtl(1000) // live for 1 second
            .build()

        // build the cloud event
        val cloudEvent: CloudEvent = CloudEventFactory.buildBaseCloudEvent(
            id, source, protoPayload.toByteArray(),
            protoPayload.typeUrl, attributes
        ).withType(UCloudEvent.getEventType(UMessageType.UMESSAGE_TYPE_PUBLISH)).build()
        val validator: CloudEventValidator = CloudEventValidator.Validators.PUBLISH.validator()
        val status: UStatus = validator.validate(cloudEvent)
        assertEquals(UCode.OK, status.code)
        assertTrue(UCloudEvent.isExpired(cloudEvent))
    }

    private fun buildLongUriForTest(): String {
        return LongUriSerializer.instance().serialize(buildUUriForTest())
    }

    private fun buildUUriForTest(): UUri {
        return UUri.newBuilder().setEntity(UEntity.newBuilder().setName("body.access"))
            .setResource(UResource.newBuilder().setName("door").setInstance("front_left").setMessage("Door"))
            .build()
    }
}