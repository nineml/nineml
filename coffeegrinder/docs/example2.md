# Example 2, repetition

Continuing the [simple example](example1.md), let’s look at what is
required to support repetition. Revisiting our number or hex grammar,
what we’d really like to do is specify that letters and digits can be repeated:

```
id => word; hex
word => letter+
hex => digit+
letter => ["a"-"z"; "A"-"Z"]
digit => ["0"-"9"; "a"-"f"; "A"-"F"]
```

Unfortunately, the parsing API doesn’t support this feature directly.
(It will let you mark nonterminals as optional, but you can’t say
anything about repetition).

Luckily, this can be achieved by rewriting the grammar. Consider:

```
id => word; hex
word => letter, _letter?
_letter => letter, _letter?
hex => digit, _digit,
_digit => digit, _digit?
letter => ["a"-"z"; "A"-"Z"]
digit => ["0"-"9"; "a"-"f"; "A"-"F"]
```

You can create a parser for this grammar with the API:

```
Grammar grammar = new Grammar();
NonterminalSymbol id = grammar.getNonterminal("id");
NonterminalSymbol word = grammar.getNonterminal("word");
NonterminalSymbol hex = grammar.getNonterminal("hex");
NonterminalSymbol _letter = grammar.getNonterminal("_letter");
NonterminalSymbol _digit = grammar.getNonterminal("_digit");

CharacterSet set_0_9 = CharacterSet.range('0', '9');
CharacterSet set_a_f = CharacterSet.range('a', 'f');
CharacterSet set_A_F = CharacterSet.range('A', 'F');
CharacterSet set_a_z = CharacterSet.range('a', 'z');
CharacterSet set_A_Z = CharacterSet.range('A', 'Z');

TerminalSymbol letter = new TerminalSymbol(TokenCharacterSet.inclusion(set_a_z, set_A_Z));
TerminalSymbol digit = new TerminalSymbol(TokenCharacterSet.inclusion(set_0_9, set_a_f, set_A_F));

grammar.addRule(id, word);
grammar.addRule(id, hex);
grammar.addRule(word, letter);
grammar.addRule(word, letter, _letter);
grammar.addRule(_letter, letter);
grammar.addRule(_letter, letter, _letter);
grammar.addRule(hex, digit);
grammar.addRule(hex, digit, _digit);
grammar.addRule(_digit, digit);
grammar.addRule(_digit, digit, _digit);
```

Notice that the optionality of the symbols on the “right hand side” of
each rule had to be expressed explicitly. The API supports
“attributes” on symbols and tokens. One of those attributes allows you
to specify that the symbol is optional (only nonterminals can be optional).

```
NonterminalSymbol _letter = grammar.getNonterminal("_letter", Symbol.OPTIONAL);
NonterminalSymbol _digit = grammar.getNonterminal("_digit", Symbol.OPTIONAL);
```

(The `_letter` and `_digit` nonterminals are always optional, so I
don’t need to distinguish between optional and non-optional
occurrences. But if I did, I could just create two different
nonterminals.)

With these definitions, I can simply the rules to:

```
grammar.addRule(id, word);
grammar.addRule(id, hex);
grammar.addRule(word, letter, _letter);
grammar.addRule(_letter, letter, _letter);
grammar.addRule(hex, digit, _digit);
grammar.addRule(_digit, digit, _digit);
```

