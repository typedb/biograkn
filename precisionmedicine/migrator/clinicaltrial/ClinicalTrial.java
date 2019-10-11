package grakn.biograkn.precisionmedicine.migrator.clinicaltrial;


import grakn.biograkn.utils.Utils;
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
import java.util.ArrayList;

@SuppressWarnings("Duplicates")
public class ClinicalTrial {

    public static void migrate(GraknClient.Session session, String dataset) {
        System.out.print("\tMigrating Clinical Trials");

        File dir = new File(dataset + "/clinicaltrials/AllPublicXML");
        File[] directoryListing = dir.listFiles();

        int counter = 0;
        GraknClient.Transaction tx = session.transaction().write();
        for (File subDir : directoryListing) {

            if (subDir.toString().contains("Store")){
                continue;
            }

            File[] clinicalTrials = subDir.listFiles();

            for (File clinicalTrial : clinicalTrials) {
                try {
                    // First, create a new XMLInputFactory
                    XMLInputFactory inputFactory = XMLInputFactory.newInstance();

                    // Setup a new eventReader
                    InputStream in = new FileInputStream(clinicalTrial);
                    XMLEventReader eventReader = inputFactory.createXMLEventReader(in);

                    String url = "";
                    String briefTitle = "";
                    String officialTitle = "";
                    String status = "";
                    String condition = "";
                    String interventionType = "";
                    String interventionName = "";
                    String nctId = "";
                    double min_age = -2;
                    double max_age = -2;
                    String gender = "";

                    // read the XML document
                    while (eventReader.hasNext()) {

                        XMLEvent event = eventReader.nextEvent();

                        if (event.isStartElement()) {
                            StartElement startElement = event.asStartElement();

                            if (startElement.getName().getLocalPart().equals("nct_id")) {
                                event = eventReader.nextEvent();
                                nctId = event.asCharacters().getData();
                            }

                            if (startElement.getName().getLocalPart().equals("url")) {
                                event = eventReader.nextEvent();
                                url = event.asCharacters().getData();
                            }

                            if (startElement.getName().getLocalPart().equals("brief_title")) {
                                event = eventReader.nextEvent();
                                briefTitle = event.asCharacters().getData();
                            }

                            if (startElement.getName().getLocalPart().equals("official_title")) {
                                event = eventReader.nextEvent();
                                officialTitle = event.asCharacters().getData();
                            }

                            if (startElement.getName().getLocalPart().equals("overall_status")) {
                                event = eventReader.nextEvent();
                                status = event.asCharacters().getData();
                            }

                            if (startElement.getName().getLocalPart().equals("condition")) {
                                event = eventReader.nextEvent();
                                condition = event.asCharacters().getData();
                            }

                            if (startElement.getName().getLocalPart().equals("intervention_type")) {
                                event = eventReader.nextEvent();
                                interventionType = event.asCharacters().getData();
                            }

                            if (startElement.getName().getLocalPart().equals("intervention_name")) {
                                event = eventReader.nextEvent();
                                interventionName = event.asCharacters().getData();
                            }

                            if (startElement.getName().getLocalPart().equals("minimum_age")) {
                                event = eventReader.nextEvent();
                                String minimum_age = event.asCharacters().getData();

                                if (minimum_age.equals("N/A")) {
                                    min_age = -1;
                                } else if (minimum_age.contains("Years")) {
                                    min_age = Double.parseDouble(minimum_age.substring(0, minimum_age.indexOf(" ")));
                                } else if (minimum_age.contains("Months")) {
                                    min_age = Double.parseDouble(minimum_age.substring(0, minimum_age.indexOf(" "))) / 12;
                                } else if (minimum_age.contains("Weeks")) {
                                    min_age = Double.parseDouble(minimum_age.substring(0, minimum_age.indexOf(" "))) / 52;
                                } else {
                                    min_age = Double.parseDouble(minimum_age.substring(0, minimum_age.indexOf(" "))) / 365;
                                }
                            }
                            if (startElement.getName().getLocalPart().equals("maximum_age")) {
                                event = eventReader.nextEvent();
                                String maximum_age = event.asCharacters().getData();

                                if (maximum_age.equals("N/A")) {
                                    max_age = -1;
                                } else if (maximum_age.contains("Years")) {
                                    max_age = Double.parseDouble(maximum_age.substring(0, maximum_age.indexOf(" ")));
                                } else if (maximum_age.contains("Months")) {
                                    max_age = Double.parseDouble(maximum_age.substring(0, maximum_age.indexOf(" "))) / 12;
                                } else if (maximum_age.contains("Weeks")) {
                                    max_age = Double.parseDouble(maximum_age.substring(0, maximum_age.indexOf(" "))) / 52;
                                } else {
                                    max_age = Double.parseDouble(maximum_age.substring(0, maximum_age.indexOf(" "))) / 365;
                                }
                            }

                            if (startElement.getName().getLocalPart().equals("gender")) {
                                event = eventReader.nextEvent();
                                gender = event.asCharacters().getData();
                            }
                        }
                    }

                    if(!url.equals("") && !briefTitle.equals("") && !nctId.equals("") && !officialTitle.equals("") && !interventionName.equals("") && !interventionType.equals("") && !condition.equals("") && !status.equals("") && min_age != -2 & max_age != -2 && !gender.equals("")) {

                        GraqlInsert graqlInsert = Graql.insert(var("ct").isa("clinical-trial")
                                .has("url", url)
                                .has("brief-title", briefTitle)
                                .has("official-title", officialTitle)
                                .has("min-age", min_age)
                                .has("max-age", max_age)
                                .has("intervention-type", interventionType)
                                .has("intervention-name", interventionName)
                                .has("status", status)
                                .has("condition", condition)
                                .has("nct-id", nctId)
                                .has("gender", gender));

                        tx.execute(graqlInsert);
                        System.out.print(".");
                        if (counter % 50 == 0) {
                            tx.commit();
                            System.out.println("committed!");
                            tx = session.transaction().write();
                        }
                        counter++;
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                    tx = session.transaction().write();
                }
            }
        }
        tx.commit();
        System.out.println("- [DONE]");
    }
}
