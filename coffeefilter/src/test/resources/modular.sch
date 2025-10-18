<s:schema queryBinding="xslt2"
          xmlns:s="http://purl.oclc.org/dsdl/schematron">
  <s:pattern>
    <s:rule context="/">
      <s:assert test="ixml">The root is wrong.</s:assert>
      <s:assert test="ixml/rule[@name='partno']">No partno rule</s:assert>
      <s:assert test="ixml/rule[@name='hex']">No hex rule</s:assert>
      <s:assert test="ixml/rule[@name='digit']">No digit rule</s:assert>
      <s:assert test="ixml/rule[@name='decimal']">No decimal rule</s:assert>
      <s:assert test="ixml/rule[@name='_1_digit']">No renamed digit rule</s:assert>
      <s:assert test="count(ixml/rule) = 5">Wrong number of rules</s:assert>
    </s:rule>
  </s:pattern>

  <s:pattern>
    <s:rule context="/ixml/rule[@name='partno']">
      <s:assert test=".//nonterminal[@name='decimal']">No decimal in partno</s:assert>
      <s:assert test=".//nonterminal[@name='hex']">No hex in partno</s:assert>
    </s:rule>
  </s:pattern>

  <s:pattern>
    <s:rule context="/ixml/rule[@name='decimal']">
      <s:assert test=".//nonterminal[@name='_1_digit']">No renamed digit in partno</s:assert>
    </s:rule>
  </s:pattern>

  <s:pattern>
    <s:rule context="/ixml/rule[@name='digit']">
      <s:assert test="alt/inclusion/member[@from='0']">No 0-9 in digit</s:assert>
      <s:assert test="alt/inclusion/member[@from='a']">No a-f in digit</s:assert>
      <s:assert test="alt/inclusion/member[@from='A']">No A-F in digit</s:assert>
    </s:rule>
  </s:pattern>

  <s:pattern>
    <s:rule context="/ixml/rule[@name='_1_digit']">
      <s:assert test="@alias='digit'">No alias for digit</s:assert>
      <s:assert test="alt/inclusion/member[@from='0']">No 0-9 in digit</s:assert>
      <s:assert test="empty(alt/inclusion/member[@from='a'])">Chars a-f in renamed digit</s:assert>
      <s:assert test="empty(alt/inclusion/member[@from='A'])">Chars A-F in renamed digit</s:assert>
    </s:rule>
  </s:pattern>
</s:schema>
