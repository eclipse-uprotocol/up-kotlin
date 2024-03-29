/*
 * Copyright (c) 2024 General Motors GTO LLC
 *
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *
 * SPDX-FileType: SOURCE
 * SPDX-FileCopyrightText: 2024 General Motors GTO LLC
 * SPDX-License-Identifier: Apache-2.0
 */

package org.eclipse.uprotocol.uri.factory

import com.google.protobuf.Descriptors.ServiceDescriptor
import org.eclipse.uprotocol.UprotocolOptions
import org.eclipse.uprotocol.v1.UEntity
import org.eclipse.uprotocol.v1.uEntity


/**
 * Create UEntity to/from proto information
 */
object UEntityFactory {
    /**
     * Builds a UEntity for a protobuf generated code Service Descriptor.
     * @param descriptor The protobuf generated code Service Descriptor.
     * @return Returns a UEntity for a protobuf generated code Service Descriptor.
     */
    fun fromProto(descriptor: ServiceDescriptor): UEntity {
        return uEntity {
             descriptor.options.getExtension(UprotocolOptions.name)?.let{ name = it }
             descriptor.options.getExtension(UprotocolOptions.id)?.let{ id = it}
             descriptor.options.getExtension(UprotocolOptions.versionMajor)?.let{versionMajor = it}
        }
    }
}