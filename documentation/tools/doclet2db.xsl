<?xml version="1.0" encoding="utf-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:d="https://saxonica.com/ns/doclet"
                xmlns:db="http://docbook.org/ns/docbook"
                xmlns:f="https://saxonica.com/ns/functions"
                xmlns:h="http://www.w3.org/1999/xhtml"
                xmlns:m="http://docbook.org/ns/docbook/modes"
                xmlns:xlink="http://www.w3.org/1999/xlink"
                xmlns:xs="http://www.w3.org/2001/XMLSchema"
                xmlns="http://docbook.org/ns/docbook"
                exclude-result-prefixes="d db f h m xs"
                expand-text="yes"
                version="3.0">

<xsl:import href="html2db.xsl"/>

<xsl:output method="xml" encoding="utf-8" indent="no"
            omit-xml-declaration="yes"/>

<xsl:param name="package-prefix" required="yes"/>
<xsl:param name="className" required="yes"/>

<xsl:key name="method" match="d:method|d:constructor" use="@fullsig"/>
<xsl:key name="fullname" match="d:class|d:interface" use="@fullname"/>

<xsl:mode on-no-match="shallow-copy"/>

<xsl:template match="/" name="xsl:initial-template">
  <xsl:variable name="class"
                select="/*/*/d:class[starts-with(@package, $package-prefix)
                                     and @name=$className]
                        |/*/*/d:interface[starts-with(@package, $package-prefix)
                                          and @name=$className]"/>
  <xsl:choose>
    <xsl:when test="empty($class)">
      <xsl:message terminate="yes">Failed to find a class named {$className}</xsl:message>
    </xsl:when>
    <xsl:when test="count($class) gt 1">
      <xsl:message terminate="yes">Class name is not unique: {$className}</xsl:message>
    </xsl:when>
    <xsl:otherwise>
      <xsl:apply-templates select="$class"/>
    </xsl:otherwise>
  </xsl:choose>
</xsl:template>

<xsl:template match="d:class|d:interface">
  <section>
    <xsl:attribute name="xml:id" select="'c_' || @fullname"/>
    <title>
      <xsl:sequence select="@name/string()"/>
    </title>

    <para>
      <xsl:text>The </xsl:text>
      <xsl:choose>
        <xsl:when test="self::d:class">
          <classname>{@fullname/string()}</classname>
          <xsl:text> class.</xsl:text>
        </xsl:when>
        <xsl:otherwise>
          <interfacename>{@fullname/string()}</interfacename>
          <xsl:text> interface.</xsl:text>
        </xsl:otherwise>
      </xsl:choose>
    </para>

    <xsl:if test="d:superclass and not(d:superclass/d:type/@fullname = 'java.lang.Object')">
      <para>
        <xsl:choose>
          <xsl:when test="self::d:class">
            <xsl:text>This class extends </xsl:text>
            <classname>{d:superclass/d:type/@fullname/string()}</classname>
          </xsl:when>
          <xsl:otherwise>
            <xsl:text>This interface extends </xsl:text>
            <interfacename>{d:superclass/d:type/@fullname/string()}</interfacename>
          </xsl:otherwise>
        </xsl:choose>
        <xsl:text>.</xsl:text>
      </para>
    </xsl:if>

    <xsl:apply-templates select="d:description/h:body" mode="m:html2db"/>

    <xsl:if test="d:field[@access='public']">
      <section>
        <title>Field summary</title>

        <para>
          <xsl:text>Public fields for the </xsl:text>
          <classname>{@fullname/string()}</classname>
          <xsl:text> class.</xsl:text>
        </para>

        <variablelist role="fsummary">
          <xsl:apply-templates select="d:field[@access='public']">
            <xsl:with-param name="parent" select="."/>
          </xsl:apply-templates>
        </variablelist>
      </section>
    </xsl:if>

    <xsl:if test="d:constructor[@access='public']">
      <section>
        <title>Constructor summary</title>

        <para>
          <xsl:text>Public constructors for the </xsl:text>
          <classname>{@fullname/string()}</classname>
          <xsl:text> class.</xsl:text>
        </para>

        <variablelist role="csummary msummary">
          <xsl:apply-templates select="d:constructor[@access='public']"/>
        </variablelist>
      </section>
    </xsl:if>

    <xsl:if test="d:method[@access='public']">
      <section>
        <title>Method summary</title>

        <para>
          <xsl:text>Public methods on the </xsl:text>
          <xsl:choose>
            <xsl:when test="self::d:class">
              <classname>{@fullname/string()}</classname>
              <xsl:text> class.</xsl:text>
            </xsl:when>
            <xsl:otherwise>
              <interfacename>{@name/string()}</interfacename>
              <xsl:text> interface.</xsl:text>
            </xsl:otherwise>
          </xsl:choose>
        </para>

        <!--
        <xsl:message select="'====================='"/>
        <xsl:message select="string-join(d:method[@access='public']/@fullsig, '&#10;')"/>
        <xsl:message select="'- - - -'"/>
        <xsl:message select="string-join(f:inherited-methods(.)/@fullsig, '&#10;')"/>
        -->

        <variablelist role="msummary">
          <xsl:apply-templates select="(d:method[@access='public'], f:inherited-methods(.))">
            <xsl:with-param name="parent" select="."/>
            <xsl:sort select="substring-after(@fullsig, '#')"/>
          </xsl:apply-templates>
        </variablelist>
      </section>
    </xsl:if>
  </section>
</xsl:template>

<xsl:template match="d:field">
  <xsl:param name="parent" as="element()?"/>

  <varlistentry>
    <xsl:apply-templates select="." mode="generate-id">
      <xsl:with-param name="parent" select="($parent, parent::*)[1]"/>
    </xsl:apply-templates>
    <term role="synopsis">
      <code role="language-java">
        <xsl:text>public </xsl:text>
        <xsl:if test="@static">static </xsl:if>
        <xsl:if test="@final">final </xsl:if>
        <xsl:apply-templates select="d:type"/>
        <xsl:text> </xsl:text>
        <xsl:sequence select="@name/string()"/>
        <xsl:text> = </xsl:text>
      </code>
      <xsl:choose>
        <xsl:when test="@value">
          <code>
            <xsl:choose>
              <xsl:when test="d:type/ancestor-or-self::*[@fullname='java.lang.String']">
                <xsl:text>"</xsl:text>
                <xsl:sequence select="@value/string()"/>
                <xsl:text>"</xsl:text>
              </xsl:when>
              <xsl:otherwise>
                <xsl:sequence select="@value/string()"/>
              </xsl:otherwise>
            </xsl:choose>
          </code>
        </xsl:when>
        <xsl:otherwise>
          <replaceable>(runtime initializer)</replaceable>
        </xsl:otherwise>
      </xsl:choose>
    </term>
    <listitem>
      <para>
        <xsl:apply-templates select="d:purpose/h:body" mode="m:html2db"/>
      </para>
      <xsl:apply-templates select="d:description/h:body" mode="m:html2db"/>
    </listitem>
  </varlistentry>
</xsl:template>

<xsl:template match="d:constructor[d:implements and not(d:description)]
                     |d:method[d:implements and not(d:description)]
                     |d:method[d:overrides and not(d:description)]"
                     priority="10">
  <xsl:param name="parent" as="element()?"/>

  <xsl:variable name="iimpl"
                select="(key('method', d:implements/@interface),
                         key('method', d:overrides/@method))[1]"/>
  <xsl:next-match>
    <xsl:with-param name="parent" select="$parent"/>
    <xsl:with-param name="iimpl" select="$iimpl"/>
  </xsl:next-match>
</xsl:template>

<xsl:template match="d:constructor|d:method">
  <xsl:param name="parent" as="element()?"/>
  <xsl:param name="iimpl" as="element()?" select="()"/>

  <xsl:variable name="defclass" select="substring-before(@fullsig, '#')"/>
  <xsl:variable name="class" select="$parent/@fullname/string()"/>

  <varlistentry>
    <xsl:apply-templates select="." mode="generate-id">
      <xsl:with-param name="parent" select="($parent, parent::*)[1]"/>
    </xsl:apply-templates>
    <term role="synopsis">
      <code role="language-java">
        <xsl:text>public </xsl:text>
        <xsl:if test="d:return">
          <xsl:apply-templates select="d:return"/>
          <xsl:text> </xsl:text>
        </xsl:if>
        <xsl:choose>
          <xsl:when test="self::d:constructor">
            <xsl:sequence select="../@name/string()"/>
          </xsl:when>
          <xsl:otherwise>
            <xsl:sequence select="@name/string()"/>
          </xsl:otherwise>
        </xsl:choose>
        <xsl:text>(</xsl:text>
        <xsl:apply-templates select="d:parameter"/>
        <xsl:text>)</xsl:text>
      </code>
    </term>
    <xsl:if test="$defclass != $class">
      <term role="inherited">
        <xsl:text>Inherited from </xsl:text>
        <xsl:variable name="id"
                      select="$defclass||'#'||substring-after(@fullsig, '#')
                              => translate('&lt;&gt;()','____')
                              => translate('[]#','...')
                              => replace(',\s*', '.')"/>
        <code>
          <link role="ref">
            <xsl:attribute name="xlink:href">
              <xsl:sequence select="$id"/>
            </xsl:attribute>
            <xsl:sequence select="$defclass"/>
          </link>
        </code>
      </term>
    </xsl:if>
    <xsl:apply-templates select="($iimpl, .)[1]/d:deprecated"/>
    <xsl:apply-templates select="($iimpl, .)[1]/d:throws"/>
    <xsl:apply-templates select="($iimpl, .)[1]/d:overrides"/>
    <xsl:apply-templates select="($iimpl, .)[1]/d:implements"/>
    <listitem>
      <para>
        <xsl:apply-templates select="($iimpl, .)[1]/d:purpose/h:body" mode="m:html2db"/>
      </para>
      <xsl:apply-templates select="($iimpl, .)[1]/d:description/h:body" mode="m:html2db"/>
    </listitem>
  </varlistentry>
</xsl:template>

<xsl:template match="d:deprecated">
  <term role="deprecated">
    <xsl:text>deprecated </xsl:text>
    <xsl:apply-templates select="h:body" mode="m:html2db"/>
  </term>
</xsl:template>

<xsl:template match="d:throws">
  <term role="throws">
    <xsl:text>throws </xsl:text>
    <exceptionname>{@exception/string()}</exceptionname>
    <xsl:text> </xsl:text>
    <xsl:apply-templates select="h:body" mode="m:html2db"/>
  </term>
</xsl:template>

<xsl:template match="d:overrides">
  <xsl:variable name="id"
                select="@method
                        => translate('&lt;&gt;()','____')
                        => translate('[]#','...')
                        => replace(',\s*', '.')"/>

  <term role="overrides">
    <xsl:text>overrides </xsl:text>
    <code>
      <link role="ref">
        <xsl:attribute name="xlink:href">
          <xsl:sequence select="$id"/>
        </xsl:attribute>
        <xsl:sequence select="(substring-before(@method/string(), '#') => tokenize('\.'))[last()]"/>
      </link>
    </code>
  </term>
</xsl:template>

<xsl:template match="d:implements">
  <term role="implements">
    <xsl:text>implements </xsl:text>
    <code>
      <link role="ref">
        <xsl:attribute name="xlink:href">
          <xsl:apply-templates select=".." mode="generate-id-string">
            <xsl:with-param name="class" select="@interface/string()"/>
          </xsl:apply-templates>
        </xsl:attribute>
        <xsl:sequence select="(@interface/string() => tokenize('\.'))[last()]"/>
      </link>
    </code>
  </term>
</xsl:template>

<xsl:template match="d:parameter">
  <xsl:apply-templates select="d:type|d:array"/>
  <xsl:text> </xsl:text>
  <xsl:sequence select="@name/string()"/>
</xsl:template>

<!-- ============================================================ -->

<xsl:template match="d:field" mode="generate-id">
  <xsl:param name="parent" as="element()?"/>
  <xsl:variable name="id" as="xs:string">
    <xsl:apply-templates select="." mode="generate-id-string">
      <xsl:with-param name="parent" select="($parent, parent::*)[1]"/>
    </xsl:apply-templates>
  </xsl:variable>

  <xsl:attribute name="xml:id" select="'f_' || $id"/>
</xsl:template>

<xsl:template match="d:constructor" mode="generate-id">
  <xsl:param name="parent" as="element()?"/>
  <xsl:variable name="id" as="xs:string">
    <xsl:apply-templates select="." mode="generate-id-string">
      <xsl:with-param name="parent" select="($parent, parent::*)[1]"/>
    </xsl:apply-templates>
  </xsl:variable>

  <xsl:attribute name="xml:id" select="'m_' || $id"/>
</xsl:template>

<xsl:template match="d:method" mode="generate-id">
  <xsl:param name="parent" as="element()?"/>
  <xsl:variable name="id" as="xs:string">
    <xsl:apply-templates select="." mode="generate-id-string">
      <xsl:with-param name="parent" select="($parent, parent::*)[1]"/>
    </xsl:apply-templates>
  </xsl:variable>

  <xsl:attribute name="xml:id" select="'m_' || $id"/>
</xsl:template>

<xsl:template match="d:field" mode="generate-id-string" as="xs:string">
  <xsl:param name="parent" as="element()"/>
  <xsl:param name="class" as="xs:string" select="$parent/@fullname"/>
  <xsl:param name="name" as="xs:string" select="@name"/>

  <xsl:variable name="id" select="$name"/>

  <xsl:sequence select="$class || '.' || $id"/>
</xsl:template>

<xsl:template match="d:constructor|d:method" mode="generate-id-string" as="xs:string">
  <xsl:param name="parent" as="element()"/>
  <xsl:param name="class" as="xs:string" select="$parent/@fullname"/>
  <xsl:param name="name" as="xs:string" select="(@name, '_init_')[1]"/>

  <xsl:variable name="parts" as="xs:string+">
    <xsl:sequence select="$name"/>
    <xsl:text>(</xsl:text>
    <xsl:for-each select="d:parameter">
      <xsl:if test="position() gt 1">, </xsl:if>
      <xsl:sequence select="(d:type[@kind='primitive']/@name/string(),
                             d:type/@fullname/string(),
                             d:array/d:component/@fullname/string())[1]"/>
    </xsl:for-each>
    <xsl:text>)</xsl:text>
  </xsl:variable>

  <xsl:variable name="id"
                select="string-join($parts,'')
                        => translate('&lt;&gt;()','____')
                        => translate('[]','..')
                        => replace(',\s*', '.')"/>

  <xsl:sequence select="$class || '.' || $id"/>
</xsl:template>

<!-- ============================================================ -->

<xsl:template match="d:return[d:type/@name='void']|d:return[d:type/@name='Void']">
  <xsl:text>void</xsl:text>
</xsl:template>

<xsl:template match="d:return">
  <type>
    <xsl:apply-templates select="d:type"/>
  </type>
</xsl:template>

<xsl:template match="d:type">
  <xsl:sequence select="@name/string()"/>
  <xsl:if test="d:param">
    <xsl:text>&lt;</xsl:text>
    <xsl:apply-templates select="d:param"/>
    <xsl:text>&gt;</xsl:text>
  </xsl:if>
</xsl:template>

<xsl:template match="d:array">
  <xsl:apply-templates select="d:component"/>
  <xsl:text>[]</xsl:text>
</xsl:template>

<xsl:template match="d:component">
  <xsl:sequence select="@name/string()"/>
</xsl:template>

<xsl:template match="d:type/d:param[position() gt 1]" priority="10">
  <xsl:text>, </xsl:text>
  <xsl:next-match/>
</xsl:template>

<xsl:template match="d:type/d:param">
  <xsl:sequence select="@name/string()"/>
</xsl:template>

<!-- ============================================================ -->

<xsl:function name="f:signatures" as="xs:string*">
  <xsl:param name="class" as="element()"/>
  <xsl:for-each select="$class/d:method">
    <xsl:sequence select="substring-after(@fullsig, '#')"/>
  </xsl:for-each>
</xsl:function>

<xsl:function name="f:supertype-methods" as="element(d:method)*">
  <xsl:param name="class" as="element()"/>
  <xsl:param name="overrides" as="element(d:method)*"/>

  <xsl:variable name="superclass"
                select="if ($class/d:superclass/d:type/@fullname)
                        then key('fullname', $class/d:superclass/d:type/@fullname, $class/root())
                        else ()"/>

  <xsl:if test="$superclass">
    <xsl:variable name="current" as="element(d:method)*">
      <xsl:for-each select="$superclass/d:method">
        <xsl:if test="not(substring-after(@fullsig,'#') = ($overrides/@fullsig ! substring-after(., '#')))">
          <xsl:sequence select="."/>
        </xsl:if>
      </xsl:for-each>
    </xsl:variable>
    <xsl:sequence select="$current"/>
    <xsl:sequence select="f:supertype-methods($superclass, ($overrides, $current))"/>
  </xsl:if>
</xsl:function>

<xsl:function name="f:inherited-methods" as="element(d:method)*">
  <xsl:param name="class" as="element()"/>

  <xsl:variable name="sigs" select="f:signatures($class)"/>

  <xsl:variable name="supersigs" select="f:supertype-methods($class, $class/d:method)"/>

  <xsl:sequence select="$supersigs[@access='public']"/>
</xsl:function>

</xsl:stylesheet>
