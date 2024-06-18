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
package org.eclipse.uprotocol.uri.validator

import org.eclipse.uprotocol.uri.UUriConstant.DEFAULT_RESOURCE_ID
import org.eclipse.uprotocol.uri.UUriConstant.MIN_TOPIC_ID
import org.eclipse.uprotocol.v1.*

/**
 * Returns true if URI is of type RPC. A UUri is of type RPC if it contains the word rpc in the resource name
 * and has an instance name and/or the id is less than MIN_TOPIC_ID.
 *
 * @return Returns true if URI is of type RPC.
 */
fun UUri.isRpcMethod(): Boolean {
    return !isEmpty() &&
            resourceId != DEFAULT_RESOURCE_ID &&
            resourceId < MIN_TOPIC_ID
}

/**
 * Returns true if URI is of type RPC response.
 *
 * @return Returns true if URI is of type RPC response.
 */
fun UUri.isRpcResponse(): Boolean {
    return isDefaultResourceId()
}

/**
 * Indicates that this  URI is an empty as it does not contain authority, entity, and resource.
 *
 * @return Returns true if this  URI is an empty container and has no valuable information in building uProtocol sinks or sources.
 */
fun UUri.isEmpty(): Boolean {
    return equals(UUri.getDefaultInstance())
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
 * Returns true if URI has the resource id of 0.
 *
 * @return Returns true if URI has a resource id of 0.
 */
fun UUri.isDefaultResourceId(): Boolean {
    return !isEmpty() && resourceId == DEFAULT_RESOURCE_ID
}

/**
 * Returns true if URI is of type Topic used for publish and notifications.
 *
 * @return Returns true if URI is of type Topic.
 */
fun UUri.isTopic(): Boolean {
    return !isEmpty() && resourceId >= MIN_TOPIC_ID
}


