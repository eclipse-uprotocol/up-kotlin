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
 * SPDX-FileType: SOURCE
 * SPDX-FileCopyrightText: 2023 General Motors GTO LLC
 * SPDX-License-Identifier: Apache-2.0
 */

package org.eclipse.uprotocol.validation

import org.eclipse.uprotocol.v1.UCode
import org.eclipse.uprotocol.v1.UStatus
import org.eclipse.uprotocol.v1.uStatus
import java.util.*

/**
 * Class wrapping a ValidationResult of success or failure wrapping the value of a google.rpc.Status.
 */
sealed class ValidationResult {

    companion object {
        val STATUS_SUCCESS: UStatus = uStatus {
            code = UCode.OK
            message = "OK"
        }
        private val SUCCESS: ValidationResult = Success

        fun success(): ValidationResult = SUCCESS
        fun failure(message: String): ValidationResult = Failure(message)
    }

    fun isFailure(): Boolean {
        return !isSuccess()
    }

    abstract fun toStatus(): UStatus
    abstract fun isSuccess(): Boolean
    abstract fun getMessage(): String

    class Failure(private val msg: String) : ValidationResult() {

        init {
            Objects.requireNonNull(msg)
        }

        override fun toStatus(): UStatus {
            return uStatus {
                code = UCode.INVALID_ARGUMENT
                message = msg
            }
        }

        override fun isSuccess(): Boolean {
            return false
        }

        override fun getMessage(): String {
            return msg
        }

        override fun toString(): String {
            return "ValidationResult.Failure(message='$msg')"
        }
    }

    data object Success : ValidationResult() {
        override fun toStatus(): UStatus {
            return STATUS_SUCCESS
        }

        override fun isSuccess(): Boolean {
            return true
        }

        override fun getMessage(): String {
            return ""
        }

        override fun toString(): String {
            return "ValidationResult.Success()"
        }
    }
}
