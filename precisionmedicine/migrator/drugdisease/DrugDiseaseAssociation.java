package grakn.biograkn.precisionmedicine.migrator.drugdisease;

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
import java.util.Arrays;
import java.util.List;

@SuppressWarnings("Duplicates")
public class DrugDiseaseAssociation {
    public static void migrate(GraknClient.Session session) {
        try {
            BufferedReader reader = Files.newBufferedReader(Paths.get("precisionmedicine/dataset/ctdbase/CTD_chemicals_diseases.csv"));
            CSVParser csvParser = new CSVParser(reader, CSVFormat.DEFAULT);

            for (CSVRecord csvRecord : csvParser) {

                // skip header
                if (csvRecord.getRecordNumber() < 30) {
                    continue;
                }

                String drugMeshId = csvRecord.get(1);
                String diseaseMeshId = csvRecord.get(4).substring(5);

                GraqlInsert GraqlInsert = Graql.match(
                        var("dr").isa("drug").has("mesh-id", drugMeshId),
                        var("di").isa("disease").has("mesh-id", diseaseMeshId))
                        .insert(var("dda").isa("drug-disease-association").rel("associated-drug", "dr").rel("associated-disease", "di"));

                GraknClient.Transaction writeTransaction = session.transaction().write();
                List<ConceptMap> insertedIds = writeTransaction.execute(GraqlInsert);

                // if (insertedIds.isEmpty()) {
                //     List<Class> prereqs = Arrays.asList(Drug.class, Disease.class);
                //     throw new IllegalStateException("Nothing was inserted for: " + GraqlInsert.toString() +
                //             "\nA prerequisite dataset may have not been loaded. This dataset requires: " + prereqs.toString());
                // }

                System.out.println("Inserted a drug disease association at record number: " + csvRecord.getRecordNumber());
                writeTransaction.commit();
            }

            System.out.println("-----drug disease associations have been migrated-----");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
