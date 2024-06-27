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

import kotlinx.coroutines.test.runTest
import org.eclipse.uprotocol.v1.UCode
import org.eclipse.uprotocol.v1.UUri
import org.eclipse.uprotocol.v1.uUri
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

class SimplePublisherTest {
    @Test
    @DisplayName("Test sending a simple publish message without a payload")
    fun testSendPublish() = runTest {
        val publisher: Publisher = SimplePublisher(TestUTransport())
        val result= publisher.publish(createTopic(), null)
        assertEquals(UCode.OK, result.code)
    }

    @Test
    @DisplayName("Test sending a simple publish message with a stuffed UPayload that was build with packToAny()")
    fun testSendPublishWithStuffedPayload() = runTest {
        val uri = uUri {  }
        val publisher: Publisher = SimplePublisher(TestUTransport())
        val result= publisher.publish(createTopic(), UPayload.packToAny(uri))
        assertEquals(UCode.OK, result.code)
    }


    private fun createTopic(): UUri {
        return uUri {
            authorityName = "Hartley"
            ueId = 3
            ueVersionMajor = 1
            resourceId = 0x8000
        }
    }
}