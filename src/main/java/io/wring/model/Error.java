/**
 * Copyright (c) 2016-2019, Wring.io
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
import org.xembly.Directive;

/**
 * Error occurred during pipe execution.
 *
 * @author Paulo Lobo (pauloeduardolobo@gmail.com)
 * @version $Id$
 * @since 1.0
 */
public interface Error {

    /**
     * Print it into xembly.
     * @return Xembly directives
     * @throws IOException If fails
     */
    Iterable<Directive> asXembly() throws IOException;

    /**
     * Delete it.
     * @throws IOException If fails
     */
    void delete() throws IOException;

    /**
     * Simple Error implementation.
     * @todo #76:30min Implement Error.Simple asXembly and delete method.
     *  asXembly must return a xembly directive with element 'error' and
     *  child elements 'title'
     *  and 'description'.
     */
    final class Simple implements Error {

        /**
         * Error title.
         */
        private final String title;

        /**
         * Error description.
         */
        private final String description;

        /**
         * Constructor.
         * @param title Title
         * @param description Description
         */
        public Simple(final String title, final String description) {
            this.title = title;
            this.description = description;
        }

        @Override
        public Iterable<Directive> asXembly() throws IOException {
            throw new UnsupportedOperationException("not implemented");
        }

        @Override
        public void delete() throws IOException {
            throw new UnsupportedOperationException("delete() not implemented");
        }
    }
}
