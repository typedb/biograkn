package grakn.biograkn.precisionmedicine.migrator.drugdisease;

import grakn.biograkn.utils.Utils;
import grakn.client.GraknClient;
import graql.lang.Graql;
import graql.lang.query.GraqlInsert;
import grakn.core.concept.answer.ConceptMap;
import static graql.lang.Graql.var;

import grakn.biograkn.precisionmedicine.migrator.disease.Disease;
import grakn.biograkn.precisionmedicine.migrator.drug.Drug;
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

@SuppressWarnings("Duplicates")
public class DrugDiseaseAssociation {
    public static void migrate(GraknClient.Session session, String dataset) {
        try {
            System.out.print("\tMigrating Drug Disease Associations");

            BufferedReader reader = Files.newBufferedReader(Paths.get(dataset + "/ctdbase/CTD_chemicals_diseases.csv"));
            CSVParser csvParser = new CSVParser(reader, CSVFormat.DEFAULT);

            List<GraqlInsert> insertQueries = new ArrayList<>();

            GraknClient.Transaction writeTransaction = session.transaction().write();

            int counter = 0;

            for (CSVRecord csvRecord : csvParser) {

                // skip header
                if (csvRecord.getRecordNumber() < 30) {
                    continue;
                }

                if (counter % 300 == 0) {
                    writeTransaction.commit();
                    writeTransaction = session.transaction().write();
                }

                String drugMeshId = csvRecord.get(1);
                String diseaseMeshId = csvRecord.get(4).substring(5);

                GraqlInsert graqlInsert = Graql.match(
                        var("dr").isa("drug").has("mesh-id", drugMeshId),
                        var("di").isa("disease").has("mesh-id", diseaseMeshId))
                        .insert(var("dda").isa("drug-disease-association").rel("associated-drug", "dr").rel("associated-disease", "di"));

                insertQueries.add(graqlInsert);

                if (insertQueries.size() % 100000 == 0) {
                    Utils.executeQueriesConcurrently(session, insertQueries);
                    insertQueries = new ArrayList<>();
                }
            }

            Utils.executeQueriesConcurrently(session, insertQueries);
            System.out.println(" - [DONE]");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
