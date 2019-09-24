package grakn.biograkn.precisionmedicine.migrator.variantdisease;

import grakn.biograkn.utils.Utils;
import grakn.client.GraknClient;import graql.lang.Graql;
import graql.lang.query.GraqlInsert;
import grakn.core.concept.answer.ConceptMap;

import static graql.lang.Graql.var;

import grakn.biograkn.precisionmedicine.migrator.disease.Disease;
import grakn.biograkn.precisionmedicine.migrator.variant.Variant;
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
public class VariantDiseaseAssociation {
    public static void migrate(GraknClient.Session session, String dataset) {
        try {
            System.out.print("\tMigrating Variant Disease Association");

            BufferedReader reader = Files.newBufferedReader(Paths.get(dataset + "/disgenet/curated_variant_disease_associations.csv"));
            CSVParser csvParser = new CSVParser(reader, CSVFormat.DEFAULT);

            int counter = 0;
            GraknClient.Transaction tx = session.transaction().write();
            for (CSVRecord csvRecord: csvParser) {

                // skip header
                if (csvRecord.getRecordNumber() == 1) {
                    continue;
                }

                String snpId = csvRecord.get(0);
                String diseaseId = csvRecord.get(5);
                double score = Double.parseDouble(csvRecord.get(10));

                GraqlInsert graqlInsert = Graql.match(
                        var("v").isa("variant").has("snp-id", snpId),
                        var("d").isa("disease").has("disease-id", diseaseId))
                        .insert(var("vda").isa("variant-disease-association").rel("associated-variant", "v").rel("associated-disease", "d").has("score", score));

                // if (insertedIds.isEmpty()) {
                //     List<Class> prereqs = Arrays.asList(Variant.class, Disease.class);
                //     throw new IllegalStateException("Nothing was inserted for: " + GraqlInsert.toString() +
                //             "\nA prerequisite dataset may have not been loaded. This dataset requires: " + prereqs.toString());
                // }

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
