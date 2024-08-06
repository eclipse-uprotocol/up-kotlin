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

package org.eclipse.uprotocol.uri.validator

import org.eclipse.uprotocol.uri.factory.UUriFactory
import org.eclipse.uprotocol.v1.UUri
import org.eclipse.uprotocol.v1.uAttributes
import org.eclipse.uprotocol.v1.uUri
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

class UriFilterTest {
    @Test
    @DisplayName("Test matches with empty source and sink UUri and empty UAttributes")
    fun testMatchesWithEmptySourceAndSinkUUriAndEmptyUAttributes() {
        val uriFilter = UriFilter(uUri { }, uUri { })
        assertTrue(uriFilter.matches(uAttributes { }))
    }

    @Test
    @DisplayName("Test matches with empty source and sink UUri and non-empty UAttributes")
    fun testMatchesWithEmptySourceAndSinkUUriAndNonEmptyUAttributes() {
        val uriFilter = UriFilter(uUri { }, uUri { })
        assertFalse(
            uriFilter.matches(
                uAttributes {
                    source = SOURCE_URI
                    sink = SOURCE_URI
                }
            )
        )
    }

    @Test
    @DisplayName("Test matches with non-empty source and sink UUri and empty UAttributes")
    fun testMatchesWithNonEmptySourceAndSinkUUriAndEmptyUAttributes() {
        val uriFilter = UriFilter(SOURCE_URI, SINK_URI)
        assertFalse(uriFilter.matches(uAttributes { }))
    }

    @Test
    @DisplayName("Test matches with non-empty source and sink UUri and non-matching UAttributes")
    fun testMatchesWithNonEmptySourceAndSinkUUriAndNonMatchingUAttributes() {
        val uriFilter = UriFilter(SOURCE_URI, SINK_URI)
        assertFalse(
            uriFilter.matches(
                uAttributes {
                    source = OTHER_URI
                    sink = OTHER_URI
                }
            )
        )
    }

    @Test
    @DisplayName("Test matches with non-empty source and sink UUri and matching UAttributes")
    fun testMatchesWithNonEmptySourceAndSinkUUriAndMatchingUAttributes() {
        val uriFilter = UriFilter(SOURCE_URI, SINK_URI)
        assertTrue(
            uriFilter.matches(
                uAttributes {
                    source = SOURCE_URI
                    sink = SINK_URI
                }
            )
        )
    }

    @Test
    @DisplayName("Test matches source and sink UUri and matching source and non-matching sink UAttributes")
    fun testMatchesWithNonEmptySourceAndSinkUUriAndMatchingSourceAndNonMatchingSinkUAttributes() {
        val uriFilter = UriFilter(SOURCE_URI, SINK_URI)
        assertFalse(
            uriFilter.matches(
                uAttributes {
                    source = SOURCE_URI
                    sink = OTHER_URI
                }
            )
        )
    }

    @Test
    @DisplayName("Test matches source and sink UUri and non-matching source and matching sink UAttributes")
    fun testMatchesWithNonEmptySourceAndSinkUUriAndNonMatchingSourceAndMatchingSinkUAttributes() {
        val uriFilter = UriFilter(SOURCE_URI, SINK_URI)
        assertFalse(
            uriFilter.matches(
                uAttributes {
                    source = OTHER_URI
                    sink = SINK_URI
                }
            )
        )
    }

    @Test
    @DisplayName("Test fetching the source and sink and verifying they match what was passed to the constructor")
    fun testFetchingSourceAndSink() {
        val uriFilter = UriFilter(SOURCE_URI, SINK_URI)
        assertTrue(SOURCE_URI == uriFilter.source)
        assertTrue(SINK_URI == uriFilter.sink)
    }

    @Test
    @DisplayName("Test matching when source is UriFactory.ANY and sink is not UriFactory.ANY")
    fun testMatchingWhenSourceIsUriFactoryAnyAndSinkIsNotUriFactoryAny() {
        val uriFilter = UriFilter(UUriFactory.ANY, SINK_URI)
        assertTrue(uriFilter.matches(
            uAttributes {
                sink = SINK_URI
            }
        ))
    }

    @Test
    @DisplayName("Test matching when sink is UriFactory.ANY and source is not UriFactory.ANY")
    fun testMatchingWhenSinkIsUriFactoryAnyAndSourceIsNotUriFactoryAny() {
        val uriFilter = UriFilter(SOURCE_URI, UUriFactory.ANY)
        assertTrue(uriFilter.matches(
            uAttributes {
                source = SOURCE_URI
            }))
    }

    @Test
    @DisplayName("Test UriFilter constructor")
    fun testUriFilterConstructor() {
        val uriFilter1 = UriFilter()
        assertEquals(UUriFactory.ANY, uriFilter1.source)
        assertEquals(UUriFactory.ANY, uriFilter1.sink)

        val uriFilter2 = UriFilter(SOURCE_URI)
        assertEquals(SOURCE_URI, uriFilter2.source)
        assertEquals(UUriFactory.ANY, uriFilter2.sink)

        val uriFilter3 = UriFilter(sink = SINK_URI)
        assertEquals(UUriFactory.ANY, uriFilter3.source)
        assertEquals(SINK_URI, uriFilter3.sink)

        val uriFilter4 = UriFilter(SOURCE_URI, SINK_URI)
        assertEquals(SOURCE_URI, uriFilter4.source)
        assertEquals(SINK_URI, uriFilter4.sink)
    }

    companion object {
        private val SOURCE_URI: UUri = UUri.newBuilder().setAuthorityName("source").build()
        private val SINK_URI: UUri = UUri.newBuilder().setAuthorityName("sink").build()
        private val OTHER_URI: UUri = UUri.newBuilder().setAuthorityName("other").build()
    }
}