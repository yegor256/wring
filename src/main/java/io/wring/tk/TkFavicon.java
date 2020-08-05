/*
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
package io.wring.tk;

import com.google.common.collect.Iterables;
import com.jcabi.aspects.Tv;
import io.wring.model.Base;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import javax.imageio.ImageIO;
import org.takes.Request;
import org.takes.Response;
import org.takes.Take;
import org.takes.rs.RsWithBody;
import org.takes.rs.RsWithType;

/**
 * List of events.
 *
 * @since 0.5
 */
final class TkFavicon implements Take {

    /**
     * Max to show.
     */
    private static final int MAX = 20;

    /**
     * Base.
     */
    private final transient Base base;

    /**
     * Ctor.
     * @param bse Base
     */
    TkFavicon(final Base bse) {
        this.base = bse;
    }

    @Override
    public Response act(final Request req) throws IOException {
        final int width = 64;
        final int height = 64;
        final BufferedImage image = new BufferedImage(
            width, height, BufferedImage.TYPE_INT_RGB
        );
        final Graphics2D graph = Graphics2D.class.cast(image.getGraphics());
        // @checkstyle MagicNumber (1 line)
        graph.setColor(new Color(0x36, 0x7a, 0xc3));
        graph.fillRect(0, 0, width, height);
        final int total = Iterables.size(
            Iterables.limit(
                this.base.user(new RqUser(req).urn()).events().iterate(),
                TkFavicon.MAX
            )
        );
        if (total > 0) {
            final String text;
            if (total >= TkFavicon.MAX) {
                text = String.format("%d", TkFavicon.MAX);
            } else {
                text = Integer.toString(total);
            }
            graph.setColor(Color.WHITE);
            graph.setFont(new Font(Font.SANS_SERIF, Font.BOLD, height / 2));
            graph.setRenderingHint(
                RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON
            );
            graph.drawString(
                text,
                width - width / Tv.TEN
                    - graph.getFontMetrics().stringWidth(text),
                height - height / Tv.TEN
            );
        }
        final ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(image, "gif", baos);
        return new RsWithType(
            new RsWithBody(baos.toByteArray()),
            "image/gif"
        );
    }

}
