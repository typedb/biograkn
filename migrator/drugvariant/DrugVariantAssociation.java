package grakn.biograkn.migrator.drugvariant;

import ai.grakn.GraknTxType;
import ai.grakn.client.Grakn;
import ai.grakn.graql.Graql;
import ai.grakn.graql.InsertQuery;
import ai.grakn.graql.answer.ConceptMap;
import grakn.biograkn.migrator.drug.Drug;
import grakn.biograkn.migrator.gene.Gene;
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

public class DrugVariantAssociation {

    public static void migrate(Grakn.Session session) {
        try {
            BufferedReader reader = Files.newBufferedReader(Paths.get("dataset/disgenet/drug-variant-association.csv"));
            CSVParser csvParser = new CSVParser(reader, CSVFormat.DEFAULT);

            for (CSVRecord csvRecord: csvParser) {

                // skip header
                if (csvRecord.getRecordNumber() == 1) {
                    continue;
                }

                String snpId = csvRecord.get(0);
                String drugBankId = csvRecord.get(1);

                InsertQuery insertQuery = Graql.match(
                        var("v").isa("variant").has("snp-id", snpId),
                        var("d").isa("drug").has("drug-bank-id", drugBankId))
                        .insert(var("dva").isa("drug-variant-association").rel("associated-variant", "v").rel("associated-drug", "d"));

                Grakn.Transaction writeTransaction = session.transaction(GraknTxType.WRITE);
                List<ConceptMap> insertedIds = insertQuery.withTx(writeTransaction).execute();

                if (insertedIds.isEmpty()) {
                    List<Class> prereqs = Arrays.asList(Drug.class, Variant.class);
                    throw new IllegalStateException("Nothing was inserted for: " + insertQuery.toString() +
                            "\nA prerequisite dataset may have not been loaded. This dataset requires: " + prereqs.toString());
                }

                System.out.println("Inserted a genetic variation with ID: " + insertedIds.get(0).get("dva").id());
                writeTransaction.commit();
            }

            System.out.println("-----drug variant associations have been migrated-----");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
