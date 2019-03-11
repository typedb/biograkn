package grakn.biograkn.migrator.disease;

import grakn.core.client.GraknClient;
import graql.lang.Graql;
import graql.lang.query.GraqlInsert;
import graql.lang.query.GraqlGet;
import grakn.core.concept.answer.ConceptMap;

import static graql.lang.Graql.var;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

@SuppressWarnings("Duplicates")
public class Disease {

    public static void migrate(GraknClient.Session session) {
        migrateFromClinvar(session);
        migrateFromCtdbase(session);
        System.out.println("-----diseases have been migrated-----");
    }

    private static void migrateFromClinvar(GraknClient.Session session) {
        try {
            BufferedReader reader = Files.newBufferedReader(Paths.get("dataset/clinvar/disease_names.csv"));
            CSVParser csvParser = new CSVParser(reader, CSVFormat.DEFAULT);

            for (CSVRecord csvRecord: csvParser) {

                // skip header
                if (csvRecord.getRecordNumber() == 1) {
                    continue;
                }

                String name = csvRecord.get(0);
                String sourceName = csvRecord.get(1);
                String diseaseId = csvRecord.get(2);
                String sourceId = csvRecord.get(3);
                String category = csvRecord.get(6);

                GraqlInsert GraqlInsert = Graql.insert(var("d").isa("disease")
                        .has("name", name)
                        .has("source-name", sourceName)
                        .has("disease-id", diseaseId)
                        .has("source-id", sourceId)
                        .has("category", category));

                GraknClient.Transaction writeTransaction = session.transaction().write();
                List<ConceptMap> insertedIds = writeTransaction.execute(GraqlInsert);
                System.out.println("Inserted a disease at record number: " + csvRecord.getRecordNumber() + " of clinvar");
                writeTransaction.commit();
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void migrateFromCtdbase(GraknClient.Session session) {
        try {
            BufferedReader reader = Files.newBufferedReader(Paths.get("dataset/ctdbase/CTD_diseases.csv"));
            CSVParser csvParser = new CSVParser(reader, CSVFormat.DEFAULT);

            for (CSVRecord csvRecord: csvParser) {

                // skip header
                if (csvRecord.getRecordNumber() < 30) {
                    continue;
                }

                String name = csvRecord.get(0);
                String meshId = csvRecord.get(1).substring(5);

                GraknClient.Transaction readTransaction = session.transaction().read();

                GraqlGet graqlGet = Graql.match(
                        var("d").isa("disease").has("name", name)).get();

                List<ConceptMap> getIds = readTransaction.execute(graqlGet);

                readTransaction.close();

                if (getIds.size() == 0) {
                    GraqlInsert GraqlInsert = Graql.insert(var("d").isa("disease")
                            .has("name", name)
                            .has("mesh-id", meshId));

                    GraknClient.Transaction writeTransaction = session.transaction().write();
                    List<ConceptMap> insertedIds = writeTransaction.execute(GraqlInsert);

                    System.out.println("Inserted a disease at record number: " + csvRecord.getRecordNumber() + " of ctdbase");
                    writeTransaction.commit();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
