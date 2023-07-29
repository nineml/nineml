<?xml version="1.0" encoding="utf-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:h="http://www.w3.org/1999/xhtml"
                xmlns:xs="http://www.w3.org/2001/XMLSchema"
                exclude-result-prefixes="#all"
                expand-text="yes"
                version="3.0">

<xsl:output method="text" encoding="utf-8" indent="no"/>
<xsl:strip-space elements="h:div h:body"/>

<xsl:param name="version" as="xs:string" required="yes"/>

<xsl:template match="/">
  <xsl:variable name="changelog"
                select="/h:html/h:body//h:section[@id='changelog']"/>
  <xsl:variable name="revision"
                select="$changelog//h:li[contains-token(@class, 'revision')
                           and ./h:div/h:span[contains-token(@class, 'revnumber')]=$version]"/>
  <xsl:if test="empty($revision)">
    <xsl:message terminate="yes" select="'No changelog for version ' || $version"/>
  </xsl:if>
  <xsl:apply-templates select="$revision/*"/>
</xsl:template>

<xsl:template match="h:div[contains-token(@class, 'revnumber')]"
              priority="10"/>

<xsl:template match="h:div">
  <xsl:apply-templates/>
</xsl:template>

<xsl:template match="h:p">
  <xsl:apply-templates/>
  <xsl:text>&#10;&#10;</xsl:text>
</xsl:template>

<xsl:template match="h:p/text() | h:code/text() | h:a/text()">
  <xsl:value-of select="replace(., '&#10;', ' ')"/>
</xsl:template>

<xsl:template match="h:ul">
  <xsl:apply-templates/>
  <xsl:text>&#10;&#10;</xsl:text>
</xsl:template>

<xsl:template match="h:ul/h:li">
  <xsl:text>{count(preceding-sibling::h:li)+1}. </xsl:text>
  <xsl:apply-templates/>
</xsl:template>

<xsl:template match="h:code">
  <xsl:text>`</xsl:text>
  <xsl:apply-templates/>
  <xsl:text>`</xsl:text>
</xsl:template>

<xsl:template match="h:span">
  <xsl:apply-templates/>
</xsl:template>

<xsl:template match="h:a">
  <xsl:apply-templates/>
</xsl:template>

<xsl:template match="h:a[@href]">
  <xsl:text>[</xsl:text>
  <xsl:apply-templates/>
  <xsl:text>]({string(@href)}])</xsl:text>
</xsl:template>

<xsl:template match="*">
  <xsl:message terminate="yes" select="'No template for ' || local-name(.)"/>
</xsl:template>

</xsl:stylesheet>
