<?xml version="1.0" encoding="utf-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:cs="http://nineml.com/ns/coffeesacks"
                exclude-result-prefixes="#all"
                version="3.0">

<xsl:output method="xml" encoding="utf-8" indent="yes"/>

<xsl:template match="/">
  <xsl:variable name="parser" select="cs:load-grammar('date.ixml')"/>
  <doc>
    <xsl:sequence select="$parser('15 February 2022')"/>
  </doc>
</xsl:template>

</xsl:stylesheet>
