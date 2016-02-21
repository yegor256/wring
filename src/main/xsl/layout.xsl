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
    <xsl:template match="/page">
        <html lang="en">
            <head>
                <meta charset="UTF-8"/>
                <link rel="shortcut icon" type="image/png" href="/images/icon.png"/>
                <link rel="stylesheet" href="http://yegor256.github.io/tacit/tacit.min.css"/>
                <link rel="stylesheet" href="/css/main.css"/>
                <xsl:apply-templates select="." mode="head"/>
            </head>
            <body>
                <section>
                    <header>
                        <nav>
                            <ul>
                                <li>
                                    <a href="{links/link[@rel='home']/@href}">
                                        <img src="/images/logo.svg" class="logo"/>
                                    </a>
                                </li>
                            </ul>
                        </nav>
                        <nav>
                            <ul class="menu">
                                <li>
                                    <xsl:if test="identity">
                                        <xsl:text>@</xsl:text>
                                        <xsl:value-of select="identity/name"/>
                                    </xsl:if>
                                    <xsl:if test="not(identity)">
                                        <a href="{links/link[@rel='takes:github']/@href}">
                                            <xsl:text>login</xsl:text>
                                        </a>
                                    </xsl:if>
                                </li>
                                <xsl:if test="identity">
                                    <li>
                                        <a href="{links/link[@rel='pipes']/@href}">
                                            <xsl:text>pipes</xsl:text>
                                        </a>
                                    </li>
                                </xsl:if>
                                <xsl:if test="identity">
                                    <li>
                                        <a href="{links/link[@rel='takes:logout']/@href}">
                                            <xsl:text>exit</xsl:text>
                                        </a>
                                    </li>
                                </xsl:if>
                            </ul>
                        </nav>
                        <xsl:apply-templates select="flash"/>
                    </header>
                    <article>
                        <xsl:apply-templates select="." mode="body"/>
                    </article>
                    <footer>
                        <nav>
                            <ul>
                                <li>version</li>
                            </ul>
                        </nav>
                    </footer>
                </section>
            </body>
        </html>
    </xsl:template>
    <xsl:template match="flash">
        <p>
            <xsl:attribute name="style">
                <xsl:text>color:</xsl:text>
                <xsl:choose>
                    <xsl:when test="level = 'INFO'">
                        <xsl:text>#348C62</xsl:text>
                    </xsl:when>
                    <xsl:when test="level = 'WARNING'">
                        <xsl:text>orange</xsl:text>
                    </xsl:when>
                    <xsl:when test="level = 'SEVERE'">
                        <xsl:text>red</xsl:text>
                    </xsl:when>
                    <xsl:otherwise>
                        <xsl:text>inherit</xsl:text>
                    </xsl:otherwise>
                </xsl:choose>
            </xsl:attribute>
            <xsl:value-of select="message"/>
        </p>
    </xsl:template>
</xsl:stylesheet>
