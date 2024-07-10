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

import kotlinx.coroutines.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.eclipse.uprotocol.core.usubscription.v3.*
import org.eclipse.uprotocol.transport.UListener
import org.eclipse.uprotocol.transport.UTransport
import org.eclipse.uprotocol.uri.factory.UUriFactory
import org.eclipse.uprotocol.v1.*
import java.util.*
import java.util.logging.Logger

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
class InMemorySubscriber(
    private val transport: UTransport,
    private val rpcClient: RpcClient,
    private val notifier: Notifier,
    dispatcher: CoroutineDispatcher = Dispatchers.IO
) : Subscriber {
    // Map to store subscription change notification handlers
    private val mHandlers = HashMap<UUri, SubscriptionChangeHandler>()

    private val scope = CoroutineScope(SupervisorJob() + dispatcher)

    private val mutex = Mutex()

    // transport Notification listener that will process subscription change notifications
    private val mNotificationListener = UListener { message ->
        handleNotifications(message)
    }

    init {
        scope.launch {
            notifier.registerNotificationListener(NOTIFICATION_TOPIC, mNotificationListener)
        }
    }

    /**
     * Subscribe to a given topic.
     *
     * The API will return a [Result] with the response [SubscriptionResponse] or exception
     * with the failure if the subscription was not successful. The API will also register the listener to be
     * called when messages are received.
     *
     * @param topic The topic to subscribe to.
     * @param listener The listener to be called when a message is received on the topic.
     * @param options The call options for the subscription.
     * @return Returns the [Result] with the response UMessage or exception with the failure
     * reason as [UStatus].
     */
    override suspend fun subscribe(
        topic: UUri,
        listener: UListener,
        options: CallOptions,
        handler: SubscriptionChangeHandler?
    ): Result<SubscriptionResponse> {

        val request = subscriptionRequest {
            this.topic = topic
            this.subscriber = subscriberInfo { uri = transport.getSource() }
        }

        return rpcClient.invokeMethod(SUBSCRIBE_METHOD, UPayload.pack(request), options)
            .mapToMessage<SubscriptionResponse>()
            .onSuccess {
                val state = it.status.state
                if (state == SubscriptionStatus.State.SUBSCRIBED || state == SubscriptionStatus.State.SUBSCRIBE_PENDING) {
                    transport.registerListener(topic, listener = listener)
                }
            }.mapCatching { response ->
                handler?.let { handler ->
                    mutex.withLock {
                        if (mHandlers[topic] != null && mHandlers[topic] != handler) {
                            throw UStatusException(UCode.ALREADY_EXISTS, "Handler already registered")
                        } else if (mHandlers[topic] == handler) {
                            response
                        } else {
                            mHandlers[topic] = handler
                            response
                        }
                    }
                } ?: response
            }
    }


    /**
     * Unsubscribe to a given topic.
     *
     * The subscriber no longer wishes to be subscribed to said topic so we issue a unsubscribe
     * request to the USubscription service. The API will return a [UStatus]. If we are unable to
     * unsubscribe to the topic with USubscription service, the listener and handler (if any) will remain registered.
     *
     * @param topic The topic to unsubscribe to.
     * @param listener The listener to be called when a message is received on the topic.
     * @param options The call options for the subscription.
     * @return Returns [UStatus] with the result from the unsubscribe request.
     */
    override suspend fun unsubscribe(
        topic: UUri,
        listener: UListener,
        options: CallOptions
    ): UStatus {
        val unsubscribeRequest = unsubscribeRequest { this.topic = topic }

        return rpcClient.invokeMethod(
            UNSUBSCRIBE_METHOD, UPayload.pack(unsubscribeRequest), options
        ).mapToMessage<UnsubscribeResponse>().fold({
            mutex.withLock {
                mHandlers.remove(topic)
            }
            transport.unregisterListener(topic, listener = listener)
        }, { e ->
            when (e) {
                is UStatusException -> {
                    e.status
                }

                else -> {
                    uStatus {
                        code = UCode.INVALID_ARGUMENT
                        message = e.message ?: "Invalid argument"
                    }
                }
            }
        })
    }


    /**
     * Unregister a listener and remove any registered [SubscriptionChangeHandler] for the topic.
     *
     * This method is used to remove handlers/listeners without notifying the uSubscription service
     * so that we can be persistently subscribed even when the uE is not running.
     *
     * @param topic The topic to subscribe to.
     * @param listener The listener to be called when a message is received on the topic.
     * @return Returns [UStatus] with the status of the listener unregister request.
     */
    override suspend fun unregisterListener(topic: UUri, listener: UListener): UStatus {
        return transport.unregisterListener(topic, listener = listener).also {
            mHandlers.remove(topic)
        }
    }

    fun close() {
        mHandlers.clear()
        scope.launch {
            notifier.unregisterNotificationListener(NOTIFICATION_TOPIC, mNotificationListener)
        }

    }


    /**
     * Handle incoming notifications from the USubscription service.
     *
     * @param message The notification message from the USubscription service
     */
    private fun handleNotifications(message: UMessage) {
        // Ignore messages that are not notifications
        message.takeIf { it.attributes.type == UMessageType.UMESSAGE_TYPE_NOTIFICATION }?.let { msg ->
            // Unpack the notification message from uSubscription called Update
            UPayload.unpack<Update>(msg.payload, msg.attributes.payloadFormat)?.let {
                // Check if we have a handler registered for the subscription change notification for the specific
                // topic that triggered the subscription change notification. It is very possible that the client
                // did not register one to begin with (ex/ they don't care to receive it)
                try{
                    mHandlers[it.topic]?.handleSubscriptionChange(it.topic, it.status)
                }catch (e:Exception){
                    Logger.getGlobal().info(e.message)
                }
            }
        }
    }

    companion object {
        private val USUBSCRIPTION = USubscriptionProto.getDescriptor().services[0]

        // TODO: The following items need to be pulled from generated code
        private val SUBSCRIBE_METHOD: UUri = UUriFactory.fromProto(USUBSCRIPTION, 1)
        private val UNSUBSCRIBE_METHOD: UUri = UUriFactory.fromProto(USUBSCRIPTION, 2)
        private val NOTIFICATION_TOPIC: UUri = UUriFactory.fromProto(USUBSCRIPTION, 0x8000)
    }
}