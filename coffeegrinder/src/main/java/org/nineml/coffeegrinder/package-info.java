/**
 * CoffeeGrinder implements both Earley and GLL parsers. Both are capable
 * of using ambiguous grammars. CoffeeGrinder parses (grinds?) a sequence of tokens and
 * returns a Shared Packed Parse Forest.
 * Individual parse trees can be obtained from the forest.
 *
 * <h2>References</h2>
 * <ul>
 *     <li>
 *         <a href="https://loup-vaillant.fr/tutorials/earley-parsing/parser">Earley Parsing Explained</a>
 *     </li>
 *     <li>
 *         <a href="https://web.stanford.edu/class/archive/cs/cs143/cs143.1128/lectures/07/Slides07.pdf">Advanced
 *         Parsing Techniques</a>
 *     </li>
 *     <li>
 *         <a href="http://dinhe.net/~aredridel/.notmine/PDFs/Parsing/SCOTT,%20Elizabeth%20-%20SPPF-Style%20Parsing%20From%20Earley%20Recognizers.pdf">SPPF-Style
 *         Parsing From Earley Recognizers</a>
 *     </li>
 * </ul>
 */
package org.nineml.coffeegrinder;
