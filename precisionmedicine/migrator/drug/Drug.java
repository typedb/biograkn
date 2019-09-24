package grakn.biograkn.precisionmedicine.migrator.drug;

import grakn.biograkn.utils.Utils;
import grakn.client.GraknClient;
import grakn.core.concept.answer.ConceptMap;
import graql.lang.Graql;
import graql.lang.query.GraqlGet;
import graql.lang.query.GraqlInsert;
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
public class Drug {

    public static void migrate(GraknClient.Session session, String dataset) {
        System.out.print("\tMigrating Drugs");

        migrateFromDrugsAtFda(session, dataset + "/drugsatfda/Products.csv");
        migrateFromPharmgkb(session,  dataset + "/pharmgkb/drugs.csv");
        migrateFromCtdbase(session, dataset + "/ctdbase/CTD_chemicals.csv");

        System.out.println(" - [DONE]");
    }

     static void migrateFromDrugsAtFda(GraknClient.Session session, String path) {
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


                String applNo = csvRecord.get(0);
                String productNo = csvRecord.get(1);
                String form = csvRecord.get(2);
                String strength = csvRecord.get(3);
                String name = csvRecord.get(5).replace("\"", "'");
                String activeIngredient = csvRecord.get(6);

                GraqlInsert graqlInsert = Graql.insert(var("dr").isa("drug")
                        .has("appl-no", applNo)
                        .has("product-no", productNo)
                        .has("form", form)
                        .has("strength", strength)
                        .has("name", name)
                        .has("active-ingredient", activeIngredient));
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

     static void migrateFromPharmgkb(GraknClient.Session session, String path) {
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
                String name = csvRecord.get(1);

                GraknClient.Transaction readTransaction = session.transaction().read();

                GraqlGet graqlGet = Graql.match(
                        var("dr").isa("drug").has("name", name)).get();

                List<ConceptMap> getIds = readTransaction.execute(graqlGet);

                readTransaction.close();

                if (getIds.size() == 0) {
                    GraqlInsert graqlInsert = Graql.insert(var("dr").isa("drug")
                            .has("pharmgkb-id", pharmgkbId)
                            .has("name", name));
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
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

     static void migrateFromCtdbase(GraknClient.Session session, String path) {
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
                        var("dr").isa("drug").has("name", name)).get();

                List<ConceptMap> getIds = readTransaction.execute(graqlGet);

                readTransaction.close();

                if (getIds.size() == 0) {
                    GraqlInsert graqlInsert = Graql.insert(var("dr").isa("drug")
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

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
