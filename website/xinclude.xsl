<?xml version="1.0" encoding="utf-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:ext="http://docbook.org/extensions/xslt"
                xmlns:xi='http://www.w3.org/2001/XInclude'
                exclude-result-prefixes="#all"
                version="3.0">

<xsl:output method="xml" encoding="utf-8" indent="no"/>

<xsl:mode on-no-match="shallow-copy"/>

<xsl:template match="xi:include">
  <xsl:sequence select="ext:xinclude(., map { 'fixup-xml-base': false(),
                                              'trim-text': true() })"/>
</xsl:template>

</xsl:stylesheet>
