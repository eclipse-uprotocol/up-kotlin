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

package org.eclipse.uprotocol.transport.datamodel

import java.util.*

/**
 * UProtocol general status for all operations.
 * A UStatus is generated using the static factory methods, making is easy to quickly create UStatus objects.
 * Example: UStatus ok = UStatus.ok();
 */
abstract class UStatus {
    abstract fun isSuccess(): Boolean
    abstract fun msg(): String?
    abstract fun getCode(): Int

    /**
     * Enum to contain the status code that we map to google.rpc.Code.
     * Please refer to [code.proto](https://github.com/googleapis/googleapis/blob/master/google/rpc/code.proto)
     * for documentation on the codes listed below
     *
     */
    enum class Code(val value: Int) {
        OK(com.google.rpc.Code.OK_VALUE), CANCELLED(com.google.rpc.Code.CANCELLED_VALUE), UNKNOWN(com.google.rpc.Code.UNKNOWN_VALUE), INVALID_ARGUMENT(com.google.rpc.Code.INVALID_ARGUMENT_VALUE), DEADLINE_EXCEEDED(com.google.rpc.Code.DEADLINE_EXCEEDED_VALUE), NOT_FOUND(com.google.rpc.Code.NOT_FOUND_VALUE), ALREADY_EXISTS(com.google.rpc.Code.ALREADY_EXISTS_VALUE), PERMISSION_DENIED(com.google.rpc.Code.PERMISSION_DENIED_VALUE), UNAUTHENTICATED(com.google.rpc.Code.UNAUTHENTICATED_VALUE), RESOURCE_EXHAUSTED(com.google.rpc.Code.RESOURCE_EXHAUSTED_VALUE), FAILED_PRECONDITION(com.google.rpc.Code.FAILED_PRECONDITION_VALUE), ABORTED(com.google.rpc.Code.ABORTED_VALUE), OUT_OF_RANGE(com.google.rpc.Code.OUT_OF_RANGE_VALUE), UNIMPLEMENTED(com.google.rpc.Code.UNIMPLEMENTED_VALUE), INTERNAL(com.google.rpc.Code.INTERNAL_VALUE), UNAVAILABLE(com.google.rpc.Code.UNAVAILABLE_VALUE), DATA_LOSS(com.google.rpc.Code.DATA_LOSS_VALUE), UNSPECIFIED(-1);

        fun value(): Int {
            return value
        }

        companion object {
            /**
             * Get the Code from an integer value.
             * @param value The integer value of the Code.
             * @return Returns the Code if found, otherwise returns Optional.empty().
             */
            fun from(value: Int): Optional<Code> {
                return Arrays.stream(entries.toTypedArray()).filter { p: Code -> p.value() == value }.findAny()
            }

            /**
             * Get the Code from a google.rpc.Code.
             * @param code The google.rpc.Code.
             * @return Returns the Code if found, otherwise returns Optional.empty().
             */
            fun from(code: com.google.rpc.Code?): Optional<Code> {
                return if (code == null || code == com.google.rpc.Code.UNRECOGNIZED) {
                    Optional.empty()
                } else Arrays.stream(entries.toTypedArray()).filter { p: Code -> p.value() == code.number }.findAny()
            }
        }
    }

    val isFailed: Boolean
        /**
         * Return true if UStatus is a failure
         * @return Returns true if the UStatus is successful.
         */
        get() = !isSuccess()

    override fun toString(): String {
        return String.format("UStatus %s %s%s code=%s", if (isSuccess()) "ok" else "failed", if (isSuccess()) "id=" else "msg=", msg(), getCode())
    }

    /**
     * A successful UStatus.
     */
    private class OKSTATUS(
            /**
             * A successful status could contain an id for tracking purposes.
             */
            private val id: String) : UStatus() {
        override fun isSuccess(): Boolean {
            return true
        }


        override fun msg(): String? {
            return id
        }

        override fun getCode(): Int {
            return Code.OK.value()
        }

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other == null || javaClass != other.javaClass) return false
            val okstatus = other as OKSTATUS
            return id == okstatus.id
        }

        override fun hashCode(): Int {
            return Objects.hash(id)
        }
    }

    /**
     * A failed UStatus.
     */
    private class FAILSTATUS : UStatus {
        private val failMsg: String
        private val code: Code

        constructor(failMsg: String, code: Code) {
            this.failMsg = failMsg
            this.code = code
        }

        constructor(failMsg: String, value: Int) {
            val code = Code.from(value)
            this.failMsg = failMsg
            this.code = code.orElse(Code.UNSPECIFIED)
        }

        override fun isSuccess(): Boolean {
            return false
        }

        override fun msg(): String {
            return failMsg
        }

        override fun getCode(): Int {
            return code.value
        }



        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other == null || javaClass != other.javaClass) return false
            val that = other as FAILSTATUS
            return failMsg == that.failMsg && code == that.code
        }

        override fun hashCode(): Int {
            return Objects.hash(failMsg, code)
        }
    }

    companion object {
        private const val OK = "ok"
        private const val FAILED = "failed"
        fun ok(): UStatus {
            return OKSTATUS(OK)
        }

        fun ok(ackId: String): UStatus {
            return OKSTATUS(ackId)
        }

        fun failed(): UStatus {
            return FAILSTATUS(FAILED, Code.UNKNOWN.value())
        }

        fun failed(msg: String): UStatus {
            return FAILSTATUS(msg, Code.UNKNOWN.value())
        }

        fun failed(msg: String, failureReason: Int): UStatus {
            return FAILSTATUS(msg, failureReason)
        }

        fun failed(msg: String, code: Code): UStatus {
            return FAILSTATUS(msg, code)
        }
    }
}
