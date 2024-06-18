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
import org.eclipse.uprotocol.v1.UStatus
import org.eclipse.uprotocol.v1.uStatus
import java.util.*

/**
 * Class wrapping a ValidationResult of success or failure wrapping the value of a google.rpc.Status.
 */
sealed class ValidationResult {

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

    companion object {
        val STATUS_SUCCESS: UStatus = uStatus {
            code = UCode.OK
            message = "OK"
        }
        private val SUCCESS: ValidationResult = Success

        fun success(): ValidationResult = SUCCESS
        fun failure(message: String): ValidationResult = Failure(message)
    }
}
