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
package io.wring.dynamo;

import com.amazonaws.services.dynamodbv2.model.AttributeAction;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.dynamodbv2.model.AttributeValueUpdate;
import com.jcabi.dynamo.Attributes;
import com.jcabi.dynamo.Item;
import io.wring.model.Event;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.xml.bind.DatatypeConverter;
import org.apache.commons.text.StringEscapeUtils;
import org.xembly.Directive;
import org.xembly.Directives;
import org.xembly.Xembler;

/**
 * Dynamo event.
 *
 * @author Yegor Bugayenko (yegor256@gmail.com)
 * @version $Id$
 * @since 1.0
 * @checkstyle MultipleStringLiteralsCheck (500 lines)
 */
@SuppressWarnings("PMD.AvoidDuplicateLiterals")
public final class DyEvent implements Event {

    /**
     * The item.
     */
    private final transient Item item;

    /**
     * Ctor.
     * @param itm Item with pitch
     */
    public DyEvent(final Item itm) {
        this.item = itm;
    }

    @Override
    public Iterable<Directive> asXembly() throws IOException {
        final String text = this.item.get("text").getS();
        return new Directives()
            .add("event")
            .add("urn").set(this.item.get("urn").getS()).up()
            .add("rank").set(this.item.get("rank").getN()).up()
            .add("title")
            .set(Xembler.escape(this.item.get("title").getS())).up()
            .add("text")
            .set(Xembler.escape(text)).up()
            .add("md5")
            .set(DyEvent.hash(text)).up()
            .add("html")
            .set(Xembler.escape(DyEvent.html(text))).up();
    }

    @Override
    public void delete() throws IOException {
        this.item.frame().table().delete(
            new Attributes()
                .with("urn", this.item.get("urn").getS())
                .with("title", this.item.get("title").getS())
        );
    }

    @Override
    public void vote(final int points) throws IOException {
        this.item.put(
            "rank",
            new AttributeValueUpdate()
                .withAction(AttributeAction.ADD)
                .withValue(new AttributeValue().withN(Integer.toString(points)))
        );
    }

    /**
     * To md5 hash.
     * @param text The text
     * @return The hash
     */
    private static String hash(final String text) {
        final MessageDigest digest;
        try {
            digest = MessageDigest.getInstance("MD5");
        } catch (final NoSuchAlgorithmException ex) {
            throw new IllegalStateException(ex);
        }
        digest.update(text.getBytes());
        return DatatypeConverter.printHexBinary(
            digest.digest()
        );
    }

    /**
     * To HTML.
     * @param text Text in Markdown (simplified)
     * @return HTML
     */
    private static String html(final String text) {
        final Pattern ptn = Pattern.compile("\\[([^]]+)]\\(([^)]+)\\)");
        final Matcher mtr = ptn.matcher(StringEscapeUtils.escapeHtml4(text));
        final StringBuffer out = new StringBuffer(text.length());
        while (mtr.find()) {
            mtr.appendReplacement(
                out,
                String.format(
                    "<a href='%s'>%s</a>",
                    mtr.group(2), mtr.group(1)
                ).replace("$", "\\$")
            );
        }
        mtr.appendTail(out);
        return out.toString();
    }

}

