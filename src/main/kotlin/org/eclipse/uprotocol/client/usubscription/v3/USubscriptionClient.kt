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

import org.eclipse.uprotocol.communication.CallOptions
import org.eclipse.uprotocol.communication.UStatusException
import org.eclipse.uprotocol.core.usubscription.v3.*
import org.eclipse.uprotocol.transport.UListener
import org.eclipse.uprotocol.v1.UCode
import org.eclipse.uprotocol.v1.UStatus
import org.eclipse.uprotocol.v1.UUri

/**
 * The Client-side interface for communicating with the USubscription service.
 */
interface USubscriptionClient {
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
     * @param options The call options for the subscription.
     * @param handler {@link SubscriptionChangeHandler} to handle changes to subscription states
     * @return Returns the [Result] with the response UMessage or exception with the failure reason.
     */
    suspend fun subscribe(
        topic: UUri,
        listener: UListener,
        options: CallOptions = CallOptions(),
        handler: SubscriptionChangeHandler? = null
    ): Result<SubscriptionResponse>


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
    suspend fun unsubscribe(
        topic: UUri,
        listener: UListener,
        options: CallOptions = CallOptions()
    ): UStatus


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
    suspend fun unregisterListener(topic: UUri, listener: UListener): UStatus

    /**
     * Register for Subscription Change Notifications.
     *
     * This API allows producers to register to receive subscription change notifications for
     * topics that they produce only.
     *
     * NOTE: Subscribers are automatically registered to receive notifications when they call
     * `subscribe()` API passing a [SubscriptionChangeHandler] so they do not need to
     * call this API.
     *
     * @param topic The topic to register for notifications.
     * @param options The [CallOptions] to be used for the request.
     * @param handler The [SubscriptionChangeHandler] to handle the subscription changes.
     * @return [Result] with [NotificationsResponse] if uSubscription service accepts the
     * request to register the caller to be notified of subscription changes, or
     * [Result] with [UStatusException] that indicates
     * the failure reason.
     */
    suspend fun registerForNotifications(
        topic: UUri,
        options: CallOptions = CallOptions(),
        handler: SubscriptionChangeHandler? = null
    ): Result<NotificationsResponse>

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
    suspend fun unregisterForNotifications(
        topic: UUri,
        options: CallOptions = CallOptions(),
        handler: SubscriptionChangeHandler? = null
    ): Result<NotificationsResponse>

    /**
     * Fetch the list of subscribers for a given produced topic.
     *
     * @param topic The topic to fetch the subscribers for.
     * @param options The [CallOptions] to be used for the request.
     * @return [Result] with [NotificationsResponse] contains the list of subscribers, or
     * [Result] with [UStatusException] that indicates the reason
     * for the failure.
     */
    suspend fun fetchSubscribers(topic: UUri, options: CallOptions = CallOptions()): Result<FetchSubscribersResponse>

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
    suspend fun fetchSubscriptions(
        request: FetchSubscriptionsRequest,
        options: CallOptions = CallOptions()
    ): Result<FetchSubscriptionsResponse>
}