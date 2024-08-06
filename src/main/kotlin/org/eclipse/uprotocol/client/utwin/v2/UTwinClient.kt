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

package org.eclipse.uprotocol.client.utwin.v2

import org.eclipse.uprotocol.communication.CallOptions
import org.eclipse.uprotocol.core.utwin.v2.GetLastMessagesResponse
import org.eclipse.uprotocol.v1.UUriBatch

/**
 * The uTwin client-side interface.
 *
 * UTwin is used to fetch the last published message for a given topic. This is the client-side of the
 * UTwin Service contract and communicates with a local uTwin service to fetch the last message for a given topic.
 *
 */
interface UTwinClient {
    /**
     * Fetch the last messages for a batch of topics.
     *
     * @param topics  [UUriBatch] batch of 1 or more topics to fetch the last messages for.
     * @param options The call options.
     * @return Return [GetLastMessagesResponse] wrapped in [Result] if uTwin was able to fetch the topics or
     * exception with the failure reason as UStatus, wrapped in [Result]
     */
    suspend fun getLastMessages(
        topics: UUriBatch,
        options: CallOptions = CallOptions()
    ): Result<GetLastMessagesResponse>
}