package org.approvej.http;

import java.net.URI;
import java.util.List;
import java.util.SortedMap;
import org.jspecify.annotations.NullMarked;

/**
 * A {@link ReceivedHttpRequest} represents a request that was received by the {@link
 * HttpStubServer}.
 *
 * @param method the HTTP method
 * @param uri the {@link URI} that was called
 * @param headers the {@link SortedMap} of headers
 * @param body the received body
 */
@NullMarked
public record ReceivedHttpRequest(
    String method, URI uri, SortedMap<String, List<String>> headers, String body) {}
