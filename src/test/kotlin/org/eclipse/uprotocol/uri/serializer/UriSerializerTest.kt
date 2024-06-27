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
package org.eclipse.uprotocol.uri.serializer

import org.eclipse.uprotocol.uri.validator.isEmpty
import org.eclipse.uprotocol.v1.UUri
import org.eclipse.uprotocol.v1.uUri
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test


class UriSerializerTest {
    @Test
    @DisplayName("Test using the serializers")
    fun test_using_the_serializers() {
        val uri = UUri.newBuilder()
            .setAuthorityName("myAuthority")
            .setUeId(1)
            .setUeVersionMajor(2)
            .setResourceId(3)
            .build()

        val serializedUri: String = uri.serialize()
        assertEquals("//myAuthority/1/2/3", serializedUri)
    }

    @Test
    @DisplayName("Test deserializing an empty UUri")
    fun test_deserializing_an_empty_UUri() {
        val uri: UUri = "".deserializeAsUUri()
        assertTrue(uri.isEmpty())
    }


    @Test
    @DisplayName("Test deserializing a blank UUri")
    fun test_deserializing_a_blank_UUri() {
        val uri: UUri = "  ".deserializeAsUUri()
        assertTrue(uri.isEmpty())
    }

    @Test
    @DisplayName("Test deserializing with a valid URI that has scheme")
    fun test_deserializing_with_a_valid_URI_that_has_scheme() {
        val uri: UUri = "up://myAuthority/1/2/3".deserializeAsUUri()
        assertEquals("myAuthority", uri.authorityName)
        assertEquals(1, uri.ueId)
        assertEquals(2, uri.ueVersionMajor)
        assertEquals(3, uri.resourceId)
    }

    @Test
    @DisplayName("Test deserializing with a valid URI that has scheme but nothing else")
    fun test_deserializing_with_a_valid_URI_that_has_scheme_but_nothing_else() {
        val uri: UUri = "up://".deserializeAsUUri()
        assertTrue(uri.isEmpty())
    }

    @Test
    @DisplayName("Test deserializing a valid UUri with all fields")
    fun test_deserializing_a_valid_UUri_with_all_fields() {
        val uri: UUri = "//myAuthority/1/2/3".deserializeAsUUri()
        assertEquals("myAuthority", uri.authorityName)
        assertEquals(1, uri.ueId)
        assertEquals(2, uri.ueVersionMajor)
        assertEquals(3, uri.resourceId)
    }

    @Test
    @DisplayName("Test deserializing a valid UUri with only authority")
    fun test_deserializing_a_valid_UUri_with_only_authority() {
        val uri: UUri = "//myAuthority".deserializeAsUUri()
        assertEquals("myAuthority", uri.authorityName)
        assertEquals(0, uri.ueId)
        assertEquals(0, uri.ueVersionMajor)
        assertEquals(0, uri.resourceId)
    }

    @Test
    @DisplayName("Test deserializing a valid UUri with only authority and ueId")
    fun test_deserializing_a_valid_UUri_with_only_authority_and_ueId() {
        val uri: UUri = "//myAuthority/1".deserializeAsUUri()
        assertEquals("myAuthority", uri.authorityName)
        assertEquals(1, uri.ueId)
        assertEquals(0, uri.ueVersionMajor)
        assertEquals(0, uri.resourceId)
    }

    @Test
    @DisplayName("Test deserializing a valid UUri with only authority, ueId and ueVersionMajor")
    fun test_deserializing_a_valid_UUri_with_only_authority_ueId_and_ueVersionMajor() {
        val uri: UUri = "//myAuthority/1/2".deserializeAsUUri()
        assertEquals("myAuthority", uri.authorityName)
        assertEquals(1, uri.ueId)
        assertEquals(2, uri.ueVersionMajor)
        assertEquals(0, uri.resourceId)
    }

    @Test
    @DisplayName("Test deserializing a string with invalid characters at the beginning")
    fun test_deserializing_a_string_with_invalid_characters() {
        val uri: UUri = "$$".deserializeAsUUri()
        assertTrue(uri.isEmpty())
    }

    @Test
    @DisplayName("Test deserializing a string with names instead of ids for UeId")
    fun test_deserializing_a_string_with_names_instead_of_ids_for_UeId() {
        val uri: UUri = "//myAuthority/myUeId/2/3".deserializeAsUUri()
        assertTrue(uri.isEmpty())
    }

    @Test
    @DisplayName("Test deserializing a string with names instead of ids for UeVersionMajor")
    fun test_deserializing_a_string_with_names_instead_of_ids_for_UeVersionMajor() {
        val uri: UUri = "//myAuthority/1/myUeVersionMajor/3".deserializeAsUUri()
        assertTrue(uri.isEmpty())
    }

    @Test
    @DisplayName("Test deserializing a string with names instead of ids for ResourceId")
    fun test_deserializing_a_string_with_names_instead_of_ids_for_ResourceId() {
        val uri: UUri = "//myAuthority/1/2/myResourceId".deserializeAsUUri()
        assertTrue(uri.isEmpty())
    }

    @Test
    @DisplayName("Test deserializing a string without authority")
    fun test_deserializing_a_string_without_authority() {
        val uri: UUri = "/1/2/3".deserializeAsUUri()
        assertEquals(1, uri.ueId)
        assertEquals(2, uri.ueVersionMajor)
        assertEquals(3, uri.resourceId)
        assertTrue(uri.authorityName.isBlank())
    }

    @Test
    @DisplayName("Test deserializing a string without authority and ResourceId")
    fun test_deserializing_a_string_without_authority_and_ResourceId() {
        val uri: UUri = "/1/2".deserializeAsUUri()
        assertEquals(1, uri.ueId)
        assertEquals(2, uri.ueVersionMajor)
        assertEquals(0, uri.resourceId)
        assertTrue(uri.authorityName.isBlank())
    }

    @Test
    @DisplayName("Test deserializing a string without authority, ResourceId and UeVersionMajor")
    fun test_deserializing_a_string_without_authority_ResourceId_and_UeVersionMajor() {
        val uri: UUri = "/1".deserializeAsUUri()
        assertEquals(1, uri.ueId)
        assertEquals(0, uri.ueVersionMajor)
        assertEquals(0, uri.resourceId)
        assertTrue(uri.authorityName.isBlank())
    }

    @Test
    @DisplayName("Test deserializing a string with blank authority")
    fun test_deserializing_a_string_with_blank_authority() {
        val uri: UUri = "///2".deserializeAsUUri()
        assertTrue(uri.isEmpty())
    }

    @Test
    @DisplayName("Test deserializing a string with all the items are the wildcard values")
    fun test_deserializing_a_string_with_all_the_items_are_the_wildcard_values() {
        val uri: UUri = "//*/FFFF/ff/ffff".deserializeAsUUri()
        assertEquals("*", uri.authorityName)
        assertEquals(0xFFFF, uri.ueId)
        assertEquals(0xFF, uri.ueVersionMajor)
        assertEquals(0xFFFF, uri.resourceId)
    }

    @Test
    @DisplayName("Test deserializing a string with uEId() out of range")
    fun test_deserializing_a_string_with_uEId_out_of_range() {
        val uri: UUri = "/fffffffff/2/3".deserializeAsUUri()
        assertTrue(uri.isEmpty())
    }

    @Test
    @DisplayName("Test deserializing a string with uEVersionMajor out of range")
    fun test_deserializing_a_string_with_uEVersionMajor_out_of_range() {
        val uri: UUri = "/1/256/3".deserializeAsUUri()
        assertTrue(uri.isEmpty())
    }

    @Test
    @DisplayName("Test deserializing a string with resourceId out of range")
    fun test_deserializing_a_string_with_resourceId_out_of_range() {
        val uri: UUri = "/1/2/65536".deserializeAsUUri()
        assertTrue(uri.isEmpty())
    }

    @Test
    @DisplayName("Test deserializing a string with negative uEId")
    fun test_deserializing_a_string_with_negative_uEId() {
        val uri: UUri = "/-1/2/3".deserializeAsUUri()
        assertTrue(uri.isEmpty())
    }

    @Test
    @DisplayName("Test deserializing a string with negative uEVersionMajor")
    fun test_deserializing_a_string_with_negative_uEVersionMajor() {
        val uri: UUri = "/1/-2/3".deserializeAsUUri()
        assertTrue(uri.isEmpty())
    }

    @Test
    @DisplayName("Test deserializing a string with negative resourceId")
    fun test_deserializing_a_string_with_negative_resourceId() {
        val uri: UUri = "/1/2/-3".deserializeAsUUri()
        assertTrue(uri.isEmpty())
    }

    @Test
    @DisplayName("Test deserializing a string with wildcard ResourceId")
    fun test_deserializing_a_string_with_wildcard_resourceId() {
        val uri: UUri = "/1/2/ffff".deserializeAsUUri()
        assertEquals(1, uri.ueId)
        assertEquals(2, uri.ueVersionMajor)
        assertEquals(0xFFFF, uri.resourceId)
    }

    @Test
    @DisplayName("Test serializing an Empty UUri")
    fun test_serializing_an_empty_UUri() {
        val uri = uUri {  }
        val serializedUri: String = uri.serialize()
        assertTrue(serializedUri.isBlank())
    }

    @Test
    @DisplayName("Test serializing a full UUri")
    fun test_serializing_a_full_UUri() {
        val uri = uUri {
            authorityName = "myAuthority"
            ueId = 1
            ueVersionMajor = 2
            resourceId = 3
        }
        val serializedUri: String = uri.serialize()
        assertEquals("//myAuthority/1/2/3", serializedUri)
    }

    @Test
    @DisplayName("Test serializing a UUri with only authority")
    fun test_serializing_a_UUri_with_only_authority() {
        val uri = uUri {
            authorityName = "myAuthority"

        }
        val serializedUri: String = uri.serialize()
        assertEquals("//myAuthority/0/0/0", serializedUri)
    }

    @Test
    @DisplayName("Test serializing a UUri with authority and ueId")
    fun test_serializing_a_UUri_with_only_authority_and_ueId() {
        val uri = uUri {
            authorityName = "myAuthority"
            ueId = 1
        }
        val serializedUri: String = uri.serialize()
        assertEquals("//myAuthority/1/0/0", serializedUri)
    }

    @Test
    @DisplayName("Test serializing a UUri with authority, ueId and ueVersionMajor")
    fun test_serializing_a_UUri_with_only_authority_ueId_and_ueVersionMajor() {
        val uri = uUri {
            authorityName = "myAuthority"
            ueId = 1
            ueVersionMajor = 2

        }
        val serializedUri: String = uri.serialize()
        assertEquals("//myAuthority/1/2/0", serializedUri)
    }

    @Test
    @DisplayName("Test serializing a UUri with authority, ueId, ueVersionMajor and resourceId")
    fun test_serializing_a_UUri_with_only_authority_ueId_ueVersionMajor_and_resourceId() {
        val uri = uUri {
            authorityName = "myAuthority"
            ueId = 1
            ueVersionMajor = 2
            resourceId = 3

        }
        val serializedUri: String = uri.serialize()
        assertEquals("//myAuthority/1/2/3", serializedUri)
    }
    @Test
    @DisplayName("Test serializing a UUri with empty authority, ueId, ueVersionMajor and resourceId")
    fun test_serializing_a_UUri_with_empty_authority_ueId_ueVersionMajor_and_resourceId() {
        val uri = uUri {
            authorityName = ""
            ueId = 1
            ueVersionMajor = 2
            resourceId = 3
        }
        val serializedUri: String = uri.serialize()
        assertEquals("/1/2/3", serializedUri)
    }

    @Test
    @DisplayName("Test serializing a UUri with blank authority, ueId, ueVersionMajor and resourceId")
    fun test_serializing_a_UUri_with_blank_authority_ueId_ueVersionMajor_and_resourceId() {
        val uri = uUri {
            authorityName = "  "
            ueId = 1
            ueVersionMajor = 2
            resourceId = 3
        }
        val serializedUri: String = uri.serialize()
        assertEquals("/1/2/3", serializedUri)
    }
}
