package grakn.biograkn.precisionmedicine.migrator.variantdisease;

import grakn.core.client.GraknClient;import graql.lang.Graql;
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
import java.util.Arrays;
import java.util.List;


@SuppressWarnings("Duplicates")
public class VariantDiseaseAssociation {
    public static void migrate(GraknClient.Session session) {
        try {
            BufferedReader reader = Files.newBufferedReader(Paths.get("precisionmedicine/dataset/disgenet/curated_variant_disease_associations.csv"));
            CSVParser csvParser = new CSVParser(reader, CSVFormat.DEFAULT);

            for (CSVRecord csvRecord: csvParser) {

                // skip header
                if (csvRecord.getRecordNumber() == 1) {
                    continue;
                }

                String snpId = csvRecord.get(0);
                String diseaseId = csvRecord.get(5);
                double score = Double.parseDouble(csvRecord.get(10));

                GraqlInsert GraqlInsert = Graql.match(
                        var("v").isa("variant").has("snp-id", snpId),
                        var("d").isa("disease").has("disease-id", diseaseId))
                        .insert(var("vda").isa("variant-disease-association").rel("associated-variant", "v").rel("associated-disease", "d").has("score", score));

                GraknClient.Transaction writeTransaction = session.transaction().write();
                List<ConceptMap> insertedIds = writeTransaction.execute(GraqlInsert);

                // if (insertedIds.isEmpty()) {
                //     List<Class> prereqs = Arrays.asList(Variant.class, Disease.class);
                //     throw new IllegalStateException("Nothing was inserted for: " + GraqlInsert.toString() +
                //             "\nA prerequisite dataset may have not been loaded. This dataset requires: " + prereqs.toString());
                // }

                System.out.println("Inserted a variant disease association at record number: " + csvRecord.getRecordNumber());
                writeTransaction.commit();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
