<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:db="http://docbook.org/ns/docbook"
                xmlns:f="http://docbook.org/ns/docbook/functions"
                xmlns:fp="http://docbook.org/ns/docbook/functions/private"
                xmlns:h="http://www.w3.org/1999/xhtml"
                xmlns:m="http://docbook.org/ns/docbook/modes"
                xmlns:mp="http://docbook.org/ns/docbook/modes/private"
                xmlns:t="http://docbook.org/ns/docbook/templates"
                xmlns:tp="http://docbook.org/ns/docbook/templates/private"
                xmlns:v="http://docbook.org/ns/docbook/variables"
                xmlns:xs="http://www.w3.org/2001/XMLSchema"
                xmlns="http://www.w3.org/1999/xhtml"
                exclude-result-prefixes="#all"
                version="3.0">

<xsl:import href="../../website/docbook.xsl"/>
<xsl:import href="xpath.xsl"/>

<!-- ============================================================ -->

<xsl:param name="verbatim-trim-leading-blank-lines" select="'true'"/>

<!-- ============================================================ -->

<xsl:template match="db:productname" mode="m:titlepage"
              expand-text="yes">
  <div class="versions">
    <p class="app">
      <xsl:apply-templates mode="m:titlepage"/>
      <xsl:text> version </xsl:text>
      <xsl:apply-templates select="$ninemlVersion"/>
    </p>
  </div>
</xsl:template>

<xsl:template match="*" mode="m:html-head-links">
  <xsl:next-match/>
  <link rel="shortcut icon" href="icon/CoffeeSacks.png"/>

  <meta name="viewport" content="width=device-width, initial-scale=1.0"/>
  <link rel="stylesheet" href="css/nineml.css"/>
  <link rel="stylesheet" href="css/coffeesacks.css"/>
</xsl:template>

<xsl:template match="db:funcsynopsis" mode="m:docbook">
  <div class="funcsynopsis">
    <xsl:apply-templates select="." mode="m:xpath"/>
  </div>
</xsl:template>

<!-- ============================================================ -->

<xsl:template match="processing-instruction('coffeesacks-version')">
  <xsl:value-of select="$ninemlVersion"/>
</xsl:template>

</xsl:stylesheet>
