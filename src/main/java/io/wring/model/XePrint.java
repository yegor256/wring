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
package io.wring.model;

import com.jcabi.xml.XML;
import com.jcabi.xml.XMLDocument;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.xembly.Directive;
import org.xembly.Xembler;

/**
 * Print.
 * @author Yegor Bugayenko (yegor@teamed.io)
 * @version $Id$
 * @since 1.0
 */
public final class XePrint {

    /**
     * Directives.
     */
    private final transient Iterable<Directive> dirs;

    /**
     * Ctor.
     * @param list List of Xembly directives
     */
    public XePrint(final Iterable<Directive> list) {
        this.dirs = list;
    }

    /**
     * Render text via XPath.
     * @param pattern Pattern to use
     * @return Plain text
     */
    public String text(final CharSequence pattern) {
        final XML xml = new XMLDocument(
            new Xembler(this.dirs).domQuietly()
        );
        final Pattern ptn = Pattern.compile("\\{([^}]+)}");
        final Matcher mtr = ptn.matcher(pattern);
        final StringBuffer out = new StringBuffer(pattern.length());
        while (mtr.find()) {
            mtr.appendReplacement(out, xml.xpath(mtr.group(1)).get(0));
        }
        mtr.appendTail(out);
        return out.toString();
    }

}
