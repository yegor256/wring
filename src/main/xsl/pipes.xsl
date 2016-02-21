<?xml version="1.0"?>
<!--
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
 -->
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns="http://www.w3.org/1999/xhtml" version="1.0">
    <xsl:output method="html" doctype-system="about:legacy-compat"
        encoding="UTF-8" indent="yes" />
    <xsl:include href="/xsl/layout.xsl"/>
    <xsl:template match="page" mode="head">
        <title>
            <xsl:text>pipes</xsl:text>
        </title>
    </xsl:template>
    <xsl:template match="page" mode="body">
        <xsl:apply-templates select="pipes"/>
        <form action="{links/link[@rel='add']/@href}" method="post">
            <label for="yaml">YAML configuration of a new pipe:</label>
            <textarea id="yaml" name="yaml" style="width:100%;height:8em;">
                <xsl:text> </xsl:text>
            </textarea>
            <button type="submit">Add</button>
        </form>
    </xsl:template>
    <xsl:template match="pipes">
        <xsl:apply-templates select="pipe"/>
    </xsl:template>
    <xsl:template match="pipe">
        <p>
            <xsl:text>#</xsl:text>
            <xsl:value-of select="id"/>
            <xsl:text> (</xsl:text>
            <a href="{links/link[@rel='delete']/@href}">
                <xsl:text>delete</xsl:text>
            </a>
            <xsl:text>):</xsl:text>
        </p>
        <pre><xsl:value-of select="yaml"/></pre>
    </xsl:template>
</xsl:stylesheet>
