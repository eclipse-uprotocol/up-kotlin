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

package org.eclipse.uprotocol.uri.factory

import org.eclipse.uprotocol.core.usubscription.v3.USubscriptionProto
import org.eclipse.uprotocol.v1.UUri
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

class UUriFactoryTest {
    @Test
    @DisplayName("Test fromProto")
    fun testFromProto() {
        val uri: UUri = UUriFactory.fromProto(
            USubscriptionProto.getDescriptor().services[0], 0
        )

        assertEquals(uri.authorityName, "")
        assertEquals(uri.ueId, 0)
        assertEquals(uri.ueVersionMajor, 3)
        assertEquals(uri.resourceId, 0)
    }

    @Test
    @DisplayName("Test ANY")
    fun testAny() {
        val uri: UUri = UUriFactory.ANY

        assertEquals(uri.authorityName, "*")
        assertEquals(uri.ueId, 65535)
        assertEquals(uri.ueVersionMajor, 255)
        assertEquals(uri.resourceId, 65535)
    }

    @Test
    @DisplayName("Test fromProto with authority name")
    fun testFromProtoWithAuthorityName() {
        val uri: UUri = UUriFactory.fromProto(
            USubscriptionProto.getDescriptor().services[0], 0, "hartley"
        )

        assertEquals(uri.authorityName, "hartley")
        assertEquals(uri.ueId, 0)
        assertEquals(uri.ueVersionMajor, 3)
        assertEquals(uri.resourceId, 0)
    }

    @Test
    @DisplayName("Test fromProto with empty authority name string")
    fun testFromProtoWithEmptyAuthorityName() {
        val uri: UUri = UUriFactory.fromProto(
            USubscriptionProto.getDescriptor().services[0], 0, ""
        )

        assertEquals(uri.authorityName, "")
        assertEquals(uri.ueId, 0)
        assertEquals(uri.ueVersionMajor, 3)
        assertEquals(uri.resourceId, 0)
    }
}