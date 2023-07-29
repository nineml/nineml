<?xml version="1.0" encoding="utf-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:array="http://www.w3.org/2005/xpath-functions/array"
                xmlns:f="https://nineml.org/ns/functions"
                xmlns:t="https://nineml.org/ns/templates"
                xmlns:map="http://www.w3.org/2005/xpath-functions/map"
                xmlns:xs="http://www.w3.org/2001/XMLSchema"
                exclude-result-prefixes="#all"
                expand-text="yes"
                version="3.0">

<xsl:output method="text" encoding="utf-8" indent="no"/>
<xsl:strip-space elements="*"/>

<xsl:key name="id" match="sppf/*" use="@id"/>
<xsl:key name="target" match="*" use="@target"/>
<xsl:key name="label" match="sppf/*" use="@label"/>

<xsl:param name="bgcolor" select="'none'"/>
<xsl:param name="label-color" select="'none'"/>
<xsl:param name="show-states" select="'false'"/>
<xsl:param name="show-real-root" select="'true'"/>
<xsl:param name="rankdir" select="'TB'"/>
<xsl:param name="subgraph-color" select="'none'"/>

<xsl:param name="terminal-shape" select="'house'"/>
<xsl:param name="state-shape" select="'box'"/>
<xsl:param name="nonterminal-shape" select="'oval'"/>

<xsl:param name="show-ranges" select="'true'"/>
<xsl:param name="show-priority" select="'false'"/>
<xsl:param name="priority-color" select="'#56b4e9'"/>
<xsl:param name="priority-size" select="'10pt'"/>

<xsl:param name="ambiguity-font-color" select="'#0072b2'"/>

<xsl:param name="node-font-name" select="'Arial'"/>
<xsl:param name="node-color" select="'black'"/>
<xsl:param name="node-fill-color" select="'white'"/>
<xsl:param name="node-font-color" select="'black'"/>
<xsl:param name="node-pen-width" select="1"/>

<xsl:param name="selected-node-font-name" select="'Arial'"/>
<xsl:param name="selected-node-color" select="'#d55e00'"/>
<xsl:param name="selected-node-fill-color" select="'white'"/>
<xsl:param name="selected-node-font-color" select="'black'"/>
<xsl:param name="selected-node-pen-width" select="3"/>

<xsl:param name="edge-color" select="'black'"/>
<xsl:param name="edge-style" select="'solid'"/>
<xsl:param name="alt-edge-color" select="'#aaaaaa'"/>
<xsl:param name="alt-edge-style" select="'dashed'"/>
<xsl:param name="edge-pen-width" select="1"/>

<xsl:param name="selected-nodes" select="''"/>
<xsl:param name="selected-root" select="()"/>
<xsl:param name="selected-depth" select="'INF'"/>

<!-- The root is the node that isn't a link target of any other node -->
<xsl:variable name="real-graph-root" select="sppf/*[empty(key('target', @id))]"/>

<xsl:variable name="root-node" as="element()?">
  <xsl:choose>
    <xsl:when test="empty($selected-root)">
      <xsl:sequence select="()"/>
    </xsl:when>
    <xsl:when test="count(key('label', $selected-root)) = 1">
      <xsl:sequence select="key('label', $selected-root)"/>
    </xsl:when>
    <xsl:when test="count(key('id', $selected-root)) = 1">
      <xsl:sequence select="key('id', $selected-root)"/>
    </xsl:when>
    <xsl:when test="count(key('label', $selected-root)) gt 1">
      <xsl:message select="'The $selected-root doesn''t uniquely identify a node:', $selected-root"/>
      <xsl:sequence select="()"/>
    </xsl:when>
    <xsl:otherwise>
      <xsl:message select="'The $selected-root doesn''t identify a node:', $selected-root"/>
      <xsl:sequence select="()"/>
    </xsl:otherwise>
  </xsl:choose>
</xsl:variable>

<xsl:variable name="graph-root" as="element()">
  <xsl:choose>
    <xsl:when test="$show-real-root = 'true'">
      <xsl:sequence select="$real-graph-root"/> 
    </xsl:when>
    <xsl:otherwise>
      <xsl:variable name="id" select="$real-graph-root/link/@target/string()"/>
      <xsl:sequence select="key('id', $id)"/>
    </xsl:otherwise>
  </xsl:choose>
</xsl:variable>

<xsl:variable name="selected-node-ids" as="xs:string*"
              select="tokenize($selected-nodes, '\s*,\s*')"/>

<xsl:function name="f:descendants" as="xs:string*" cache="yes">
  <xsl:param name="context" as="element()"/>
  <xsl:sequence select="f:descendants($context, (), 0)"/>
</xsl:function>

<xsl:function name="f:descendants" as="xs:string*" cache="yes">
  <xsl:param name="context" as="element()"/>
  <xsl:param name="seen" as="xs:string*"/>
  <xsl:param name="depth" as="xs:integer"/>

<!--
  <xsl:message select="'DESC:', $context"/>
  <xsl:message select="'    :', $seen"/>
  <xsl:message select="'    :', $depth"/>
-->

  <xsl:variable name="max-depth" as="xs:integer">
    <xsl:choose>
      <xsl:when test="$selected-depth castable as xs:integer">
        <xsl:sequence select="max((0, xs:integer($selected-depth)))"/>
      </xsl:when>
      <xsl:otherwise>
        <xsl:sequence select="-1"/>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:variable>

  <xsl:if test="$depth le $max-depth or $max-depth lt 0">
    <xsl:sequence select="$context/@id/string()"/>

    <xsl:for-each select="$context//link">
      <xsl:variable name="node" select="key('id', @target, root($context))"/>
      <xsl:if test="not($node/@id = $seen)">
        <xsl:sequence select="f:descendants($node, ($seen, $node/@id/string()), $depth + 1)"/>
      </xsl:if>
    </xsl:for-each>
  </xsl:if>
</xsl:function>

<xsl:function name="f:include" as="xs:boolean">
  <xsl:param name="node" as="element()"/>

  <xsl:variable name="target"
                select="if ($node/self::link)
                        then key('id', $node/@target, root($node))
                        else $node"/>

<!--
  <xsl:message select="local-name($node), $target/@id/string()"/>
  <xsl:message select="'  r:', $selected-root"/>
  <xsl:message select="'   :', $root-node"/>
  <xsl:message select="'  t:', f:descendants($target)"/>
  <xsl:message select="'  d:', f:descendants($root-node)"/>
-->

  <xsl:variable name="include" select="empty($root-node)
                                       (: or $selected-root = f:descendants($target) :)
                                       or $target/@id = f:descendants($root-node)"/>

  <xsl:sequence select="$include"/>
</xsl:function>

<xsl:function name="f:link-to" as="xs:boolean" cache="true">
  <xsl:param name="node" as="element()"/>
  <xsl:variable name="id" select="$node/@id/string()"/>
  <xsl:variable name="link" as="xs:boolean*">
    <xsl:for-each select="root($node)//link">
      <xsl:choose>
        <xsl:when test="@target = $id and parent::pair">
          <xsl:sequence select="f:include(../..)"/>
        </xsl:when>
        <xsl:when test="@target = $id">
          <xsl:sequence select="f:include(..)"/>
        </xsl:when>
        <xsl:otherwise>
          <!-- no -->
        </xsl:otherwise>
      </xsl:choose>
    </xsl:for-each>
  </xsl:variable>
  <xsl:sequence select="true() = $link"/>
</xsl:function>

<xsl:template match="sppf">
  <xsl:text>digraph SPPF {{&#10;</xsl:text>
  <xsl:text>rankdir={$rankdir}&#10;</xsl:text>
  <xsl:text>bgcolor="{$bgcolor}"&#10;</xsl:text>
  <xsl:apply-templates select="*[$show-real-root = 'true' or not(@label = '$$')]"/>

  <xsl:if test="$root-node and not($root-node is $graph-root)">
    <xsl:text>fakeroot [ style=invis ]&#10;</xsl:text>
    <xsl:text>fakeroot -> node{$root-node/@id} </xsl:text>
    <xsl:text>[color="{$alt-edge-color}" penwidth={$edge-pen-width} </xsl:text>
    <xsl:text>style={$alt-edge-style} ];&#10;</xsl:text>
  </xsl:if>

  <xsl:text>}}&#10;</xsl:text>
</xsl:template>

<xsl:template name="t:node">
  <xsl:variable name="shape"
                select="if (@type = 'state')          then $state-shape
                        else if (@type = 'terminal')  then $terminal-shape
                        else $nonterminal-shape"/>

  <xsl:variable name="prio" as="xs:string*">
    <xsl:if test="$show-priority != 'false' and @priority != '0.00'">
      <xsl:text>&lt;sup>&lt;font </xsl:text>
      <xsl:text>point-size="{$priority-size}" color="{$priority-color}"> </xsl:text>
      <xsl:text>{@priority/string()}&lt;/font>&lt;/sup></xsl:text>
    </xsl:if>
  </xsl:variable>
  <xsl:variable name="prio" select="string-join($prio, '')"/>

  <xsl:variable name="label" as="xs:string+">
    <xsl:choose>
      <!-- Support the way the GLL parser encodes empty matches -->
      <xsl:when test="@type = 'terminal' and @leftExtent = @rightExtent">
        <xsl:sequence select="'ε'"/>
      </xsl:when>
      <xsl:otherwise>
        <xsl:sequence select="replace(replace(@label, '\\', '\\\\'), '&quot;', '\\&quot;') || $prio"/>
        <xsl:if test="@type = 'nonterminal' and $show-states != 'false'">
          <xsl:sequence select="replace(replace(@state, '\\', '\\\\'), '&quot;', '\\&quot;')"/>
        </xsl:if>
      </xsl:otherwise>
    </xsl:choose>
    
    <xsl:if test="@leftExtent and @rightExtent">
      <xsl:variable name="left" select="xs:integer(@leftExtent)"/>
      <xsl:variable name="right" select="xs:integer(@rightExtent)"/>
      <xsl:variable name="extent" as="xs:string">
        <xsl:choose>
          <xsl:when test="$left != $right and $left + 1 != $right">
            <xsl:sequence select="$left+1 || ' – ' || $right+1"/>
          </xsl:when>
          <xsl:when test="$left + 1 = $right">
            <xsl:sequence select="string($left+1)"/>
          </xsl:when>
          <xsl:otherwise>
            <xsl:sequence select="'ε'"/>
          </xsl:otherwise>
        </xsl:choose>
      </xsl:variable>
      <xsl:variable name="ambig" as="xs:string?">
        <xsl:if test="@trees ne '1'">
          <xsl:sequence select="' / ' || @trees"/>
        </xsl:if>
      </xsl:variable>

      <xsl:if test="$show-ranges = 'true' and ($extent != 'ε' or $ambig != '')">
        <xsl:sequence select="'« ' || $extent || ' »' || $ambig"/>
      </xsl:if>
    </xsl:if>
  </xsl:variable>

  <xsl:text>node{@id/string()} [ label=&lt;{string-join($label, '&lt;BR/>&#10;')}&gt; </xsl:text>

  <xsl:choose>
    <xsl:when test="contains(string-join($label, ''), 'ε')">
      <xsl:text>shape=none </xsl:text>
    </xsl:when>
    <xsl:otherwise>
      <xsl:text>shape={$shape} </xsl:text>
    </xsl:otherwise>
  </xsl:choose>

  <xsl:variable name="font-color" as="xs:string">
    <xsl:choose>
      <xsl:when test="not(@trees) or @trees = '1' or $ambiguity-font-color = ''
                      and @id = $selected-node-ids">
        <xsl:sequence select="$selected-node-font-color"/>
      </xsl:when>
      <xsl:when test="not(@trees) or @trees = '1' or $ambiguity-font-color = ''">
        <xsl:sequence select="$node-font-color"/>
      </xsl:when>
      <xsl:otherwise>
        <xsl:sequence select="$ambiguity-font-color"/>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:variable>

  <xsl:choose>
    <xsl:when test="@id = $selected-node-ids">
      <xsl:variable name="style"
                    select="if (@type = 'state')
                            then 'style=&quot;rounded,filled&quot;'
                            else 'style=&quot;filled&quot;'"/>

      <xsl:text>{$style} </xsl:text>
      <xsl:text>color="{$selected-node-color}" style="filled" fillcolor="{$selected-node-fill-color}" </xsl:text>
      <xsl:text>penwidth="{$selected-node-pen-width}" </xsl:text>
      <xsl:text>fontcolor="{$font-color}" </xsl:text>
      <xsl:text>fontname="{$selected-node-font-name}" </xsl:text>
    </xsl:when>
    <xsl:otherwise>
      <xsl:variable name="style"
                    select="if (@type = 'state')
                            then 'style=&quot;rounded&quot;'
                            else ''"/>

      <xsl:text>{$style} </xsl:text>
      <xsl:text>color="{$node-color}" style="filled" fillcolor="{$node-fill-color}" </xsl:text>
      <xsl:text>penwidth="{$node-pen-width}" </xsl:text>
      <xsl:text>fontcolor="{$font-color}" </xsl:text>
      <xsl:text>fontname="{$node-font-name}" </xsl:text>
    </xsl:otherwise>
  </xsl:choose>

  <xsl:text>]&#10;</xsl:text>
</xsl:template>

<xsl:template match="sppf/*">
  <xsl:choose>
    <xsl:when test="f:include(.)">
      <xsl:text>subgraph cluster_{@id/string()} {{&#10;</xsl:text>
      <xsl:text>style="filled"; color="{$subgraph-color}";&#10;</xsl:text>

      <xsl:if test="$label-color != 'none'">
        <xsl:text>label={@id}; </xsl:text>
        <xsl:text>fontsize="10pt"; fontcolor={$label-color}&#10;</xsl:text>
      </xsl:if>

      <xsl:call-template name="t:node"/>

      <xsl:apply-templates/>
      <xsl:text>}}&#10;</xsl:text>
    </xsl:when>
    <xsl:when test="f:link-to(.)">
      <xsl:text>node{@id/string()} [ label="···" color=none ]&#10;</xsl:text>
    </xsl:when>
  </xsl:choose>
</xsl:template>

<xsl:template name="t:pair">
  <xsl:variable name="prio" as="xs:string*">
    <xsl:if test="$show-priority != 'false' and @priority != '0.00'">
      <xsl:text>&lt;font </xsl:text>
      <xsl:text>point-size="{$priority-size}" color="{$priority-color}"></xsl:text>
      <xsl:text>{@priority/string()}&lt;/font></xsl:text>
    </xsl:if>
  </xsl:variable>
  <xsl:variable name="prio" select="string-join($prio, '')"/>

  <xsl:variable name="size" select="if ($show-priority = 'false') then '0.25' else '0.5'"/>

  <xsl:text>node{../@id/string()} -&gt; anon{generate-id(.)} </xsl:text>
  <xsl:text>[color={$edge-color} penwidth={$edge-pen-width}];&#10;</xsl:text>
  <xsl:text>anon{generate-id(.)} [ label=&lt;{$prio}&gt; </xsl:text>
  <xsl:text>shape=circle color={$edge-color} width={$size} height={$size} fixedsize=true </xsl:text>
  <xsl:text>penwidth={$node-pen-width} ]&#10;</xsl:text>
</xsl:template>

<xsl:template match="pair">
  <xsl:call-template name="t:pair"/>

  <xsl:if test="f:include(link[1]) and f:include(link[2])">
    <xsl:text>{{ rank=same;&#10;</xsl:text>
    <xsl:text>node{link[1]/@target/string()} -&gt; node{link[2]/@target/string()} </xsl:text>  
    <xsl:text>[ style=invis ]}}&#10;</xsl:text>
  </xsl:if>

  <xsl:apply-templates/>
</xsl:template>

<xsl:template match="link">
  <xsl:choose>
    <xsl:when test="f:include(..) and f:include(.)">
      <xsl:text>node{../@id/string()} -&gt; node{@target/string()} </xsl:text>
      <xsl:text>[color="{$edge-color}" penwidth={$edge-pen-width} </xsl:text>
      <xsl:text>style={$edge-style} ];&#10;</xsl:text>
    </xsl:when>
    <xsl:when test="f:include(..)">
      <xsl:text>node{../@id/string()} -&gt; node{@target/string()} </xsl:text>
      <xsl:text>[color="{$alt-edge-color}" penwidth={$edge-pen-width} </xsl:text>
      <xsl:text>style={$alt-edge-style} ];&#10;</xsl:text>
    </xsl:when>
    <xsl:otherwise>
      <!-- no edge -->
    </xsl:otherwise>
  </xsl:choose>
</xsl:template>

<xsl:template match="pair/link" priority="100">
  <xsl:choose>
    <xsl:when test="f:include(../..) and f:include(.)">
      <xsl:text>anon{generate-id(..)} -&gt; node{@target/string()} </xsl:text>
      <xsl:text>[color="{$edge-color}" penwidth={$edge-pen-width} </xsl:text>
      <xsl:text>style={$edge-style} ];&#10;</xsl:text>
    </xsl:when>
    <xsl:when test="f:include(../..)">
      <xsl:text>anon{generate-id(..)} -&gt; node{@target/string()} </xsl:text>
      <xsl:text>[color="{$alt-edge-color}" penwidth={$edge-pen-width} </xsl:text>
      <xsl:text>style={$alt-edge-style} ];&#10;</xsl:text>
    </xsl:when>
    <xsl:otherwise>
      <!-- no edge -->
    </xsl:otherwise>
  </xsl:choose>
</xsl:template>

<xsl:template match="epsilon">
  <xsl:text>epsilon{generate-id(.)} [ label="ε" shape=none ]&#10;</xsl:text>
  <xsl:text>node{../@id/string()} -&gt; epsilon{generate-id(.)} </xsl:text>
  <xsl:text>[color={$edge-color} penwidth={$edge-pen-width}];&#10;</xsl:text>
</xsl:template>

</xsl:stylesheet>
