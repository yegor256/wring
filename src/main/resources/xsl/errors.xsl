<?xml version="1.0"?>
<!--
Copyright (c) 2016-2020, Yegor Bugayenko
All rights reserved.

Redistribution and use in source and binary forms, with or without
modification, are permitted provided that the following conditions
are met: 1) Redistributions of source code must retain the above
copyright notice, this list of conditions and the following
disclaimer. 2) Redistributions in binary form must reproduce the above
copyright notice, this list of conditions and the following
disclaimer in the documentation and/or other materials provided
with the distribution. 3) Neither the name of the wring.io nor
the names of its contributors may be used to endorse or promote
products derived from this software without specific prior written
permission.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
"AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT
NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL
THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
(INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
OF THE POSSIBILITY OF SUCH DAMAGE.
-->
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns="http://www.w3.org/1999/xhtml" version="1.0">
  <xsl:output method="html" doctype-system="about:legacy-compat" encoding="UTF-8" indent="yes"/>
  <xsl:include href="/xsl/layout.xsl"/>
  <xsl:template match="page" mode="head">
    <title>
      <xsl:text>wring</xsl:text>
    </title>
  </xsl:template>
  <xsl:template match="page" mode="body">
    <xsl:if test="not(errors/error)">
      <p>
        <xsl:text>No error registered.</xsl:text>
      </p>
    </xsl:if>
    <xsl:apply-templates select="errors"/>
  </xsl:template>
  <xsl:template match="errors">
    <xsl:apply-templates select="error"/>
    <xsl:if test="count(error) &lt; @total">
      <p>
        <xsl:text>There are more errors registered (</xsl:text>
        <strong>
          <xsl:value-of select="@total - count(error)"/>
          <xsl:text>+</xsl:text>
        </strong>
        <xsl:text>), but they are not visible in the list.</xsl:text>
        <xsl:text> Once you check and delete these ones,</xsl:text>
        <xsl:text> you will see others.</xsl:text>
      </p>
    </xsl:if>
  </xsl:template>
  <xsl:template match="error">
    <p class="error">
      <xsl:text> </xsl:text>
      <xsl:value-of select="title"/>
      <xsl:text> </xsl:text>
      <a href="{links/link[@rel='delete']/@href}" title="delete it forever">
        <xsl:text>delete</xsl:text>
      </a>
    </p>
    <pre>
      <xsl:value-of select="html" disable-output-escaping="yes"/>
    </pre>
  </xsl:template>
</xsl:stylesheet>
