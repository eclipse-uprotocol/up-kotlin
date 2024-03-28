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
package org.eclipse.uprotocol.rpc


import com.google.protobuf.Any
import com.google.protobuf.Int32Value
import com.google.protobuf.kotlin.toByteString
import io.cloudevents.v1.proto.CloudEvent
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.test.runTest
import org.eclipse.uprotocol.uri.serializer.LongUriSerializer
import org.eclipse.uprotocol.v1.*
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import java.util.concurrent.CompletionException
import kotlin.test.fail


internal class RpcTest {
    private var returnsNumber3: RpcClient = object : RpcClient {
        override fun invokeMethod(methodUri: UUri, requestPayload: UPayload, options: CallOptions): Flow<UMessage> {
            return flowOf(uMessage {
                payload = uPayload {
                    format = UPayloadFormat.UPAYLOAD_FORMAT_PROTOBUF
                    value = Any.pack(Int32Value.of(3)).toByteString()
                }
            })
        }
    }
    private var happyPath: RpcClient = object : RpcClient {
        override fun invokeMethod(methodUri: UUri, requestPayload: UPayload, options: CallOptions): Flow<UMessage> {
            return flowOf(testUMessage)
        }
    }
    private var withStatusCodeInsteadOfHappyPath: RpcClient = object : RpcClient {
        override fun invokeMethod(methodUri: UUri, requestPayload: UPayload, options: CallOptions): Flow<UMessage> {
            val status: UStatus = uStatus {
                code = UCode.INVALID_ARGUMENT
                message = "boom"
            }
            val any: Any = Any.pack(status)
            return flowOf(uMessage {
                payload = uPayload {
                    format = UPayloadFormat.UPAYLOAD_FORMAT_PROTOBUF
                    value = any.toByteString()
                }
            })
        }
    }
    private var withStatusCodeHappyPath: RpcClient = object : RpcClient {
        override fun invokeMethod(methodUri: UUri, requestPayload: UPayload, options: CallOptions): Flow<UMessage> {
            val status: UStatus = uStatus {
                code = UCode.OK
                message = "all good"
            }
            val any: Any = Any.pack(status)
            return flowOf(uMessage {
                payload = uPayload {
                    format = UPayloadFormat.UPAYLOAD_FORMAT_PROTOBUF
                    value = any.toByteString()
                }
            })
        }
    }
    private var thatBarfsCrapyPayload: RpcClient = object : RpcClient {
        override fun invokeMethod(methodUri: UUri, requestPayload: UPayload, options: CallOptions): Flow<UMessage> {
            return flowOf(uMessage {
                payload = uPayload {
                    format = UPayloadFormat.UPAYLOAD_FORMAT_RAW
                    value = byteArrayOf(0).toByteString()
                }
            })
        }
    }
    private var thatFlowWithAnException: RpcClient = object : RpcClient {
        override fun invokeMethod(methodUri: UUri, requestPayload: UPayload, options: CallOptions): Flow<UMessage> {
            return flow {
                throw RuntimeException("Boom")
            }
        }
    }
    private var thatReturnsTheWrongProto: RpcClient = object : RpcClient {
        override fun invokeMethod(methodUri: UUri, requestPayload: UPayload, options: CallOptions): Flow<UMessage> {
            val any: Any = Any.pack(Int32Value.of(42))
            return flowOf(uMessage {
                payload = uPayload {
                    format = UPayloadFormat.UPAYLOAD_FORMAT_PROTOBUF
                    value = any.toByteString()
                }
            })
        }
    }

    private var thatReturnsUMessageWithoutPayload: RpcClient = object : RpcClient {
        override fun invokeMethod(methodUri: UUri, requestPayload: UPayload, options: CallOptions): Flow<UMessage> {
            return flowOf(uMessage {
            })
        }
    }

    @Test
    fun test_compose_happy_path() = runTest {
        val payload: UPayload = buildUPayload()
        returnsNumber3.invokeMethod(buildTopic(), payload, buildUCallOptions()).toResponse<Int32Value>().map {
            Int32Value.of(it.value + 5)
        }.first().run {
            assertEquals(Int32Value.of(8), this)
        }
    }

    @Test
    fun test_compose_that_returns_status() = runTest {
        val payload: UPayload = buildUPayload()
        try {
            withStatusCodeInsteadOfHappyPath.invokeMethod(buildTopic(), payload, buildUCallOptions())
                .toResponse<Int32Value>().map {
                    Int32Value.of(it.value + 5)
                }.first()
            fail("should not reach here")
        } catch (e: Exception) {
            assertThrows(RuntimeException::class.java) {
                throw e
            }
        }
    }

    @Test
    fun test_success_invoke_method_happy_flow_using_toResponse() = runTest {
        val payload: UPayload = buildUPayload()
        happyPath.invokeMethod(buildTopic(), payload, buildUCallOptions()).toResponse<CloudEvent>().first().run {
            assertEquals(buildCloudEvent(), this)
        }
    }

    @Test
    fun test_fail_invoke_method_when_invoke_method_returns_a_status_using_toResponse() = runTest {
        val payload: UPayload = buildUPayload()
        try {
            withStatusCodeInsteadOfHappyPath.invokeMethod(buildTopic(), payload, buildUCallOptions())
                .toResponse<CloudEvent>().first()
            fail("should not reach here")
        } catch (e: Exception) {
            assertThrows(RuntimeException::class.java) {
                throw e
            }
            assertEquals(
                "Unknown payload type [type.googleapis.com/uprotocol.v1.UStatus]. Expected " + "[io.cloudevents.v1.proto.CloudEvent]",
                e.message,
            )
        }
    }

    @Test
    fun test_fail_invoke_method_when_invoke_method_threw_an_exception_using_toResponse() = runTest {
        val payload: UPayload = buildUPayload()
        try {
            thatFlowWithAnException.invokeMethod(buildTopic(), payload, buildUCallOptions())
                .toResponse<CloudEvent>().first()
            fail("should not reach here")
        } catch (e: Exception) {
            assertThrows(CompletionException::class.java) {
                throw e
            }
            assertEquals(
                "Boom",
                e.message,
            )
        }
    }

    @Test
    fun test_fail_invoke_method_when_invoke_method_returns_a_bad_proto_using_toResponse() = runTest {
        val payload: UPayload = buildUPayload()
        try {
            thatReturnsTheWrongProto.invokeMethod(buildTopic(), payload, buildUCallOptions()).toResponse<CloudEvent>()
                .first()
            fail("should not reach here")
        } catch (e: Exception) {
            assertThrows(RuntimeException::class.java) {
                throw e
            }
            assertEquals(
                "Unknown payload type [type.googleapis.com/google.protobuf.Int32Value]. Expected [io.cloudevents.v1.proto.CloudEvent]",
                e.message,
            )
        }
    }

    @Test
    @DisplayName("Invoke method that expects a UStatus payload and returns successfully with OK UStatus in the payload")
    fun test_success_invoke_method_happy_flow_that_returns_status_using_toResponse() = runTest {
        val payload: UPayload = buildUPayload()
        withStatusCodeHappyPath.invokeMethod(buildTopic(), payload, buildUCallOptions()).toResponse<UStatus>().first()
            .run {
                assertEquals(UCode.OK, code)
                assertEquals("all good", message)
            }
    }

    @Test
    @DisplayName("test invalid payload that is not of type any")
    fun test_invalid_payload_that_is_not_type_any() = runTest {
        val payload: UPayload = buildUPayload()
        try {
            thatBarfsCrapyPayload.invokeMethod(buildTopic(), payload, buildUCallOptions()).toResponse<CloudEvent>()
                .first()
            fail("should not reach here")
        } catch (e: Exception) {
            assertThrows(RuntimeException::class.java) {
                throw e
            }
            assertEquals(
                "Protocol message contained an invalid tag (zero). [org.eclipse.uprotocol.v1.UStatus]",
                e.message,
            )
        }
    }

    @Test
    fun test_no_payload_in_umessage() = runTest {
        val payload: UPayload = buildUPayload()
        try {
            thatReturnsUMessageWithoutPayload.invokeMethod(buildTopic(), payload, buildUCallOptions())
                .toResponse<CloudEvent>()
                .first()
            fail("should not reach here")
        } catch (e: Exception) {
            assertThrows(RuntimeException::class.java) {
                throw e
            }
            assertEquals(
                "Server returned a null payload. Expected [io.cloudevents.v1.proto.CloudEvent]",
                e.message,
            )
        }
    }


    @Test
    fun test_toResult_nonUStatus_happy_path() = runTest {
        val payload = buildUPayload()
        returnsNumber3.invokeMethod(buildTopic(), payload, buildUCallOptions()).toResult<Int32Value>().first().run {
            assertTrue(isSuccess)
            assertEquals(Int32Value.of(3), getOrNull())
        }
    }

    @Test
    fun test_toResult_uStatus_happy_path() = runTest {
        val payload = buildUPayload()
        withStatusCodeHappyPath.invokeMethod(buildTopic(), payload, buildUCallOptions()).toResult<UStatus>().first()
            .run {
                assertTrue(isSuccess)
                assertEquals(UCode.OK, getOrNull()?.code)
                assertEquals("all good", getOrNull()?.message)

            }
    }

    @Test
    fun test_toResult_uStatus_not_ok() = runTest {
        val payload = buildUPayload()
        withStatusCodeInsteadOfHappyPath.invokeMethod(buildTopic(), payload, buildUCallOptions()).toResult<UStatus>()
            .first().run {
                assertTrue(isFailure)
                assertTrue(exceptionOrNull() is IllegalStateException)
                assertEquals("boom, UStatus: INVALID_ARGUMENT", exceptionOrNull()?.message)
            }
    }

    @Test
    fun test_toResult_no_payload_in_umessage() = runTest {
        val payload: UPayload = buildUPayload()
        thatReturnsUMessageWithoutPayload.invokeMethod(buildTopic(), payload, buildUCallOptions())
            .toResult<CloudEvent>()
            .first().run {
                assertTrue(isFailure)
                assertTrue(exceptionOrNull() is RuntimeException)
                assertEquals(
                    "Server returned a null payload. Expected [io.cloudevents.v1.proto.CloudEvent]",
                    exceptionOrNull()?.message
                )
            }
    }


    @Test
    fun test_fail_invoke_method_when_invoke_method_threw_an_exception_using_toResult() = runTest {
        val payload: UPayload = buildUPayload()
        thatFlowWithAnException.invokeMethod(buildTopic(), payload, buildUCallOptions())
            .toResult<CloudEvent>().first().run {
                assertTrue(isFailure)
                assertTrue(exceptionOrNull() is CompletionException)
                assertEquals(
                    "Boom",
                    exceptionOrNull()?.message
                )
            }
    }

    @Test
    fun test_fail_invoke_method_when_invoke_method_returns_a_status_using_toResult() = runTest {
        val payload: UPayload = buildUPayload()
        withStatusCodeInsteadOfHappyPath.invokeMethod(buildTopic(), payload, buildUCallOptions())
            .toResult<CloudEvent>().first().run {
                assertTrue(isFailure)
                assertTrue(exceptionOrNull() is RuntimeException)
                assertEquals(
                    "Unknown payload type [type.googleapis.com/uprotocol.v1.UStatus]. Expected [io.cloudevents.v1.proto.CloudEvent]",
                    exceptionOrNull()?.message
                )
            }
    }

    @Test
    @DisplayName("test toResult with invalid payload that is not of type any")
    fun test_invalid_payload_that_is_not_type_any_toResult() = runTest {
        val payload: UPayload = buildUPayload()
        thatBarfsCrapyPayload.invokeMethod(buildTopic(), payload, buildUCallOptions())
            .toResult<CloudEvent>().first().run {
                assertTrue(isFailure)
                assertTrue(exceptionOrNull() is RuntimeException)
                assertEquals(
                    "Protocol message contained an invalid tag (zero). [org.eclipse.uprotocol.v1.UStatus]",
                    exceptionOrNull()?.message
                )
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

    private val testUMessage = uMessage {
        payload = buildUPayload()
    }

    private fun buildTopic(): UUri {
        return LongUriSerializer.INSTANCE.deserialize("//vcu.vin/hartley/1/rpc.Raise")
    }

    private fun buildUCallOptions(): CallOptions {
        return callOptions {  }
    }
}