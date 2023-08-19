<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:db="http://docbook.org/ns/docbook"
                xmlns:f="http://docbook.org/ns/docbook/functions"
                xmlns:mp="http://docbook.org/ns/docbook/modes/private"
                xmlns:xs="http://www.w3.org/2001/XMLSchema"
                xmlns="http://www.w3.org/1999/xhtml"
                exclude-result-prefixes="#all"
                expand-text="yes"
                default-mode="mp:doclet"
                version="3.0">

<xsl:template match="class|interface" mode="mp:doclet-class-summary">
  <xsl:apply-templates select="description" mode="patch-ns"/>
</xsl:template>

<xsl:template match="class|interface" mode="mp:doclet-constructor-summary">
  <xsl:where-populated>
    <dl>
      <xsl:apply-templates select="constructor[@public]">
        <xsl:sort select="@name"/>
      </xsl:apply-templates>
    </dl>
  </xsl:where-populated>
</xsl:template>

<xsl:template match="class|interface" mode="mp:doclet-method-summary">
  <xsl:where-populated>
    <dl>
      <xsl:apply-templates select="method[@public]">
        <xsl:sort select="@name"/>
      </xsl:apply-templates>
    </dl>
  </xsl:where-populated>
</xsl:template>

<xsl:template match="constructor">
  <div>
    <dt class="constructor">
      <xsl:sequence select="f:method-name(., true())"/>
    </dt>
    <xsl:if test="purpose">
      <dt class="purpose">
        <xsl:apply-templates select="purpose" mode="patch-ns"/>
      </dt>
    </xsl:if>
    <xsl:if test="deprecated">
      <dt class="deprecated">
        <xsl:text>Deprecated </xsl:text>
        <xsl:apply-templates select="deprecated" mode="patch-ns"/>
      </dt>
    </xsl:if>
    <dd>
      <xsl:apply-templates select="description" mode="patch-ns"/>
    </dd>
  </div>
</xsl:template>

<xsl:template match="method">
  <xsl:param name="force" as="xs:boolean" select="false()"/>

  <xsl:variable name="name" select="@name/string()"/>

  <xsl:variable name="basename"
                select="if (starts-with(@name, 'get'))
                        then substring-after(@name, 'get')
                        else if (starts-with(@name, 'set'))
                             then substring-after(@name, 'set')
                             else 'XXXX'"/>

  <xsl:variable name="getter"
                select="../method[starts-with($name, 'set') and @name='get'||$basename]"/>
  <xsl:variable name="setter"
                select="../method[starts-with($name, 'get') and @name='set'||$basename]"/>

  <xsl:if test="$force or not(starts-with($name, 'set')) or not($getter)">
    <div>
      <xsl:if test="not(preceding-sibling::match[@name = $name])">
        <xsl:attribute name="id" select="'m_' || ../@type || '_' || @name"/>
      </xsl:if>
      <dt class="method">
        <xsl:if test="child::return/@type/string()">
          <span class="api apit">
            <xsl:sequence select="child::return/@type/string()"/>
          </span>
          <xsl:text> </xsl:text>
        </xsl:if>
        <xsl:sequence select="f:method-name(., true())"/>
      </dt>
      <xsl:if test="purpose">
        <dt class="purpose">
          <xsl:apply-templates select="purpose" mode="patch-ns"/>
        </dt>
      </xsl:if>
      <xsl:if test="deprecated">
        <dt class="deprecated">
          <xsl:text>Deprecated </xsl:text>
          <xsl:apply-templates select="deprecated" mode="patch-ns"/>
        </dt>
      </xsl:if>
      <xsl:if test="child::return
                    and not(child::return/@type = 'void' or child::return/@type = 'Void')">
        <dt class="returns">
          <xsl:text>Returns </xsl:text>
          <xsl:apply-templates select="return" mode="patch-ns"/>
        </dt>
      </xsl:if>
      <xsl:for-each select="throws">
        <dt class="throws">
          <xsl:text>Throws </xsl:text>
          <code class="exception">
            <xsl:sequence select="@exception/string()"/>
          </code>
          <xsl:text> </xsl:text>
          <xsl:apply-templates select="." mode="patch-ns"/>
        </dt>
      </xsl:for-each>
      <xsl:for-each select="see">
        <dt class="see">
          <xsl:text>See </xsl:text>
          <xsl:apply-templates select="." mode="patch-ns"/>
        </dt>
      </xsl:for-each>
      <dd>
        <xsl:apply-templates select="description" mode="patch-ns"/>
      </dd>
    </div>
    <xsl:if test="$setter">
      <xsl:apply-templates select="$setter">
        <xsl:with-param name="force" select="true()"/>
      </xsl:apply-templates>
    </xsl:if>
  </xsl:if>
</xsl:template>

<xsl:template match="xmethod[not(starts-with(@name, 'set'))]">
  <xsl:variable name="basename" select="substring-after(@name, 'get')"/>
  <xsl:variable name="setter" select="../method[@name='set'||$basename]"/>
  <div>
    <dt>
      <xsl:if test="child::return/@type/string()">
        <span class="api apit">
          <xsl:sequence select="child::return/@type/string()"/>
        </span>
        <xsl:text> </xsl:text>
      </xsl:if>
      <xsl:sequence select="f:method-name(., true())"/>
    </dt>
    <xsl:if test="purpose">
      <dt class="purpose">
        <xsl:apply-templates select="purpose" mode="patch-ns"/>
      </dt>
    </xsl:if>
    <dd>
      <xsl:apply-templates select="description" mode="patch-ns"/>
    </dd>
  </div>
  <xsl:if test="$setter">
    <xsl:apply-templates select="$setter">
      <xsl:with-param name="force" select="true()"/>
    </xsl:apply-templates>
  </xsl:if>
</xsl:template>

<xsl:template match="xmethod[starts-with(@name, 'set')]">
  <xsl:variable name="basename" select="substring(@name, 4)"/>
  <xsl:if test="not(../method[@name = 'get'||$basename])">
    <div>
      <dt>
        <xsl:if test="child::return/@type/string()">
          <span class="api apit">
            <xsl:sequence select="child::return/@type/string()"/>
          </span>
          <xsl:text> </xsl:text>
        </xsl:if>
        <xsl:sequence select="f:method-name(., true())"/>
      </dt>
      <dd>
        <xsl:apply-templates select="description" mode="patch-ns"/>
      </dd>
    </div>
  </xsl:if>
</xsl:template>

<!-- ============================================================ -->

<xsl:function name="f:apidoc" as="xs:string">
  <xsl:param name="elem" as="element()"/>
  <xsl:variable name="path"
                select="substring-after($elem/../@fulltype, '.nineml.')
                        => translate('.', '/')"/>
  <xsl:variable name="first" select="substring-before($path, '/')"/>
  <xsl:variable name="path"
                select="'/' || $first || '/apidoc/org/nineml/'
                        || $path || '.html#'"/>

  <xsl:variable name="name" select="($elem/@name/string(), '&lt;init&gt;')[1]"/>

  <xsl:variable name="args" as="xs:string*">
    <xsl:for-each select="$elem/param">
      <xsl:choose>
        <xsl:when test="contains(@fulltype, '&lt;')">
          <xsl:sequence select="substring-before(@fulltype, '&lt;')"/>
        </xsl:when>
        <xsl:otherwise>
          <xsl:sequence select="@fulltype/string()"/>
        </xsl:otherwise>
      </xsl:choose>
    </xsl:for-each>
  </xsl:variable>

  <xsl:sequence select="$path||$name||'('||string-join($args,',')||')'"/>
</xsl:function>

<xsl:function name="f:method-name" as="element()">
  <xsl:param name="method" as="element()"/>
  <xsl:sequence select="f:method-name($method, false())"/>
</xsl:function>

<xsl:function name="f:method-name" as="element()">
  <xsl:param name="method" as="element()"/>
  <xsl:param name="show-parameters" as="xs:boolean"/>

  <span class="api apim">
    <xsl:choose>
      <xsl:when test="$method/self::constructor">
        <a href="{f:apidoc($method)}">{$method/../@type/string()}</a>
      </xsl:when>
      <xsl:otherwise>
        <a href="{f:apidoc($method)}">{$method/@name/string()}</a>
      </xsl:otherwise>
    </xsl:choose>
    <xsl:if test="$show-parameters">
      <xsl:text>(</xsl:text>
      <xsl:for-each select="$method/param">
        <xsl:if test="position() gt 1">, </xsl:if>
        <xsl:sequence select="@type/string()"/>
        <xsl:text> </xsl:text>
        <xsl:sequence select="@name/string()"/>
      </xsl:for-each>
      <xsl:text>)</xsl:text>
    </xsl:if>
  </span>
</xsl:function>

<!-- ============================================================ -->

<xsl:mode name="patch-ns" on-no-match="shallow-copy"/>
<xsl:mode name="patch-ns-inner" on-no-match="shallow-copy"/>

<xsl:template match="*" mode="patch-ns">
  <xsl:variable name="content" as="element()">
    <db:the-cake-is-a-lie>
      <xsl:apply-templates select="node()" mode="patch-ns-inner"/>
    </db:the-cake-is-a-lie>
  </xsl:variable>

  <xsl:choose>
    <xsl:when test="contains(string-join(.//text(), ''), '&quot;')">
      <xsl:variable name="qnodes" as="node()*">
        <xsl:apply-templates select="$content" mode="patch-dquote"/>
      </xsl:variable>
      <xsl:sequence select="$qnodes"/>
    </xsl:when>
    <xsl:otherwise>
      <xsl:sequence select="$content/node()"/>
    </xsl:otherwise>
  </xsl:choose>
</xsl:template>

<xsl:template match="a|p|code|em|ul|li" mode="patch-ns-inner">
  <xsl:element namespace="http://www.w3.org/1999/xhtml"
               name="{local-name(.)}">
    <xsl:apply-templates select="@*,node()" mode="patch-ns-inner"/>
  </xsl:element>
</xsl:template>

<xsl:template match="see/ref" mode="patch-ns-inner">
  <xsl:call-template name="ref-crossref">
    <xsl:with-param name="signature" select="string(.)"/>
  </xsl:call-template>
</xsl:template>

<xsl:template match="ref" mode="patch-ns-inner">
  <xsl:call-template name="ref-crossref">
    <xsl:with-param name="signature" select="@signature/string()"/>
    <xsl:with-param name="content" select="node()"/>
  </xsl:call-template>
</xsl:template>

<xsl:template name="ref-crossref">
  <xsl:param name="signature" as="xs:string"/>
  <xsl:param name="content" as="node()*"/>

  <xsl:variable name="href" as="xs:string?">
    <xsl:choose>
      <xsl:when test="starts-with($signature, '#') and ends-with($signature, '()')">
        <xsl:sequence select="'#m_' || ancestor::class/@type || '_'
                              || (substring($signature, 2) => substring-before('()'))"/>
      </xsl:when>
      <xsl:otherwise>
        <xsl:message select="'Unexpected crossref: ' || $signature"/>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:variable>

  <xsl:variable name="prose" as="item()*">
    <xsl:choose>
      <xsl:when test="$content">
        <xsl:apply-templates select="$content" mode="patch-ns-inner"/>
      </xsl:when>
      <xsl:when test="starts-with($signature, '#') and ends-with($signature, '()')">
        <xsl:sequence select="substring($signature, 2)"/>
      </xsl:when>
      <xsl:otherwise>
        <xsl:sequence select="$signature"/>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:variable>

  <xsl:choose>
    <xsl:when test="$href">
      <a href="{$href}">
        <xsl:sequence select="$prose"/>
      </a>
    </xsl:when>
    <xsl:otherwise>
      <xsl:sequence select="$prose"/>
    </xsl:otherwise>
  </xsl:choose>
</xsl:template>

<xsl:template match="text()" mode="patch-ns-inner">
  <xsl:choose>
    <xsl:when test="contains(., '''')">
      <xsl:variable name="parts" select="tokenize(., '''')"/>
      <xsl:variable name="bits" as="xs:string*">
        <xsl:for-each select="$parts">
          <xsl:choose>
            <xsl:when test="position() eq last()">
              <xsl:sequence select="."/>
            </xsl:when>
            <xsl:when test="matches(., '^.*\s$')">
              <xsl:sequence select=". || '‘'"/>
            </xsl:when>
            <xsl:otherwise>
              <xsl:sequence select=". || '’'"/>
            </xsl:otherwise>
          </xsl:choose>
        </xsl:for-each>
      </xsl:variable>
      <xsl:sequence select="string-join($bits, '')"/>
    </xsl:when>
    <xsl:otherwise>
      <xsl:sequence select="."/>
    </xsl:otherwise>
  </xsl:choose>
</xsl:template>

<xsl:template match="*" mode="patch-ns-inner">
  <xsl:message select="'Unexpected element:', ."/>
  <xsl:element namespace="http://www.w3.org/1999/xhtml"
               name="{local-name(.)}">
    <xsl:apply-templates select="@*,node()" mode="patch-ns-inner"/>
  </xsl:element>
</xsl:template>

<!-- ============================================================ -->

<xsl:accumulator name="dquote" as="xs:integer" initial-value="0">
  <xsl:accumulator-rule match="db:the-cake-is-a-lie" select="0"/>
  <xsl:accumulator-rule match="text()"
                        select="$value + string-length(replace(., '[^&quot;]', ''))"/>
</xsl:accumulator>

<xsl:mode name="patch-dquote" on-no-match="shallow-copy"/>

<xsl:template match="text()" mode="patch-dquote">
  <xsl:variable name="qcount" select="string-length(replace(., '[^&quot;]', ''))"/>
  <xsl:variable name="tcount" select="accumulator-before('dquote')"/>
  <xsl:variable name="first" select="$tcount - $qcount + 1"/>
  <xsl:choose>
    <xsl:when test="$qcount gt 0">
      <xsl:variable name="bits" as="xs:string*">
        <xsl:for-each select="tokenize(., '&quot;')">
          <xsl:choose>
            <xsl:when test="position() eq last()">
              <xsl:sequence select="."/>
            </xsl:when>
            <xsl:when test="($first + position() - 1) mod 2 = 1">
              <xsl:sequence select=". || '“'"/>
            </xsl:when>
            <xsl:otherwise>
              <xsl:sequence select=". || '”'"/>
            </xsl:otherwise>
          </xsl:choose>
        </xsl:for-each>
      </xsl:variable>
      <xsl:sequence select="string-join($bits, '')"/>
    </xsl:when>
    <xsl:otherwise>
      <xsl:copy/>
    </xsl:otherwise>
  </xsl:choose>
</xsl:template>

</xsl:stylesheet>
