/**
 * Copyright (c) 2016-2020, Yegor Bugayenko
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
package io.wring.model;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;

/**
 * Errors.
 *
 * @author Paulo Lobo (pauloeduardolobo@gmail.com)
 * @version $Id$
 * @since 1.0
 */
public interface Errors {

    /**
     * Iterate first errors.
     * @return Events
     */
    Iterable<Error> iterate();

    /**
     * Add a new error.
     * @param title Title
     * @param description Description
     * @throws IOException If fails
     */
    void register(String title, String description);

    /**
     * Retrieves the error with given title at given time.
     *
     * @param title Error title
     * @param time Time of error
     * @return Error
     */
    Error error(String title, long time);

    /**
     * Simple implementation of Error repo.
     */
    final class Simple implements Errors {

        /**
         * Errors.
         */
        private final Collection<Error> errors;

        /**
         * Constructor.
         */
        public Simple() {
            this.errors = new ArrayList<>(0);
        }

        @Override
        public Iterable<Error> iterate() {
            return this.errors;
        }

        @Override
        public Error error(final String title, final long time) {
            throw new UnsupportedOperationException("error() not implemented");
        }

        @Override
        public void register(final String title, final String description) {
            this.errors.add(new Error.Simple(title, description));
        }
    }
}
