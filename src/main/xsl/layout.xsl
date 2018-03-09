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
  <xsl:include href="/org/takes/rs/xe/sla.xsl"/>
  <xsl:include href="/org/takes/rs/xe/memory.xsl"/>
  <xsl:include href="/org/takes/rs/xe/millis.xsl"/>
  <xsl:include href="/org/takes/facets/flash/flash.xsl"/>
  <xsl:template match="/page">
    <html lang="en">
      <head>
        <meta charset="UTF-8"/>
        <meta name="viewport" content="width=device-width,minimum-scale=1,initial-scale=1"/>
        <xsl:if test="not(identity)">
          <link rel="shortcut icon" href="/images/logo.png"/>
        </xsl:if>
        <xsl:if test="identity">
          <link id="favicon" rel="shortcut icon" type="image/png" data-origin="{links/link[@rel='favicon']/@href}" href="{links/link[@rel='favicon']/@href}?{@date}{@sla}"/>
          <script type="text/javascript">
            setInterval(
              function() {
                var link = document.getElementById('favicon');
                link.setAttribute(
                  'href',
                  link.getAttribute('data-origin') + '?' + new Date().getTime()
                );
              },
              60 * 1000
            );
          </script>
        </xsl:if>
        <link rel="stylesheet" href="//yegor256.github.io/tacit/tacit.min.css"/>
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
                    <xsl:value-of select="identity/login"/>
                  </xsl:if>
                  <xsl:if test="not(identity)">
                    <a href="{links/link[@rel='takes:github']/@href}">
                      <xsl:text>login</xsl:text>
                    </a>
                  </xsl:if>
                </li>
                <xsl:if test="identity">
                  <li>
                    <a href="{links/link[@rel='home']/@href}">
                      <xsl:text>inbox</xsl:text>
                    </a>
                    <xsl:if test="events/@total">
                      <xsl:text> (</xsl:text>
                      <xsl:value-of select="events/@total"/>
                      <xsl:text>)</xsl:text>
                    </xsl:if>
                  </li>
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
            <xsl:call-template name="takes_flash">
              <xsl:with-param name="flash" select="flash"/>
            </xsl:call-template>
          </header>
          <article>
            <xsl:apply-templates select="." mode="body"/>
          </article>
          <footer>
            <nav>
              <ul style="color:gray;" class="bottom">
                <li title="Currently deployed version">
                  <xsl:text>v</xsl:text>
                  <xsl:value-of select="version/name"/>
                </li>
                <li title="Server time to generate this page">
                  <xsl:call-template name="takes_millis">
                    <xsl:with-param name="millis" select="millis"/>
                  </xsl:call-template>
                </li>
                <li title="Load average of the server">
                  <xsl:call-template name="takes_sla">
                    <xsl:with-param name="sla" select="@sla"/>
                  </xsl:call-template>
                </li>
                <li title="Free/total memory in Mb">
                  <xsl:call-template name="takes_memory">
                    <xsl:with-param name="memory" select="memory"/>
                  </xsl:call-template>
                </li>
                <li title="Current date/time">
                  <xsl:value-of select="@date"/>
                </li>
              </ul>
            </nav>
            <nav>
              <ul>
                <li>
                  <a href="//www.0crat.com/p/C7FCB1EQN">
                    <img src="//www.0crat.com/badge/C7FCB1EQN.svg"/>
                  </a>
                </li>
              </ul>
            </nav>
            <nav>
              <ul>
                <li>
                  <a href="//github.com/yegor256/wring/stargazers">
                    <img src="//img.shields.io/github/stars/yegor256/wring.svg?style=flat-square" alt="github stars"/>
                  </a>
                </li>
              </ul>
            </nav>
          </footer>
        </section>
        <script>
          (function(i,s,o,g,r,a,m){i['GoogleAnalyticsObject']=r;i[r]=i[r]||function(){
          (i[r].q=i[r].q||[]).push(arguments)},i[r].l=1*new Date();a=s.createElement(o),
          m=s.getElementsByTagName(o)[0];a.async=1;a.src=g;m.parentNode.insertBefore(a,m)
          })(window,document,'script','//www.google-analytics.com/analytics.js','ga');
          ga('create', 'UA-1963507-43', 'auto');
          ga('send', 'pageview');
        </script>
      </body>
    </html>
  </xsl:template>
</xsl:stylesheet>
