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
    <xsl:if test="not(events/event)">
      <p>
        <xsl:text>Inbox is empty.</xsl:text>
      </p>
    </xsl:if>
    <xsl:apply-templates select="events"/>
  </xsl:template>
  <xsl:template match="events">
    <xsl:apply-templates select="event"/>
  </xsl:template>
  <xsl:template match="event">
    <p class="event">
      <code>
        <a href="{links/link[@rel='down']/@href}" title="drop it down">
          <xsl:text>&#x25BC;</xsl:text>
        </a>
        <xsl:text> </xsl:text>
        <xsl:value-of select="rank"/>
      </code>
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
