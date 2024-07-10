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

package org.eclipse.uprotocol.uri

object UUriConstant {
    const val WILDCARD_AUTHORITY: String = "*"

    /**
     * The wildcard id for a field.
     */
    const val WILDCARD_ENTITY_ID: Int = 0xFFFF

    /**
     * major version wildcard
     */
    const val WILDCARD_ENTITY_VERSION: Int = 0xFF

    const val WILDCARD_RESOURCE_ID: Int = 0xFFFF

    /**
     * The minimum publish/notification topic id for a URI.
     */
    const val MIN_TOPIC_ID = 0x8000

    /**
     * The Default resource id.
     */
    const val DEFAULT_RESOURCE_ID = 0
}