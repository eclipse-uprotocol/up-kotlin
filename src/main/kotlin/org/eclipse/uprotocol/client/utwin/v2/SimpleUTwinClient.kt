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
package org.eclipse.uprotocol.client.utwin.v2

import com.google.protobuf.Descriptors.ServiceDescriptor
import org.eclipse.uprotocol.communication.*
import org.eclipse.uprotocol.core.utwin.v2.GetLastMessagesRequest
import org.eclipse.uprotocol.core.utwin.v2.GetLastMessagesResponse
import org.eclipse.uprotocol.core.utwin.v2.UTwinProto
import org.eclipse.uprotocol.uri.factory.UUriFactory
import org.eclipse.uprotocol.v1.UCode
import org.eclipse.uprotocol.v1.UUri
import org.eclipse.uprotocol.v1.UUriBatch

/**
 * The uTwin client implementation using the RpcClient uP-L2 communication layer interface.
 * @param rpcClient The RPC client to use for communication.
 */
class SimpleUTwinClient(private val rpcClient: RpcClient) : UTwinClient {
    /**
     * Fetch the last messages for a batch of topics.
     *
     * @param topics  [UUriBatch] batch of 1 or more topics to fetch the last messages for.
     * @param options The call options.
     * @return Return [GetLastMessagesResponse] wrapped in [Result] if uTwin was able to fetch the topics or
     * exception with the failure reason as UStatus, wrapped in [Result]
     */
    override suspend fun getLastMessages(topics: UUriBatch, options: CallOptions): Result<GetLastMessagesResponse> {
        // Check if topics is empty
        if (topics == UUriBatch.getDefaultInstance()) {
            return Result.failure(UStatusException(UCode.INVALID_ARGUMENT, "topics must not be empty"))
        }
        val request = GetLastMessagesRequest.newBuilder().setTopics(topics).build()
        return rpcClient.invokeMethod(GETLASTMESSAGE_METHOD, UPayload.pack(request), options).mapToMessage()
    }

    companion object {
        private val UTWIN: ServiceDescriptor = UTwinProto.getDescriptor().services[0]

        // TODO: The following items eventually need to be pulled from generated code
        private val GETLASTMESSAGE_METHOD: UUri = UUriFactory.fromProto(UTWIN, 1)
    }
}