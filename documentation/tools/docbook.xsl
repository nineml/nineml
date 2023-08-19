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
                xmlns:xlink="http://www.w3.org/1999/xlink"
                xmlns:xs="http://www.w3.org/2001/XMLSchema"
                xmlns="http://www.w3.org/1999/xhtml"
                exclude-result-prefixes="#all"
                expand-text="yes"
                version="3.0">

<xsl:import href="../../website/docbook.xsl"/>
<xsl:import href="xpath.xsl"/>
<xsl:import href="doclet.xsl"/>

<xsl:param name="verbatim-numbered-elements" select="''"/>

<xsl:param name="verbatim-syntax-highlighter" select="'prism'"/>
<xsl:param name="verbatim-syntax-highlight-languages"
           select="'python perl html xml xslt xquery javascript json css java'"/>

<xsl:param name="nineml-version" as="xs:string" required="yes"/>

<!--<xsl:param name="docbook-transclusion" select="'true'"/>-->

<!-- List of classes that are known to be absent from the apidocs;
     don't generate warnings for links to them that don't resolve -->
<xsl:param name="excluded-classes"
           select="('org.nineml.coffeegrinder.util.Decoratable',
                    'org.nineml.logging.Logger',
                    'java.lang.System')"/>

<xsl:variable name="jd" as="document-node()">
  <xsl:document>
    <xsl:sequence select="doc('../../coffeegrinder/build/xmldoc/doclet.xml')/*"/>
    <xsl:sequence select="doc('../../coffeefilter/build/xmldoc/doclet.xml')/*"/>
    <xsl:sequence select="doc('../../coffeesacks/build/xmldoc/doclet.xml')/*"/>
    <xsl:sequence select="doc('../../coffeepot/build/xmldoc/doclet.xml')/*"/>
  </xsl:document>
</xsl:variable>

<xsl:template match="*" mode="m:html-body-script">
  <xsl:param name="rootbaseuri"/>
  <xsl:param name="chunkbaseuri"/>
  <script src="/js/sectmarks.js"></script>
  <!-- hack -->
  <xsl:if test="ends-with($chunkbaseuri, '/index.html') and self::h:article">
    <div class="nineml-float" title="Part of the NineML family">
      <a href="../"><img src="../icon/nineml.png" alt="NineML logo"/></a>
    </div>
  </xsl:if>
</xsl:template>

<xsl:template match="*" mode="m:html-head-links">
  <xsl:next-match/>
  <link rel="stylesheet" href="{$resource-base-uri}css/coldark-cold.css"/>
  <link rel="stylesheet" href="{$resource-base-uri}css/nineml.css"/>
  <link rel="stylesheet" href="{$resource-base-uri}css/ninemlset.css"/>
  <link rel="shortcut icon" href="{$resource-base-uri}icon/nineml.png"/>
</xsl:template>

<xsl:template match="db:book" mode="m:head-additions">
  <xsl:choose>
    <xsl:when test="starts-with(@xml:id, 'coffeegrinder')">
      <link rel="shortcut icon" href="{$resource-base-uri}icon/CoffeeGrinder.png"/>
      <link rel="stylesheet" href="{$resource-base-uri}css/coffeepot.css"/>
    </xsl:when>
    <xsl:when test="starts-with(@xml:id, 'coffeefilter')">
      <link rel="shortcut icon" href="{$resource-base-uri}icon/CoffeeFilter.png"/>
      <link rel="stylesheet" href="{$resource-base-uri}css/coffeefilter.css"/>
    </xsl:when>
    <xsl:when test="starts-with(@xml:id, 'coffeesacks')">
      <link rel="shortcut icon" href="{$resource-base-uri}icon/CoffeeSacks.png"/>
      <link rel="stylesheet" href="{$resource-base-uri}css/coffeesacks.css"/>
    </xsl:when>
    <xsl:when test="starts-with(@xml:id, 'coffeepot')">
      <link rel="shortcut icon" href="{$resource-base-uri}icon/CoffeePot.png"/>
      <link rel="stylesheet" href="{$resource-base-uri}css/coffeepot.css"/>
    </xsl:when>
    <xsl:when test="starts-with(@xml:id, 'references')">
      <link rel="shortcut icon" href="{$resource-base-uri}icon/nineml.png"/>
      <link rel="stylesheet" href="{$resource-base-uri}css/references.css"/>
    </xsl:when>
    <xsl:when test="starts-with(@xml:id, 'introduction')">
      <link rel="shortcut icon" href="{$resource-base-uri}icon/nineml.png"/>
      <link rel="stylesheet" href="{$resource-base-uri}css/introduction.css"/>
    </xsl:when>
    <xsl:otherwise>
      <xsl:message terminate="yes" select="'Unknown element for head-additions: ' || local-name(.)"/>
    </xsl:otherwise>
  </xsl:choose>
</xsl:template>

<xsl:template match="db:section|db:sect1|db:sect2|db:sect3|db:sect4|db:sect5
                     |db:refsection|db:refsect1|db:refsect2|db:refsect3"
              mode="m:toc-nested">
  <xsl:param name="persistent" as="xs:boolean" tunnel="yes"/>
  <xsl:param name="root-element" as="element()" tunnel="yes"/>
  <!-- no sections in the set toc -->
  <xsl:if test="not($root-element/self::db:set)">
    <xsl:next-match/>
  </xsl:if>
</xsl:template>

<xsl:template match="db:funcsynopsis[@language='xpath']" mode="m:docbook">
  <div class="funcsynopsis">
    <xsl:apply-templates select="." mode="m:xpath"/>
  </div>
</xsl:template>

<xsl:template match="db:productnumber" mode="m:titlepage">
  <div class="versions">
    <p class="app">Version {$nineml-version}</p>
  </div>
</xsl:template>

<!-- this is complete tag abuse -->
<xsl:template match="db:productname" mode="m:titlepage">
  <table class="tools" width="100%">
    <tr>
      <td>
        <a href="coffeepot/">
          <img src="images/CoffeePot.png"/>
        </a>
      </td>
      <td>
        <a href="coffeesacks/">
          <img src="images/CoffeeSacks.png"/>
        </a>
      </td>
      <td>
        <a href="coffeefilter/">
          <img src="images/CoffeeFilter.png"/>
        </a>
      </td>
      <td>
        <a href="coffeegrinder/">
          <img src="images/CoffeeGrinder.png"/>
        </a>
      </td>
    </tr>
    <tr>
      <th>CoffeePot</th>
      <th>CoffeeSacks</th>
      <th>CoffeeFilter</th>
      <th>CoffeeGrinder</th>
    </tr>
    <tr>
      <td>An Invisible XML processor</td>
      <td>Saxon extensions functions</td>
      <td>Invisible XML API</td>
      <td>Earley &amp; GLL parser APIs</td>
    </tr>
  </table>
</xsl:template>

<!-- ============================================================ -->

<xsl:template match="db:variablelist[contains-token(@role, 'csummary')
                                     or contains-token(@role, 'msummary')
                                     or contains-token(@role, 'fsummary')]" mode="m:docbook">
  <div class="api-summary">
    <dl>
      <xsl:apply-templates mode="m:api-summary"/>
    </dl>
  </div>
</xsl:template>

<xsl:template match="db:link[@xlink:href and contains-token(@role, 'ref')]"
              priority="10" mode="m:docbook">
  <xsl:variable name="default">
    <xsl:next-match/>
  </xsl:variable>

  <xsl:variable name="rewrite" as="xs:string?">
    <xsl:choose>
      <xsl:when test="starts-with(@xlink:href, 'https:')">
        <xsl:sequence select="@xlink:href/string()"/>
      </xsl:when>
      <xsl:when test="exists(f:classLink(.))">
        <xsl:sequence select="f:classLink(.)"/>
      </xsl:when>
      <xsl:when test="exists(f:methodLink(.))">
        <xsl:sequence select="f:methodLink(.)"/>
      </xsl:when>
      <xsl:when test="exists(f:fieldLink(.))">
        <xsl:sequence select="f:fieldLink(.)"/>
      </xsl:when>
      <xsl:when test="@xlink:href = $excluded-classes
                      or (contains(@xlink:href, '#')
                          and substring-before(@xlink:href, '#') = $excluded-classes)">
        <!-- no message -->
      </xsl:when>
      <xsl:otherwise>
        <xsl:message select="'LINK: ' || @xlink:href"/>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:variable>

  <xsl:choose>
    <xsl:when test="empty($rewrite)">
      <xsl:sequence select="$default/h:a/node()"/>
    </xsl:when>
    <xsl:otherwise>
      <a href="#{$rewrite}">
        <xsl:sequence select="$default/h:a/@* except $default/h:a/@href"/>
        <xsl:sequence select="$default/h:a/node()"/>
      </a>
    </xsl:otherwise>
  </xsl:choose>
</xsl:template>

<!-- ============================== -->

<xsl:template match="db:varlistentry" mode="m:api-summary">
  <div>
    <xsl:if test="@xml:id">
      <xsl:attribute name="id" select="@xml:id"/>
    </xsl:if>
    <xsl:apply-templates select="db:term" mode="m:api-summary"/>
    <dd>
      <xsl:apply-templates select="db:listitem" mode="m:docbook"/>
    </dd>
  </div>
</xsl:template>

<xsl:template match="db:term" mode="m:api-summary">
  <dt>
    <xsl:if test="@role">
      <xsl:attribute name="class" select="@role"/>
    </xsl:if>
    <xsl:apply-templates mode="m:docbook"/>
  </dt>
</xsl:template>

<!-- ============================================================ -->

<xsl:function name="f:classLink" as="xs:string?">
  <xsl:param name="node" as="element()"/>

  <xsl:variable name="link" select="'c_' || $node/@xlink:href"/>

  <!--
  <xsl:if test="not(key('id', $link, $node/root()))">
    <xsl:message select="'C:', $node/@xlink:href/string()"/>
    <xsl:message select="$link"/>
  </xsl:if>
  -->

  <xsl:if test="key('id', $link, $node/root())">
    <xsl:sequence select="$link"/>
  </xsl:if>
</xsl:function>

<xsl:function name="f:methodLink" as="xs:string?">
  <xsl:param name="node" as="element()"/>

  <xsl:variable name="id"
                select="$node/@xlink:href/string()
                        => translate('&lt;&gt;()','____')
                        => translate('[]#','...')
                        => replace(',\s*', '.')"/>

  <xsl:variable name="link" select="'m_' || $id"/>

  <!--
  <xsl:if test="not(key('id', $link, $node/root()))">
    <xsl:message select="'M:', $node/@xlink:href/string()"/>
    <xsl:message select="$link"/>
  </xsl:if>
  -->

  <xsl:if test="key('id', $link, $node/root())">
    <xsl:sequence select="$link"/>
  </xsl:if>
</xsl:function>

<xsl:function name="f:fieldLink" as="xs:string?">
  <xsl:param name="node" as="element()"/>

  <xsl:variable name="id"
                select="$node/@xlink:href/string()
                        => translate('&lt;&gt;()','____')
                        => translate('[]#','...')
                        => replace(',\s*', '.')"/>

  <xsl:variable name="link" select="'f_' || $id"/>

  <!--
  <xsl:if test="not(key('id', $link, $node/root()))">
    <xsl:message select="'M:', $node/@xlink:href/string()"/>
    <xsl:message select="$link"/>
  </xsl:if>
  -->

  <xsl:if test="key('id', $link, $node/root())">
    <xsl:sequence select="$link"/>
  </xsl:if>
</xsl:function>

<!-- ============================================================ -->

<xsl:template match="db:revdescription[ancestor::db:revhistory[contains-token(@role, 'changelog')]]"
              mode="m:docbook">
  <div class="changelog">
    <xsl:apply-templates select="." mode="m:attributes"/>
    <xsl:apply-templates select="*[not(@audience) and not(empty(node()))]" mode="m:docbook"/>
    <dl>
      <xsl:apply-templates select="*[contains-token(@audience, 'coffeegrinder')
                                   and not(empty(node()))]" mode="revdesc"/>
      <xsl:apply-templates select="*[contains-token(@audience, 'coffeefilter')
                                   and not(empty(node()))]" mode="revdesc"/>
      <xsl:apply-templates select="*[contains-token(@audience, 'coffeesacks')
                                   and not(empty(node()))]" mode="revdesc"/>
      <xsl:apply-templates select="*[contains-token(@audience, 'coffeepot')
                                   and not(empty(node()))]" mode="revdesc"/>
      <xsl:apply-templates select="*[@audience and empty(node())]" mode="emptyrevdesc"/>
    </dl>
  </div>
</xsl:template>

<!-- This doesn't *quite* work if you mark an element with multiple audiences -->
<xsl:template match="*[@audience]" mode="revdesc">
  <xsl:variable name="audience" select="@audience/string()"/>
  <xsl:if test="empty(preceding-sibling::*[@audience=$audience])">
    <div>
      <xsl:choose>
        <xsl:when test="contains-token($audience, 'coffeegrinder')"><dt>CoffeeGrinder</dt></xsl:when>
        <xsl:when test="contains-token($audience, 'coffeefilter')"><dt>CoffeeFilter</dt></xsl:when>
        <xsl:when test="contains-token($audience, 'coffeesacks')"><dt>CoffeeSacks</dt></xsl:when>
        <xsl:when test="contains-token($audience, 'coffeepot')"><dt>CoffeePot</dt></xsl:when>
        <xsl:otherwise><dt>???</dt></xsl:otherwise>
      </xsl:choose>
      <dd>
        <xsl:apply-templates select="../*[@audience=$audience]" mode="m:docbook"/>
      </dd>
    </div>
  </xsl:if>
</xsl:template>

<xsl:template match="*" mode="emptyrevdesc">
  <xsl:if test="not(preceding-sibling::*[@audience and empty(node())])">
    <xsl:variable name="empties" select="../*[@audience and empty(node())]"/>
    <xsl:variable name="empty-products" as="xs:string+">
      <xsl:for-each select="('coffeegrinder', 'coffeefilter', 'coffeesacks', 'coffeepot')">
        <xsl:variable name="prod" select="."/>
        <xsl:if test="exists($empties[contains-token(@audience, $prod)])">
          <xsl:sequence select="."/>
        </xsl:if>
      </xsl:for-each>
    </xsl:variable>
    <div>
      <dt>
        <xsl:for-each select="$empty-products">
          <xsl:if test="position() gt 1">, </xsl:if>
          <xsl:choose>
            <xsl:when test=". = 'coffeegrinder'">CoffeeGrinder</xsl:when>
            <xsl:when test=". = 'coffeefilter'">CoffeeFilter</xsl:when>
            <xsl:when test=". = 'coffeesacks'">CoffeeSacks</xsl:when>
            <xsl:when test=". = 'coffeepot'">CoffeePot</xsl:when>
          </xsl:choose>
        </xsl:for-each>
      </dt>
      <dd>
        <p>No significant changes; dependencies updated to latest version.</p>
      </dd>
    </div>
  </xsl:if>
</xsl:template>

<!-- ============================================================ -->

<xsl:template match="processing-instruction('constructor-summary')" mode="m:docbook">
  <xsl:variable name="fqn" select="normalize-space(.)"/>
  <xsl:variable name="class"
                select="$jd/*/class[@fulltype=$fqn] | $jd/*/interface[@fulltype=$fqn]"/>
  <xsl:if test="count($class) != 1">
    <xsl:message terminate="yes">No unique match for class {$fqn} ({count($class)}</xsl:message>
  </xsl:if>
  <xsl:apply-templates select="$class" mode="mp:doclet-constructor-summary"/>
</xsl:template>

<xsl:template match="processing-instruction('method-summary')" mode="m:docbook">
  <xsl:variable name="fqn" select="normalize-space(.)"/>
  <xsl:variable name="class"
                select="$jd/*/class[@fulltype=$fqn] | $jd/*/interface[@fulltype=$fqn]"/>
  <xsl:if test="count($class) != 1">
    <xsl:message terminate="yes">No unique match for class {$fqn} ({count($class)}</xsl:message>
  </xsl:if>
  <xsl:apply-templates select="$class" mode="mp:doclet-method-summary"/>
</xsl:template>

<xsl:template match="processing-instruction('class-summary')" mode="m:docbook">
  <xsl:variable name="fqn" select="normalize-space(.)"/>
  <xsl:variable name="class"
                select="$jd/*/class[@fulltype=$fqn] | $jd/*/interface[@fulltype=$fqn]"/>
  <xsl:if test="count($class) != 1">
    <xsl:message terminate="yes">No unique match for class {$fqn} ({count($class)}</xsl:message>
  </xsl:if>
  <xsl:apply-templates select="$class" mode="mp:doclet-class-summary"/>
</xsl:template>

</xsl:stylesheet>
