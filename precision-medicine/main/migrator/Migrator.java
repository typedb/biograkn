package grakn.template.java.migrator;

import ai.grakn.GraknTxType;
import ai.grakn.client.Grakn;
import ai.grakn.graql.GetQuery;
import ai.grakn.graql.Graql;
import ai.grakn.graql.InsertQuery;
import ai.grakn.graql.answer.ConceptMap;

import java.util.ArrayList;
import java.util.List;
import static ai.grakn.graql.Graql.var;
import java.io.*;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

public class Migrator {

    public void migratePatients(Grakn.Session session) {
        try {
            // First, create a new XMLInputFactory
            XMLInputFactory inputFactory = XMLInputFactory.newInstance();

            // Setup a new eventReader
            InputStream in = new FileInputStream("/Users/syedirtazaraza/Desktop/datasets/topics2018.xml");
            XMLEventReader eventReader = inputFactory.createXMLEventReader(in);

            double patient_id = 1;
            double age;
            String gender;

            // read the XML document
            while (eventReader.hasNext()) {
                Grakn.Transaction writeTransaction = session.transaction(GraknTxType.WRITE);

                XMLEvent event = eventReader.nextEvent();

                if (event.isStartElement()) {
                    StartElement startElement = event.asStartElement();

                    if (startElement.getName().getLocalPart().equals("demographic")) {
                        event = eventReader.nextEvent();

                        String demographic = event.asCharacters().getData();
                        age = Double.parseDouble(demographic.substring(0, demographic.indexOf("-")));
                        gender = demographic.substring(demographic.indexOf(" ") + 1);

                        InsertQuery insertQuery = Graql.insert(var("p").isa("patient")
                                .has("patient_id", patient_id)
                                .has("age", age)
                                .has("gender", gender));
                        List<ConceptMap> insertedId = insertQuery.withTx(writeTransaction).execute();
                        System.out.println("Inserted a patient with ID: " + insertedId.get(0).get("p").id());

                        patient_id++;
                    }
                }
                writeTransaction.commit();
            }

            System.out.println("-----patients have been loaded-----");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void migrateDiseases(Grakn.Session session) {
        try {
            BufferedReader reader = Files.newBufferedReader(Paths.get("/Users/syedirtazaraza/Desktop/datasets/diseases.csv"));
            CSVParser csvParser = new CSVParser(reader, CSVFormat.DEFAULT);

            for (CSVRecord csvRecord: csvParser) {
                Grakn.Transaction writeTransaction = session.transaction(GraknTxType.WRITE);

                if (csvRecord.getRecordNumber() == 1) {
                    continue;
                }

                String disease_name = csvRecord.get(0);
                String source = csvRecord.get(1);
                String disease_id = csvRecord.get(2);

                InsertQuery insertQuery = Graql.insert(var("d").isa("disease")
                        .has("disease_name", disease_name)
                        .has("source", source)
                        .has("disease_id", disease_id));

                List<ConceptMap> insertedId = insertQuery.withTx(writeTransaction).execute();
                System.out.println("Inserted a disease with ID: " + insertedId.get(0).get("d").id());
                writeTransaction.commit();
            }

            System.out.println("-----diseases have been loaded-----");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void migrateGenes(Grakn.Session session) {
        try {
            BufferedReader reader = Files.newBufferedReader(Paths.get("/Users/syedirtazaraza/Desktop/datasets/genes.csv"));
            CSVParser csvParser = new CSVParser(reader, CSVFormat.DEFAULT);

            for (CSVRecord csvRecord: csvParser) {
                Grakn.Transaction writeTransaction = session.transaction(GraknTxType.WRITE);

                if (csvRecord.getRecordNumber() == 1) {
                    continue;
                }

                String gene_id = csvRecord.get(0);
                String gene_symbol = csvRecord.get(1);

                InsertQuery insertQuery = Graql.insert(var("g").isa("gene")
                        .has("gene_id", gene_id)
                        .has("gene_symbol", gene_symbol));

                List<ConceptMap> insertedId = insertQuery.withTx(writeTransaction).execute();
                System.out.println("Inserted a gene with ID: " + insertedId.get(0).get("g").id());
                writeTransaction.commit();
            }

            System.out.println("-----genes have been loaded-----");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void migrateGeneDiseaseAssociations(Grakn.Session session) {
        try {

            BufferedReader reader = Files.newBufferedReader(Paths.get("/Users/syedirtazaraza/Desktop/datasets/gene_disease_associations.csv"));
            CSVParser csvParser = new CSVParser(reader, CSVFormat.DEFAULT);

            for (CSVRecord csvRecord: csvParser) {
                Grakn.Transaction writeTransaction = session.transaction(GraknTxType.WRITE);


                if (csvRecord.getRecordNumber() == 1) {
                    continue;
                }

                String gene_id = csvRecord.get(0);
                String disease_id = csvRecord.get(2);
                Double score = Double.parseDouble(csvRecord.get(4));
                String no_of_pmids = csvRecord.get(5);
                String no_of_snps = csvRecord.get(6);
                String source = csvRecord.get(7);

                InsertQuery insertQuery = Graql.match(
                        var("g").isa("gene").has("gene_id", gene_id),
                        var("d").isa("disease").has("disease_id", disease_id))
                        .insert(var("gda").isa("gene_disease_association")
                                .rel("associated_gene", "g")
                                .rel("associated_disease", "d")
                                .has("score", score)
                                .has("no_of_pmids", no_of_pmids)
                                .has("no_of_snps", no_of_snps)
                                .has("source", source));

                List<ConceptMap> insertedId = insertQuery.withTx(writeTransaction).execute();
                System.out.println("Inserted a gene_disease_association at: " + csvRecord.getRecordNumber());
                writeTransaction.commit();
            }

            System.out.println("-----gene disease associations have been loaded-----");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void migrateVariants(Grakn.Session session) {
        try {
            BufferedReader reader = Files.newBufferedReader(Paths.get("/Users/syedirtazaraza/Desktop/datasets/variants.csv"));
            CSVParser csvParser = new CSVParser(reader, CSVFormat.DEFAULT);

            for (CSVRecord csvRecord: csvParser) {
                Grakn.Transaction writeTransaction = session.transaction(GraknTxType.WRITE);

                if (csvRecord.getRecordNumber() == 1) {
                    continue;
                }

                String snp_id = csvRecord.get(0);

                InsertQuery insertQuery = Graql.insert(var("v").isa("variant")
                        .has("snp_id", snp_id));

                List<ConceptMap> insertedId = insertQuery.withTx(writeTransaction).execute();
                System.out.println("Inserted a variant with ID: " + insertedId.get(0).get("v").id());
                writeTransaction.commit();
            }

            System.out.println("-----variants have been loaded-----");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void migrateVariantDiseaseAssociations(Grakn.Session session) {
        try {

            BufferedReader reader = Files.newBufferedReader(Paths.get("/Users/syedirtazaraza/Desktop/datasets/variant_disease_associations.csv"));
            CSVParser csvParser = new CSVParser(reader, CSVFormat.DEFAULT);

            for (CSVRecord csvRecord: csvParser) {
                Grakn.Transaction writeTransaction = session.transaction(GraknTxType.WRITE);

                if (csvRecord.getRecordNumber() == 1) {
                    continue;
                }

                String snp_id = csvRecord.get(0);
                String disease_id = csvRecord.get(2);
                Double score = Double.parseDouble(csvRecord.get(3));
                String no_of_pmids = csvRecord.get(4);
                String source = csvRecord.get(5);

                InsertQuery insertQuery = Graql.match(
                        var("v").isa("variant").has("snp_id", snp_id),
                        var("d").isa("disease").has("disease_id", disease_id))
                        .insert(var("vda").isa("variant_disease_association")
                                .rel("associated_variant", "v")
                                .rel("associated_disease", "d")
                                .has("score", score)
                                .has("no_of_pmids", no_of_pmids)
                                .has("source", source));

                List<ConceptMap> insertedId = insertQuery.withTx(writeTransaction).execute();
                System.out.println("Inserted a variant_disease_association at: " + csvRecord.getRecordNumber());
                writeTransaction.commit();
            }

            System.out.println("-----variant disease associations have been loaded-----");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void migrateClinicalTrials(Grakn.Session session) {

        File dir = new File("/Users/syedirtazaraza/Desktop/datasets/clinical_trials");
        File[] directoryListing = dir.listFiles();

//        GetQuery query = Graql.match(var("d").isa("disease").has("disease_name", var("n"))).get("n");
//
//        Grakn.Transaction readTransaction = session.transaction(GraknTxType.READ);
//
//        List<ConceptMap> answers = query.withTx(readTransaction).execute();
//
//        String diseaseNames = "";
//
//        for (ConceptMap conceptMap : answers) {
//            diseaseNames += (String) conceptMap.get("n").asAttribute().value() + " ";
//        }

        for (File subDir : directoryListing) {
            File[] clinicalTrials = subDir.listFiles();
            for (File clinicalTrial : clinicalTrials) {


                try {
                    // First, create a new XMLInputFactory
                    XMLInputFactory inputFactory = XMLInputFactory.newInstance();

                    // Setup a new eventReader
                    InputStream in = new FileInputStream(clinicalTrial);
                    XMLEventReader eventReader = inputFactory.createXMLEventReader(in);

                    String url = "";
                    String title = "";
                    double min_age = -2;
                    double max_age = -2;
                    String gender = "";

                    // read the XML document
                    while (eventReader.hasNext()) {

                        XMLEvent event = eventReader.nextEvent();

                        if (event.isStartElement()) {
                            StartElement startElement = event.asStartElement();

                            if (startElement.getName().getLocalPart().equals("url")) {
                                event = eventReader.nextEvent();
                                if (event.asCharacters().getData().contains("clinicaltrials.gov")) {
                                    url = event.asCharacters().getData();
                                }
                            }

                            if (startElement.getName().getLocalPart().equals("brief_title")) {
                                event = eventReader.nextEvent();
                                title = event.asCharacters().getData();
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

                    if(!url.equals("") && !title.equals("") && min_age != -2 & max_age != -2 && !gender.equals("")) {
                        Grakn.Transaction writeTransaction = session.transaction(GraknTxType.WRITE);

                        InsertQuery insertQuery = Graql.insert(var("ct").isa("clinical_trial")
                                .has("url", url)
                                .has("title", title)
                                .has("min_age", min_age)
                                .has("max_age", max_age)
                                .has("gender", gender));

                        List<ConceptMap> insertedId = insertQuery.withTx(writeTransaction).execute();
                        System.out.println("Inserted a clinical trial with ID: " + insertedId.get(0).get("ct").id());

                        writeTransaction.commit();
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        System.out.println("-----clinical trials have been loaded-----");
    }

//    private boolean compareConditions(String diseaseNames, String condition) {
//
//        String[] words = condition.split("\\s+");
//
//
//        System.out.println("asdsd");
//
//        return true;
//    }


//    public void migrateGene2Accession(Grakn.Transaction writeTransaction) {
//
//        try {
//
//            BufferedReader reader = Files.newBufferedReader(Paths.get("/Users/syedirtazaraza/Desktop/Datasets/gene/gene2accession.csv"));
//            CSVParser csvParser = new CSVParser(reader, CSVFormat.DEFAULT);
//
//            for (CSVRecord csvRecord: csvParser) {
//
//                if (csvRecord.getRecordNumber() == 1) {
//                    continue;
//                }
//
//                Double tax_id = Double.parseDouble(csvRecord.get(0));
//                Double gene_id = Double.parseDouble(csvRecord.get(1));
//                String status = csvRecord.get(2);
//                String protein_accession_version = csvRecord.get(3);
//                String protein_gi = csvRecord.get(4);
//                String genomic_nucleotide_accession_version = csvRecord.get(5);
//                String genomic_nucleotide_gi = csvRecord.get(6);
//                String start_position = csvRecord.get(7);
//                String end_position = csvRecord.get(8);
//                String orientation = csvRecord.get(9);
//                String symbol = csvRecord.get(10);
//
//
//                InsertQuery insertQuery = Graql.insert(var("g").isa("gene")
//                        .has("tax_id", tax_id)
//                        .has("gene_id", gene_id)
//                        .has("status", status)
//                        .has("protein_accession_version", protein_accession_version)
//                        .has("protein_gi", protein_gi)
//                        .has("genomic_nucleotide_accession_version", genomic_nucleotide_accession_version)
//                        .has("genomic_nucleotide_gi", genomic_nucleotide_gi)
//                        .has("start_position", start_position)
//                        .has("end_position", end_position)
//                        .has("orientation", orientation)
//                        .has("symbol", symbol));
//
//                List<ConceptMap> insertedId = insertQuery.withTx(writeTransaction).execute();
//                System.out.println("Inserted a gene with ID: " + insertedId.get(0).get("g").id());
//            }
//
//            writeTransaction.commit();
//
//            System.out.println("gene2accession dataset has been loaded");
//
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//    }
}