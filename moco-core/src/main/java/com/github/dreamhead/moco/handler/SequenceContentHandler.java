package com.github.dreamhead.moco.handler;

import com.github.dreamhead.moco.MocoConfig;
import com.github.dreamhead.moco.ResponseHandler;
import com.google.common.base.Function;
import com.google.common.collect.FluentIterable;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;

import static com.google.common.collect.FluentIterable.from;
import static com.google.common.collect.ImmutableList.copyOf;

public class SequenceContentHandler implements ResponseHandler {
    private final ResponseHandler[] handlers;
    private int index;

    public SequenceContentHandler(final ResponseHandler[] handlers) {
        this.handlers = handlers;
    }

    @Override
    public void writeToResponse(FullHttpRequest request, FullHttpResponse response) {
        handlers[current()].writeToResponse(request, response);
    }

    private int current() {
        int current = this.index;
        if (++index >= handlers.length) {
            index = handlers.length - 1;
        }

        return current;
    }

    @Override
    public ResponseHandler apply(final MocoConfig config) {
        FluentIterable<ResponseHandler> transformedResources = from(copyOf(handlers)).transform(applyConfig(config));
        return new SequenceContentHandler(transformedResources.toArray(ResponseHandler.class));
    }

    private Function<ResponseHandler, ResponseHandler> applyConfig(final MocoConfig config) {
        return new Function<ResponseHandler, ResponseHandler>() {
            @Override
            public ResponseHandler apply(ResponseHandler input) {
                return input.apply(config);
            }
        };
    }
}
