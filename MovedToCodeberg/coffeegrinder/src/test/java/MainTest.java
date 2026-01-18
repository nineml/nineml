import org.junit.jupiter.api.Assertions;
import org.nineml.coffeegrinder.gll.GllParser;
import org.nineml.coffeegrinder.gll.GllResult;
import org.nineml.coffeegrinder.parser.*;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Calendar;

public class MainTest {
    public static void main(String[] args) {
        ParserOptions options = new ParserOptions();
        options.setParserType("GLL");
        SourceGrammar grammar = new SourceGrammar(options);

/*
S ⇒ UnicodeData
UnicodeData ⇒ record_pp_LF, LF
record ⇒ codepoint, ';', name, ';', category, ';', combining, ';', bidi, ';', decomposition, ';', decimal, ';', digit, ';', numeric, ';', mirrored, ';', name_1_0, ';', comment, ';', uppercase, ';', lowercase, ';', titlecase
LF ⇒ '\n'
bidi ⇒ value
category ⇒ value
codepoint ⇒ hexPlus
combining ⇒ value
comment ⇒ value
decimal ⇒ value
decomposition ⇒ value
digit ⇒ value
hexPlus ⇒ ["0"-"9";"A"-"F"], hex_opt
hex_opt ⇒
hex_opt ⇒ hex_rep
hex_rep ⇒ ["0"-"9";"A"-"F"], hex_opt
lowercase ⇒ value
mirrored ⇒ value
name ⇒ notSemiPlus
name_1_0 ⇒ value
notSemiPlus ⇒ ~[";"], optMoreNotSemi
notSemiRep ⇒ ~[";"]
notSemiRep ⇒ ~[";"], notSemiRep
notSemiStar ⇒
notSemiStar ⇒ notSemiRep
numeric ⇒ value
optMoreNotSemi ⇒
optMoreNotSemi ⇒ optNextNotSemi
optNextNotSemi ⇒ ~[";"], optMoreNotSemi
opt_LF_record ⇒
opt_LF_record ⇒ opt_record
opt_record ⇒ LF, record
opt_record ⇒ LF, record, opt_record
record_pp_LF ⇒ record
record_pp_LF ⇒ record, opt_LF_record
titlecase ⇒ value
uppercase ⇒ value
value ⇒
value ⇒ notSemiStar
*/

        NonterminalSymbol S = grammar.getNonterminal("S");
        NonterminalSymbol UnicodeData = grammar.getNonterminal("UnicodeData");
        NonterminalSymbol bidi = grammar.getNonterminal("bidi");
        NonterminalSymbol category = grammar.getNonterminal("category");
        NonterminalSymbol codepoint = grammar.getNonterminal("codepoint");
        NonterminalSymbol combining = grammar.getNonterminal("combining");
        NonterminalSymbol comment = grammar.getNonterminal("comment");
        NonterminalSymbol decimal = grammar.getNonterminal("decimal");
        NonterminalSymbol decomposition = grammar.getNonterminal("decomposition");
        NonterminalSymbol digit = grammar.getNonterminal("digit");
        NonterminalSymbol hexPlus = grammar.getNonterminal("hexPlus");
        NonterminalSymbol hex_opt = grammar.getNonterminal("hex_opt");
        NonterminalSymbol hex_rep = grammar.getNonterminal("hex_rep");
        NonterminalSymbol lowercase = grammar.getNonterminal("lowercase");
        NonterminalSymbol mirrored = grammar.getNonterminal("mirrored");
        NonterminalSymbol name = grammar.getNonterminal("name");
        NonterminalSymbol name_1_0 = grammar.getNonterminal("name_1_0");
        NonterminalSymbol notSemiPlus = grammar.getNonterminal("notSemiPlus");
        NonterminalSymbol notSemiRep = grammar.getNonterminal("notSemiRep");
        NonterminalSymbol notSemiStar = grammar.getNonterminal("notSemiStar");
        NonterminalSymbol numeric = grammar.getNonterminal("numeric");
        NonterminalSymbol optMoreNotSemi = grammar.getNonterminal("optMoreNotSemi");
        NonterminalSymbol optNextNotSemi = grammar.getNonterminal("optNextNotSemi");
        NonterminalSymbol opt_LF_record = grammar.getNonterminal("opt_LF_record");
        NonterminalSymbol opt_record = grammar.getNonterminal("opt_record");
        NonterminalSymbol record = grammar.getNonterminal("record");
        NonterminalSymbol record_pp_LF = grammar.getNonterminal("record_pp_LF");
        NonterminalSymbol titlecase = grammar.getNonterminal("titlecase");
        NonterminalSymbol uppercase = grammar.getNonterminal("uppercase");
        NonterminalSymbol value = grammar.getNonterminal("value");

        TerminalSymbol LF = TerminalSymbol.ch('\n');
        TerminalSymbol semi = TerminalSymbol.ch(';');
        TerminalSymbol notsemi = TerminalSymbol.regex("[^;]");
        TerminalSymbol hexdigit = TerminalSymbol.regex("[0-9,A-F]");

        grammar.addRule(S, UnicodeData);
        grammar.addRule(UnicodeData, record_pp_LF, LF);
        grammar.addRule(record, codepoint, semi, name, semi, category, semi, combining, semi, bidi, semi,
                decomposition, semi, decimal, semi, digit, semi, numeric, semi, mirrored, semi, name_1_0,
                semi, comment, semi, uppercase, semi, lowercase, semi, titlecase);
        grammar.addRule(bidi, value);
        grammar.addRule(category, value);
        grammar.addRule(codepoint, hexPlus);
        grammar.addRule(combining, value);
        grammar.addRule(comment, value);
        grammar.addRule(decimal, value);
        grammar.addRule(decomposition, value);
        grammar.addRule(digit, value);
        grammar.addRule(hexPlus, hexdigit, hex_opt);
        grammar.addRule(hex_opt);
        grammar.addRule(hex_opt, hex_rep);
        grammar.addRule(hex_rep, hexdigit, hex_opt);
        grammar.addRule(lowercase, value);
        grammar.addRule(mirrored, value);
        grammar.addRule(name, notSemiPlus);
        grammar.addRule(name_1_0, value);
        grammar.addRule(notSemiPlus, notsemi, optMoreNotSemi);
        grammar.addRule(notSemiRep, notsemi);
        grammar.addRule(notSemiRep, notsemi, notSemiRep);
        grammar.addRule(notSemiStar);
        grammar.addRule(notSemiStar, notSemiRep);
        grammar.addRule(numeric, value);
        grammar.addRule(optMoreNotSemi);
        grammar.addRule(optMoreNotSemi, optNextNotSemi);
        grammar.addRule(optNextNotSemi, notsemi, optMoreNotSemi);
        grammar.addRule(opt_LF_record);
        grammar.addRule(opt_LF_record, opt_record);
        grammar.addRule(opt_record, LF, record);
        grammar.addRule(opt_record, LF, record, opt_record);
        grammar.addRule(record_pp_LF, record);
        grammar.addRule(record_pp_LF, record, opt_LF_record);
        grammar.addRule(titlecase, value);
        grammar.addRule(uppercase, value);
        grammar.addRule(value);
        grammar.addRule(value, notSemiStar);

        try {
            GllParser gllParser = (GllParser) grammar.getParser(options, S);

            int lineCount = 0;

            //BufferedInputStream stream = new BufferedInputStream(Files.newInputStream(Paths.get("src/test/resources/SmallData.txt")));
            BufferedReader reader = new BufferedReader(new FileReader("src/test/resources/LargeData.txt"));
            StringBuilder sb = new StringBuilder();
            String line = reader.readLine();
            while (line != null) {
                sb.append(line).append("\n");
                lineCount++;
                line = reader.readLine();
            }

            long start = Calendar.getInstance().getTimeInMillis();

            GllResult gresult = gllParser.parse(sb.toString());

            long duration = Calendar.getInstance().getTimeInMillis() - start;
            double rate = (1.0 * lineCount) / duration;

            System.err.printf("%s: %dms (%5.2f lps)%n", gresult.success, duration, rate * 1000);

        } catch (IOException ex) {
            Assertions.fail();
        }

    }
}
