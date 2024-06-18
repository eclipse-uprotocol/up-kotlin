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
package org.eclipse.uprotocol.uri.validator

import org.eclipse.uprotocol.v1.*
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test


class UriValidatorTest {
    @Test
    @DisplayName("Test isEmpty with default UUri")
    fun test_isEmpty_with_default_UUri() {
        assertTrue(UUri.getDefaultInstance().isEmpty())
    }

    @Test
    @DisplayName("Test isEmpty for non empty UUri")
    fun test_isEmpty_for_non_empty_UUri() {
        val uri = UUri.newBuilder()
            .setAuthorityName("myAuthority")
            .setUeId(0)
            .setUeVersionMajor(1)
            .setResourceId(1).build()
        assertFalse(uri.isEmpty())
        assertTrue(uri.isNotEmpty())
    }

    @Test
    @DisplayName("Test isEmpty UUri for empty built UUri")
    fun test_isEmpty_UUri_for_empty_built_UUri() {
        val uri = UUri.newBuilder().build()
        assertTrue(uri.isEmpty())
    }

    @Test
    @DisplayName("Test isRpcMethod with default UUri")
    fun test_isRpcMethod_with_default_UUri() {
        assertFalse(UUri.getDefaultInstance().isRpcMethod())
    }

    @Test
    @DisplayName("Test isRpcMethod with UUri having resourceId less than MIN_TOPIC_ID")
    fun test_isRpcMethod_with_UUri_having_resourceId_less_than_MIN_TOPIC_ID() {
        val uri = UUri.newBuilder()
            .setResourceId(0x7FFF).build()
        assertTrue(uri.isRpcMethod())
    }

    @Test
    @DisplayName("Test isRpcMethod with UUri having resourceId greater than MIN_TOPIC_ID")
    fun test_isRpcMethod_with_UUri_having_resourceId_greater_than_MIN_TOPIC_ID() {
        val uri = UUri.newBuilder()
            .setResourceId(0x8000).build()
        assertTrue(!uri.isRpcMethod())
    }

    @Test
    @DisplayName("Test isRpcMethod with UUri having resourceId equal to MIN_TOPIC_ID")
    fun test_isRpcMethod_with_UUri_having_resourceId_equal_to_MIN_TOPIC_ID() {
        val uri = UUri.newBuilder()
            .setResourceId(0x8000).build()
        assertTrue(!uri.isRpcMethod())
    }

    @Test
    @DisplayName("Test isRpcResponse with default UUri")
    fun test_isRpcResponse_with_default_UUri() {
        assertTrue(!UUri.getDefaultInstance().isRpcResponse())
    }

    @Test
    @DisplayName("Test isRpcResponse with UUri having resourceId equal to 0")
    fun test_isRpcResponse_with_UUri_having_resourceId_equal_to_0() {
        val uri = UUri.newBuilder()
            .setAuthorityName("hartley")
            .setUeId(1)
            .setUeVersionMajor(1)
            .setResourceId(0).build()
        assertTrue(uri.isRpcResponse())
    }

    @Test
    @DisplayName("Test isRpcResponse with UUri having resourceId not equal to 0")
    fun test_isRpcResponse_with_UUri_having_resourceId_not_equal_to_0() {
        val uri = UUri.newBuilder()
            .setAuthorityName("hartley")
            .setUeId(1)
            .setUeVersionMajor(1)
            .setResourceId(1).build()
        assertTrue(!uri.isRpcResponse())
    }

    @Test
    @DisplayName("Test isRpcResponse with UUri having resourceId less than 0")
    fun test_isRpcResponse_with_UUri_having_resourceId_less_than_0() {
        val uri = UUri.newBuilder()
            .setResourceId(-1).build()
        assertTrue(!uri.isRpcResponse())
    }

    @Test
    @DisplayName("Test isTopic with default UUri")
    fun test_isTopic_with_default_UUri() {
        assertFalse(UUri.getDefaultInstance().isTopic())
    }

    @Test
    @DisplayName("Test isTopic with UUri having resourceId greater than 0")
    fun test_isTopic_with_UUri_having_resourceId_greater_than_0() {
        val uri = UUri.newBuilder()
            .setResourceId(1).build()
        assertFalse(uri.isTopic())
    }

    @Test
    @DisplayName("Test isTopic with UUri having resourceId greater than 0x8000")
    fun test_isTopic_with_UUri_having_resourceId_greater_than_0x8000() {
        val uri = UUri.newBuilder()
            .setResourceId(0x8001).build()
        assertTrue(uri.isTopic())
    }

    @Test
    @DisplayName("Test isRpcMethod should be false when resourceId is 0")
    fun test_isRpcMethod_should_be_false_when_resourceId_is_0() {
        val uri = UUri.newBuilder()
            .setUeId(1)
            .setResourceId(0).build()
        assertFalse(uri.isRpcMethod())
    }
}
