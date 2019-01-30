package grakn.biograkn.migrator.clinicaltrial;

import ai.grakn.GraknTxType;
import ai.grakn.client.Grakn;
import ai.grakn.graql.Graql;
import ai.grakn.graql.InsertQuery;
import ai.grakn.graql.answer.ConceptMap;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

import static ai.grakn.graql.Graql.var;

@SuppressWarnings("Duplicates")
public class ClinicalTrial {

    public static void migrate(Grakn.Session session) {
        try {
            BufferedReader reader = Files.newBufferedReader(Paths.get("dataset/clinical_trials_melanoma.csv"));
            CSVParser csvParser = new CSVParser(reader, CSVFormat.DEFAULT);

            for (CSVRecord csvRecord : csvParser) {

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
                    minAge = Double.parseDouble(ages[0].replaceAll("\\D+", ""));
                } else {
                    minAge = Double.parseDouble(ages[0].replaceAll("\\D+", "")) / 12;
                }

                if (ages[1].contains("older")) {
                    maxAge = 130;
                } else if (ages[1].contains("Years")) {
                    maxAge = Double.parseDouble(ages[1].replaceAll("\\D+", ""));
                } else {
                    maxAge = Double.parseDouble(ages[1].replaceAll("\\D+", "")) / 12;
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
}
