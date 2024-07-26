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
package org.eclipse.uprotocol.client.usubscription.v3

import kotlinx.coroutines.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.eclipse.uprotocol.communication.*
import org.eclipse.uprotocol.core.usubscription.v3.*
import org.eclipse.uprotocol.transport.UListener
import org.eclipse.uprotocol.transport.UTransport
import org.eclipse.uprotocol.uri.factory.UUriFactory
import org.eclipse.uprotocol.v1.*
import java.util.*
import java.util.logging.Logger


/**
 * Implementation of USubscriptionClient that caches state information within the object
 * and used for single tenant applications (ex. in-vehicle). The implementation uses [InMemoryRpcClient]
 * that also stores RPC correlation information within the objects
 *
 * @param transport the transport to use for sending the notifications
 * @param rpcClient the rpc client to use for sending the RPC requests
 * @param notifier the notifier to use for registering the notification listener
 */
class InMemoryUSubscriptionClient(
    private val transport: UTransport,
    private val rpcClient: RpcClient,
    private val notifier: Notifier,
    dispatcher: CoroutineDispatcher = Dispatchers.IO
) : USubscriptionClient {
    // Map to store subscription change notification handlers
    private val mHandlers = HashMap<UUri, SubscriptionChangeHandler>()

    private val scope = CoroutineScope(SupervisorJob() + dispatcher)

    private val mutex = Mutex()

    /**
     * Creates a new USubscription client passing [UTransport]
     * used to provide additional options for the RPC requests to uSubscription service.
     *
     * @param transport the transport to use for sending the notifications
     */
    constructor(transport: UTransport):this(transport, InMemoryRpcClient(transport), SimpleNotifier(transport))

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
     * with the failure if the subscription was not successful. The optional passed [SubscriptionChangeHandler]
     * is used to receive notifications of changes to the subscription status like a transition from
     * [SubscriptionStatus.State.SUBSCRIBE_PENDING] to [SubscriptionStatus.State.SUBSCRIBED] that
     * occurs when we subscribe to remote topics that the device we are on has not yet a subscriber that has
     * subscribed to said topic.
     *
     * @param topic The topic to subscribe to.
     * @param listener The listener to be called when a message is received on the topic.
     * @param options The [CallOptions] to be used for the subscription.
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
                    // When registering the listener fails, we have end up in a situation where we
                    // have successfully (logically) subscribed to the topic via the USubscription service,
                    // but we have not been able to register the listener with the local transport.
                    // This means that events might start getting forwarded to the local authority which
                    // are not being consumed. Apart from this inefficiency, this does not pose a real
                    // problem and since we return a failed future, the client might be inclined to try
                    // again and (eventually) succeed in registering the listener as well.
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
     * Unsubscribe from a given topic.
     *
     * The subscriber no longer wishes to be subscribed to said topic, so we issue an unsubscribe
     * request to the USubscription service. The API will return a [UStatus]. If we are unable to
     * unsubscribe to the topic with USubscription service, the listener and handler (if any) will remain registered.
     *
     * @param topic The topic to unsubscribe to.
     * @param listener The listener to be called when messages are received.
     * @param options The [CallOptions] to be used for the unsubscribe request.
     * @return Returns [UStatus] with the result from the unsubscribe request.
     */
    override suspend fun unsubscribe(
        topic: UUri,
        listener: UListener,
        options: CallOptions
    ): UStatus {
        val unsubscribeRequest = unsubscribeRequest {
            this.topic = topic
            subscriber = subscriberInfo {
                uri = transport.getSource()
            }
        }

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
     * Unregister the listener and remove any registered [SubscriptionChangeHandler] for the topic.
     *
     * This method is used to remove handlers/listeners without notifying the uSubscription service
     * so that we can be persistently subscribed even when the uE is not running.
     *
     * @param topic The topic to subscribe to.
     * @param listener The listener to be called when messages are received.
     * @return Returns [UStatus] with the status of the listener unregister request.
     */
    override suspend fun unregisterListener(topic: UUri, listener: UListener): UStatus {
        return transport.unregisterListener(topic, listener = listener).also {
            mHandlers.remove(topic)
        }
    }

    /**
     * Register for Subscription Change Notifications.
     *
     * This API allows producers to register to receive subscription change notifications for
     * topics that they produce only.
     *
     * @param topic The topic to register for notifications.
     * @param options The [CallOptions] to be used for the request.
     * @param handler The [SubscriptionChangeHandler] to handle the subscription changes.
     * @return [Result] with [NotificationsResponse] if uSubscription service accepts the
     * request to register the caller to be notified of subscription changes, or
     * [Result] with [UStatusException] that indicates
     * the failure reason.
     */
    override suspend fun registerForNotifications(
        topic: UUri,
        options: CallOptions,
        handler: SubscriptionChangeHandler?
    ): Result<NotificationsResponse> {
        val request = notificationsRequest {
            this.topic = topic
            this.subscriber = subscriberInfo { uri = transport.getSource() }
        }
        return rpcClient.invokeMethod(REGISTER_NOTIFICATIONS_METHOD, UPayload.pack(request), options)
            .mapToMessage<NotificationsResponse>().mapCatching { response ->
                mHandlers[topic]?.takeIf { it != handler}?.let {
                    throw UStatusException(UCode.ALREADY_EXISTS, "Handler already registered")
                }
                response
            }
    }

    /**
     * Unregister for subscription change notifications.
     *
     * @param topic The topic to unregister for notifications.
     * @param options The [CallOptions] to be used for the request.
     * @param handler The [SubscriptionChangeHandler] to be unregistered.
     * @return [Result] with [NotificationsResponse] if uSubscription service accepts the
     * request to unregister the caller to be notified of subscription changes, or
     * [Result] with [UStatusException] that indicates the reason for the failure.
     */
    override suspend fun unregisterForNotifications(
        topic: UUri,
        options: CallOptions,
        handler: SubscriptionChangeHandler?
    ): Result<NotificationsResponse> {
        val request = notificationsRequest {
            this.topic = topic
            this.subscriber = subscriberInfo { uri = transport.getSource() }
        }
        return rpcClient.invokeMethod(UNREGISTER_NOTIFICATIONS_METHOD, UPayload.pack(request), options)
            .mapToMessage<NotificationsResponse>().onSuccess {
                mutex.withLock {
                    mHandlers.remove(topic)
                }
            }
    }

    /**
     * Fetch the list of subscribers for a given produced topic.
     *
     * @param topic The topic to fetch the subscribers for.
     * @param options The [CallOptions] to be used for the request.
     * @return [Result] with [NotificationsResponse] contains the list of subscribers, or
     * [Result] with [UStatusException] that indicates the reason
     * for the failure.
     */
    override suspend fun fetchSubscribers(topic: UUri, options: CallOptions): Result<FetchSubscribersResponse> {
        val request = fetchSubscribersRequest {
            this.topic = topic
        }
        return rpcClient.invokeMethod(FETCH_SUBSCRIBERS_METHOD, UPayload.pack(request), options)
            .mapToMessage<FetchSubscribersResponse>()
    }

    /**
     * Fetch list of Subscriptions for a given topic.
     *
     * API provides more information than `fetchSubscribers()` in that it also returns
     * [SubscribeAttributes] per subscriber that might be useful to the producer to know.
     *
     * @param request The topic to fetch subscriptions for.
     * @param options The [CallOptions] to be used for the request.
     * @return [Result] with [NotificationsResponse] contains the subscription information
     * per subscriber to the topic, or [Result] with [UStatusException] that indicates the reason
     * for the failure. [UCode.PERMISSION_DENIED] is returned if the
     * topic ue_id does not equal the callers ue_id.
     */
    override suspend fun fetchSubscriptions(
        request: FetchSubscriptionsRequest,
        options: CallOptions
    ): Result<FetchSubscriptionsResponse> {
        return rpcClient.invokeMethod(FETCH_SUBSCRIPTIONS_METHOD, UPayload.pack(request), options)
            .mapToMessage<FetchSubscriptionsResponse>()
    }

    /**
     * Close the subscription client and clean up resources.
     */
    fun close() {
        mHandlers.clear()
        scope.launch {
            notifier.unregisterNotificationListener(NOTIFICATION_TOPIC, mNotificationListener)
        }

    }


    /**
     * Handles incoming notifications from the USubscription service.
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

        // TODO: The following items eventually need to be pulled from generated code
        private val SUBSCRIBE_METHOD: UUri = UUriFactory.fromProto(USUBSCRIPTION, 1)
        private val UNSUBSCRIBE_METHOD: UUri = UUriFactory.fromProto(USUBSCRIPTION, 2)
        private val NOTIFICATION_TOPIC: UUri = UUriFactory.fromProto(USUBSCRIPTION, 0x8000)
        private val FETCH_SUBSCRIBERS_METHOD: UUri = UUriFactory.fromProto(USUBSCRIPTION, 8)
        private val FETCH_SUBSCRIPTIONS_METHOD: UUri = UUriFactory.fromProto(USUBSCRIPTION, 3)
        private val REGISTER_NOTIFICATIONS_METHOD: UUri = UUriFactory.fromProto(USUBSCRIPTION, 6)
        private val UNREGISTER_NOTIFICATIONS_METHOD: UUri = UUriFactory.fromProto(USUBSCRIPTION, 7)
    }
}