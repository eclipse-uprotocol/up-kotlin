/**
 * SPDX-FileCopyrightText: 2024 Contributors to the Eclipse Foundation
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information regarding copyright ownership.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Apache License Version 2.0 which is available at
 * https://www.apache.org/licenses/LICENSE-2.0
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package org.eclipse.uprotocol.communication

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.eclipse.uprotocol.transport.forNotification
import org.eclipse.uprotocol.transport.forRequest
import org.eclipse.uprotocol.v1.*
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

@ExperimentalCoroutinesApi
class InMemoryRpcServerTest {
    private val testDispatcher = UnconfinedTestDispatcher()
    private val testScope = TestScope(testDispatcher)

    @Test
    @DisplayName("Test registering and unregister a request listener")
    fun test_registering_request_listener() = runTest {
        val handler = RequestHandler { UPayload.EMPTY }
        val method = createMethodUri()
        val server: RpcServer = InMemoryRpcServer(TestUTransport())
        val result = server.registerRequestHandler(method, handler)
        assertEquals(UCode.OK, result.code)

        // second time should return an error
        val result2 = server.unregisterRequestHandler(method, handler)
        assertEquals(UCode.OK, result2.code)
    }

    @Test
    @DisplayName("Test registering twice the same request handler for the same method")
    fun test_registering_twice_the_same_request_handler() = runTest {
        val handler = RequestHandler { UPayload.EMPTY }
        val server: RpcServer = InMemoryRpcServer(TestUTransport())
        server.registerRequestHandler(createMethodUri(), handler)
        val result = server.registerRequestHandler(createMethodUri(), handler)

        assertEquals(UCode.ALREADY_EXISTS, result.code)

    }

    @Test
    @DisplayName("Test unregistering a request handler that wasn't registered already")
    fun test_unregistering_non_registered_request_handler() = runTest {
        val handler = RequestHandler { UPayload.EMPTY }
        val server: RpcServer = InMemoryRpcServer(TestUTransport())
        val result = server.unregisterRequestHandler(createMethodUri(), handler)
        assertEquals(UCode.NOT_FOUND, result.code)
    }

    @Test
    @DisplayName("Test register a request handler where authority does not match the transport source authority")
    fun test_registering_request_listener_with_wrong_authority() = runTest {
        val handler = RequestHandler { UPayload.EMPTY }
        val server: RpcServer = InMemoryRpcServer(TestUTransport())
        val method = uUri {
            authorityName = "Steven"
            ueId = 4
            ueVersionMajor = 1
            resourceId = 3
        }
        val result = server.registerRequestHandler(method, handler)
        assertEquals(UCode.INVALID_ARGUMENT, result.code)
        assertEquals("Method URI does not match the transport source URI", result.message)
    }

    @Test
    @DisplayName("Test register a request handler where ue_id does not match the transport source ue)_id")
    fun test_registering_request_listener_with_wrong_ue_id() = runTest {
        val handler = RequestHandler { UPayload.EMPTY }
        val server: RpcServer = InMemoryRpcServer(TestUTransport())
        val method = uUri {
            authorityName = "Hartley"
            ueId = 5
            ueVersionMajor = 1
            resourceId = 3

        }
        val result = server.registerRequestHandler(method, handler)

        assertEquals(UCode.INVALID_ARGUMENT, result.code)
        assertEquals("Method URI does not match the transport source URI", result.message)

    }

    @Test
    @DisplayName(
        "Test register request handler where ue_version_major does not " +
                "match the transport source ue_version_major"
    )
    fun test_registering_request_listener_with_wrong_ue_version_major() = runTest {
        val handler = RequestHandler { UPayload.EMPTY }
        val server: RpcServer = InMemoryRpcServer(TestUTransport())
        val method = uUri {
            authorityName = "Hartley"
            ueId = 4
            ueVersionMajor = 2
            resourceId = 3
        }
        val result = server.registerRequestHandler(method, handler)

        assertEquals(UCode.INVALID_ARGUMENT, result.code)
        assertEquals("Method URI does not match the transport source URI", result.message)

    }

    @Test
    @DisplayName("Test unregister requesthandler where authority not match the transport source URI")
    fun test_unregistering_request_handler_with_wrong_authority() = runTest {
        val handler = RequestHandler { UPayload.EMPTY }
        val server: RpcServer = InMemoryRpcServer(TestUTransport())
        val method = uUri {
            authorityName = "Steven"
            ueId = 4
            ueVersionMajor = 1
            resourceId = 3
        }

        val result = server.unregisterRequestHandler(method, handler)

        assertEquals(UCode.INVALID_ARGUMENT, result.code)
        assertEquals("Method URI does not match the transport source URI", result.message)
    }


    @Test
    @DisplayName("Test unregister request handler where ue_id does not match the transport source URI")
    fun test_unregistering_request_handler_with_wrong_ue_id() = runTest {
        val handler = RequestHandler { UPayload.EMPTY }
        val server: RpcServer = InMemoryRpcServer(TestUTransport())
        val method = uUri {
            authorityName = "Hartley"
            ueId = 5
            ueVersionMajor = 1
            resourceId = 3
        }
        val result = server.unregisterRequestHandler(method, handler)

        assertEquals(UCode.INVALID_ARGUMENT, result.code)
        assertEquals("Method URI does not match the transport source URI", result.message)
    }

    @Test
    @DisplayName("Test unregister request handler where ue_version_major does not match the transport source URI")
    fun test_unregistering_request_handler_with_wrong_ue_version_major() = runTest {
        val handler = RequestHandler { UPayload.EMPTY }
        val server: RpcServer = InMemoryRpcServer(TestUTransport())
        val method = uUri {
            authorityName = "Hartley"
            ueId = 4
            ueVersionMajor = 2
            resourceId = 3
        }

        val result = server.unregisterRequestHandler(method, handler)

        assertEquals(UCode.INVALID_ARGUMENT, result.code)
        assertEquals("Method URI does not match the transport source URI", result.message)
    }


    @Test
    @DisplayName("Test register a request handler when we use the ErrorUTransport that returns an error")
    fun test_registering_request_listener_with_error_transport() = runTest {
        val handler = RequestHandler { UPayload.EMPTY }
        val server: RpcServer = InMemoryRpcServer(ErrorUTransport())
        val result = server.registerRequestHandler(createMethodUri(), handler)
        assertEquals(UCode.FAILED_PRECONDITION, result.code)
    }

    @Test
    @DisplayName("Test handleRequests the handler triggered an exception")
    fun test_handleRequests_exception() = testScope.runTest {
        // test transport that will trigger the handleRequest()
        val transport = EchoUTransport(testDispatcher)
        val handler = RequestHandler {
            throw UStatusException(
                uStatus {
                    code = UCode.FAILED_PRECONDITION
                    message = "failed!"
                }
            )
        }

        val server: RpcServer = InMemoryRpcServer(transport)

        val method = createMethodUri()

        server.registerRequestHandler(method, handler)

        transport.send(uMessage {
            forRequest(transport.getSource(), method, 1000)
        })
        delay(100) // wait for the scope to launch
        assertEquals(UCode.FAILED_PRECONDITION, transport.receivedResponse.last().attributes.commstatus)
        assertEquals(UPayload.EMPTY.data, transport.receivedResponse.last().payload)
        assertEquals(UPayload.EMPTY.format, transport.receivedResponse.last().attributes.payloadFormat)
    }

    @Test
    @DisplayName("Test handleRequests the handler triggered an unknown exception")
    fun test_handleRequests_unknown_exception() = testScope.runTest {
        // test transport that will trigger the handleRequest()
        val transport = EchoUTransport(testDispatcher)
        val handler = RequestHandler {
            throw UnsupportedOperationException()
        }

        val server: RpcServer = InMemoryRpcServer(transport)

        val method = createMethodUri()

        server.registerRequestHandler(method, handler)

        transport.send(uMessage {
            forRequest(transport.getSource(), method, 1000)
        })
        delay(100) // wait for the scope to launch
        assertEquals(UCode.INTERNAL, transport.receivedResponse.last().attributes.commstatus)
        assertEquals(UPayload.EMPTY.data, transport.receivedResponse.last().payload)
        assertEquals(UPayload.EMPTY.format, transport.receivedResponse.last().attributes.payloadFormat)
    }

    @Test
    @DisplayName("Test handleRequests when we receive a request for a method that we do not have a registered handler")
    fun test_handleRequests_no_handler() = runTest {
        // test transport that will trigger the handleRequest()
        val transport = EchoUTransport()
        val handler = RequestHandler { throw UnsupportedOperationException("this should not be called") }

        val server: RpcServer = InMemoryRpcServer(transport)
        val method = createMethodUri()
        val method2 = method.copy {
            resourceId = 69
        }
        val result = server.registerRequestHandler(method, handler)
        assertEquals(UCode.OK, result.code)

        val status: UStatus = transport.send(uMessage {
            forRequest(transport.getSource(), method2, 1000)
        })
        assertEquals(UCode.OK, status.code)
    }

    @Test
    @DisplayName("Test handling a request where the handler returns a payload and completes successfully")
    fun test_handleRequests_with_payload() = testScope.runTest {
        // test transport that will trigger the handleRequest()
        val transport = EchoUTransport(testDispatcher)

        val handler = RequestHandler { UPayload.EMPTY }

        val server: RpcServer = InMemoryRpcServer(transport)

        val method = createMethodUri()

        server.registerRequestHandler(method, handler)

        val status: UStatus = transport.send(uMessage {
            forRequest(transport.getSource(), method, 1000)
        })
        assertEquals(UCode.OK, status.code)
        delay(100) // wait for the scope to launch
        assertEquals(UPayload.EMPTY.data, transport.receivedResponse.last().payload)
        assertEquals(UPayload.EMPTY.format, transport.receivedResponse.last().attributes.payloadFormat)
    }

    @Test
    @DisplayName("Test handling a request where the handler returns a message with wrong type")
    fun test_handleRequests_with_wrong_type() = testScope.runTest {
        // test transport that will trigger the handleRequest()
        val transport = EchoUTransport(testDispatcher)

        val handler = RequestHandler { UPayload.EMPTY }

        val server: RpcServer = InMemoryRpcServer(transport)

        val method = createMethodUri()

        server.registerRequestHandler(method, handler)

        transport.send(uMessage {
            forNotification(transport.getSource().copy {
                resourceId = 0x8000
            }, createMethodUri().copy {
                resourceId = 0
            })
        })

        assertTrue(transport.receivedResponse.isEmpty())
    }

    // Helper method to create a UUri that matches that of the default TestUTransport
    private fun createMethodUri(): UUri {
        return uUri {
            authorityName = "Hartley"
            ueId = 4
            ueVersionMajor = 1
            resourceId = 3
        }
    }
}