/*
 * Copyright (c) 2023 General Motors GTO LLC
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
package org.eclipse.uprotocol.cloudevent.validate


import org.eclipse.uprotocol.v1.UCode
import org.eclipse.uprotocol.v1.UStatus
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*
import org.eclipse.uprotocol.validation.ValidationResult

internal class ValidationResultTest {
    @Test
    @DisplayName("Test success validation result to string")
    fun test_success_validation_result_toString() {
        val success: ValidationResult = ValidationResult.success()
        assertEquals("ValidationResult.Success()", success.toString())
    }

    @Test
    @DisplayName("Test failure validation result to string")
    fun test_failure_validation_result_toString() {
        val failure: ValidationResult = ValidationResult.failure("boom")
        assertEquals("ValidationResult.Failure(message='boom')", failure.toString())
    }

    @Test
    @DisplayName("Test success validation result isSuccess")
    fun test_success_validation_result_isSuccess() {
        val success: ValidationResult = ValidationResult.success()
        assertTrue(success.isSuccess())
    }

    @Test
    @DisplayName("Test failure validation result isSuccess")
    fun test_failure_validation_result_isSuccess() {
        val failure: ValidationResult = ValidationResult.failure("boom")
        assertFalse(failure.isSuccess())
    }

    @Test
    @DisplayName("Test success message")
    fun test_success_validation_result_getMessage() {
        val success: ValidationResult = ValidationResult.success()
        assertTrue(success.getMessage().isBlank())
    }

    @Test
    @DisplayName("Test failure message")
    fun test_failure_validation_result_getMessage() {
        val failure: ValidationResult = ValidationResult.failure("boom")
        assertEquals("boom", failure.getMessage())
    }

    @Test
    @DisplayName("Test success toStatus")
    fun test_success_validation_result_toStatus() {
        val success: ValidationResult = ValidationResult.success()
        assertEquals(ValidationResult.STATUS_SUCCESS, success.toStatus())
    }

    @Test
    @DisplayName("Test failure toStatus")
    fun test_failure_validation_result_toStatus() {
        val failure: ValidationResult = ValidationResult.failure("boom")
        val status: UStatus = UStatus.newBuilder().setCode(UCode.INVALID_ARGUMENT).setMessage("boom").build()
        assertEquals(status, failure.toStatus())
    }
}