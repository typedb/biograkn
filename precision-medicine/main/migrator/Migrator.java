package grakn.template.java.migrator;

import ai.grakn.GraknTxType;
import ai.grakn.client.Grakn;
import ai.grakn.concept.ConceptId;
import ai.grakn.graql.*;
import ai.grakn.graql.answer.ConceptMap;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static ai.grakn.graql.Graql.insert;
import static ai.grakn.graql.Graql.var;
import java.io.*;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

public class Migrator {

    public void migratePersons(Grakn.Session session) {
        try {
            BufferedReader reader = Files.newBufferedReader(Paths.get("precision-medicine/datasets/persons.csv"));
            CSVParser csvParser = new CSVParser(reader, CSVFormat.DEFAULT);

            for (CSVRecord csvRecord: csvParser) {

                // skip header
                if (csvRecord.getRecordNumber() == 1) {
                    continue;
                }

                double personId = Double.parseDouble(csvRecord.get(0));
                double age = Double.parseDouble(csvRecord.get(1));
                String gender = csvRecord.get(2);


                InsertQuery insertQuery = Graql.insert(var("p").isa("person")
                        .has("person-id", personId)
                        .has("age", age)
                        .has("gender", gender));

                Grakn.Transaction writeTransaction = session.transaction(GraknTxType.WRITE);
                List<ConceptMap> insertedId = insertQuery.withTx(writeTransaction).execute();
                System.out.println("Inserted a person with ID: " + insertedId.get(0).get("p").id());
                writeTransaction.commit();
            }

            System.out.println("-----persons have been migrated-----");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void migrateDiseases(Grakn.Session session) {
        try {
            BufferedReader reader = Files.newBufferedReader(Paths.get("precision-medicine/datasets/diseases.csv"));
            CSVParser csvParser = new CSVParser(reader, CSVFormat.DEFAULT);

            for (CSVRecord csvRecord: csvParser) {

                // skip header
                if (csvRecord.getRecordNumber() == 1) {
                    continue;
                }

                String diseaseName = csvRecord.get(0);
                String source = csvRecord.get(1);
                String diseaseId = csvRecord.get(2);


                InsertQuery insertQuery = Graql.insert(var("d").isa("disease")
                        .has("disease-name", diseaseName)
                        .has("source", source)
                        .has("disease-id", diseaseId));

                Grakn.Transaction writeTransaction = session.transaction(GraknTxType.WRITE);
                List<ConceptMap> insertedId = insertQuery.withTx(writeTransaction).execute();
                System.out.println("Inserted a disease with ID: " + insertedId.get(0).get("d").id());
                writeTransaction.commit();
            }

            System.out.println("-----diseases have been migrated-----");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void migrateGenes(Grakn.Session session) {
        try {
            BufferedReader reader = Files.newBufferedReader(Paths.get("precision-medicine/datasets/genes.csv"));
            CSVParser csvParser = new CSVParser(reader, CSVFormat.DEFAULT);

            for (CSVRecord csvRecord: csvParser) {

                // skip header
                if (csvRecord.getRecordNumber() == 1) {
                    continue;
                }

                String geneId = csvRecord.get(0);
                String geneSymbol = csvRecord.get(1);

                InsertQuery insertQuery = Graql.insert(var("g").isa("gene")
                        .has("gene-id", geneId)
                        .has("gene-symbol", geneSymbol));

                Grakn.Transaction writeTransaction = session.transaction(GraknTxType.WRITE);
                List<ConceptMap> insertedId = insertQuery.withTx(writeTransaction).execute();
                System.out.println("Inserted a gene with ID: " + insertedId.get(0).get("g").id());
                writeTransaction.commit();
            }

            System.out.println("-----genes have been migrated-----");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public void migrateVariants(Grakn.Session session) {
        try {
            BufferedReader reader = Files.newBufferedReader(Paths.get("precision-medicine/datasets/variants.csv"));
            CSVParser csvParser = new CSVParser(reader, CSVFormat.DEFAULT);

            for (CSVRecord csvRecord: csvParser) {

                // skip header
                if (csvRecord.getRecordNumber() == 1) {
                    continue;
                }

                String snpId = csvRecord.get(0);
                String alleleSymbol = csvRecord.get(1);

                InsertQuery insertQuery = Graql.insert(var("v").isa("allele")
                        .has("snp-id", snpId)
                        .has("allele-symbol", alleleSymbol));

                Grakn.Transaction writeTransaction = session.transaction(GraknTxType.WRITE);
                List<ConceptMap> insertedId = insertQuery.withTx(writeTransaction).execute();
                System.out.println("Inserted a allele with ID: " + insertedId.get(0).get("v").id());
                writeTransaction.commit();
            }

            System.out.println("-----alleles have been migrated-----");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void migrateClinicalTrials(Grakn.Session session) {
        try {
            BufferedReader reader = Files.newBufferedReader(Paths.get("precision-medicine/datasets/clinical_trials_melanoma.csv"));
            CSVParser csvParser = new CSVParser(reader, CSVFormat.DEFAULT);

            for (CSVRecord csvRecord: csvParser) {

                // skip header
                if (csvRecord.getRecordNumber() == 1) {
                    continue;
                }

                String nctId = csvRecord.get(0);
                String clinicalTrialTitle = csvRecord.get(1);
                String status = csvRecord.get(2);
                boolean results;
                if (csvRecord.get(3).equals("Has Results")) {
                    results = true;
                } else {
                    results = false;
                }

                String interventionType = csvRecord.get(4);
                String participantsGender = csvRecord.get(5);

                String[] ages = csvRecord.get(6).split("(and|to)");

                double minAge;
                double maxAge;

                if (ages[0].contains("up")) {
                    minAge = 0;
                } else if (ages[0].contains("Years")) {
                    minAge = Double.parseDouble(ages[0].replaceAll("\\D+",""));
                } else {
                    minAge = Double.parseDouble(ages[0].replaceAll("\\D+",""))/12;
                }

                if (ages[1].contains("older")) {
                    maxAge = 130;
                } else if (ages[1].contains("Years")) {
                    maxAge = Double.parseDouble(ages[1].replaceAll("\\D+",""));
                } else {
                    maxAge = Double.parseDouble(ages[1].replaceAll("\\D+",""))/12;
                }

                String url = csvRecord.get(7);

                InsertQuery insertQuery = Graql.match(
                        var("d").isa("disease").has("disease-id", "C0025202"))
                        .insert(var("c").isa("clinical-trial")
                        .has("nct-id", nctId)
                        .has("clinical-trial-title", clinicalTrialTitle)
                        .has("status", status)
                        .has("results", results)
                        .has("intervention-type", interventionType)
                        .has("participants-gender", participantsGender)
                        .has("min-age", minAge)
                        .has("max-age", maxAge)
                        .has("url", url)
                        .rel("targeted-disease", "d"));

                Grakn.Transaction writeTransaction = session.transaction(GraknTxType.WRITE);
                List<ConceptMap> insertedId = insertQuery.withTx(writeTransaction).execute();
                System.out.println("Inserted a clinical trial with ID: " + insertedId.get(0).get("c").id());
                writeTransaction.commit();
            }

            System.out.println("-----clinical trials have been migrated-----");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void migrateDiagnoses(Grakn.Session session) {
        try {
            BufferedReader reader = Files.newBufferedReader(Paths.get("precision-medicine/datasets/diagnoses.csv"));
            CSVParser csvParser = new CSVParser(reader, CSVFormat.DEFAULT);

            for (CSVRecord csvRecord: csvParser) {

                // skip header
                if (csvRecord.getRecordNumber() == 1) {
                    continue;
                }

                double personId = Double.parseDouble(csvRecord.get(0));
                String diseaseId = csvRecord.get(1);

                InsertQuery insertQuery = Graql.match(
                        var("p").isa("person").has("person-id", personId),
                        var("d").isa("disease").has("disease-id", diseaseId))
                        .insert(var("dia").isa("diagnosis").rel("diagnosed-patient", "p").rel("diagnosed-disease", "d"));

                Grakn.Transaction writeTransaction = session.transaction(GraknTxType.WRITE);
                List<ConceptMap> insertedIDs = insertQuery.withTx(writeTransaction).execute();

//                if (insertedIDs.isEmpty()) {
//                    List<Class> prereqs = Arrays.asList(Migrator.class, Gene.class, );
//                    throw new IllegalStateException("Nothing was inserted for: " + insertQuery.toString() +
//                            "\nA prerequisite dataset may have not been loaded. This dataset requires: " + prereqs.toString());
//                }

                System.out.println("Inserted a diagnosis with ID: " + insertedIDs.get(0).get("dia").id());
                writeTransaction.commit();
            }

            System.out.println("-----diagnoses have been migrated-----");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void migrateGeneIdentifications(Grakn.Session session) {
        try {
            BufferedReader reader = Files.newBufferedReader(Paths.get("precision-medicine/datasets/gene_identification.csv"));
            CSVParser csvParser = new CSVParser(reader, CSVFormat.DEFAULT);

            for (CSVRecord csvRecord: csvParser) {

                // skip header
                if (csvRecord.getRecordNumber() == 1) {
                    continue;
                }

                double personId = Double.parseDouble(csvRecord.get(0));
                String geneSymbol = csvRecord.get(1);

                InsertQuery insertQuery = Graql.match(
                        var("p").isa("person").has("person-id", personId),
                        var("g").isa("gene").has("gene-symbol", geneSymbol))
                        .insert(var("gi").isa("gene-identification").rel("genome-owner", "p").rel("identified-gene", "g"));

                Grakn.Transaction writeTransaction = session.transaction(GraknTxType.WRITE);
                List<ConceptMap> insertedId = insertQuery.withTx(writeTransaction).execute();
                System.out.println("Inserted a gene identification with ID: " + insertedId.get(0).get("gi").id());
                writeTransaction.commit();
            }

            System.out.println("-----gene identifications have been migrated-----");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void migrateVariantIdentifications(Grakn.Session session) {
        try {
            BufferedReader reader = Files.newBufferedReader(Paths.get("precision-medicine/datasets/variant_identification.csv"));
            CSVParser csvParser = new CSVParser(reader, CSVFormat.DEFAULT);

            for (CSVRecord csvRecord: csvParser) {

                // skip header
                if (csvRecord.getRecordNumber() == 1) {
                    continue;
                }

                double personId = Double.parseDouble(csvRecord.get(0));
                String snpId = csvRecord.get(1);

                InsertQuery insertQuery = Graql.match(
                        var("p").isa("person").has("person-id", personId),
                        var("a").isa("allele").has("snp-id", snpId))
                        .insert(var("vi").isa("allele-identification").rel("genome-owner", "p").rel("identified-allele", "a"));

                Grakn.Transaction writeTransaction = session.transaction(GraknTxType.WRITE);
                List<ConceptMap> insertedId = insertQuery.withTx(writeTransaction).execute();

                System.out.println("Inserted a variant identification with ID: " + insertedId.get(0).get("vi").id());
                writeTransaction.commit();
            }

            System.out.println("-----variant identifications have been migrated-----");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void migrateGeneticVariations(Grakn.Session session) {
        try {
            BufferedReader reader = Files.newBufferedReader(Paths.get("precision-medicine/datasets/geneticVariations.csv"));
            CSVParser csvParser = new CSVParser(reader, CSVFormat.DEFAULT);

            for (CSVRecord csvRecord: csvParser) {

                // skip header
                if (csvRecord.getRecordNumber() == 1) {
                    continue;
                }

                String geneSymbol = csvRecord.get(0);
                String snpId = csvRecord.get(1);

                InsertQuery insertQuery = Graql.match(
                        var("g").isa("gene").has("gene-symbol", geneSymbol),
                        var("a").isa("allele").has("snp-id", snpId))
                        .insert(var("gv").isa("genetic-variation").rel("varied-gene", "g").rel("genetic-variant", "a"));

                Grakn.Transaction writeTransaction = session.transaction(GraknTxType.WRITE);
                List<ConceptMap> insertedId = insertQuery.withTx(writeTransaction).execute();
                System.out.println("Inserted a genetic variation with ID: " + insertedId.get(0).get("gv").id());
                writeTransaction.commit();
            }

            System.out.println("-----genetic variations have been migrated-----");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void migrateDrugs(Grakn.Session session) {
        try {
            BufferedReader reader = Files.newBufferedReader(Paths.get("precision-medicine/datasets/drugs_melanoma.csv"));
            CSVParser csvParser = new CSVParser(reader, CSVFormat.DEFAULT);

            for (CSVRecord csvRecord: csvParser) {

                // skip header
                if (csvRecord.getRecordNumber() == 1) {
                    continue;
                }

                String drugName = csvRecord.get(0);
                String meshId = csvRecord.get(1);
                String drugBankId = csvRecord.get(2);
                String pubChemCid = csvRecord.get(3);

                InsertQuery insertQuery = Graql.insert(var("dr").isa("drug")
                        .has("drug-name", drugName)
                        .has("mesh-id", meshId)
                        .has("drug-bank-id", drugBankId)
                        .has("pub-chem-cid", pubChemCid));

                Grakn.Transaction writeTransaction = session.transaction(GraknTxType.WRITE);
                List<ConceptMap> insertedId = insertQuery.withTx(writeTransaction).execute();
                System.out.println("Inserted a drug with ID: " + insertedId.get(0).get("dr").id());
                writeTransaction.commit();
            }

            System.out.println("-----drugs have been migrated-----");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void migrateDiseaseDrugAssociations(Grakn.Session session) {

        Grakn.Transaction readTransaction = session.transaction(GraknTxType.READ);

        GetQuery getQuery = Graql.match(Graql.var("dr").isa("drug")).get();
        List<ConceptMap> clinicalTrials = getQuery.withTx(readTransaction).execute();

        for (ConceptMap map: clinicalTrials) {
            String conceptId = map.get("dr").id().getValue();

            InsertQuery insertQuery = Graql.match(
                    var("d").isa("disease").has("disease-id", "C0025202"),
                    var("dr").isa("drug").id(ConceptId.of(conceptId)))
                    .insert(var("dda").isa("disease-drug-association").rel("associated-disease", "d").rel("associated-drug", "dr"));

            Grakn.Transaction writeTransaction = session.transaction(GraknTxType.WRITE);
            List<ConceptMap> insertedId = insertQuery.withTx(writeTransaction).execute();
            System.out.println("Inserted a disease drug association with ID: " + insertedId.get(0).get("dda").id());
            writeTransaction.commit();
        }
        System.out.println("-----disease drug associations have been migrated-----");
    }

//    public void migrateTextMinedAnalysis(Grakn.Session session) {
//        try {
//            BufferedReader reader = Files.newBufferedReader(Paths.get("precision-medicine/datasets/gene_disease_pmid_association.csv"));
//            CSVParser csvParser = new CSVParser(reader, CSVFormat.DEFAULT);
//
//            for (CSVRecord csvRecord: csvParser) {
//
//                // skip header
//                if (csvRecord.getRecordNumber() == 1) {
//                    continue;
//                }
//
//                String geneid = csvRecord.get(0);
//                String diseaseId = csvRecord.get(1);
//                String diseaseId = csvRecord.get(1);
//
//
//                InsertQuery insertQuery = Graql.match(
//                        var("g").isa("gene").has("gene-symbol", geneSymbol),
//                        var("a").isa("allele").has("snp-id", snpId))
//                        .insert(var("gv").isa("genetic-variation").rel("varied-gene", "g").rel("genetic-variant", "a"));
//
//                Grakn.Transaction writeTransaction = session.transaction(GraknTxType.WRITE);
//                List<ConceptMap> insertedId = insertQuery.withTx(writeTransaction).execute();
//                System.out.println("Inserted a genetic variation with ID: " + insertedId.get(0).get("gv").id());
//                writeTransaction.commit();
//            }
//
//            System.out.println("-----genetic variations have been migrated-----");
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//    }
}