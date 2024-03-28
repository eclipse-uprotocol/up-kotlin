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
package org.eclipse.uprotocol.uri.validator

import com.google.protobuf.ByteString
import org.eclipse.uprotocol.uri.serializer.LongUriSerializer
import org.eclipse.uprotocol.v1.*
import org.eclipse.uprotocol.validation.ValidationResult
import org.json.JSONArray
import org.json.JSONObject
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import java.io.BufferedReader
import java.io.File
import java.io.FileReader
import java.io.IOException


@Suppress("KotlinConstantConditions")
internal class UriValidatorTest {
    @Test
    @DisplayName("Test validate uri with no device name")
    fun test_validate_uri_with_no_entity_getName() {
        val uri: UUri = LongUriSerializer.INSTANCE.deserialize("//")
        val status: ValidationResult = uri.validate()
        assertTrue(uri.isEmpty())
        assertEquals("Uri is empty.", status.getMessage())
    }

    @Test
    @DisplayName("Test validate uri with uEntity")
    fun test_validate_uri_with_getEntity() {
        val uri: UUri = LongUriSerializer.INSTANCE.deserialize("/hartley")
        val status: ValidationResult = uri.validate()
        assertEquals(ValidationResult.success(), status)
    }

    @Test
    @DisplayName("Test validate with malformed URI")
    fun test_validate_with_malformed_uri() {
        val uri: UUri = LongUriSerializer.INSTANCE.deserialize("hartley")
        val status: ValidationResult = uri.validate()
        assertTrue(uri.isEmpty())
        assertEquals("Uri is empty.", status.getMessage())
    }

    @Test
    @DisplayName("Test validate with blank UEntity Name")
    fun test_validate_with_blank_uentity_name_uri() {
        val status: ValidationResult = UUri.getDefaultInstance().validate()
        assertTrue(status.isFailure())
        assertEquals("Uri is empty.", status.getMessage())
    }

    @Test
    @DisplayName("Test validateRpcMethod with valid URI")
    fun test_validateRpcMethod_with_valid_uri() {
        val uri: UUri = LongUriSerializer.INSTANCE.deserialize("/hartley//rpc.echo")
        val status: ValidationResult = uri.validateRpcMethod()
        assertEquals(ValidationResult.success(), status)
    }

    @Test
    @DisplayName("Test validateRpcMethod with invalid URI")
    fun test_validateRpcMethod_with_invalid_uri() {
        val uri: UUri = LongUriSerializer.INSTANCE.deserialize("/hartley/echo")
        val status: ValidationResult = uri.validateRpcMethod()
        assertTrue(status.isFailure())
        assertEquals("Uri is empty.", status.getMessage())
    }

    @Test
    @DisplayName("Test validateRpcMethod with malformed URI")
    fun test_validateRpcMethod_with_malformed_uri() {
        val uri: UUri = LongUriSerializer.INSTANCE.deserialize("hartley")
        val status: ValidationResult = uri.validateRpcMethod()
        assertTrue(uri.isEmpty())
        assertEquals("Uri is empty.", status.getMessage())
    }

    @Test
    @DisplayName("Test validateRpcResponse with valid URI")
    fun test_validateRpcResponse_with_valid_uri() {
        val uri: UUri = LongUriSerializer.INSTANCE.deserialize("/hartley//rpc.response")
        val status: ValidationResult = uri.validateRpcResponse()
        assertEquals(ValidationResult.success(), status)
    }

    @Test
    @DisplayName("Test validateRpcResponse with malformed URI")
    fun test_validateRpcResponse_with_malformed_uri() {
        val uri: UUri = LongUriSerializer.INSTANCE.deserialize("hartley")
        val status: ValidationResult = uri.validateRpcResponse()
        assertTrue(uri.isEmpty())
        assertEquals("Uri is empty.", status.getMessage())
    }

    @Test
    @DisplayName("Test validateRpcResponse with rpc type")
    fun test_validateRpcResponse_with_rpc_type() {
        val uri: UUri = LongUriSerializer.INSTANCE.deserialize("/hartley//dummy.wrong")
        val status: ValidationResult = uri.validateRpcResponse()
        assertTrue(status.isFailure())
        assertEquals("Invalid RPC response type.", status.getMessage())
    }

    @Test
    @DisplayName("Test validateRpcResponse with invalid rpc response type")
    fun test_validateRpcResponse_with_invalid_rpc_response_type() {
        val uri: UUri = LongUriSerializer.INSTANCE.deserialize("/hartley//rpc.wrong")
        val status: ValidationResult = uri.validateRpcResponse()
        assertTrue(status.isFailure())
        assertEquals("Invalid RPC response type.", status.getMessage())
    }

    @Test
    @DisplayName("Test validate topic uri with version, when it is valid microRemote")
    fun test_topic_uri_with_version_when_it_is_valid_remote() {
        val uri = "//VCU.MY_CAR_VIN/body.access/1/door.front_left#Door"
        val status: ValidationResult = LongUriSerializer.INSTANCE.deserialize(uri).validate()
        assertTrue(status.isSuccess())
    }

    @Test
    @DisplayName("Test validate topic uri no version, when it is valid microRemote")
    fun test_topic_uri_no_version_when_it_is_valid_remote() {
        val uri = "//VCU.MY_CAR_VIN/body.access//door.front_left#Door"
        val status: ValidationResult = LongUriSerializer.INSTANCE.deserialize(uri).validate()
        assertTrue(status.isSuccess())
    }

    @Test
    @DisplayName("Test validate topic uri with version, when it is valid local")
    fun test_topic_uri_with_version_when_it_is_valid_local() {
        val uri = "/body.access/1/door.front_left#Door"
        val status: ValidationResult = LongUriSerializer.INSTANCE.deserialize(uri).validate()
        assertTrue(status.isSuccess())
    }

    @Test
    @DisplayName("Test validate topic uri no version, when it is valid local")
    fun test_topic_uri_no_version_when_it_is_valid_local() {
        val uri = "/body.access//door.front_left#Door"
        val status: ValidationResult = LongUriSerializer.INSTANCE.deserialize(uri).validate()
        assertTrue(status.isSuccess())
    }

    @Test
    @DisplayName("Test validate topic uri is invalid when uri contains nothing but schema")
    fun test_topic_uri_invalid_when_uri_has_schema_only() {
        val uri = ":"
        val status: ValidationResult = LongUriSerializer.INSTANCE.deserialize(uri).validate()
        assertTrue(status.isFailure())
    }

    @Test
    @DisplayName("Test validate topic uri is invalid when uri contains empty use name local")
    fun test_topic_uri_invalid_when_uri_has_empty_use_name_local() {
        val uri = "/"
        val status: ValidationResult = LongUriSerializer.INSTANCE.deserialize(uri).validate()
        assertTrue(status.isFailure())
    }

    @Test
    @DisplayName("Test validate topic uri is invalid when uri is microRemote but missing authority")
    fun test_topic_uri_invalid_when_uri_is_remote_no_authority() {
        val uri = "//"
        val status: ValidationResult = LongUriSerializer.INSTANCE.deserialize(uri).validate()
        assertTrue(status.isFailure())
    }

    @Test
    @DisplayName("Test validate topic uri is invalid when uri is microRemote with use but missing authority")
    fun test_topic_uri_invalid_when_uri_is_remote_no_authority_with_use() {
        val uri = "///body.access/1/door.front_left#Door"
        val status: ValidationResult = LongUriSerializer.INSTANCE.deserialize(uri).validate()
        assertTrue(status.isFailure())
    }

    @Test
    @DisplayName("Test validate topic uri is invalid when uri has no use information")
    fun test_topic_uri_invalid_when_uri_is_missing_use_remote() {
        val uri = "//VCU.myvin///door.front_left#Door"
        val status: ValidationResult = LongUriSerializer.INSTANCE.deserialize(uri).validate()
        assertTrue(status.isFailure())
    }

    @Test
    @DisplayName("Test validate microRemote topic uri is invalid when uri is missing use name")
    fun test_topic_uri_invalid_when_uri_is_missing_use_name_remote() {
        val uri = "/1/door.front_left#Door"
        val status: ValidationResult = LongUriSerializer.INSTANCE.deserialize(uri).validate()
        assertTrue(status.isFailure())
    }

    @Test
    @DisplayName("Test validate local topic uri is invalid when uri is missing use name")
    fun test_topic_uri_invalid_when_uri_is_missing_use_name_local() {
        val uri = "//VCU.myvin//1"
        val status: ValidationResult = LongUriSerializer.INSTANCE.deserialize(uri).validate()
        assertTrue(status.isFailure())
    }

    @Test
    @DisplayName("Test validate rpc topic uri with version, when it is valid microRemote")
    fun test_rpc_topic_uri_with_version_when_it_is_valid_remote() {
        val uri = "//bo.cloud/petapp/1/rpc.response"
        val status: ValidationResult = LongUriSerializer.INSTANCE.deserialize(uri).validateRpcMethod()
        assertTrue(status.isSuccess())
    }

    @Test
    @DisplayName("Test validate rpc topic uri no version, when it is valid microRemote")
    fun test_rpc_topic_uri_no_version_when_it_is_valid_remote() {
        val uri = "//bo.cloud/petapp//rpc.response"
        val status: ValidationResult = LongUriSerializer.INSTANCE.deserialize(uri).validateRpcMethod()
        assertTrue(status.isSuccess())
    }

    @Test
    @DisplayName("Test validate rpc topic uri with version, when it is valid local")
    fun test_rpc_topic_uri_with_version_when_it_is_valid_local() {
        val uri = "/petapp/1/rpc.response"
        val status: ValidationResult = LongUriSerializer.INSTANCE.deserialize(uri).validateRpcMethod()
        assertTrue(status.isSuccess())
    }

    @Test
    @DisplayName("Test validate rpc topic uri no version, when it is valid local")
    fun test_rpc_topic_uri_no_version_when_it_is_valid_local() {
        val uri = "/petapp//rpc.response"
        val status: ValidationResult = LongUriSerializer.INSTANCE.deserialize(uri).validateRpcMethod()
        assertTrue(status.isSuccess())
    }

    @Test
    @DisplayName("Test validate rpc topic uri is invalid when uri contains nothing but schema")
    fun test_rpc_topic_uri_invalid_when_uri_has_schema_only() {
        val uri = ":"
        val status: ValidationResult = LongUriSerializer.INSTANCE.deserialize(uri).validateRpcMethod()
        assertTrue(status.isFailure())
    }

    @Test
    @DisplayName("Test validate rpc topic uri with version, when it is local but missing rpc.respons")
    fun test_rpc_topic_uri_with_version_when_it_is_not_valid_missing_rpc_response_local() {
        val uri = "/petapp/1/dog"
        val status: ValidationResult = LongUriSerializer.INSTANCE.deserialize(uri).validateRpcMethod()
        assertTrue(status.isFailure())
    }

    @Test
    @DisplayName("Test validate rpc topic uri with version, when it is microRemote but missing rpc.respons")
    fun test_rpc_topic_uri_with_version_when_it_is_not_valid_missing_rpc_response_remote() {
        val uri = "//petapp/1/dog"
        val status: ValidationResult = LongUriSerializer.INSTANCE.deserialize(uri).validateRpcMethod()
        assertTrue(status.isFailure())
    }

    @Test
    @DisplayName("Test validate rpc topic uri is invalid when uri is microRemote but missing authority")
    fun test_rpc_topic_uri_invalid_when_uri_is_remote_no_authority() {
        val uri = "//"
        val status: ValidationResult = LongUriSerializer.INSTANCE.deserialize(uri).validateRpcMethod()
        assertTrue(status.isFailure())
    }

    @Test
    @DisplayName("Test validate rpc topic uri is invalid when uri is microRemote with use but missing authority")
    fun test_rpc_topic_uri_invalid_when_uri_is_remote_no_authority_with_use() {
        val uri = "///body.access/1"
        val status: ValidationResult = LongUriSerializer.INSTANCE.deserialize(uri).validateRpcMethod()
        assertTrue(status.isFailure())
    }

    @Test
    @DisplayName("Test validate rpc topic uri is invalid when uri has no use information")
    fun test_rpc_topic_uri_invalid_when_uri_is_missing_use() {
        val uri = "//VCU.myvin"
        val status: ValidationResult = LongUriSerializer.INSTANCE.deserialize(uri).validateRpcMethod()
        assertTrue(status.isFailure())
    }

    @Test
    @DisplayName("Test validate microRemote rpc topic uri is invalid when uri is missing use name")
    fun test_rpc_topic_uri_invalid_when_uri_is_missing_use_name_remote() {
        val uri = "/1"
        val status: ValidationResult = LongUriSerializer.INSTANCE.deserialize(uri).validateRpcMethod()
        assertTrue(status.isFailure())
    }

    @Test
    @DisplayName("Test validate local rpc topic uri is invalid when uri is missing use name")
    fun test_rpc_topic_uri_invalid_when_uri_is_missing_use_name_local() {
        val uri = "//VCU.myvin//1"
        val status: ValidationResult = LongUriSerializer.INSTANCE.deserialize(uri).validateRpcMethod()
        assertTrue(status.isFailure())
    }

    @Test
    @DisplayName("Test validate rpc method uri with version, when it is valid microRemote")
    fun test_rpc_method_uri_with_version_when_it_is_valid_remote() {
        val uri = "//VCU.myvin/body.access/1/rpc.UpdateDoor"
        val status: ValidationResult = LongUriSerializer.INSTANCE.deserialize(uri).validateRpcMethod()
        assertTrue(status.isSuccess())
    }

    @Test
    @DisplayName("Test validate rpc method uri no version, when it is valid microRemote")
    fun test_rpc_method_uri_no_version_when_it_is_valid_remote() {
        val uri = "//VCU.myvin/body.access//rpc.UpdateDoor"
        val status: ValidationResult = LongUriSerializer.INSTANCE.deserialize(uri).validateRpcMethod()
        assertTrue(status.isSuccess())
    }

    @Test
    @DisplayName("Test validate rpc method uri with version, when it is valid local")
    fun test_rpc_method_uri_with_version_when_it_is_valid_local() {
        val uri = "/body.access/1/rpc.UpdateDoor"
        val status: ValidationResult = LongUriSerializer.INSTANCE.deserialize(uri).validateRpcMethod()
        assertTrue(status.isSuccess())
    }

    @Test
    @DisplayName("Test validate rpc method uri no version, when it is valid local")
    fun test_rpc_method_uri_no_version_when_it_is_valid_local() {
        val uri = "/body.access//rpc.UpdateDoor"
        val status: ValidationResult = LongUriSerializer.INSTANCE.deserialize(uri).validateRpcMethod()
        assertTrue(status.isSuccess())
    }

    @Test
    @DisplayName("Test validate rpc method uri is invalid when uri contains nothing but schema")
    fun test_rpc_method_uri_invalid_when_uri_has_schema_only() {
        val uri = ":"
        val status: ValidationResult = LongUriSerializer.INSTANCE.deserialize(uri).validateRpcMethod()
        assertTrue(status.isFailure())
    }

    @Test
    @DisplayName("Test validate rpc method uri with version, when it is local but not an rpc method")
    fun test_rpc_method_uri_with_version_when_it_is_not_valid_not_rpc_method_local() {
        val uri = "/body.access//UpdateDoor"
        val status: ValidationResult = LongUriSerializer.INSTANCE.deserialize(uri).validateRpcMethod()
        assertTrue(status.isFailure())
    }

    @Test
    @DisplayName("Test validate rpc method uri with version, when it is microRemote but not an rpc method")
    fun test_rpc_method_uri_with_version_when_it_is_not_valid_not_rpc_method_remote() {
        val uri = "//body.access/1/UpdateDoor"
        val status: ValidationResult = LongUriSerializer.INSTANCE.deserialize(uri).validateRpcMethod()
        assertTrue(status.isFailure())
    }

    @Test
    @DisplayName("Test validate rpc method uri is invalid when uri is microRemote but missing authority")
    fun test_rpc_method_uri_invalid_when_uri_is_remote_no_authority() {
        val uri = "//"
        val status: ValidationResult = LongUriSerializer.INSTANCE.deserialize(uri).validateRpcMethod()
        assertTrue(status.isFailure())
    }

    @Test
    @DisplayName("Test validate rpc method uri is invalid when uri is microRemote with use but missing authority")
    fun test_rpc_method_uri_invalid_when_uri_is_remote_no_authority_with_use() {
        val uri = "///body.access/1/rpc.UpdateDoor"
        val uuri: UUri = LongUriSerializer.INSTANCE.deserialize(uri)
        val status: ValidationResult = uuri.validateRpcMethod()
        assertEquals("", uuri.toString())
        assertTrue(status.isFailure())
    }

    @Test
    @DisplayName("Test validate rpc method uri is invalid when uri has authority but missing remote case")
    fun test_rpc_method_uri_invalid_when_uri_is_remote_missing_authority_remotecase() {
        val uuri: UUri = uUri {
            entity = uEntity { name = "body.access" }
            resource = uResource {
                name = "rpc"
                instance = "UpdateDoor"
            }
            authority = uAuthority { }
        }


        val status: ValidationResult = uuri.validateRpcMethod()
        assertTrue(status.isFailure())
        assertEquals("Uri is remote missing uAuthority.", status.getMessage())
    }

    @Test
    @DisplayName("Test validate rpc method uri is invalid when uri has no use information")
    fun test_rpc_method_uri_invalid_when_uri_is_missing_use() {
        val uri = "//VCU.myvin"
        val status: ValidationResult = LongUriSerializer.INSTANCE.deserialize(uri).validateRpcMethod()
        assertTrue(status.isFailure())
    }

    @Test
    @DisplayName("Test validate local rpc method uri is invalid when uri is missing use name")
    fun test_rpc_method_uri_invalid_when_uri_is_missing_use_name_local() {
        val uri = "/1/rpc.UpdateDoor"
        val status: ValidationResult = LongUriSerializer.INSTANCE.deserialize(uri).validateRpcMethod()
        assertTrue(status.isFailure())
    }

    @Test
    @DisplayName("Test validate microRemote rpc method uri is invalid when uri is missing use name")
    fun test_rpc_method_uri_invalid_when_uri_is_missing_use_name_remote() {
        val uri = "//VCU.myvin//1/rpc.UpdateDoor"
        val status: ValidationResult = LongUriSerializer.INSTANCE.deserialize(uri).validateRpcMethod()
        assertTrue(status.isFailure())
    }

    @Test
    @DisplayName("Test all valid uris from uris.json")
    @Throws(IOException::class)
    fun test_all_valid_uris() {
        // Access the "validUris" array
        val validUris: JSONArray = jsonObject.getJSONArray("validUris")
        for (i in 0 until validUris.length()) {
            val uuri: UUri = LongUriSerializer.INSTANCE.deserialize(validUris.getString(i))
            val status: ValidationResult = uuri.validate()
            assertTrue(status.isSuccess())
        }
    }

    @Test
    @DisplayName("Test all invalid uris from uris.json")
    @Throws(IOException::class)
    fun test_all_invalid_uris() {
        // Access the "invalidUris" array
        val invalidUris: JSONArray = jsonObject.getJSONArray("invalidUris")
        for (i in 0 until invalidUris.length()) {
            val uriObject: JSONObject = invalidUris.getJSONObject(i)
            val uuri: UUri = LongUriSerializer.INSTANCE.deserialize(uriObject.getString("uri"))
            val status: ValidationResult = uuri.validate()
            assertTrue(status.isFailure())
            assertEquals(status.getMessage(), uriObject.getString("status_message"))
        }
    }

    @Test
    @DisplayName("Test all valid rpc uris from uris.json")
    @Throws(IOException::class)
    fun test_all_valid_rpc_uris() {
        // Access the "validRpcUris" array
        val validRpcUris: JSONArray = jsonObject.getJSONArray("validRpcUris")
        for (i in 0 until validRpcUris.length()) {
            val uuri: UUri = LongUriSerializer.INSTANCE.deserialize(validRpcUris.getString(i))
            val status: ValidationResult = uuri.validateRpcMethod()
            assertTrue(status.isSuccess())
        }
    }

    @Test
    @DisplayName("Test all invalid rpc uris from uris.json")
    @Throws(IOException::class)
    fun test_all_invalid_rpc_uris() {
        // Access the "invalidRpcUris" array
        val invalidRpcUris: JSONArray = jsonObject.getJSONArray("invalidRpcUris")
        for (i in 0 until invalidRpcUris.length()) {
            val uriObject: JSONObject = invalidRpcUris.getJSONObject(i)
            val uuri: UUri = LongUriSerializer.INSTANCE.deserialize(uriObject.getString("uri"))
            val status: ValidationResult = uuri.validateRpcMethod()
            assertTrue(status.isFailure())
            assertEquals(status.getMessage(), uriObject.getString("status_message"))
        }
    }

    @Test
    @DisplayName("Test all valid rpc response uris from uris.json")
    @Throws(IOException::class)
    fun test_all_valid_rpc_response_uris() {
        // Access the "validRpcResponseUris" array
        val validRpcResponseUris: JSONArray = jsonObject.getJSONArray("validRpcResponseUris")
        for (i in 0 until validRpcResponseUris.length()) {
            val uuri: UUri = LongUriSerializer.INSTANCE.deserialize(validRpcResponseUris.getString(i))
            val status: ValidationResult = uuri.validateRpcResponse()
            assertTrue(uuri.isRpcResponse())
            assertTrue(status.isSuccess())
        }
    }

    @Test
    @DisplayName("Test valid rpc response uri")
    @Throws(IOException::class)
    fun test_valid_rpc_response_uri() {
        val uuri: UUri = uUri {
            entity = uEntity { name = "hartley" }
            resource = uResource {
                forRpcResponse()
            }
        }

        val status: ValidationResult = uuri.validateRpcResponse()
        assertTrue(uuri.isRpcResponse())
        assertTrue(status.isSuccess())
    }

    @Test
    @DisplayName("Test invalid rpc method uri")
    fun test_invalid_rpc_method_uri() {
        val uuri = uUri {
            entity = uEntity {
                name = "hello.world"
            }
            resource = uResource {
                name = "testrpc"
                instance = "SayHello"
            }
        }
        val status: ValidationResult = uuri.validateRpcMethod()
        assertFalse(uuri.isRpcMethod())
        assertFalse(status.isSuccess())
    }

    @Test
    @DisplayName("Test invalid rpc response uri")
    @Throws(IOException::class)
    fun test_invalid_rpc_response_uri() {
        val uuri1: UUri = uUri {
            entity = uEntity { name = "hartley" }
            resource = uResource {
                name = "rpc"
                id = 19999
            }
        }
        val status1 = uuri1.validateRpcResponse()
        assertFalse(uuri1.isRpcResponse())
        assertFalse(status1.isSuccess())

        val uuri2: UUri = uUri {
            entity = uEntity { name = "hartley" }
            resource = uResource {
                name = "testrpc"
                instance = "response"
            }
        }
        val status2 = uuri2.validateRpcResponse()
        assertFalse(uuri2.isRpcResponse())
        assertFalse(status2.isSuccess())

        val uuri3: UUri = uUri {
            entity = uEntity { name = "hartley" }
            resource = uResource {
                name = "rpc"
                instance = "testresponse"
            }
        }
        val status3 = uuri3.validateRpcResponse()
        assertFalse(uuri3.isRpcResponse())
        assertFalse(status3.isSuccess())
    }

    @Test
    @DisplayName("Test invalid rpc response uri")
    @Throws(IOException::class)
    fun test_another_invalid_rpc_response_uri() {
        val uuri: UUri = uUri {
            entity = uEntity { name = "hartley" }
            resource = uResource {
                name = "hello"
                id = 19999
            }
        }

        val status = uuri.validateRpcResponse()
        assertFalse(uuri.isRpcResponse())
        assertFalse(status.isSuccess())
    }

    @Test
    @DisplayName("Test all invalid rpc response uris from uris.json")
    @Throws(IOException::class)
    fun test_all_invalid_rpc_response_uris() {
        // Access the "invalidRpcResponseUris" array
        val invalidRpcResponseUris: JSONArray = jsonObject.getJSONArray("invalidRpcResponseUris")
        for (i in 0 until invalidRpcResponseUris.length()) {
            val uuri: UUri = LongUriSerializer.INSTANCE.deserialize(invalidRpcResponseUris.getString(i))
            val status: ValidationResult = uuri.validateRpcResponse()
            assertTrue(status.isFailure())
        }
    }

    @Test
    @DisplayName("Test is Remote is false for URI without UAuthority")
    fun test_is_remote_is_false_for_uri_without_uauthority() {
        val uri = uUri {
            authority = uAuthority { }
            entity = uEntity { name = "hartley" }
            resource = uResource {
                forRpcResponse()
            }
        }
        assertFalse(UAuthority.getDefaultInstance().isRemote())
        assertFalse(uri.authority.isRemote())
    }

    @Test
    @DisplayName("Test isRpcMethod with UResource and no UAuthority")
    fun test_is_rpc_method_with_uresource_and_no_uauthority() {
        assertFalse(uUri { }.isRpcMethod())

        val uri: UUri = uUri {
            entity = uEntity { name = "hartley" }
            resource = uResource { from(0x8000) }
        }
        assertFalse(uri.isRpcMethod())
    }

    @Test
    @DisplayName("Test isRpcMethod for UResource without an instance")
    fun test_is_rpc_method_for_uresource_without_an_instance() {
        val resource = uResource { name = "rpc" }
        assertFalse(resource.isRpcMethod())
    }

    @Test
    @DisplayName("Test isRpcMethod for UResource an empty instance")
    fun test_is_rpc_method_for_uresource_with_an_empty_instance() {
        val resource = uResource {
            name = "rpc"
            instance = ""
        }
        assertFalse(resource.isRpcMethod())
    }

    @Test
    @DisplayName("Test isRpcMethod for UResource with id that is less than min_topic")
    fun test_is_rpc_method_for_uresource_with_id_that_is_less_than_min_topic() {
        val resource = uResource {
            name = "rpc"
            id = 0
        }
        assertTrue(resource.isRpcMethod())
    }

    @Test
    @DisplayName("Test isRpcMethod for UResource with id that is greater than min_topic")
    fun test_is_rpc_method_for_uresource_with_id_that_is_greater_than_min_topic() {
        val resource = uResource {
            name = "rpc"
            id = 0x8000
        }
        assertFalse(resource.isRpcMethod())
    }

    @Test
    @DisplayName("Test isResolved when URI is long form only")
    fun test_is_resolved_when_uri_is_long_form_only() {
        val uri = uUri {
            entity = uEntity {
                name = "hartley"
                versionMajor = 23
            }
            resource = uResource {
                name = "rpc"
                instance = "echo"
            }
        }
        assertFalse(uri.isResolved())
    }

    @Test
    @DisplayName("Test isResolved when URI is micro form only")
    fun test_is_resolved_when_uri_is_micro_form_only() {
        val uri: UUri = uUri {
            entity = uEntity {
                id = 0
                versionMajor = 23
            }
            resource = uResource { from(0x8000) }
        }
        assertFalse(uri.isResolved())
    }

    @Test
    @DisplayName("Test isResolved when URI is both long and micro form")
    fun test_is_resolved_when_uri_is_both_long_and_micro_form() {
        val uri: UUri = uUri {
            entity = uEntity {
                name = "hartley"
                id = 0
                versionMajor = 23
            }
            resource = uResource { forRpcResponse() }
        }
        assertTrue(uri.isResolved())
    }

    @Test
    @DisplayName("Test isRpcResponse when uri is a valid RPC response")
    fun test_is_rpc_response_when_uri_is_a_valid_rpc_response() {
        val uri: UUri = uUri {
            entity = uEntity {
                name = "hartley"
                id = 0
                versionMajor = 23
            }
            resource = uResource { forRpcResponse() }
        }
        assertTrue(uri.isRpcResponse())
    }

    @Test
    @DisplayName("Test isMicroForm when URI is empty")
    fun test_is_micro_form_when_uri_is_empty() {
        assertFalse(UUri.getDefaultInstance().isMicroForm())
    }

    @Test
    @DisplayName("Test isMicroForm when URI does not have UResource but does have UEntity")
    fun test_is_micro_form_when_uri_does_not_have_uresource_but_does_have_uentity() {
        val uri = uUri {
            entity = uEntity {
                name = "hartley"
                id = 0
                versionMajor = 23
            }
        }

        assertFalse(uri.isMicroForm())
    }

    @Test
    @DisplayName("Test isLongForm when URI is null")
    fun test_is_long_form_when_uri_is_null() {
        assertFalse((null as UAuthority?).isLongForm())
    }

    @Test
    @DisplayName("Test isLongForm when UAuthority is not long form")
    fun test_is_long_form_when_uauthority_is_not_long_form() {
        val uri = uUri {
            entity = uEntity {
                name = "hartley"
                id = 0
                versionMajor = 23
            }
            authority = UAuthority.getDefaultInstance()
        }
        assertFalse(uri.isLongForm())
        assertTrue(uri.authority.isLongForm())
    }

    @Test
    @DisplayName("Test isLongForm when UAuthority is long form but not the rest")
    fun test_is_long_form_when_uauthority_is_long_form_but_not_the_rest() {
        val uri = uUri {
            entity = uEntity {
                name = "hartley"
                id = 0
                versionMajor = 23
            }
            authority = uAuthority { name = "vcu.veh.gm.com" }
        }
        assertFalse(uri.isLongForm())
        assertTrue(uri.authority.isLongForm())
    }

    @Test
    @DisplayName("Test isLongForm when UAuthority blank name")
    fun test_is_long_form_when_uauthority_is_blank_name() {
        val uri = uUri {
            entity = uEntity {
                name = "hartley"
                id = 0
                versionMajor = 23
            }
            authority = uAuthority { name = "" }
        }
        assertFalse(uri.isLongForm())
        assertFalse(uri.authority.isLongForm())
    }

    @Test
    @DisplayName("Test isLongForm when UAuthority is not long form but the rest is")
    fun test_is_long_form_when_uauthority_is_not_long_form_but_the_rest_is() {
           val uri = uUri {
            entity = uEntity {
                name = "hartley"
                id = 0
                versionMajor = 23
            }
            resource = uResource { forRpcResponse() }
            authority = uAuthority { id = ByteString.copyFromUtf8("hello Jello") }
        }
        assertFalse(uri.isLongForm())
        assertFalse(uri.authority.isLongForm())
    }

    @Test
    @DisplayName("Test isLocal when authority is null")
    fun test_is_local_when_authority_is_null() {
        assertFalse((null as UAuthority?).isLocal())
    }

    @Test
    @DisplayName("Test isRemote when authority is null")
    fun test_is_remote_when_authority_is_null() {
        assertFalse((null as UAuthority?).isRemote())
    }

    @Test
    @DisplayName("Test isRemote when authority doesn't have a name but does have a number set")
    fun test_is_remote_when_authority_does_not_have_a_name_but_does_have_a_number_set() {
        val authority = uAuthority {
            id = ByteString.copyFromUtf8("hello Jello")
        }
        assertTrue(authority.isRemote())
        assertFalse(authority.hasName())
        assertEquals(authority.numberCase, UAuthority.NumberCase.ID)
    }

    @Test
    @DisplayName("Test isRemote when authority has name and number set")
    fun test_is_remote_when_authority_has_name_and_number_set() {
        val authority = uAuthority {
            name = "vcu.veh.gm.com"
            id = ByteString.copyFromUtf8("hello Jello")
        }
        assertTrue(authority.isRemote())
        assertTrue(authority.hasName())
        assertEquals(authority.numberCase, UAuthority.NumberCase.ID)
    }

    @Test
    @DisplayName("Test isRemote when authority has name and number is NOT set")
    fun test_is_remote_when_authority_has_name_and_number_is_not_set() {
        val authority = uAuthority {
            name = "vcu.veh.gm.com"
        }
        assertTrue(authority.isRemote())
        assertTrue(authority.hasName())
        assertEquals(authority.numberCase, UAuthority.NumberCase.NUMBER_NOT_SET)
    }

    @get:Throws(IOException::class)
    private val jsonObject: JSONObject
        get() {
            val currentDirectory: String = System.getProperty("user.dir")
            val pkgname: String = this.javaClass.getPackage().name.replace(".", "/")
            val jsonFile = File(
                currentDirectory,
                ((("src" + File.separator) + "test" + File.separator) + "kotlin" + File.separator + pkgname + File.separator) + "uris.json"
            )

            // Open the file for reading
            val reader = BufferedReader(FileReader(jsonFile))
            // Read the JSON data as a string
            val jsonStringBuilder = StringBuilder()
            var line: String?
            while (reader.readLine().also { line = it } != null) {
                jsonStringBuilder.append(line)
            }
            reader.close()
            // Parse the JSON data into a JSONObject
            return JSONObject(jsonStringBuilder.toString())
        }
}
