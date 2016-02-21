/**
 * Copyright (c) 2016, Yegor Bugayenko
 * All rights reserved.
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
package io.wring.dynamo;

import com.jcabi.dynamo.Attributes;
import com.jcabi.dynamo.Item;
import io.wring.model.Pipe;
import java.io.IOException;
import org.xembly.Directive;
import org.xembly.Directives;

/**
 * Dynamo pipe.
 *
 * @author Yegor Bugayenko (yegor@teamed.io)
 * @version $Id$
 * @since 1.0
 */
public final class DyPipe implements Pipe {

    /**
     * The item.
     */
    private final transient Item item;

    /**
     * Ctor.
     * @param itm Item with pitch
     */
    public DyPipe(final Item itm) {
        this.item = itm;
    }

    @Override
    public Iterable<Directive> asXembly() throws IOException {
        return new Directives()
            .add("pipe")
            .add("id").set(this.item.get("id").getN()).up()
            .add("text").set(this.item.get("text").getS()).up();
    }

    @Override
    public void delete() throws IOException {
        this.item.frame().table().delete(
            new Attributes()
                .with("urn", this.item.get("urn").getS())
                .with("id", this.item.get("id").getN())
        );
    }

}
