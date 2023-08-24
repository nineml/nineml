<?xml version="1.0" encoding="utf-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:db="http://docbook.org/ns/docbook"
                xmlns:f="https://saxonica.com/ns/functions"
                xmlns:h="http://www.w3.org/1999/xhtml"
                xmlns:m="http://docbook.org/ns/docbook/modes"
                xmlns:xlink="http://www.w3.org/1999/xlink"
                xmlns:xs="http://www.w3.org/2001/XMLSchema"
                xmlns="http://docbook.org/ns/docbook"
                exclude-result-prefixes="db f h m xs"
                expand-text="yes"
                default-mode="m:html2db"
                version="3.0">

<xsl:accumulator name="dquote" as="xs:integer" initial-value="0">
  <xsl:accumulator-rule match="db:the-cake-is-a-lie" select="0"/>
  <xsl:accumulator-rule match="text()"
                        select="$value + string-length(replace(., '[^&quot;]', ''))"/>
</xsl:accumulator>

<!-- ============================================================ -->

<xsl:template match="h:body">
  <xsl:variable name="content" as="element()">
    <db:the-cake-is-a-lie>
      <xsl:apply-templates/>
    </db:the-cake-is-a-lie>
  </xsl:variable>

  <xsl:choose>
    <xsl:when test="contains(string-join(.//text(), ''), '&quot;')">
      <xsl:variable name="qnodes" as="node()*">
        <xsl:apply-templates select="$content" mode="patch-dquote"/>
      </xsl:variable>
      <xsl:sequence select="$qnodes/node()"/>
    </xsl:when>
    <xsl:otherwise>
      <xsl:sequence select="$content/node()"/>
    </xsl:otherwise>
  </xsl:choose>
</xsl:template>

<xsl:mode name="patch-dquote" on-no-match="shallow-copy"/>

<xsl:template match="text()" mode="patch-dquote">
  <xsl:variable name="qcount" select="string-length(replace(., '[^&quot;]', ''))"/>
  <xsl:variable name="tcount" select="accumulator-before('dquote')"/>
  <xsl:variable name="first" select="$tcount - $qcount + 1"/>
  <xsl:choose>
    <xsl:when test="$qcount gt 0">
      <xsl:variable name="bits" as="xs:string*">
        <xsl:for-each select="tokenize(., '&quot;')">
          <xsl:choose>
            <xsl:when test="position() eq last()">
              <xsl:sequence select="."/>
            </xsl:when>
            <xsl:when test="($first + position() - 1) mod 2 = 1">
              <xsl:sequence select=". || '“'"/>
            </xsl:when>
            <xsl:otherwise>
              <xsl:sequence select=". || '”'"/>
            </xsl:otherwise>
          </xsl:choose>
        </xsl:for-each>
      </xsl:variable>
      <xsl:sequence select="string-join($bits, '')"/>
    </xsl:when>
    <xsl:otherwise>
      <xsl:copy/>
    </xsl:otherwise>
  </xsl:choose>
</xsl:template>

<!-- ============================================================ -->

<xsl:template match="h:p">
  <para>
    <xsl:apply-templates/>
  </para>
</xsl:template>

<xsl:template match="h:code">
  <code>
    <xsl:apply-templates/>
  </code>
</xsl:template>

<xsl:template match="h:span">
  <phrase>
    <xsl:apply-templates/>
  </phrase>
</xsl:template>

<xsl:template match="h:em">
  <emphasis>
    <xsl:apply-templates/>
  </emphasis>
</xsl:template>

<xsl:template match="h:ul">
  <itemizedlist>
    <xsl:apply-templates/>
  </itemizedlist>
</xsl:template>

<xsl:template match="h:li">
  <listitem>
    <xsl:choose>
      <xsl:when test="h:p">
        <xsl:apply-templates/>
      </xsl:when>
      <xsl:otherwise>
        <para>
          <xsl:apply-templates/>
        </para>
      </xsl:otherwise>
    </xsl:choose>
  </listitem>
</xsl:template>

<xsl:template match="h:dl">
  <variablelist>
    <xsl:apply-templates/>
  </variablelist>
</xsl:template>

<xsl:template match="h:dl/h:div">
  <varlistentry>
    <xsl:apply-templates/>
  </varlistentry>
</xsl:template>

<xsl:template match="h:dl/h:div/h:dt">
  <term>
    <xsl:apply-templates/>
  </term>
</xsl:template>

<xsl:template match="h:dl/h:div/h:dd">
  <listitem>
    <xsl:apply-templates/>
  </listitem>
</xsl:template>

<xsl:template match="h:a[contains-token(@class, 'ref')]">
  <link xlink:href="{@href}" role='ref'>
    <xsl:choose>
      <xsl:when test="contains-token(@class, 'label')">
        <xsl:apply-templates/>
      </xsl:when>
      <xsl:when test="contains(@java-signature, '#')
                      and not(contains(@java-signature, '('))">
        <!-- a field reference -->
        <xsl:sequence select="substring-after(@java-signature, '#')"/>
      </xsl:when>
      <xsl:when test="@java-method">
        <xsl:apply-templates/>
      </xsl:when>
      <xsl:when test="@java-class">
        <xsl:sequence select="@java-class/string()"/>
      </xsl:when>
      <xsl:otherwise>
        <xsl:apply-templates/>
      </xsl:otherwise>
    </xsl:choose>
  </link>
</xsl:template>

<xsl:template match="h:a">
  <link xlink:href="{@href}">
    <xsl:apply-templates/>
  </link>
</xsl:template>

<xsl:template match="*">
  <xsl:message select="'Unexpected: ' || local-name(.)"/>
  <xsl:message select="."/>
  <xsl:apply-templates/>
</xsl:template>

<xsl:template match="text()">
  <xsl:variable name="text" select="f:expand-unicode-refs(.)"/>
  
  <xsl:choose>
    <xsl:when test="contains($text, '''')">
      <xsl:variable name="parts" select="tokenize($text, '''')"/>
      <xsl:variable name="bits" as="xs:string*">
        <xsl:for-each select="$parts">
          <xsl:choose>
            <xsl:when test="position() eq last()">
              <xsl:sequence select="."/>
            </xsl:when>
            <xsl:when test="matches(., '^.*\s$')">
              <xsl:sequence select=". || '‘'"/>
            </xsl:when>
            <xsl:otherwise>
              <xsl:sequence select=". || '’'"/>
            </xsl:otherwise>
          </xsl:choose>
        </xsl:for-each>
      </xsl:variable>
      <xsl:sequence select="string-join($bits, '')"/>
    </xsl:when>
    <xsl:otherwise>
      <xsl:sequence select="$text"/>
    </xsl:otherwise>
  </xsl:choose>
</xsl:template>

<xsl:function name="f:expand-unicode-refs" as="xs:string">
  <xsl:param name="text" as="xs:string"/>

  <xsl:variable name="expanded" as="xs:string*">
    <xsl:analyze-string select="$text" regex="\\u[0-9a-fA-F][0-9a-fA-F][0-9a-fA-F][0-9a-fA-F]">
      <xsl:matching-substring>
        <xsl:variable name="dec" select="f:hex-to-dec(substring(., 3))"/>
        <xsl:sequence select="codepoints-to-string($dec)"/>
      </xsl:matching-substring>
      <xsl:non-matching-substring>
        <xsl:sequence select="."/>
      </xsl:non-matching-substring>
    </xsl:analyze-string>
  </xsl:variable>

  <xsl:sequence select="string-join($expanded, '')"/>
</xsl:function>

<xsl:function name="f:hex-to-dec" as="xs:integer">
  <xsl:param name="hex" as="xs:string"/>
  <xsl:iterate select="reverse(string-to-codepoints(upper-case($hex)))">
    <xsl:param name="dec" select="0"/>
    <xsl:param name="pow" select="1"/>
    <xsl:on-completion select="$dec"/>
    <xsl:variable name="digit" select="if (. gt 64) then . - 55 else . - 48"/>
    <xsl:next-iteration>
      <xsl:with-param name="dec" select="$dec + ($digit * $pow)"/>
      <xsl:with-param name="pow" select="$pow * 16"/>
    </xsl:next-iteration>
  </xsl:iterate>
</xsl:function>

</xsl:stylesheet>
