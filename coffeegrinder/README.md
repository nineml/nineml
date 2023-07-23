# CoffeeGrinder, an Earley parser in Java

This package provides an implementation of an Earley parser. The
Earley parsing algorithm allows grammars to be ambiguous. This API
parses a sequence of tokens and returns a Shared Packed Parser Forest.
Individual parse trees can be obtained from the forest.

This parser is a fairly literal implementation of the parser in §5 of
[SPPF-Style Parsing From Earley Recognisers](https://www.sciencedirect.com/science/article/pii/S1571066108001497?via%3Dihub).
It has been extended slightly to return a little more information in
the forest and to support parses that match only a prefix of the input
sequence.

## HOWTO

In brief:

* Create a `Grammar`.
* Use the grammar to create `NonterminalSymbol`s.
* Use the grammar (or `Rule` directly) to create rules and add them to the grammar.
* Create an `EarleyParser` from the grammar.
* Parse a sequence of input tokens to obtain an `EarleyResult`.
* From the result, you can get information about the success or
  failure of the parse, and possibly continue parsing.
* If the parse was successful, get the `ParseForest` (shared packed
  parse forest) from the parse. (If the parse was unsuccessful, you
  can also get the `EarleyChart` and a bit more information about
  where the parsing failed.)
* Use the forest to get a `ParseTree` parse tree(s).
* The forest will also tell you about the ambiguity of the result, the
  number of parse trees available, etc.

Profit.

## Examples

Several examples of the API are documented in the [docs](docs) directory.

## References

The following, no doubt incomplete, references influenced my thinking about this parser.

* [Earley Parsing Explained](https://loup-vaillant.fr/tutorials/earley-parsing/parser)
* [Advanced Parsing Techniques](https://web.stanford.edu/class/archive/cs/cs143/cs143.1128/lectures/07/Slides07.pdf)
* [SPPF-Style Parsing From Earley Recognizers](http://dinhe.net/~aredridel/.notmine/PDFs/Parsing/SCOTT,%20Elizabeth%20-%20SPPF-Style%20Parsing%20From%20Earley%20Recognizers.pdf)
* [Brabrand et. al 2010] Brabrand, Claus, Robert
Giegerich, and Anders Möller. “Analyzing Ambiguity of Context-Free Grammars.”
<em>Science of Computer Programming</em>, volume 75, number 3.
Elsevier. (2010). https://cs.au.dk/~amoeller/papers/ambiguity/

CoffeeGrinder includes a copy of Anders Møller’s
[ambiguity analyzer](https://www.brics.dk/grammar/).
