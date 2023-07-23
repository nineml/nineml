<?xml version="1.0" encoding="utf-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:xs="http://www.w3.org/2001/XMLSchema"
                xmlns:cs="http://nineml.com/ns/coffeesacks"
                xmlns:f="http://coffeesacks.nineml.com/fn/testing"
                xmlns:map="http://www.w3.org/2005/xpath-functions/map"
                exclude-result-prefixes="#all"
                version="3.0">

<xsl:output method="xml" encoding="utf-8" indent="no"/>

<xsl:mode on-no-match="shallow-copy"/>

<xsl:template match="/">
  <xsl:variable name="parser" select="cs:load-grammar('horiz1.ixml',
                                      map {'choose-alternative': f:choose#2 })"/>
  <doc>
    <xsl:sequence select="$parser('xay')"/>
  </doc>
</xsl:template>

<xsl:function name="f:choose" as="map(*)">
  <xsl:param name="context" as="element()"/>
  <xsl:param name="options" as="map(*)"/>

  <xsl:sequence select="map { 'selection': $options?available-choices[1] }"/>
</xsl:function>

</xsl:stylesheet>
