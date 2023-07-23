<?xml version="1.0" encoding="utf-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:xs="http://www.w3.org/2001/XMLSchema"
                xmlns:cs="http://nineml.com/ns/coffeesacks"
                xmlns:map="http://www.w3.org/2005/xpath-functions/map"
                exclude-result-prefixes="#all"
                version="3.0">

<xsl:output method="xml" encoding="utf-8" indent="no"/>

<xsl:mode on-no-match="shallow-copy"/>

<xsl:template match="/">
  <xsl:variable name="grammar" select="cs:grammar-uri('src/test/resources/date.ixml')"/>
  <doc>
    <xsl:sequence select="cs:parse-uri($grammar, 'src/test/resources/date.inp')"/>
    <xsl:sequence select="cs:parse-uri($grammar, resolve-uri('date.inp', base-uri()))"/>
    <xsl:sequence select="cs:parse-string($grammar, '15 February 2022')"/>

    <xsl:variable name="map" select="cs:parse-uri($grammar, resolve-uri('date.inp', base-uri()),
                                                  map { 'format': 'json' })"/>
    <m>
      <xsl:sequence select="serialize($map, map { 'method': 'json' })"/>
    </m>

    <xsl:variable name="tmap" select="cs:parse-string($grammar, '17 February 2022',
                                                  map { 'format': 'json-text' })"/>
    <t>
      <xsl:sequence select="serialize($tmap, map { 'method': 'json' })"/>
    </t>

    <xsl:variable name="emap" select="cs:parse-string($grammar, '17 Fred 2022',
                                                   map { 'format': 'json' })"/>
    <e>
      <xsl:sequence select="serialize($emap, map { 'method': 'json' })"/>
    </e>
  </doc>

</xsl:template>

</xsl:stylesheet>
