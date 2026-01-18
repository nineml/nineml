<?xml version="1.0" encoding="utf-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:xs="http://www.w3.org/2001/XMLSchema"
                xmlns:cs="http://nineml.com/ns/coffeesacks"
                xmlns:cse="http://nineml.com/ns/coffeesacks/errors"
                xmlns:err="http://www.w3.org/2005/xqt-errors"
                xmlns:f="http://coffeesacks.nineml.com/fn/testing"
                xmlns:map="http://www.w3.org/2005/xpath-functions/map"
                exclude-result-prefixes="#all"
                version="3.0">

<xsl:param name="start-symbol" select="()"/>

<xsl:output method="xml" encoding="utf-8" indent="no"/>

<xsl:mode on-no-match="shallow-copy"/>

<xsl:template match="/">
  <xsl:try>
    <xsl:variable name="parser" select="cs:make-parser('S = ''a''|B. B=''a''.',
                                        map {'start-symbol': $start-symbol })"/>
    <doc>
      <xsl:sequence select="$parser('c')"/>
    </doc>
    <xsl:catch errors="cse:CSPA0001">
      <pass/>
    </xsl:catch>
    <xsl:catch>
      <fail/>
    </xsl:catch>
  </xsl:try>
</xsl:template>

</xsl:stylesheet>
