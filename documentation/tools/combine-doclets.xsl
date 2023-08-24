<?xml version="1.0" encoding="utf-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:xs="http://www.w3.org/2001/XMLSchema"
                exclude-result-prefixes="xs"
                version="3.0">

<xsl:output method="xml" encoding="utf-8" indent="no"
            omit-xml-declaration="yes"/>

<xsl:mode on-no-match="shallow-copy"/>

<xsl:template name="xsl:initial-template">
  <doclet-set xmlns='https://saxonica.com/ns/doclet'>
    <xsl:sequence select="doc('../../coffeegrinder/build/xmldoc/doclet.xml')/*"/>
    <xsl:sequence select="doc('../../coffeefilter/build/xmldoc/doclet.xml')/*"/>
    <xsl:sequence select="doc('../../coffeesacks/build/xmldoc/doclet.xml')/*"/>
    <xsl:sequence select="doc('../../coffeepot/build/xmldoc/doclet.xml')/*"/>
  </doclet-set>
</xsl:template>

</xsl:stylesheet>
