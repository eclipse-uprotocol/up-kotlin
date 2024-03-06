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
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.contract

/**
 * Validate a [UUri] to ensure that it has at least a name for the uEntity.
 *
 * @return Returns UStatus containing a success or a failure with the error message.
 */
fun UUri.validate(): ValidationResult {
    if (isEmpty()) {
        return ValidationResult.failure("Uri is empty.")
    }
    if (hasAuthority() && !authority.isRemote()) {
        return ValidationResult.failure("Uri is remote missing uAuthority.")
    }
    return if (entity.name.isBlank()) {
        ValidationResult.failure("Uri is missing uSoftware Entity name.")
    } else ValidationResult.success()
}

/**
 * Validate a [UUri] that is meant to be used as an RPC method URI. Used in Request sink values and Response source values.
 *
 * @return Returns UStatus containing a success or a failure with the error message.
 */
fun UUri.validateRpcMethod(): ValidationResult {
    val status: ValidationResult = validate()
    if (status.isFailure()) {
        return status
    }
    return if (!isRpcMethod()) {
        ValidationResult.failure("Invalid RPC method uri. Uri should be the method to be called, or method from response.")
    } else ValidationResult.success()
}

/**
 * Validate a [UUri] that is meant to be used as an RPC response URI. Used in Request source values and Response sink values.
 *
 * @return Returns UStatus containing a success or a failure with the error message.
 */
fun UUri.validateRpcResponse(): ValidationResult {
    val status: ValidationResult = validate()
    if (status.isFailure()) {
        return status
    }
    return if (!isRpcResponse()) {
        ValidationResult.failure("Invalid RPC response type.")
    } else ValidationResult.success()
}

/**
 * Returns true if URI is of type RPC.
 *
 * @return Returns true if URI is of type RPC.
 */
fun UUri.isRpcMethod(): Boolean {
    return resource.name.contains("rpc") && (resource
        .hasInstance() && resource.instance.trim().isNotEmpty() || resource
        .hasId() && resource.id != 0)
}

/**
 * Returns true if URI contains both names and numeric representations of the names inside its belly.
 * Meaning that this UUri can be serialized to long or micro formats.
 *
 * @return Returns true if URI contains both names and numeric representations of the names inside its belly.
 * Meaning that this UUri can buree serialized to long or micro formats.
 */
fun UUri.isResolved(): Boolean {
    return !isEmpty() && isLongForm() && isMicroForm()
}

/**
 * Returns true if URI is of type RPC response.
 *
 * @return Returns true if URI is of type RPC response.
 */
fun UUri.isRpcResponse(): Boolean {
    val resource: UResource = resource
    return resource.getName().contains("rpc") &&
            resource.hasInstance() && resource.getInstance().contains("response") &&
            resource.hasId() && resource.id == 0
}

/**
 * Indicates that this  URI is an empty as it does not contain authority, entity, and resource.
 *
 * @return Returns true if this  URI is an empty container and has no valuable information in building uProtocol sinks or sources.
 */
fun UUri.isEmpty(): Boolean {
    return !this.hasAuthority() && !this.hasEntity() && !this.hasResource()
}

/**
 * Indicates that this  URI is NOT an empty as it does contain authority or entity or resource.
 *
 * @return Returns true if this URI is NOT an empty container and has valuable information in building uProtocol sinks or sources.
 */
fun UUri.isNotEmpty(): Boolean {
    return !this.isEmpty()
}

/**
 * Returns true if URI contains numbers so that it can be serialized into micro format.
 *
 * @return Returns true if URI contains numbers so that it can be serialized into micro format.
 */
fun UUri.isMicroForm(): Boolean {
    return !isEmpty() && entity.hasId() && resource
        .hasId() && authority.isMicroForm()
}

/**
 * check if UAuthority can be represented in micro format. Micro UAuthorities are local or ones
 * that contain IP address or IDs.
 *
 * @return Returns true if UAuthority can be represented in micro format
 */
fun UAuthority?.isMicroForm(): Boolean {
    return isLocal() || (this?.hasIp() == true || this?.hasId() == true)
}

/**
 * Returns true if URI contains names so that it can be serialized into long format.
 *
 * @return Returns true if URI contains names so that it can be serialized into long format.
 */
fun UUri.isLongForm(): Boolean {
    return !isEmpty() &&
            authority.isLongForm() &&
            entity.getName().isNotBlank() && resource.getName().isNotBlank()
}

/**
 * Returns true if UAuthority contains names so that it can be serialized into long format.
 *
 * @return Returns true if URI contains names so that it can be serialized into long format.
 */
@OptIn(ExperimentalContracts::class)
fun UAuthority?.isLongForm(): Boolean {
    contract {
        returns(true) implies (this@isLongForm is UAuthority)
    }
    return (this != null) && this.hasName() && this.name.isNotBlank()
}

/**
 * Returns true if UAuthority is local meaning there is no name/ip/id set.
 *
 * @return Returns true if UAuthority is local meaning the Authority is not populated with name, ip and id
 */
fun UAuthority?.isLocal(): Boolean {
    return (this == null) || this == UAuthority.getDefaultInstance()
}

/**
 * Returns true if UAuthority is remote meaning the name and/or ip/id is populated.
 *
 * @return Returns true if UAuthority is remote meaning the name and/or ip/id is populated.
 */
@OptIn(ExperimentalContracts::class)
fun UAuthority?.isRemote(): Boolean {
    contract {
        returns(true) implies (this@isRemote is UAuthority)
    }
    return (this != null) && this != UAuthority.getDefaultInstance() &&
            (this.isLongForm() || this.isMicroForm())
}
