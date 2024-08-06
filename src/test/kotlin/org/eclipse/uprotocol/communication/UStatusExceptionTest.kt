/*
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

import org.eclipse.uprotocol.v1.UCode
import org.eclipse.uprotocol.v1.uStatus
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class UStatusExceptionTest {
    @Test
    fun testConstructorWithStatus() {
        val exception = UStatusException(STATUS)
        assertEquals(STATUS, exception.status)
        assertEquals(CODE, exception.code)
        assertEquals(MESSAGE, exception.message)
    }

    @Test
    fun testConstructorWithStatusAndCause() {
        val exception = UStatusException(STATUS, CAUSE)
        assertEquals(STATUS, exception.status)
        assertEquals(CODE, exception.code)
        assertEquals(MESSAGE, exception.message)
        assertEquals(CAUSE, exception.cause)
    }

    @Test
    fun testConstructorWithCodeAndMessage() {
        val exception = UStatusException(CODE, MESSAGE)
        assertEquals(STATUS, exception.status)
        assertEquals(CODE, exception.code)
        assertEquals(MESSAGE, exception.message)
    }

    @Test
    fun testConstructorWithCodeAndNullMessage() {
        val exception = UStatusException(CODE, null)
        assertEquals(CODE, exception.code)
        assertEquals("", exception.message)
    }

    @Test
    fun testConstructorWithCodeMessageAndCause() {
        val exception = UStatusException(CODE, MESSAGE, CAUSE)
        assertEquals(STATUS, exception.status)
        assertEquals(CODE, exception.code)
        assertEquals(MESSAGE, exception.message)
        assertEquals(CAUSE, exception.cause)
    }

    @Test
    fun testConstructorWithCodeNullMessageAndCause() {
        val exception = UStatusException(CODE, null, CAUSE)
        assertEquals(CODE, exception.code)
        assertEquals("", exception.message)
        assertEquals(CAUSE, exception.cause)
    }

    @Test
    fun testGetStatus() {
        val exception = UStatusException(STATUS)
        assertEquals(STATUS, exception.status)
    }

    @Test
    fun testGetCode() {
        val exception = UStatusException(STATUS)
        assertEquals(CODE, exception.code)
    }

    @Test
    fun testGetMessage() {
        val exception = UStatusException(STATUS)
        assertEquals(MESSAGE, exception.message)
    }

    companion object {
        private val CODE = UCode.OK
        private const val MESSAGE = "Test message"
        private val STATUS = uStatus {
            code = CODE
            message = MESSAGE
        }
        private val CAUSE = Throwable(MESSAGE)
    }
}
