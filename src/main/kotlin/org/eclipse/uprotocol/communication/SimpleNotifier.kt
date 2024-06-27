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

import org.eclipse.uprotocol.transport.UListener
import org.eclipse.uprotocol.transport.UTransport
import org.eclipse.uprotocol.transport.forNotification
import org.eclipse.uprotocol.transport.setPayload
import org.eclipse.uprotocol.v1.UStatus
import org.eclipse.uprotocol.v1.UUri
import org.eclipse.uprotocol.v1.uMessage

/**
 * The following is an example implementation of the [Notifier] interface that
 * wraps the [UTransport] for implementing the notification pattern to send
 * notifications and register to receive notification events.
 *
 * *NOTE:* Developers are not required to use these APIs, they can implement their own
 * or directly use the [UTransport] to send notifications and register listeners.
 *
 * @param transport The transport to use for sending the notifications.
 */
class SimpleNotifier(private val transport: UTransport) : Notifier {
    /**
     * Send a notification to a given topic.
     *
     * @param topic The topic to send the notification to.
     * @param destination The destination to send the notification to.
     * @param payload The payload to send with the notification.
     * @return Returns the [UStatus] with the status of the notification.
     */
    override suspend fun notify(topic: UUri, destination: UUri, payload: UPayload?): UStatus {
        return transport.send( uMessage {
            forNotification(topic, destination)
            payload?.let {
                setPayload(it)
            }
        })
    }


    /**
     * Register a listener for a notification topic.
     *
     * @param topic The topic to register the listener to.
     * @param listener The listener to be called when a message is received on the topic.
     * @return Returns the [UStatus] with the status of the listener registration.
     */
    override suspend fun registerNotificationListener(topic: UUri, listener: UListener): UStatus {
        return transport.registerListener(topic, transport.getSource(), listener)
    }


    /**
     * Unregister a listener from a notification topic.
     *
     * @param topic The topic to unregister the listener from.
     * @param listener The listener to be unregistered from the topic.
     * @return Returns the [UStatus] with the status of the listener that was unregistered.
     */
    override suspend fun unregisterNotificationListener(topic: UUri, listener: UListener): UStatus {
        return transport.unregisterListener(topic, transport.getSource(), listener)
    }
}