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
package org.eclipse.uprotocol.communication

import org.eclipse.uprotocol.transport.UTransport
import org.eclipse.uprotocol.transport.forPublication
import org.eclipse.uprotocol.transport.setPayload
import org.eclipse.uprotocol.v1.UStatus
import org.eclipse.uprotocol.v1.UUri
import org.eclipse.uprotocol.v1.uMessage


/**
 * The following is an example implementation of the [Publisher] interface that
 * wraps the [UTransport] for implementing the notification pattern to send
 * notifications.
 *
 * *NOTE:* Developers are not required to use these APIs, they can implement their own
 * or directly use the [UTransport] to send notifications and register listeners.
 *
 * @param transport The transport to use for sending the notifications.
 */
class SimplePublisher(private val transport: UTransport) : Publisher {
    /**
     * Publish a message to a topic passing [UPayload] as the payload.
     *
     * @param topic The topic to publish to.
     * @param payload The [UPayload] to publish.
     * @return
     */
    override suspend fun publish(topic: UUri, payload: UPayload?): UStatus {
        return transport.send(uMessage {
            forPublication(topic)
            payload?.let { setPayload(it) }
        })
    }
}