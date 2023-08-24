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

<!-- ============================================================ -->

<xsl:variable name="v:templates" as="document-node()"
              xmlns:tmp="http://docbook.org/ns/docbook/templates"
              xmlns:db="http://docbook.org/ns/docbook"
              xmlns="http://www.w3.org/1999/xhtml">
  <xsl:document>
    <db:book>
      <header>
        <div class="bg">
          <img src="images/nineml.png"/>
        </div>
        <h1><span>nineml</span></h1>
        <h2>A family of Invisible XML tools</h2>
        <div class="copyright">Copyright © <span class="copyright-years">
        <span class="year">2022</span></span>
        <span class="holder">Norm Tovey-Walsh</span>
        </div>
      </header>

      <div class="products">
        <div class="row">
          <div class="product">
            <a href="https://docs.nineml.org/current/coffeegrinder">
              <img src="images/CoffeeGrinder.png" width="300"/>
            </a>
            <h3><a href="https://docs.nineml.org/current/coffeegrinder">CoffeeGrinder</a></h3>
            <p>Earley and GLL parsers</p>
          </div>
          <div class="product">
            <a href="https://docs.nineml.org/current/coffeefilter">
              <img src="images/CoffeeFilter.png" width="300"/>
            </a>
            <h3><a href="https://docs.nineml.org/current/coffeefilter">CoffeeFilter</a></h3>
            <p>An Invisible XML parser</p>
          </div>
        </div>
        <div class="row">
          <div class="product">
            <a href="https://docs.nineml.org/current/coffeepot">
              <img src="images/CoffeePot.png" width="300"/>
            </a>
            <h3><a href="https://docs.nineml.org/current/coffeepot">CoffeePot</a></h3>
            <p>An Invisible XML processor</p>
          </div>
          <div class="product">
            <a href="https://docs.nineml.org/current/coffeesacks">
              <img src="images/CoffeeSacks.png" width="300"/>
            </a>
            <h3><a href="https://docs.nineml.org/current/coffeesacks">CoffeeSacks</a></h3>
            <p>Saxon APIs for Invisible XML</p>
          </div>
        </div>
        <div class="row">
          <div style="text-align: center; width:100%">
            Also available:
            <a href="https://github.com/ndw/coffeepress">CoffeePress</a>,
            an <a href="https://xproc.org/">XProc 3.0 step</a> for
            <a href="https://github.com/ndw/xmlcalabash2">XML Calabash</a>.
            <br/>
            Status updates:
            <a href="/@nineml/">web</a>,
            <a href="/@nineml/status.rss">RSS</a>.
          </div>
        </div>
      </div>
    </db:book>
  </xsl:document>
</xsl:variable>

<!-- ============================================================ -->

<xsl:template match="*" mode="m:html-body-script">
  <xsl:param name="rootbaseuri"/>
  <xsl:param name="chunkbaseuri"/>
  <!-- no link -->
</xsl:template>

<xsl:template match="*" mode="m:html-head-links">
  <xsl:next-match/>
  <link rel="shortcut icon" href="icon/nineml.png"/>

  <meta name="viewport" content="width=device-width, initial-scale=1.0"/>
  <link rel="stylesheet" href="css/nineml.css"/>
  <link rel="stylesheet" href="css/website.css"/>
</xsl:template>

</xsl:stylesheet>
