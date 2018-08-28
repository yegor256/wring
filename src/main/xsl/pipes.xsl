<?xml version="1.0"?>
<!--
Copyright (c) 2016, wring.io
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
      <xsl:text>pipes</xsl:text>
    </title>
  </xsl:template>
  <xsl:template match="page" mode="body">
    <xsl:apply-templates select="pipes"/>
    <form action="{links/link[@rel='add']/@href}" method="post">
      <label for="json">JSON configuration of a new pipe:</label>
      <textarea id="json" name="json" style="width:100%;height:8em;">
        <xsl:text> </xsl:text>
      </textarea>
      <button type="submit">Add</button>
    </form>
    <p>
      <xsl:text>Check general instructions here: </xsl:text>
      <a href="http://www.yegor256.com/2016/03/15/wring-dispatcher-github-notifications.html">
        <xsl:text>Wring.io, a Dispatcher of GitHub Notifications</xsl:text>
      </a>
      <xsl:text>.</xsl:text>
    </p>
    <p>
      <xsl:text>It's not possible to edit a pipe.</xsl:text>
      <xsl:text> If you want to make changes, just copy the existing one somewhere, delete it and then create a new one.</xsl:text>
    </p>
    <p>
      <xsl:text>Pipe execution clear all older notifications from Github. This is a Github API limitation, which does not allow to set notifications as read individually.</xsl:text>
    </p>
    <p>
      <xsl:text>All supported JSON elements are listed below:</xsl:text>
    </p>
    <p>
      <code>
        <xsl:text>class</xsl:text>
      </code>
      <xsl:text> is the Java class name, like </xsl:text>
      <code>
        <xsl:text>io.wring.agents.github.AgGithub</xsl:text>
      </code>
      <xsl:text>.</xsl:text>
    </p>
    <p>
      <code>
        <xsl:text>token</xsl:text>
      </code>
      <xsl:text> is your GitHub authentication token,</xsl:text>
      <xsl:text> you can get it </xsl:text>
      <a href="https://github.com/settings/tokens">
        <xsl:text>here</xsl:text>
      </a>
      <xsl:text> (you need "repo" and "notifications" scope).</xsl:text>
    </p>
    <p>
      <code>
        <xsl:text>ignore</xsl:text>
      </code>
      <xsl:text> may contain a list of texts or regular expressions to ignore.</xsl:text>
      <xsl:text> A regular expression must start and end with a slash.</xsl:text>
    </p>
    <p>
      <code>
        <xsl:text>boost</xsl:text>
      </code>
      <xsl:text> may contain a list of regular expressions.</xsl:text>
      <xsl:text> An event will be boosted (+5 to the rating) if it matches.</xsl:text>
    </p>
  </xsl:template>
  <xsl:template match="pipes">
    <xsl:apply-templates select="pipe"/>
  </xsl:template>
  <xsl:template match="pipe">
    <p>
      <xsl:text>#</xsl:text>
      <xsl:value-of select="id"/>
      <xsl:text> [</xsl:text>
      <xsl:value-of select="status"/>
      <xsl:text>] </xsl:text>
      <a href="{links/link[@rel='delete']/@href}">
        <xsl:text>delete</xsl:text>
      </a>
      <xsl:text>:</xsl:text>
    </p>
    <pre>
      <xsl:value-of select="json"/>
    </pre>
  </xsl:template>
</xsl:stylesheet>
