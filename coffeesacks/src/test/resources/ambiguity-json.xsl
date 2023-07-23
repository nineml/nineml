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
  <xsl:variable name="grammar" select="'s: n+ . n: [''0''-''9'']+ .'"/>
  <xsl:variable name="parser"
                select="cs:make-parser($grammar, map{'choose-alternative': f:choose#2,
                                                     'format': 'json'})"/>
  <doc>
    <xsl:sequence select="serialize($parser('123'), map{'method':'json','indent':true()})"/>
  </doc>
</xsl:template>

<xsl:function name="f:choose" as="map(*)">
  <xsl:param name="context" as="element()"/>
  <xsl:param name="options" as="map(*)"/>

  <!-- select the alternative that contains only a single 'n' -->
  <xsl:variable name="id" select="$context/children[count(symbol)=1]/@id/string()"/>
  <xsl:sequence select="map{'selection':$id}"/>
</xsl:function>

</xsl:stylesheet>
