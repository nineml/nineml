<s:schema queryBinding="xslt2"
          xmlns:s="http://purl.oclc.org/dsdl/schematron">
  <s:pattern>
    <s:rule context="/">
      <s:assert test="ixml">The root is wrong.</s:assert>
      <s:assert test="ixml/rule[@name='partno']">No partno rule</s:assert>
      <s:assert test="ixml/rule[@name='digit']">No digit rule</s:assert>
      <s:assert test="ixml/rule[@name='decimal']">No decimal rule</s:assert>
      <s:assert test="count(ixml/rule) = 3">Wrong number of rules</s:assert>
    </s:rule>
  </s:pattern>

  <s:pattern>
    <s:rule context="/ixml/rule[@name='partno']">
      <s:assert test=".//nonterminal[@name='decimal']">No decimal in partno</s:assert>
    </s:rule>
  </s:pattern>

  <s:pattern>
    <s:rule context="/ixml/rule[@name='decimal']">
      <s:assert test=".//nonterminal[@name='digit']">No digit in partno</s:assert>
    </s:rule>
  </s:pattern>

  <s:pattern>
    <s:rule context="/ixml/rule[@name='digit']">
      <s:assert test="alt/inclusion/member[@from='0']">No 0-9 in digit</s:assert>
      <s:assert test="alt/inclusion/member[@from='a']">No a-f in digit</s:assert>
      <s:assert test="alt/inclusion/member[@from='A']">No A-F in digit</s:assert>
    </s:rule>
  </s:pattern>
</s:schema>
