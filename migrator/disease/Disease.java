package grakn.biograkn.migrator.disease;

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
public class Disease {

    public static void migrate(Grakn.Session session) {
        try {
            BufferedReader reader = Files.newBufferedReader(Paths.get("dataset/diseases.csv"));
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
}
