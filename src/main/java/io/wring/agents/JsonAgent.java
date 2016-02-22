/**
 * Copyright (c) 2016, wring.io
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met: 1) Redistributions of source code must retain the above
 * copyright notice, this list of conditions and the following
 * disclaimer. 2) Redistributions in binary form must reproduce the above
 * copyright notice, this list of conditions and the following
 * disclaimer in the documentation and/or other materials provided
 * with the distribution. 3) Neither the name of the wring.io nor
 * the names of its contributors may be used to endorse or promote
 * products derived from this software without specific prior written
 * permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT
 * NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 * FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL
 * THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
 * INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
 * STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
 * OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package io.wring.agents;

import io.wring.model.Events;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import javax.json.Json;
import javax.json.JsonObject;
import org.apache.commons.io.IOUtils;

/**
 * Agent in JSON.
 *
 * @author Yegor Bugayenko (yegor@teamed.io)
 * @version $Id: c79829f9e91907f21c716854779af4233e496fa9 $
 * @since 1.0
 */
final class JsonAgent implements Agent {

    /**
     * JSON config.
     */
    private final transient String json;

    /**
     * Ctor.
     * @param cfg JSON config
     */
    JsonAgent(final String cfg) {
        this.json = cfg;
    }

    @Override
    public void push(final Events events) throws IOException {
        this.agent().push(events);
    }

    /**
     * Make an agent.
     * @return Agent
     */
    private Agent agent() {
        final JsonObject obj = Json.createReader(
            IOUtils.toInputStream(this.json)
        ).readObject();
        final Class<?> type;
        try {
            type = Class.forName(obj.getString("class"));
        } catch (final ClassNotFoundException ex) {
            throw new IllegalStateException(ex);
        }
        final Constructor<?> ctor;
        try {
            ctor = type.getConstructor(JsonObject.class);
        } catch (final NoSuchMethodException ex) {
            throw new IllegalStateException(ex);
        }
        try {
            return Agent.class.cast(ctor.newInstance(obj));
        } catch (final InstantiationException
            | IllegalAccessException
            | InvocationTargetException ex) {
            throw new IllegalStateException(ex);
        }
    }

}
