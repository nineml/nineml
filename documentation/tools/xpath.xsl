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
                default-mode="m:xpath"
                exclude-result-prefixes="#all"
                version="3.0">

<xsl:template match="db:funcprototype">
  <div class="funcprototype">
    <xsl:apply-templates select="db:funcdef"/>
    <xsl:if test="db:paramdef">
      <table border="0">
        <tbody>
          <xsl:for-each select="db:paramdef">
            <tr>
              <td>
                <xsl:apply-templates select="db:parameter"/>
              </td>
              <td>
                <xsl:apply-templates select="db:type"/>
              </td>
              <td>
                <xsl:apply-templates select="db:phrase"/>
              </td>
            </tr>
          </xsl:for-each>
        </tbody>
      </table>
    </xsl:if>
  </div>
</xsl:template>

<xsl:template match="db:funcdef">
  <div class="funcdef">
    <xsl:apply-templates select="db:function"/>
    <span class="op">(</span>
    <span class="parameters">
      <xsl:apply-templates select="../db:paramdef"/>
    </span>
    <span class="cp">)</span>
    <span class="rarr">→</span>
    <xsl:apply-templates select="db:type"/>
  </div>
</xsl:template>

<xsl:template match="db:function">
  <span class="function">
    <xsl:apply-templates/>
  </span>
</xsl:template>

<xsl:template match="db:paramdef">
  <xsl:if test="preceding-sibling::db:paramdef">
    <span class="comma">, </span>
  </xsl:if>
  <span class="paramdef">
    <xsl:apply-templates select="db:parameter"/>
  </span>
</xsl:template>

<xsl:template match="db:type">
  <span class="type">
    <xsl:apply-templates/>
  </span>
</xsl:template>

<xsl:template match="db:parameter">
  <span class="parameter">
    <span class="dollar">$</span>
    <xsl:apply-templates/>
  </span>
</xsl:template>

</xsl:stylesheet>
