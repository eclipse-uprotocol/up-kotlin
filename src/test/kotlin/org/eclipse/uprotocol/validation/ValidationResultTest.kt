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

package org.eclipse.uprotocol.validation

import org.eclipse.uprotocol.v1.UCode
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

class ValidationResultTest {
    @Test
    @DisplayName("Test creating a successful ValidationResult")
    fun testCreateSuccess() {
        val result = ValidationResult.success()
        Assertions.assertTrue(result.isSuccess())
        Assertions.assertFalse(result.isFailure())
        Assertions.assertEquals("", result.getMessage())
        Assertions.assertEquals(result.toStatus().code, UCode.OK)
        Assertions.assertEquals(result.toString(), "ValidationResult.Success()")
    }

    @Test
    @DisplayName("Test creating a failed ValidationResult")
    fun testCreateFailure() {
        val result = ValidationResult.failure("Failed")
        Assertions.assertFalse(result.isSuccess())
        Assertions.assertTrue(result.isFailure())
        Assertions.assertEquals("Failed", result.getMessage())
        Assertions.assertEquals(result.toStatus().code, UCode.INVALID_ARGUMENT)
        Assertions.assertEquals(result.toString(), "ValidationResult.Failure(message='Failed')")
    }
}