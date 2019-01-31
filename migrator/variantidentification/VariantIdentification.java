package grakn.biograkn.migrator.variantidentification;

import ai.grakn.GraknTxType;
import ai.grakn.client.Grakn;
import ai.grakn.graql.Graql;
import ai.grakn.graql.InsertQuery;
import ai.grakn.graql.answer.ConceptMap;
import grakn.biograkn.migrator.disease.Disease;
import grakn.biograkn.migrator.person.Person;
import grakn.biograkn.migrator.variant.Variant;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

import static ai.grakn.graql.Graql.var;

public class VariantIdentification {

    public static void migrate(Grakn.Session session) {
        try {
            BufferedReader reader = Files.newBufferedReader(Paths.get("dataset/disgenet/variant_identification.csv"));
            CSVParser csvParser = new CSVParser(reader, CSVFormat.DEFAULT);

            for (CSVRecord csvRecord: csvParser) {

                // skip header
                if (csvRecord.getRecordNumber() == 1) {
                    continue;
                }

                double personId = Double.parseDouble(csvRecord.get(0));
                String snpId = csvRecord.get(1);

                InsertQuery insertQuery = Graql.match(
                        var("p").isa("person").has("person-id", personId),
                        var("a").isa("variant").has("snp-id", snpId))
                        .insert(var("vi").isa("variant-identification").rel("genome-owner", "p").rel("identified-variant", "a"));

                Grakn.Transaction writeTransaction = session.transaction(GraknTxType.WRITE);
                List<ConceptMap> insertedIds = insertQuery.withTx(writeTransaction).execute();

                if (insertedIds.isEmpty()) {
                    List<Class> prereqs = Arrays.asList(Person.class, Variant.class);
                    throw new IllegalStateException("Nothing was inserted for: " + insertQuery.toString() +
                            "\nA prerequisite dataset may have not been loaded. This dataset requires: " + prereqs.toString());
                }

                System.out.println("Inserted a variant identification with ID: " + insertedIds.get(0).get("vi").id());
                writeTransaction.commit();
            }

            System.out.println("-----variant identifications have been migrated-----");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
