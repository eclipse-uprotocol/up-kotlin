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
package org.eclipse.uprotocol.rpc


import com.google.protobuf.Any
import com.google.protobuf.Int32Value
import com.google.protobuf.InvalidProtocolBufferException
import com.google.protobuf.kotlin.toByteString
import io.cloudevents.v1.proto.CloudEvent
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.eclipse.uprotocol.transport.builder.UAttributesBuilder
import org.eclipse.uprotocol.uri.serializer.LongUriSerializer
import org.eclipse.uprotocol.v1.*
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import java.util.concurrent.CompletionException


internal class RpcTest {
    private var returnsNumber3: RpcClient = object : RpcClient {
        override fun invokeMethod(topic: UUri, payload: UPayload, attributes: UAttributes): Flow<Result<UPayload>> {
            return flowOf(Result.success(uPayload {
                format = UPayloadFormat.UPAYLOAD_FORMAT_PROTOBUF
                value = Any.pack(Int32Value.of(3)).toByteString()
            }))
        }
    }
    private var happyPath: RpcClient = object : RpcClient {
        override fun invokeMethod(topic: UUri, payload: UPayload, attributes: UAttributes): Flow<Result<UPayload>> {
            return flowOf(Result.success(buildUPayload()))
        }
    }
    private var withStatusCodeInsteadOfHappyPath: RpcClient = object : RpcClient {
        override fun invokeMethod(topic: UUri, payload: UPayload, attributes: UAttributes): Flow<Result<UPayload>> {
            val status: UStatus = uStatus {
                code = UCode.INVALID_ARGUMENT
                message = "boom"
            }
            val any: Any = Any.pack(status)
            return flowOf(Result.success(uPayload {
                format = UPayloadFormat.UPAYLOAD_FORMAT_PROTOBUF
                value = any.toByteString()
            }))
        }
    }
    private var withStatusCodeHappyPath: RpcClient = object : RpcClient {
        override fun invokeMethod(topic: UUri, payload: UPayload, attributes: UAttributes): Flow<Result<UPayload>> {
            val status: UStatus = uStatus {
                code = UCode.OK
                message = "all good"
            }
            val any: Any = Any.pack(status)
            return flowOf(Result.success(uPayload {
                format = UPayloadFormat.UPAYLOAD_FORMAT_PROTOBUF
                value = any.toByteString()
            }))
        }
    }
    private var thatBarfsCrapyPayload: RpcClient = object : RpcClient {
        override fun invokeMethod(topic: UUri, payload: UPayload, attributes: UAttributes): Flow<Result<UPayload>> {
            return flowOf(Result.success(uPayload {
                format = UPayloadFormat.UPAYLOAD_FORMAT_RAW
                value = byteArrayOf(0).toByteString()
            }))
        }
    }
    private var thatCompletesWithAnException: RpcClient = object : RpcClient {
        override fun invokeMethod(topic: UUri, payload: UPayload, attributes: UAttributes): Flow<Result<UPayload>> {
            return flowOf(Result.failure(RuntimeException("Boom")))
        }
    }
    private var thatReturnsTheWrongProto: RpcClient = object : RpcClient {
        override fun invokeMethod(topic: UUri, payload: UPayload, attributes: UAttributes): Flow<Result<UPayload>> {
            val any: Any = Any.pack(Int32Value.of(42))
            return flowOf(Result.success(uPayload {
                format = UPayloadFormat.UPAYLOAD_FORMAT_PROTOBUF
                value = any.toByteString()
            }))
        }
    }

    @Test
    fun test_compose_happy_path() = runTest {
        val payload: UPayload = buildUPayload()
        returnsNumber3.invokeMethod(buildTopic(), payload, buildUAttributes()).first()
            .mapResponse(Int32Value::class.java).map {
            Int32Value.of(it.value + 5)
        }.run {
            assertTrue(isSuccess)
            assertEquals(Int32Value.of(8), getOrNull())
        }
    }

    @Test
    fun test_compose_that_returns_status() = runTest {
        val payload: UPayload = buildUPayload()
        withStatusCodeInsteadOfHappyPath.invokeMethod(buildTopic(), payload, buildUAttributes()).first()
            .mapResponse(Int32Value::class.java).map {
                Int32Value.of(it.value + 5)
            }.run {
                assertTrue(isFailure)
                assertThrows(RuntimeException::class.java) {
                    getOrThrow()
                }
            }
    }


    @Test
    fun test_success_invoke_method_happy_flow_using_mapResponse() = runTest {
        val payload: UPayload = buildUPayload()
        happyPath.invokeMethod(buildTopic(), payload, buildUAttributes()).first().mapResponse(CloudEvent::class.java)
            .run {
                assertTrue(isSuccess)
                assertEquals(buildCloudEvent(), getOrNull())
            }
    }

    @Test
    fun test_fail_invoke_method_when_invoke_method_returns_a_status_using_mapResponse() = runTest {
        val payload: UPayload = buildUPayload()
        withStatusCodeInsteadOfHappyPath.invokeMethod(
            buildTopic(), payload, buildUAttributes()
        ).first().mapResponse(CloudEvent::class.java).run {
            assertTrue(isFailure)
            assertThrows(RuntimeException::class.java) {
                getOrThrow()
            }
            assertEquals(
                "Unknown payload type [type.googleapis.com/uprotocol.v1.UStatus]. Expected " + "[io.cloudevents.v1.proto.CloudEvent]",
                exceptionOrNull()?.message,
            )

        }
    }

    @Test
    fun test_fail_invoke_method_when_invoke_method_threw_an_exception_using_mapResponse() = runTest {
        val payload: UPayload = buildUPayload()
        thatCompletesWithAnException.invokeMethod(buildTopic(), payload, buildUAttributes()).first()
            .mapResponse(CloudEvent::class.java).run {
                assertTrue(isFailure)
                assertThrows(CompletionException::class.java) {
                    getOrThrow()
                }
                assertEquals(
                    "Boom",
                    exceptionOrNull()?.message,
                )
            }
    }

    @Test
    fun test_fail_invoke_method_when_invoke_method_returns_a_bad_proto_using_mapResponse() = runTest {
        val payload: UPayload = buildUPayload()
        thatReturnsTheWrongProto.invokeMethod(buildTopic(), payload, buildUAttributes()).first()
            .mapResponse(CloudEvent::class.java).run {
                assertTrue(isFailure)
                assertThrows(RuntimeException::class.java) {
                    getOrThrow()
                }
                assertEquals(
                    "Unknown payload type [type.googleapis.com/google.protobuf.Int32Value]. Expected [io.cloudevents.v1.proto.CloudEvent]",
                    exceptionOrNull()?.message,
                )
            }
    }

    @Test
    @DisplayName("Invoke method that expects a UStatus payload and returns successfully with OK UStatus in the payload")
    fun test_success_invoke_method_happy_flow_that_returns_status_using_mapResponse() = runTest {
        val payload: UPayload = buildUPayload()
        withStatusCodeHappyPath.invokeMethod(buildTopic(), payload, buildUAttributes()).first()
            .mapResponse(UStatus::class.java)
            .run {
                assertTrue(isSuccess)
                assertEquals(UCode.OK, getOrNull()?.code)
                assertEquals("all good", getOrNull()?.message)
            }
    }

    @Test
    @DisplayName("test invalid payload that is not of type any")
    fun test_invalid_payload_that_is_not_type_any() = runTest {
        val payload: UPayload = buildUPayload()
        thatBarfsCrapyPayload.invokeMethod(buildTopic(), payload, buildUAttributes()).first()
            .mapResponse(UStatus::class.java)
            .run {
                assertTrue(isFailure)
                assertThrows(InvalidProtocolBufferException::class.java) {
                    getOrThrow()
                }
                assertEquals(
                    "Protocol message contained an invalid tag (zero).",
                    exceptionOrNull()?.message,
                )
            }
    }

    @Test
    fun test_ustatus_payload_happypath_using_mapUStatusOrNull() = runTest {
        val payload: UPayload = buildUPayload()
        withStatusCodeHappyPath.invokeMethod(buildTopic(), payload, buildUAttributes()).first().mapUStatus().run {
            assertNotNull(this)
            assertEquals(UCode.OK, code)
            assertEquals("all good", message)
        }
    }

    @Test
    fun test_non_ustatus_payload_using_mapUStatusOrNull() = runTest {
        val payload: UPayload = buildUPayload()
        happyPath.invokeMethod(buildTopic(), payload, buildUAttributes()).first().mapUStatus().run {
            assertNotNull(this)
            assertEquals(UCode.UNKNOWN, code)
            assertEquals(
                "Unknown payload type [type.googleapis.com/io.cloudevents.v1.CloudEvent]. Expected [org.eclipse.uprotocol.v1.UStatus]",
                message
            )
        }
    }

    @Test
    fun test_exception_using_mapUStatusOrNull() = runTest {
        val payload: UPayload = buildUPayload()
        thatCompletesWithAnException.invokeMethod(buildTopic(), payload, buildUAttributes()).first().mapUStatus().run {
            assertNotNull(this)
            assertEquals(UCode.UNKNOWN, code)
            assertEquals("Boom", message)
        }
    }

    @Test
    fun test_ustatus_payload_happypath_using_getUStatusOrNull() = runTest {
        Result.success(uStatus {
            code = UCode.OK
            message = "test message"
        }).getUStatusOrNull().run {
            assertNotNull(this)
            assertEquals(UCode.OK, this?.code)
            assertEquals("test message", this?.message)
        }
    }

    @Test
    fun test_non_ustatus_payload_using_getUStatusOrNull() = runTest {
        Result.success(buildCloudEvent()).getUStatusOrNull().run {
            assertNull(this)
        }
    }

    private fun buildCloudEvent(): CloudEvent {
        return CloudEvent.newBuilder().setSpecVersion("1.0").setId("HARTLEY IS THE BEST")
            .setSource("https://example.com").build()
    }

    private fun buildUPayload(): UPayload {
        val any: Any = Any.pack(buildCloudEvent())
        return uPayload {
            format = UPayloadFormat.UPAYLOAD_FORMAT_PROTOBUF
            value = any.toByteString()
        }


    }

    private fun buildTopic(): UUri {
        return LongUriSerializer.instance().deserialize("//vcu.vin/hartley/1/rpc.Raise")
    }

    private fun buildUAttributes(): UAttributes {
        return UAttributesBuilder.request(
            UPriority.UPRIORITY_CS4, uUri { entity = uEntity { name = "hartley" } }, 1000
        ).build()
    }
}