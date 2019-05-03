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

    public static void migrate(GraknClient.Session session) {
        System.out.print("\tMigrating Drugs");

        migrateFromDrugsAtFda(session);
        migrateFromPharmgkb(session);
        migrateFromCtdbase(session);

        System.out.println(" - [DONE]");
    }

    private static void migrateFromDrugsAtFda(GraknClient.Session session) {
        try {
            BufferedReader reader = Files.newBufferedReader(Paths.get("precisionmedicine/dataset/drugsatfda/Products.csv"));
            CSVParser csvParser = new CSVParser(reader, CSVFormat.DEFAULT);

            List<GraqlInsert> insertQueries = new ArrayList<>();

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

                insertQueries.add(graqlInsert);
            }

            Utils.executeQueriesConcurrently(session, insertQueries);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void migrateFromPharmgkb(GraknClient.Session session) {
        try {
            BufferedReader reader = Files.newBufferedReader(Paths.get("precisionmedicine/dataset/pharmgkb/drugs.csv"));
            CSVParser csvParser = new CSVParser(reader, CSVFormat.DEFAULT);

            List<GraqlInsert> insertQueries = new ArrayList<>();

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

                    insertQueries.add(graqlInsert);
                }
            }

            Utils.executeQueriesConcurrently(session, insertQueries);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void migrateFromCtdbase(GraknClient.Session session) {
        try {
            BufferedReader reader = Files.newBufferedReader(Paths.get("precisionmedicine/dataset/ctdbase/CTD_chemicals.csv"));
            CSVParser csvParser = new CSVParser(reader, CSVFormat.DEFAULT);

            List<GraqlInsert> insertQueries = new ArrayList<>();

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

                    insertQueries.add(graqlInsert);
                }
            }

            Utils.executeQueriesConcurrently(session, insertQueries);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
