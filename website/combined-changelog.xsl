<?xml version="1.0" encoding="utf-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:db="http://docbook.org/ns/docbook"
                xmlns:xs="http://www.w3.org/2001/XMLSchema"
                exclude-result-prefixes="#all"
                version="3.0">

<xsl:output method="xml" encoding="utf-8" indent="no"/>
<xsl:mode on-no-match="shallow-copy"/>

<xsl:param name="version" required="true"/>
<xsl:param name="audience" required="true"/>

<xsl:variable name="changelog" select="doc('../src/website/xml/changelog.xml')/*"/>
<xsl:variable name="revision" select="$changelog/db:revhistory/db:revision[db:revnumber=$version]"/>

<xsl:template match="/">
  <xsl:if test="count(//db:revhistory) != 1">
    <xsl:message terminate="yes" select="'Change log has no revhistory?'"/>
  </xsl:if>
  <xsl:if test="count($revision) != 1">
    <xsl:message terminate="yes" select="'Unified change log has no entry for ' || $version || '?'"/>
  </xsl:if>
  <xsl:if test="empty($revision/db:revdescription/*[@audience=$audience])">
    <xsl:message terminate="yes" select="'Unified change log has no entry for ' || $audience || '?'"/>
  </xsl:if>
  <xsl:apply-templates/>
</xsl:template>

<xsl:template match="db:revhistory">
  <xsl:copy>
    <xsl:apply-templates select="@*"/>
    <xsl:apply-templates select="$revision"/>
    <xsl:apply-templates/>
  </xsl:copy>
</xsl:template>

<xsl:template match="db:revdescription">
  <xsl:copy>
    <xsl:apply-templates select="@*"/>
    <xsl:for-each select="processing-instruction()|*[not(@audience) or @audience=$audience]">
      <xsl:apply-templates select="."/>
      <xsl:text>&#10;</xsl:text>
    </xsl:for-each>
  </xsl:copy>
</xsl:template>

<xsl:template match="db:para[@audience=$audience and empty(node())]">
  <xsl:element name="para" namespace="http://docbook.org/ns/docbook">
    <xsl:text>Dependencies updated.</xsl:text>
  </xsl:element>
</xsl:template>

</xsl:stylesheet>
