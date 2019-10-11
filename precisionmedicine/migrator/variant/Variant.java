package grakn.biograkn.precisionmedicine.migrator.variant;

import grakn.biograkn.utils.Utils;
import grakn.client.GraknClient;import graql.lang.Graql;
import graql.lang.query.GraqlInsert;
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
public class Variant {
    public static void migrate(GraknClient.Session session, String dataset) {
        System.out.print("\tMigrating Variants");

        migrateFromPharmgkb(session, dataset + "/pharmgkb/variants.csv");

        System.out.println(" - [DONE]");
    }

    private static void migrateFromPharmgkb(GraknClient.Session session, String path) {
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

                String pharmgkbId = csvRecord.get(0);
                String snpId = csvRecord.get(1);
                String geneSymbols = csvRecord.get(4);
                String location = csvRecord.get(5);


                GraqlInsert graqlInsert = Graql.insert(var("v").isa("variant")
                        .has("pharmgkb-id", pharmgkbId)
                        .has("snp-id", snpId)
                        .has("gene-symbols", geneSymbols)
                        .has("location", location));

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
}
