import net.sf.saxon.s9api.DocumentBuilder;
import net.sf.saxon.s9api.Processor;
import net.sf.saxon.s9api.QName;
import net.sf.saxon.s9api.SaxonApiException;
import net.sf.saxon.s9api.Serializer;
import net.sf.saxon.s9api.XdmNode;
import nu.validator.htmlparser.common.XmlViolationPolicy;
import nu.validator.htmlparser.dom.HtmlDocumentBuilder;
import javax.xml.transform.dom.DOMSource;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

class Html5Parser {
  public static void parse(String sourcefn, String outputfn) {
    URL url = new URL(sourcefn);
    URLConnection conn = url.openConnection();
    HtmlDocumentBuilder htmlBuilder = new HtmlDocumentBuilder(XmlViolationPolicy.ALTER_INFOSET);
    Document html = htmlBuilder.parse(conn.getInputStream());

    Processor processor = new Processor(false);
    DocumentBuilder builder = processor.newDocumentBuilder();
    XdmNode doc = builder.build(new DOMSource(html));

    PrintStream output = new PrintStream(new File(outputfn))

    Serializer serializer = processor.newSerializer(output);
    serializer.setOutputProperty(new QName("", "method"), "xhtml");
    serializer.setOutputProperty(new QName("", "omit-xml-declaration"), "yes");
    serializer.setOutputProperty(new QName("", "indent"), "no");
    serializer.serializeXdmValue(doc);
  }
}
