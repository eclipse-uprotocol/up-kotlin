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

package org.eclipse.uprotocol.uri.factory

import com.google.protobuf.Descriptors.ServiceDescriptor
import org.eclipse.uprotocol.Uoptions
import org.eclipse.uprotocol.v1.UUri
import org.eclipse.uprotocol.v1.uUri

object UUriFactory {
    /**
     * Builds a UUri for a protobuf generated code Service Descriptor.
     *
     * @param descriptor    The protobuf generated code Service Descriptor.
     * @param resourceId    The resource id.
     * @param authorityName The authority name.
     * @return Returns a UUri for a protobuf generated code Service Descriptor.
     */
    fun fromProto(descriptor: ServiceDescriptor, resourceId: Int, authorityName: String? = null): UUri {
        return uUri {
            ueId = descriptor.options.getExtension(Uoptions.serviceId)
            ueVersionMajor = descriptor.options.getExtension(Uoptions.serviceVersionMajor)
            this.resourceId = resourceId
            if (!authorityName.isNullOrEmpty()) {
                this.authorityName = authorityName
            }
        }
    }

    /**
     * Builds a UUri for a protobuf generated code Service Descriptor.
     *
     * @return Returns a UUri for a protobuf generated code Service Descriptor.
     */
    val ANY: UUri = uUri {
        authorityName = "*"
        ueId = 0xFFFF
        ueVersionMajor = 0xFF
        resourceId = 0xFFFF

    }
}