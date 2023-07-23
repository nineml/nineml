<?xml version="1.0" encoding="utf-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:xs="http://www.w3.org/2001/XMLSchema"
                xmlns:cs="http://nineml.com/ns/coffeesacks"
                xmlns:cse="http://nineml.com/ns/coffeesacks/errors"
                xmlns:map="http://www.w3.org/2005/xpath-functions/map"
                xmlns:err="http://www.w3.org/2005/xqt-errors"
                exclude-result-prefixes="#all"
                expand-text="yes"
                version="3.0">

<xsl:output method="xml" encoding="utf-8" indent="no"/>

<xsl:mode on-no-match="shallow-copy"/>

<xsl:template match="/">
  <doc>
    <xsl:try>
      <xsl:sequence select="cs:hygiene-report('S: ''a''.')"/>
      <xsl:catch>
        <xsl:message select="'CAUGHT'"/>
        <xsl:message select="'CODE', $err:code"/>
        <xsl:message select="'MODL', $err:module"/>
        <xsl:message select="'LI/C', $err:line-number, $err:column-number"/>
        <xsl:message select="'DESC', $err:description"/>
        <xsl:message select="'VALU', $err:value"/>
      </xsl:catch>
    </xsl:try>
  </doc>
</xsl:template>

</xsl:stylesheet>
