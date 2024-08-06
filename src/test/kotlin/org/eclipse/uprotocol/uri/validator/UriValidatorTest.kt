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

import org.eclipse.uprotocol.uri.serializer.deserializeAsUUri
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
        assertFalse(uri.isNotEmpty())
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

    @Test
    @DisplayName("Matches succeeds for identical URIs")
    fun test_Matches_Succeeds_For_Identical_Uris() {
        val patternUri: UUri = "//authority/A410/3/1003".deserializeAsUUri()
        val candidateUri: UUri = "//authority/A410/3/1003".deserializeAsUUri()
        assertTrue(patternUri.matches(candidateUri))
    }

    @Test
    @DisplayName("Matches succeeds for pattern with wildcard authority")
    fun test_Matches_Succeeds_For_Pattern_With_Wildcard_Authority() {
        val patternUri: UUri = "//*/A410/3/1003".deserializeAsUUri()
        val candidateUri: UUri = "//authority/A410/3/1003".deserializeAsUUri()
        assertTrue(patternUri.matches(candidateUri))
    }

    @Test
    @DisplayName("Matches succeeds for pattern with wildcard authority and local candidate URI")
    fun test_Matches_Succeeds_For_Pattern_With_Wildcard_Authority_And_Local_Candidate_Uri() {
        val patternUri: UUri = "//*/A410/3/1003".deserializeAsUUri()
        val candidateUri: UUri = "/A410/3/1003".deserializeAsUUri()
        assertTrue(patternUri.matches(candidateUri))
    }

    @Test
    @DisplayName("Matches succeeds for pattern with wildcard entity ID")
    fun test_Matches_Succeeds_For_Pattern_With_Wildcard_Entity_Id() {
        val patternUri: UUri = "//authority/FFFF/3/1003".deserializeAsUUri()
        val candidateUri: UUri = "//authority/A410/3/1003".deserializeAsUUri()
        assertTrue(patternUri.matches(candidateUri))
    }

    @Test
    @DisplayName("Matches succeeds for pattern with matching entity instance")
    fun test_Matches_Succeeds_For_Pattern_With_Matching_Entity_Instance() {
        val patternUri: UUri = "//authority/A410/3/1003".deserializeAsUUri()
        val candidateUri: UUri = "//authority/2A410/3/1003".deserializeAsUUri()
        assertTrue(patternUri.matches(candidateUri))
    }

    @Test
    @DisplayName("Matches succeeds for pattern with wildcard entity version")
    fun test_Matches_Succeeds_For_Pattern_With_Wildcard_Entity_Version() {
        val patternUri: UUri = "//authority/A410/FF/1003".deserializeAsUUri()
        val candidateUri: UUri = "//authority/A410/3/1003".deserializeAsUUri()
        assertTrue(patternUri.matches(candidateUri))
    }

    @Test
    @DisplayName("Matches succeeds for pattern with wildcard resource")
    fun test_Matches_Succeeds_For_Pattern_With_Wildcard_Resource() {
        val patternUri: UUri = "//authority/A410/3/FFFF".deserializeAsUUri()
        val candidateUri: UUri = "//authority/A410/3/1003".deserializeAsUUri()
        assertTrue(patternUri.matches(candidateUri))
    }

    @Test
    @DisplayName("Matches fails for upper case authority")
    fun test_Matches_Fail_For_Upper_Case_Authority() {
        val pattern: UUri = "//Authority/A410/3/1003".deserializeAsUUri()
        val candidate: UUri = "//authority/A410/3/1003".deserializeAsUUri()
        assertFalse(pattern.matches(candidate))
    }

    @Test
    @DisplayName("Matches fails for local pattern with authority")
    fun test_Matches_Fail_For_Local_Pattern_With_Authority() {
        val pattern: UUri = "/A410/3/1003".deserializeAsUUri()
        val candidate: UUri = "//authority/A410/3/1003".deserializeAsUUri()
        assertFalse(pattern.matches(candidate))
    }

    @Test
    @DisplayName("Matches fails for different authority")
    fun test_Matches_Fail_For_Different_Authority() {
        val pattern: UUri = "//other/A410/3/1003".deserializeAsUUri()
        val candidate: UUri = "//authority/A410/3/1003".deserializeAsUUri()
        assertFalse(pattern.matches(candidate))
    }

    @Test
    @DisplayName("Matches fails for different entity ID")
    fun test_Matches_Fail_For_Different_Entity_Id() {
        val pattern: UUri = "//authority/45/3/1003".deserializeAsUUri()
        val candidate: UUri = "//authority/A410/3/1003".deserializeAsUUri()
        assertFalse(pattern.matches(candidate))
    }

    @Test
    @DisplayName("Matches fails for different entity instance")
    fun test_Matches_Fail_For_Different_Entity_Instance() {
        val pattern: UUri = "//authority/30A410/3/1003".deserializeAsUUri()
        val candidate: UUri = "//authority/2A410/3/1003".deserializeAsUUri()
        assertFalse(pattern.matches(candidate))
    }

    @Test
    @DisplayName("Matches fails for different entity version")
    fun test_Matches_Fail_For_Different_Entity_Version() {
        val pattern: UUri = "//authority/A410/1/1003".deserializeAsUUri()
        val candidate: UUri = "//authority/A410/3/1003".deserializeAsUUri()
        assertFalse(pattern.matches(candidate))
    }

    @Test
    @DisplayName("Matches fails for different resource")
    fun test_Matches_Fail_For_Different_Resource() {
        val pattern: UUri = "//authority/A410/3/ABCD".deserializeAsUUri()
        val candidate: UUri = "//authority/A410/3/1003".deserializeAsUUri()
        assertFalse(pattern.matches(candidate))
    }

    @Test
    @DisplayName("hasWildcard() for a UUri with empty URI")
    fun test_hasWildcard_for_empty_UUri() {
        assertFalse(UUri.getDefaultInstance().hasWildcard())
    }

    @Test
    @DisplayName("hasWildcard() for a UUri with wildcard authority")
    fun test_hasWildcard_for_UUri_with_wildcard_authority() {
        val uri: UUri = "//*/A410/3/1003".deserializeAsUUri()
        assertTrue(uri.hasWildcard())
    }

    @Test
    @DisplayName("hasWildcard() for a UUri with wildcard entity ID")
    fun test_hasWildcard_for_UUri_with_wildcard_entity_id() {
        val uri: UUri = "//authority/FFFF/3/1003".deserializeAsUUri()
        assertTrue(uri.hasWildcard())
    }

    @Test
    @DisplayName("hasWildcard() for a UUri with wildcard entity version")
    fun test_hasWildcard_for_UUri_with_wildcard_entity_instance() {
        val uri: UUri = "//authority/A410/FF/1003".deserializeAsUUri()
        assertTrue(uri.hasWildcard())
    }

    @Test
    @DisplayName("hasWildcard() for a UUri with wildcard resource")
    fun test_hasWildcard_for_UUri_with_wildcard_resource() {
        val uri: UUri = "//authority/A410/3/FFFF".deserializeAsUUri()
        assertTrue(uri.hasWildcard())
    }

    @Test
    @DisplayName("hasWildcard() for a UUri with no wildcards")
    fun test_hasWildcard_for_UUri_with_no_wildcards() {
        val uri: UUri = "//authority/A410/3/1003".deserializeAsUUri()
        assertFalse(uri.hasWildcard())
    }
}
