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
package org.eclipse.uprotocol.rpc

import com.google.rpc.Code
import com.google.rpc.Status
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*

internal class RpcResultTest {
    @Test
    fun test_isSuccess_on_Success() {
        val result: RpcResult<Int> = RpcResult.success(2)
        assertTrue(result.isSuccess)
    }

    @Test
    fun test_isSuccess_on_Failure() {
        val result: RpcResult<Int> = RpcResult.failure(Code.INVALID_ARGUMENT, "boom")
        assertFalse(result.isSuccess)
    }

    @Test
    fun test_isFailure_on_Success() {
        val result: RpcResult<Int> = RpcResult.success(2)
        assertFalse(result.isFailure)
    }

    @Test
    fun test_isFailure_on_Failure() {
        val result: RpcResult<Int> = RpcResult.failure(Code.INVALID_ARGUMENT, "boom")
        assertTrue(result.isFailure)
    }

    @Test
    fun testGetOrElseOnSuccess() {
        val result: RpcResult<Int> = RpcResult.success(2)
        assertEquals(Integer.valueOf(2), result.getOrElse { default })
    }

    @Test
    fun testGetOrElseOnFailure() {
        val result: RpcResult<Int> = RpcResult.failure(Code.INVALID_ARGUMENT, "boom")
        assertEquals(default, result.getOrElse { default })
    }

    @Test
    fun testGetOrElseOnSuccess_() {
        val result: RpcResult<Int> = RpcResult.success(2)
        assertEquals(Integer.valueOf(2), result.getOrElse(5))
    }

    @Test
    fun testGetOrElseOnFailure_() {
        val result: RpcResult<Int> = RpcResult.failure(Code.INVALID_ARGUMENT, "boom")
        assertEquals(Integer.valueOf(5), result.getOrElse(5))
    }

    @Test
    fun testSuccessValue_onSuccess() {
        val result: RpcResult<Int> = RpcResult.success(2)
        assertEquals(Integer.valueOf(2), result.successValue)
    }

//    @Test
//    fun testSuccessValue_onFailure_() {
//        val result: org.eclipse.uprotocol.rpc.RpcResult<Int> = org.eclipse.uprotocol.rpc.RpcResult.failure(Code.INVALID_ARGUMENT, "boom")
//        val exception: Exception = assertThrows(IllegalStateException::class.java, result::successValue)
//        assertEquals(exception.getMessage(), "Method successValue() called on a Failure instance")
//    }

//    @Test
//    fun testFailureValue_onSuccess() {
//        val result: org.eclipse.uprotocol.rpc.RpcResult<Int> = org.eclipse.uprotocol.rpc.RpcResult.success(2)
//        val exception: Exception = assertThrows(IllegalStateException::class.java, result::failureValue)
//        assertEquals(exception.getMessage(), "Method failureValue() called on a Success instance")
//    }

    @Test
    fun testFailureValue_onFailure_() {
        val result: RpcResult<Int> = RpcResult.failure(Code.INVALID_ARGUMENT, "boom")
        val resultValue: Status = result.failureValue
        assertEquals(
            Status.newBuilder()
                .setCode(Code.INVALID_ARGUMENT.number)
                .setMessage("boom").build(), resultValue
        )
    }

    private val default: Int
        get() = 5

    @Test
    fun testMapOnSuccess() {
        val result: RpcResult<Int> = RpcResult.success(2)
        val mapped: RpcResult<Int> = result.map { x -> x * 2 }
        assertTrue(mapped.isSuccess)
        assertEquals(4, mapped.successValue)
    }

    @Test
    fun testMapSuccess_when_function_throws_exception() {
        val result: RpcResult<Int> = RpcResult.success(2)
//        val mapped: org.eclipse.uprotocol.rpc.RpcResult<Int> = result.map { x: Int -> funThatThrowsAnExceptionForMap(x) }
        val mapped: RpcResult<Int> = result.map { funThatThrowsAnExceptionForMap(it) }

        assertTrue(mapped.isFailure)
        assertEquals(Code.UNKNOWN_VALUE, mapped.failureValue.code)
        assertEquals("2 went boom", mapped.failureValue.message)
    }

    private fun funThatThrowsAnExceptionForMap(x: Int): Int {
        throw NullPointerException(String.format("%s went boom", x))
    }

    @Test
    fun testMapOnFailure() {
        val result: RpcResult<Int> = RpcResult.failure(Code.INVALID_ARGUMENT, "boom")
        val mapped: RpcResult<Int> = result.map { x -> x * 2 }
        assertTrue(mapped.isFailure)
        assertEquals(
            Status.newBuilder()
                .setCode(Code.INVALID_ARGUMENT.number)
                .setMessage("boom").build(), mapped.failureValue
        )
    }

    @Test
    fun testFlatMapSuccess_when_function_throws_exception() {
        val result: RpcResult<Int> = RpcResult.success(2)
        val flatMapped: RpcResult<Int> = result.flatMap { x: Int -> funThatThrowsAnExceptionForFlatMap(x) }
        assertTrue(flatMapped.isFailure)
        assertEquals(Code.UNKNOWN_VALUE, flatMapped.failureValue.code)
        assertEquals("2 went boom", flatMapped.failureValue.message)
    }

    private fun funThatThrowsAnExceptionForFlatMap(x: Int): RpcResult<Int> {
        throw NullPointerException(String.format("%s went boom", x))
    }

    @Test
    fun testFlatMapOnSuccess() {
        val result: RpcResult<Int> = RpcResult.success(2)
        val flatMapped: RpcResult<Int> = result.flatMap { x -> RpcResult.success(x * 2) }
        assertTrue(flatMapped.isSuccess)
        assertEquals(4, flatMapped.successValue)
    }

    @Test
    fun testFlatMapOnFailure() {
        val result: RpcResult<Int> = RpcResult.failure(Code.INVALID_ARGUMENT, "boom")
        val flatMapped: RpcResult<Int> = result.flatMap { x -> RpcResult.success(x * 2) }
        assertTrue(flatMapped.isFailure)
        assertEquals(
            Status.newBuilder()
                .setCode(Code.INVALID_ARGUMENT.number)
                .setMessage("boom").build(), flatMapped.failureValue
        )
    }

    @Test
    fun testFilterOnSuccess_that_fails() {
        val result: RpcResult<Int> = RpcResult.success(2)
        val filterResult: RpcResult<Int> = result.filter { i -> i > 5 }
        assertTrue(filterResult.isFailure)
        assertEquals(
            Status.newBuilder()
                .setCode(Code.FAILED_PRECONDITION.number)
                .setMessage("filtered out").build(), filterResult.failureValue
        )
    }

    @Test
    fun testFilterOnSuccess_that_succeeds() {
        val result: RpcResult<Int> = RpcResult.success(2)
        val filterResult: RpcResult<Int> = result.filter { i -> i < 5 }
        assertTrue(filterResult.isSuccess)
        assertEquals(2, filterResult.successValue)
    }

    @Test
    fun testFilterOnSuccess__when_function_throws_exception() {
        val result: RpcResult<Int> = RpcResult.success(2)
        val filterResult: RpcResult<Int> = result.filter { x: Int -> predicateThatThrowsAnException(x) }
        assertTrue(filterResult.isFailure)
        assertEquals(
            Status.newBuilder()
                .setCode(Code.UNKNOWN_VALUE)
                .setMessage("2 went boom").build(), filterResult.failureValue
        )
    }

    private fun predicateThatThrowsAnException(x: Int): Boolean {
        throw NullPointerException(String.format("%s went boom", x))
    }

    @Test
    fun testFilterOnFailure() {
        val result: RpcResult<Int> = RpcResult.failure(Code.INVALID_ARGUMENT, "boom")
        val filterResult: RpcResult<Int> = result.filter { i -> i > 5 }
        assertTrue(filterResult.isFailure)
        assertEquals(
            Status.newBuilder()
                .setCode(Code.INVALID_ARGUMENT.number)
                .setMessage("boom").build(), filterResult.failureValue
        )
    }

    @Test
    fun testFlattenOnSuccess() {
        val result: RpcResult<Int> = RpcResult.success(2)
        val mapped: RpcResult<RpcResult<Int>> = result.map { x: Int -> multiplyBy2(x) }
        val mappedFlattened: RpcResult<Int> = RpcResult.flatten(mapped)
        assertTrue(mappedFlattened.isSuccess)
        assertEquals(4, mappedFlattened.successValue)
    }

    @Test
    fun testFlattenOnSuccess_with_function_that_fails() {
        val result: RpcResult<Int> = RpcResult.success(2)
        val mapped: RpcResult<RpcResult<Int>> = result.map { x: Int -> funThatThrowsAnExceptionForFlatMap(x)}
        val mappedFlattened: RpcResult<Int> = RpcResult.flatten(mapped)
        assertTrue(mappedFlattened.isFailure)
        assertEquals(Code.UNKNOWN_VALUE, mappedFlattened.failureValue.code)
        assertEquals("2 went boom", mappedFlattened.failureValue.message)
    }

    @Test
    fun testFlattenOnFailure() {
        val result: RpcResult<Int> = RpcResult.failure(Code.INVALID_ARGUMENT, "boom")
        val mapped: RpcResult<RpcResult<Int>> = result.map { x: Int -> multiplyBy2(x) }
        val mappedFlattened: RpcResult<Int> = RpcResult.flatten(mapped)
        assertTrue(mappedFlattened.isFailure)
        assertEquals(
            Status.newBuilder()
                .setCode(Code.INVALID_ARGUMENT.number)
                .setMessage("boom").build(), mappedFlattened.failureValue
        )
    }

    private fun multiplyBy2(x: Int): RpcResult<Int> {
        return RpcResult.success(x * 2)
    }

    @Test
    fun testToStringSuccess() {
        val result: RpcResult<Int> = RpcResult.success(2)
        assertEquals("Success(2)", result.toString())
    }

    @Test
    fun testToStringFailure() {
        val result: RpcResult<Int> = RpcResult.failure(Code.INVALID_ARGUMENT, "boom")
        assertEquals(
            """
    Failure(code: 3
    message: "boom"
    )
    """.trimIndent(), result.toString()
        )
    }
}