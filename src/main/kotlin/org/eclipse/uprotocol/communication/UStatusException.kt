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
import org.eclipse.uprotocol.v1.UStatus
import org.eclipse.uprotocol.v1.uStatus

/**
 * The unchecked exception which carries uProtocol error model.
 * @param status An error [UStatus].
 * @param cause  An exception that caused this one.
 */
class UStatusException @JvmOverloads constructor(val status: UStatus, cause: Throwable? = null) :
    RuntimeException(status.message, cause) {

    /**
     * Constructs an instance.
     *
     * @param code    An error [UCode].
     * @param message An error message.
     */
    constructor(code: UCode, message: String?) : this(uStatus {
        this.code = code
        message?.let { this.message = it }
    }, null)

    /**
     * Constructs an instance.
     *
     * @param code    An error [UCode].
     * @param message An error message.
     * @param cause   An exception that caused this one.
     */
    constructor(code: UCode, message: String?, cause: Throwable) : this(uStatus {
        this.code = code
        message?.let { this.message = it }
    }, cause)

    val code: UCode
        /**
         * Get the error code.
         * @return The error [UCode].
         */
        get() = status.code
    override val message: String
        /**
         * Get the error message.
         * @return The error message.
         */
        get() = status.message
}
