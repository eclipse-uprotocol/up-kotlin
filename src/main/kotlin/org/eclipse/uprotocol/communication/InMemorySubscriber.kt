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

import org.eclipse.uprotocol.core.usubscription.v3.*
import org.eclipse.uprotocol.transport.UListener
import org.eclipse.uprotocol.transport.UTransport
import org.eclipse.uprotocol.uri.factory.UUriFactory
import org.eclipse.uprotocol.v1.UCode
import org.eclipse.uprotocol.v1.UStatus
import org.eclipse.uprotocol.v1.UUri
import org.eclipse.uprotocol.v1.uStatus
import java.util.concurrent.CompletionException
import java.util.concurrent.CompletionStage

/**
 * The following is an example implementation of the [Subscriber] interface that
 * wraps the [UTransport] for implementing the Subscriber-side of the pub/sub
 * messaging pattern to allow developers to subscribe and unsubscribe to topics. This
 * implementation uses the [InMemoryRpcClient] to send the subscription request
 * to the uSubscription service.
 *
 * *_NOTE:_* Developers are not required to use these APIs, they can implement their own
 * or directly use the [UTransport] communicate with the uSubscription
 * services and register their publish message listener.
 * @param transport the transport to use for sending the notifications
 * @param rpcClient the rpc client to use for sending the RPC requests
 */
class InMemorySubscriber(private val transport: UTransport, private val rpcClient: RpcClient) : Subscriber {
    /**
     * Subscribe to a given topic.
     *
     * The API will return a [CompletionStage] with the response [SubscriptionResponse] or exception
     * with the failure if the subscription was not successful. The API will also register the listener to be
     * called when messages are received.
     *
     * @param topic The topic to subscribe to.
     * @param listener The listener to be called when a message is received on the topic.
     * @param options The call options for the subscription.
     * @return Returns the CompletionStage with the response UMessage or exception with the failure
     * reason as [UStatus].
     */
    override suspend fun subscribe(
        topic: UUri,
        listener: UListener,
        options: CallOptions
    ): Result<SubscriptionResponse> {
        val subscribe: UUri = UUriFactory.fromProto(
            USubscriptionProto.getDescriptor().services[0], METHOD_SUBSCRIBE
        )

        val request = subscriptionRequest {
            this.topic = topic
            this.subscriber = subscriberInfo { uri = transport.getSource() }
        }

        return rpcClient.invokeMethod(subscribe, UPayload.pack(request), options).mapToMessage<SubscriptionResponse>()
            .also {
                transport.registerListener(topic, listener = listener)
            }
    }


    /**
     * Unsubscribe to a given topic.
     *
     * The subscriber no longer wishes to be subscribed to said topic, so we issue an unsubscribe
     * request to the USubscription service.
     *
     * @param topic The topic to unsubscribe to.
     * @param listener The listener to be called when a message is received on the topic.
     * @param options The call options for the subscription.
     * @return Returns [UStatus] with the result from the unsubscribe request.
     */
    override suspend fun unsubscribe(topic: UUri, listener: UListener, options: CallOptions): UStatus {
        val unsubscribe: UUri = UUriFactory.fromProto(
            USubscriptionProto.getDescriptor().services[0], METHOD_UNSUBSCRIBE
        )
        val unsubscribeRequest = unsubscribeRequest { this.topic = topic }

        return rpcClient.invokeMethod(
            unsubscribe, UPayload.pack(unsubscribeRequest), options
        ).mapToMessage<UnsubscribeResponse>().fold({
            transport.unregisterListener(topic, listener = listener)
        }, { e ->
            val exception = if (e is CompletionException) (e.cause ?: e) else e
            when (exception) {
                is UStatusException -> {
                    exception.status
                }

                else -> {
                    uStatus {
                        code = UCode.INVALID_ARGUMENT
                        message = exception.message ?: "Invalid argument"
                    }
                }
            }
        })
    }


    /**
     * Unregister a listener from a topic.
     *
     * This method will only unregister the listener for a given subscription thus allowing a uE to stay
     * subscribed even if the listener is removed.
     *
     * @param topic The topic to subscribe to.
     * @param listener The listener to be called when a message is received on the topic.
     * @return Returns [UStatus] with the status of the listener unregister request.
     */
    override suspend fun unregisterListener(topic: UUri, listener: UListener): UStatus {
        return transport.unregisterListener(topic, listener = listener)
    }

    companion object {
        private const val METHOD_SUBSCRIBE = 1 // TODO: Fetch this from proto generated code
        private const val METHOD_UNSUBSCRIBE = 2 // TODO: Fetch this from proto generated code
    }
}