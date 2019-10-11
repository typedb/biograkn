package grakn.biograkn.precisionmedicine.migrator.drugdisease;

import grakn.biograkn.utils.Utils;
import grakn.client.GraknClient;
import graql.lang.Graql;
import graql.lang.query.GraqlInsert;
import grakn.client.answer.ConceptMap;
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

            int counter = 0;
            GraknClient.Transaction tx = session.transaction().write();
            for (CSVRecord csvRecord : csvParser) {

                // skip header
                if (csvRecord.getRecordNumber() < 30) {
                    continue;
                }

                String drugMeshId = csvRecord.get(1);
                String diseaseMeshId = csvRecord.get(4).substring(5);

                GraqlInsert graqlInsert = Graql.match(
                        var("dr").isa("drug").has("mesh-id", drugMeshId),
                        var("di").isa("disease").has("mesh-id", diseaseMeshId))
                        .insert(var("dda").isa("drug-disease-association").rel("associated-drug", "dr").rel("associated-disease", "di"));

                tx.execute(graqlInsert);
                System.out.print(".");
                if (counter % 300 == 0) {
                    tx.commit();
                    System.out.println("committed!");
                    tx = session.transaction().write();
                }
                counter++;
            }
            tx.commit();
            System.out.println(" - [DONE]");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
