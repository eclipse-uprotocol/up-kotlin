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

import org.eclipse.uprotocol.communication.CallOptions.Companion.TIMEOUT_DEFAULT
import org.eclipse.uprotocol.v1.UPriority
import org.eclipse.uprotocol.v1.UUri
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test


class CallOptionsTest {
    @Test
    @DisplayName("Test building a null CallOptions that is equal to the default")
    fun testBuildNullCallOptions() {
        val options = CallOptions()
        assertEquals(TIMEOUT_DEFAULT, options.timeout)
        assertEquals(UPriority.UPRIORITY_CS4, options.priority)
        assertTrue(options.token.isEmpty())
    }

    @Test
    @DisplayName("Test building a CallOptions with a timeout")
    fun testBuildCallOptionsWithTimeout() {
        val options = CallOptions(1000)
        assertEquals(1000, options.timeout)
        assertEquals(UPriority.UPRIORITY_CS4, options.priority)
        assertTrue(options.token.isEmpty())
    }

    @Test
    @DisplayName("Test building a CallOptions with a priority")
    fun testBuildCallOptionsWithPriority() {
        val options = CallOptions(1000, UPriority.UPRIORITY_CS4)
        assertEquals(UPriority.UPRIORITY_CS4, options.priority)
    }


    @Test
    @DisplayName("Test building a CallOptions with all parameters")
    fun testBuildCallOptionsWithAllParameters() {
        val options = CallOptions(1000, UPriority.UPRIORITY_CS4, "token")
        assertEquals(1000, options.timeout)
        assertEquals(UPriority.UPRIORITY_CS4, options.priority)
        assertEquals("token", options.token)
    }

    @Test
    @DisplayName("Test building a CallOptions with a blank token")
    fun testBuildCallOptionsWithBlankToken() {
        val options = CallOptions(1000, UPriority.UPRIORITY_CS4, "")
        assertTrue(options.token.isEmpty())
    }

    @Test
    @DisplayName("Test isEquals when timeout is not the same")
    fun testIsEqualsWithDifferentParameters() {
        val options = CallOptions(1001, UPriority.UPRIORITY_CS3, "token")
        val otherOptions = CallOptions(1000, UPriority.UPRIORITY_CS3, "token")
        Assertions.assertFalse(options == otherOptions)
    }

    @Test
    @DisplayName("Test isEquals when priority is not the same")
    fun testIsEqualsWithDifferentParametersPriority() {
        val options = CallOptions(1000, UPriority.UPRIORITY_CS4, "token")
        val otherOptions = CallOptions(1000, UPriority.UPRIORITY_CS3, "token")
        assertFalse(options == otherOptions)
    }

    @Test
    @DisplayName("Test isEquals when token is not the same")
    fun testIsEqualsWithDifferentParametersToken() {
        val options = CallOptions(1000, UPriority.UPRIORITY_CS3, "Mytoken")
        val otherOptions = CallOptions(1000, UPriority.UPRIORITY_CS3, "token")
        assertFalse(options == otherOptions)
    }
}