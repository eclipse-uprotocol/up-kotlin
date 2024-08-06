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
import org.eclipse.uprotocol.uri.UUriConstant.WILDCARD_AUTHORITY
import org.eclipse.uprotocol.uri.UUriConstant.WILDCARD_ENTITY_ID
import org.eclipse.uprotocol.uri.UUriConstant.WILDCARD_ENTITY_VERSION
import org.eclipse.uprotocol.uri.UUriConstant.WILDCARD_RESOURCE_ID
import org.eclipse.uprotocol.uri.factory.UUriFactory
import org.eclipse.uprotocol.v1.*


/**
 * Returns true if URI is of type RPC. A UUri is of type RPC if its
 * resource ID is less than MIN_TOPIC_ID and greater than RESOURCE_ID_RESPONSE.
 *
 * @return Returns true if URI is of type RPC.
 */
fun UUri.isRpcMethod(): Boolean {
    return !isEmpty() &&
            resourceId > DEFAULT_RESOURCE_ID &&
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


/**
 * Checks if the authority of the uriToMatch matches the candidateUri.
 * A match occurs if the authority name in uriToMatch is a wildcard
 * or if both URIs have the same authority name.
 *
 * @param uri The candidate URI to match against.
 * @return True if the authority names match, False otherwise.
 */
fun UUri.matchesAuthority(uri: UUri): Boolean {
    return WILDCARD_AUTHORITY == authorityName || authorityName == uri.authorityName
}

/**
 * Checks if the entity ID of the uriToMatch matches the candidateUri.
 * A match occurs if the entity ID in uriToMatch is a wildcard (0xFFFF)
 * or if the masked entity IDs of both URIs are equal.
 * The entity ID masking is performed using a bitwise AND operation with
 * 0xFFFF. If the result of the bitwise AND operation between the
 * uriToMatch's entity ID and 0xFFFF is 0xFFFF, it indicates that the
 * uriToMatch's entity ID is a wildcard and can match any entity ID.
 * Otherwise, the function checks if the masked entity IDs of both URIs
 * are equal, meaning that the relevant parts of their entity IDs match.
 *
 * @param uri The candidate URI to match against.
 * @return True if the entity IDs match, False otherwise.
 */
fun UUri.matchesEntityId(uri: UUri): Boolean {
    return (ueId and WILDCARD_ENTITY_ID).let {
        it == WILDCARD_ENTITY_ID || it == (uri.ueId and WILDCARD_ENTITY_ID)
    }
}

/**
 * Checks if the entity instance of the uriToMatch matches the candidateUri.
 * A match occurs if the upper 16 bits of the entity ID in uriToMatch are zero
 * or if the upper 16 bits of the entity IDs of both URIs are equal.
 *
 * @param uri The candidate URI to match against.
 * @return True if the entity instances match, False otherwise.
 */
fun UUri.matchesEntityInstance(uri: UUri): Boolean {
    return (ueId and -0x10000).let {
        it == 0x00000000 || it == (uri.ueId and -0x10000)
    }
}

/**
 * Checks if the entity version of the uriToMatch matches the candidateUri.
 * A match occurs if the entity version in uriToMatch is a wildcard
 * or if both URIs have the same entity version.
 *
 * @param uri The candidate URI to match against.
 * @return True if the entity versions match, False otherwise.
 */
fun UUri.matchesEntityVersion(uri: UUri): Boolean {
    return WILDCARD_ENTITY_VERSION == ueVersionMajor || ueVersionMajor == uri.ueVersionMajor
}

/**
 * Checks if the entity of the uriToMatch matches the candidateUri.
 * A match occurs if the entity ID, entity instance, and entity version
 * of both URIs match according to their respective rules.
 *
 * @param uri The candidate URI to match against.
 * @return True if the entities match, False otherwise.
 */
fun UUri.matchesEntity(uri: UUri): Boolean {
    return matchesEntityId(uri) && matchesEntityInstance(uri) && matchesEntityVersion(uri)
}

/**
 * Checks if the resource of the uriToMatch matches the candidateUri.
 * A match occurs if the resource ID in uriToMatch is a wildcard
 * or if both URIs have the same resource ID.
 *
 * @param uri The candidate URI to match against.
 * @return True if the resource IDs match, False otherwise.
 */
fun UUri.matchesResource(uri: UUri): Boolean {
    return WILDCARD_RESOURCE_ID == resourceId || resourceId == uri.resourceId
}

/**
 * Checks if the entire URI (authority, entity, and resource) of the uriToMatch
 * matches the candidateUri. A match occurs if the authority, entity, and resource
 * of both URIs match according to their respective rules.
 *
 * @param uri The candidate URI to match against.
 * @return True if the entire URIs match, False otherwise.
 */
fun UUri.matches(uri: UUri): Boolean {
    return matchesAuthority(uri) && matchesEntity(uri) && matchesResource(uri)
}


/**
 * Checks if the URI has a wildcard in any of its fields.
 *
 * @return True if the URI has a wildcard, False otherwise.
 */
fun UUri.hasWildcard(): Boolean {
    return !isEmpty() &&
            (authorityName == WILDCARD_AUTHORITY || ueId and WILDCARD_ENTITY_ID == WILDCARD_ENTITY_ID ||
                    ueVersionMajor == WILDCARD_ENTITY_VERSION || resourceId == WILDCARD_RESOURCE_ID)
}
