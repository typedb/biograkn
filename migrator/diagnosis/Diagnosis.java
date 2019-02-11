package grakn.biograkn.migrator.diagnosis;

import ai.grakn.GraknTxType;
import ai.grakn.client.Grakn;
import ai.grakn.graql.Graql;
import ai.grakn.graql.InsertQuery;
import ai.grakn.graql.answer.ConceptMap;
import grakn.biograkn.migrator.Migrator;
import grakn.biograkn.migrator.disease.Disease;
import grakn.biograkn.migrator.gene.Gene;
import grakn.biograkn.migrator.person.Person;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static ai.grakn.graql.Graql.var;

public class Diagnosis {

    public static void migrate(Grakn.Session session) {
        try {
            BufferedReader reader = Files.newBufferedReader(Paths.get("dataset/disgenet/diagnoses.csv"));
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
                        .insert(var("dia").isa("diagnosis").rel("patient", "p").rel("diagnosed-disease", "d"));

                Grakn.Transaction writeTransaction = session.transaction(GraknTxType.WRITE);
                List<ConceptMap> insertedIds = insertQuery.withTx(writeTransaction).execute();

                if (insertedIds.isEmpty()) {
                    List<Class> prereqs = Arrays.asList(Person.class, Disease.class);
                    throw new IllegalStateException("Nothing was inserted for: " + insertQuery.toString() +
                            "\nA prerequisite dataset may have not been loaded. This dataset requires: " + prereqs.toString());
                }

                System.out.println("Inserted a diagnosis with ID: " + insertedIds.get(0).get("dia").id());
                writeTransaction.commit();
            }

            System.out.println("-----diagnoses have been migrated-----");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
