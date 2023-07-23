<?xml version="1.0" encoding="utf-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:xs="http://www.w3.org/2001/XMLSchema"
                xmlns:cs="http://nineml.com/ns/coffeesacks"
                xmlns:map="http://www.w3.org/2005/xpath-functions/map"
                exclude-result-prefixes="#all"
                version="3.0">

<xsl:output method="xml" encoding="utf-8" indent="no"/>

<xsl:mode on-no-match="shallow-copy"/>

<xsl:template name="xsl:initial-template">
  <xsl:variable name="ixml" as="xs:string">
<xsl:text>
date: s?, day, -s, month, (-s, year)? .
-s: -" "+ .
day: digit, digit? .
-digit: "0"; "1"; "2"; "3"; "4"; "5"; "6"; "7"; "8"; "9".
month: "January"; "February"; "March"; "April";
       "May"; "June"; "July"; "August";
       "September"; "October"; "November"; "December".
year: (digit, digit)?, digit, digit .
</xsl:text>
  </xsl:variable>
  <xsl:variable name="grammar" select="cs:grammar-string($ixml,
                                       map { 'type': 'ixml' })"/>
  <doc>
    <xsl:sequence select="cs:parse-uri($grammar, 'date.inp')"/>
  </doc>
</xsl:template>

</xsl:stylesheet>
