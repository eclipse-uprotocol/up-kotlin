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

package org.eclipse.uprotocol.uri

import org.eclipse.uprotocol.core.usubscription.v3.USubscriptionProto
import org.eclipse.uprotocol.uri.factory.UUriFactory
import org.eclipse.uprotocol.uri.serializer.deserializeAsUUri
import org.eclipse.uprotocol.uri.serializer.serialize
import org.eclipse.uprotocol.uri.validator.*
import org.eclipse.uprotocol.v1.UUri
import org.eclipse.uprotocol.v1.uUri
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class UUriExamples {
    @Test
    fun example_UriFactory_FromProto() {
        // Fetch the notification topic Uri from the USubscriptionProto generated code
        val uri: UUri = UUriFactory.fromProto(USubscriptionProto.getDescriptor().services[0], 0)

        assertEquals(uri.ueId, 0)
        assertEquals(uri.ueVersionMajor, 3)
        assertEquals(uri.resourceId, 0)
    }

    @Test
    fun example_Serializer_Deserializer() {
        val uri = uUri {
            ueId = 1
            ueVersionMajor = 2
            resourceId = 3
        }
        val strUri: String = uri.serialize()
        assertEquals("/1/2/3", strUri)
        assertEquals(uri, strUri.deserializeAsUUri())
    }

    @Test
    fun example_UriValidator() {
        val uri = uUri {
            ueId = 1
            ueVersionMajor = 2
            resourceId = 3
        }
        assertFalse(uri.isEmpty())
        assertFalse(uri.isDefaultResourceId())
        assertTrue(uri.isRpcMethod())
        assertFalse(uri.isRpcResponse())
        assertFalse(uri.isTopic())
    }
}