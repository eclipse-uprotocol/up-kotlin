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

package org.eclipse.uprotocol.transport

import org.eclipse.uprotocol.v1.UCode
import org.eclipse.uprotocol.v1.UMessage
import org.eclipse.uprotocol.v1.UStatus
import org.eclipse.uprotocol.v1.UUri


/**
 * UTransport is the uP-L1 interface that provides a common API for uE developers to send and receive messages.
 * UTransport implementations contain the details for connecting to the underlying transport technology and
 * sending UMessage using the configured technology. For more information please refer to
 * https://github.com/eclipse-uprotocol/up-spec/blob/main/up-l1/README.adoc.
 */
interface UTransport {
    /**
     * Send a message over the transport.
     * @param message the [UMessage] to be sent.
     * @return Returns [UStatus] with [UCode] set to the status code (successful or failure).
     */
    fun send(message: UMessage): UStatus

    /**
     * Register `UListener` for `UUri` source and sink filters to be called when a message is received.
     *
     * @param sourceFilter The UAttributes::source address pattern that the message to receive needs to match.
     * @param sinkFilter The UAttributes::sink address pattern that the message to receive needs to match.
     * @param listener The [UListener] that will be executed when the message is
     * received on the given `UUri`.
     * @return Returns [UStatus] with [UCode.OK] if the listener is registered
     * correctly, otherwise it returns with the appropriate failure.
     */
    fun registerListener(sourceFilter: UUri, sinkFilter: UUri? = null, listener: UListener): UStatus

    /**
     * Unregister `UListener` for `UUri` source and sink filters. Messages arriving on this topic will
     * no longer be processed by this listener.
     *
     * @param sourceFilter The UAttributes::source address pattern that the message to receive needs to match.
     * @param sinkFilter The UAttributes::sink address pattern that the message to receive needs to match.
     * @param listener The [UListener] that will no longer want to be registered to receive
     * messages.
     * @return Returns [UStatus] with [UCode.OK] if the listener is unregistered
     * correctly, otherwise it returns with the appropriate failure.
     */
    fun unregisterListener(sourceFilter: UUri, sinkFilter: UUri? = null, listener: UListener): UStatus

    /**
     * Return the source address for the uE (authority, entity, and resource information)
     *
     * @return [UUri] containing the source address
     */
    fun getSource(): UUri
}