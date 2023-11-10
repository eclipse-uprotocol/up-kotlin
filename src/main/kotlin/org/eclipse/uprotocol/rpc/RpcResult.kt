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

import com.google.rpc.Code
import com.google.rpc.Status
import java.util.function.Function
import java.util.function.Supplier

sealed class RpcResult<T> {

    abstract val isSuccess: Boolean
    abstract val isFailure: Boolean
    abstract fun getOrElse(defaultValue: T): T
    abstract fun getOrElse(defaultValue: Supplier<T>): T
    abstract fun <U> map(f: Function<T, U>): RpcResult<U>
    abstract fun <U> flatMap(f: Function<T, RpcResult<U>>): RpcResult<U>
    abstract fun filter(f: Function<T, Boolean>): RpcResult<T>
    abstract val failureValue: Status
    abstract val successValue: T

    class Success<T>(private val value: T) : RpcResult<T>() {

        override val isSuccess: Boolean
            get() = true
        override val isFailure: Boolean
            get() = false

        override fun getOrElse(defaultValue: T): T = successValue
        override fun getOrElse(defaultValue: Supplier<T>): T = successValue

        override fun <U> map(f: Function<T, U>): RpcResult<U> {
            return try {
                success(f.apply(successValue))
            } catch (e: Exception) {
                failure(e.message!!, e)
            }
        }

        override fun <U> flatMap(f: Function<T, RpcResult<U>>): RpcResult<U> {
            return try {
                f.apply(successValue)
            } catch (e: Exception) {
                failure(e.message!!, e)
            }
        }

        override fun filter(f: Function<T, Boolean>): RpcResult<T> {
            return try {
                if (f.apply(successValue)) this else failure(Code.FAILED_PRECONDITION, "filtered out")
            } catch (e: Exception) {
                failure(e.message!!, e)
            }
        }

        override val failureValue: Status
            get() = throw IllegalStateException("Method failureValue() called on a Success instance")

        override val successValue: T
            get() = value

        override fun toString(): String {
            return "Success($successValue)"
        }
    }

    class Failure<T> internal constructor(internal val value: Status?) : RpcResult<T>() {

        constructor(code: Code, message: String) : this(Status.newBuilder().setCode(code.number).setMessage(message).build())

        companion object {
            fun <T> fromException(e: Exception): RpcResult<T> {
                return Failure(Status.newBuilder().setCode(Code.UNKNOWN.number).setMessage(e.message).build())
            }
        }


        override val isSuccess: Boolean
            get() = false
        override val isFailure: Boolean
            get() = true

        override fun getOrElse(defaultValue: T): T = defaultValue
        override fun getOrElse(defaultValue: Supplier<T>): T = defaultValue.get()

        override fun <U> map(f: Function<T, U>): RpcResult<U> {
            return failure(this)
        }

        override fun <U> flatMap(f: Function<T, RpcResult<U>>): RpcResult<U> {
            return failure(failureValue)
        }

        override fun filter(f: Function<T, Boolean>): RpcResult<T> {
            return failure(this)
        }

        override val failureValue: Status
            get() = value!!

        override val successValue: T
            get() = throw IllegalStateException("Method successValue() called on a Failure instance")

        override fun toString(): String {
            return "Failure($value)"
        }
    }

    companion object {
        fun <T> success(value: T): Success<T> = Success(value)
        fun <T> failure(value: Status): RpcResult<T> = Failure(value)
        fun <T, U> failure(failure: Failure<U>): RpcResult<T> = Failure(failure.value)
        fun <T> failure(message: String, e: Throwable): RpcResult<T> = Failure.fromException(IllegalStateException(message, e))
        fun <T> failure(code: Code, message: String): RpcResult<T> = Failure(code, message)

        fun <T> flatten(result: RpcResult<RpcResult<T>>): RpcResult<T> = result.flatMap { it }
    }
}
