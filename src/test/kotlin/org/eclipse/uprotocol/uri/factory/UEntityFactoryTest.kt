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

package org.eclipse.uprotocol.uri.factory

import org.eclipse.uprotocol.core.udiscovery.v3.UDiscoveryProto
import org.eclipse.uprotocol.core.usubscription.v3.USubscriptionProto
import org.eclipse.uprotocol.core.utwin.v1.UTwinProto
import org.eclipse.uprotocol.uri.factory.UEntityFactory.fromProto
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test


class UEntityFactoryTest{
    @Test
    @DisplayName("Test build valid usubscription UEntity")
    fun test_build_valid_usubscription_uentity() {
        val descriptor = USubscriptionProto.getDescriptor().services[0]
        val entity = fromProto(descriptor)

        assertEquals(entity.name, "core.usubscription")
        assertEquals(entity.id, 0)
        assertEquals(entity.versionMajor, 3)
        assertEquals(entity.versionMinor, 0)
    }

    @Test
    @DisplayName("Test build valid uDiscovery UEntity")
    fun test_build_valid_udiscovery_uentity() {
        val descriptor = UDiscoveryProto.getDescriptor().services[0]

        val entity = fromProto(descriptor)

        assertEquals(entity.name, "core.udiscovery")
        assertEquals(entity.id, 1)
        assertEquals(entity.versionMajor, 3)
        assertEquals(entity.versionMinor, 0)
    }

    @Test
    @DisplayName("Test build valid uTwin UEntity")
    fun test_build_valid_utwin_uentity() {
        val descriptor = UTwinProto.getDescriptor().services[0]

        val entity = fromProto(descriptor)

        assertEquals(entity.name, "core.utwin")
        assertEquals(entity.id, 26)
        assertEquals(entity.versionMajor, 1)
        assertEquals(entity.versionMinor, 0)
    }
}