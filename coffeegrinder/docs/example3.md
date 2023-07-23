# Example 3, attributes

In the [repetition example](example2.md), we saw how nonterminals
could be marked optional with an attribute. You can create your own
attributes by extending `ParserAttribute`. The attributes that you
place in the grammar are available in the forests and trees produced
by the parser.

Another useful attribute provided by default is the “pruning”
attribute. If you mark a symbol as “prunable”, it may be removed
during a graph symplification process that happens just after the
parse finishes.

If you invent a lot of nonterminals in order to “flatten” a more
complex EBNF format, you can end up with graphs that appear to be
ambiguous where the original grammars were not.

Simplification will remove “prunable” nonterminals from the graph if
they lead to ε (matching nothing).

Simpilfication cannot remove all of the prunable symbols because that
would change the shape of the graph in some circumstances, and
changing the shape of the graph would fundamentally change the forest
of trees it represents.

The `_letter` and `_digit` nonterminals are good candidates to make
prunable:

```
List<ParserAttribute> attributes = new ArrayList<>();
attributes.add(Symbol.OPTIONAL);
attributes.add(ParserAttribute.PRUNING_ALLOWED);

NonterminalSymbol _letter = grammar.getNonterminal("_letter", attributes);
NonterminalSymbol _digit = grammar.getNonterminal("_digit", attributes);
```

In practice, these examples are so small that nothing is pruned. But the principle remains.
