<?xml version="1.0" encoding="utf-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:cs="http://nineml.com/ns/coffeesacks"
                xmlns:f="http://coffeesacks.nineml.com/fn/testing"
                exclude-result-prefixes="#all"
                version="3.0">

<xsl:output method="xml" encoding="utf-8" indent="no"/>

<xsl:mode on-no-match="shallow-copy"/>

<xsl:template match="/">
  <xsl:variable name="parser"
                select="cs:load-grammar('numbers.ixml',
                                        map {'choose-alternative': f:choose#2 })"/>
  <doc>
    <xsl:sequence select="$parser('bad&#10;cafe&#10;42')"/>
  </doc>
</xsl:template>

<xsl:function name="f:choose" as="map(*)">
  <xsl:param name="context" as="element()"/>
  <xsl:param name="options" as="map(*)"/>

  <xsl:variable name="choice"
                select="$context/children[symbol[@name='decimal']]/@id"/>

  <xsl:sequence select="map { 'selection': $choice }"/>
</xsl:function>

</xsl:stylesheet>
