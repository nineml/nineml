<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="3.0"
    xmlns:xs="http://www.w3.org/2001/XMLSchema" xmlns:cs="http://nineml.com/ns/coffeesacks">
    <xsl:param name="grammar-text" as="xs:string">s = "a", "b", a".</xsl:param>
    <xsl:template name="xsl:initial-template">
        <xsl:variable name="grammar" select="cs:grammar-string($grammar-text)"/>
        <xsl:sequence select="$grammar"/>
    </xsl:template>
</xsl:stylesheet>
