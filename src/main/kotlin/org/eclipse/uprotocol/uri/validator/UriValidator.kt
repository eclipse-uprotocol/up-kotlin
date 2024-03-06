/*
 * Copyright (c) 2024 General Motors GTO LLC
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

package org.eclipse.uprotocol.uri.validator

import org.eclipse.uprotocol.v1.UAuthority
import org.eclipse.uprotocol.v1.UResource
import org.eclipse.uprotocol.v1.UUri
import org.eclipse.uprotocol.validation.ValidationResult

/**
 * class for validating Uris.
 */
object UriValidator {
    /**
     * Validate a [UUri] to ensure that it has at least a name for the uEntity.
     *
     * @param uri [UUri] to validate.
     * @return Returns UStatus containing a success or a failure with the error message.
     */
    fun validate(uri: UUri): ValidationResult {
        if (isEmpty(uri)) {
            return ValidationResult.failure("Uri is empty.")
        }
        if (uri.hasAuthority() && !isRemote(uri.authority)) {
            return ValidationResult.failure("Uri is remote missing uAuthority.")
        }
        return if (uri.entity.name.isBlank()) {
            ValidationResult.failure("Uri is missing uSoftware Entity name.")
        } else ValidationResult.success()
    }

    /**
     * Validate a [UUri] that is meant to be used as an RPC method URI. Used in Request sink values and Response source values.
     *
     * @param uri [UUri] to validate.
     * @return Returns UStatus containing a success or a failure with the error message.
     */
    fun validateRpcMethod(uri: UUri): ValidationResult {
        val status: ValidationResult = validate(uri)
        if (status.isFailure()) {
            return status
        }
        return if (!isRpcMethod(uri)) {
            ValidationResult.failure("Invalid RPC method uri. Uri should be the method to be called, or method from response.")
        } else ValidationResult.success()
    }

    /**
     * Validate a [UUri] that is meant to be used as an RPC response URI. Used in Request source values and Response sink values.
     *
     * @param uri [UUri] to validate.
     * @return Returns UStatus containing a success or a failure with the error message.
     */
    fun validateRpcResponse(uri: UUri): ValidationResult {
        val status: ValidationResult = validate(uri)
        if (status.isFailure()) {
            return status
        }
        return if (!isRpcResponse(uri)) {
            ValidationResult.failure("Invalid RPC response type.")
        } else ValidationResult.success()
    }

    /**
     * Indicates that this  URI is an empty as it does not contain authority, entity, and resource.
     *
     * @param uri [UUri] to check if it is empty
     * @return Returns true if this  URI is an empty container and has no valuable information in building uProtocol sinks or sources.
     */
    fun isEmpty(uri: UUri): Boolean {
        return !uri.hasAuthority() && !uri.hasEntity() && !uri.hasResource()
    }

    /**
     * Returns true if URI is of type RPC.
     *
     * @param uri [UUri] to check if it is of type RPC method
     * @return Returns true if URI is of type RPC.
     */
    fun isRpcMethod(uri: UUri): Boolean {
        return uri.resource.name.contains("rpc") && (uri.resource
            .hasInstance() && uri.resource.instance.trim().isNotEmpty() || uri.resource
            .hasId() && uri.resource.id != 0)
    }

    /**
     * Returns true if URI contains both names and numeric representations of the names inside its belly.
     * Meaning that this UUri can be serialized to long or micro formats.
     *
     * @param uri [UUri] to check if resolved.
     * @return Returns true if URI contains both names and numeric representations of the names inside its belly.
     * Meaning that this UUri can buree serialized to long or micro formats.
     */
    fun isResolved(uri: UUri): Boolean {
        return !isEmpty(uri) && isLongForm(uri) && isMicroForm(uri)
    }

    /**
     * Returns true if URI is of type RPC response.
     *
     * @param uri [UUri] to check response
     * @return Returns true if URI is of type RPC response.
     */
    fun isRpcResponse(uri: UUri): Boolean {
        val resource: UResource = uri.resource
        return resource.getName().contains("rpc") &&
                resource.hasInstance() && resource.getInstance().contains("response") &&
                resource.hasId() && resource.id == 0
    }

    /**
     * Returns true if URI contains numbers so that it can be serialized into micro format.
     *
     * @param uri [UUri] to check
     * @return Returns true if URI contains numbers so that it can be serialized into micro format.
     */
    fun isMicroForm(uri: UUri): Boolean {
        return !isEmpty(uri) && uri.entity.hasId() && uri.resource
            .hasId() && isMicroForm(uri.authority)
    }

    /**
     * check if UAuthority can be represented in micro format. Micro UAuthorities are local or ones
     * that contain IP address or IDs.
     *
     * @param authority [UAuthority] to check
     * @return Returns true if UAuthority can be represented in micro format
     */
    fun isMicroForm(authority: UAuthority?): Boolean {
        return isLocal(authority) || (authority?.hasIp() == true || authority?.hasId() == true)
    }

    /**
     * Returns true if URI contains names so that it can be serialized into long format.
     *
     * @param uri [UUri] to check
     * @return Returns true if URI contains names so that it can be serialized into long format.
     */
    fun isLongForm(uri: UUri): Boolean {
        return !isEmpty(uri) &&
                isLongForm(uri.authority) &&
                uri.entity.getName().isNotBlank() && uri.resource.getName().isNotBlank()

    }

    /**
     * Returns true if UAuthority contains names so that it can be serialized into long format.
     *
     * @param authority [UAuthority] to check
     * @return Returns true if URI contains names so that it can be serialized into long format.
     */
    fun isLongForm(authority: UAuthority?): Boolean {
        return (authority != null) && authority.hasName() && authority.name.isNotBlank()
    }

    /**
     * Returns true if UAuthority is local meaning there is no name/ip/id set.
     *
     * @param authority [UAuthority] to check if it is local or not
     * @return Returns true if UAuthority is local meaning the Authority is not populated with name, ip and id
     */
    fun isLocal(authority: UAuthority?): Boolean {
        return (authority == null) || authority == UAuthority.getDefaultInstance()
    }

    /**
     * Returns true if UAuthority is remote meaning the name and/or ip/id is populated.
     * @param authority {@link UAuthority} to check if it is remote or not
     * @return Returns true if UAuthority is remote meaning the name and/or ip/id is populated.
     */
    fun isRemote(authority: UAuthority?): Boolean {
        return (authority != null) && authority != UAuthority.getDefaultInstance() &&
                (isLongForm(authority) || isMicroForm(authority))
    }
}
