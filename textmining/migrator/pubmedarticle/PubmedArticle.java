package grakn.biograkn.textmining.migrator.pubmedarticle;



import grakn.client.GraknClient;
import grakn.client.answer.ConceptMap;
import graql.lang.Graql;
import graql.lang.query.GraqlInsert;
import static graql.lang.Graql.var;

import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.List;

@SuppressWarnings("Duplicates")
public class PubmedArticle {

    public static void migrate(GraknClient.Session session) {

        File dir = new File("textmining/dataset/pubmed");

        File[] articles = dir.listFiles();

        for (File article : articles) {
            try {
                // First, create a new XMLInputFactory
                XMLInputFactory inputFactory = XMLInputFactory.newInstance();

                // Setup a new eventReader
                InputStream in = new FileInputStream(article);
                XMLEventReader eventReader = inputFactory.createXMLEventReader(in);

                String pmid = "";
                String title = "";
                String annotatedAbstract = "";

                // read the XML document
                while (eventReader.hasNext()) {

                    XMLEvent event = eventReader.nextEvent();

                    if (event.isStartElement()) {
                        StartElement startElement = event.asStartElement();

                        if (startElement.getName().getLocalPart().equals("PMID")) {
                            event = eventReader.nextEvent();
                            pmid = event.asCharacters().getData();
                        }

                        if (startElement.getName().getLocalPart().equals("ArticleTitle")) {
                            event = eventReader.nextEvent();
                            title = event.asCharacters().getData();
                        }

                        if (startElement.getName().getLocalPart().equals("AbstractText")) {
                            event = eventReader.nextEvent();
                            annotatedAbstract = event.asCharacters().getData();
                        }
                    }
                }
                GraknClient.Transaction writeTransaction = session.transaction().write();

                GraqlInsert graqlInsert = Graql.insert(var("p").isa("pubmed-article")
                        .has("pmid", pmid)
                        .has("title", title)
                        .has("annotated-abstract", annotatedAbstract));

                List<ConceptMap> insertedId = writeTransaction.execute(graqlInsert);

                System.out.println("Inserted a pubmed article at file: " + article);

                writeTransaction.commit();

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        System.out.println("-----pubmed articles have been loaded-----");
    }
}
