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

<xsl:template match="/">
  <xsl:variable name="parser" select="cs:load-grammar('date.ixml',
                                          map { 'format': 'json' })"/>
  <xsl:variable name="map" 
                select="$parser(unparsed-text('date.inp'))"/>

  <xsl:variable name="date" select="$map?date"/>
  <!-- do map:get so that we can be sure the order will be consistent. -->
  <doc>
    <xsl:text>year: {$date?year}, month: {$date?month}, day: {$date?day}</xsl:text>
  </doc>
</xsl:template>

</xsl:stylesheet>
