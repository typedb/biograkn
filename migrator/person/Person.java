package grakn.biograkn.migrator.person;

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

public class Person {
    public static void migrate(Grakn.Session session) {
        try {
            BufferedReader reader = Files.newBufferedReader(Paths.get("dataset/disgenet/persons.csv"));
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
                List<ConceptMap> insertedIds = insertQuery.withTx(writeTransaction).execute();
                System.out.println("Inserted a person with ID: " + insertedIds.get(0).get("p").id());
                writeTransaction.commit();
            }

            System.out.println("-----persons have been migrated-----");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
