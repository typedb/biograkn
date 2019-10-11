package grakn.biograkn.precisionmedicine.migrator.disease;

import grakn.biograkn.utils.Utils;
import grakn.client.GraknClient;
import graql.lang.Graql;
import graql.lang.query.GraqlInsert;
import graql.lang.query.GraqlGet;
import grakn.client.answer.ConceptMap;

import static graql.lang.Graql.var;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("Duplicates")
public class Disease {

    public static void migrate(GraknClient.Session session, String dataset) {
        System.out.print("\tMigrating Diseases");

        migrateFromClinvar(session, dataset + "/clinvar/disease_names.csv");
        migrateFromCtdbase(session, dataset + "/ctdbase/CTD_diseases.csv");

        System.out.println(" - [DONE]");
    }

    private static void migrateFromClinvar(GraknClient.Session session, String path) {
        try {
            BufferedReader reader = Files.newBufferedReader(Paths.get(path));
            CSVParser csvParser = new CSVParser(reader, CSVFormat.DEFAULT);
            int counter = 0;
            GraknClient.Transaction tx = session.transaction().write();
            for (CSVRecord csvRecord : csvParser) {

                // skip header
                if (csvRecord.getRecordNumber() == 1) {
                    continue;
                }

                String name = csvRecord.get(0);
                String sourceName = csvRecord.get(1);
                String diseaseId = csvRecord.get(2);
                String sourceId = csvRecord.get(3);
                String category = csvRecord.get(6);

                GraqlInsert graqlInsert = Graql.insert(var("d").isa("disease")
                        .has("name", name)
                        .has("source-name", sourceName)
                        .has("disease-id", diseaseId)
                        .has("source-id", sourceId)
                        .has("category", category));

                tx.execute(graqlInsert);
                System.out.print(".");
                if (counter % 50 == 0) {
                    tx.commit();
                    System.out.println("committed!");
                    tx = session.transaction().write();
                }
                counter++;
            }
            tx.commit();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void migrateFromCtdbase(GraknClient.Session session, String path) {
        try {
            BufferedReader reader = Files.newBufferedReader(Paths.get(path));
            CSVParser csvParser = new CSVParser(reader, CSVFormat.DEFAULT);

            int counter = 0;
            GraknClient.Transaction tx = session.transaction().write();
            for (CSVRecord csvRecord : csvParser) {

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
                    GraqlInsert graqlInsert = Graql.insert(var("d").isa("disease")
                            .has("name", name)
                            .has("mesh-id", meshId));
                    tx.execute(graqlInsert);
                    System.out.print(".");
                    if (counter % 50 == 0) {
                        tx.commit();
                        System.out.println("committed!");
                        tx = session.transaction().write();
                    }
                    counter++;
                }
            }
            tx.commit();
            System.out.println("committed!");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
