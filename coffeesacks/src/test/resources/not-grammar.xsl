<?xml version="1.0" encoding="utf-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:xs="http://www.w3.org/2001/XMLSchema"
                xmlns:cs="http://nineml.com/ns/coffeesacks"
                xmlns:map="http://www.w3.org/2005/xpath-functions/map"
                exclude-result-prefixes="#all"
                expand-text="yes"
                version="3.0">

<xsl:output method="xml" encoding="utf-8" indent="no"/>

<xsl:mode on-no-match="shallow-copy"/>

<xsl:template name="xsl:initial-template">
  <xsl:variable name="not-grammar"><not-a-grammar/></xsl:variable>
  <doc>
    <xsl:sequence select="cs:parse-uri($not-grammar, 'date.inp', map{'format': 'xml'})"/>
  </doc>
</xsl:template>

</xsl:stylesheet>
