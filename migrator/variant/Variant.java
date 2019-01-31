package grakn.biograkn.migrator.variant;

import ai.grakn.GraknTxType;
import ai.grakn.client.Grakn;
import ai.grakn.graql.Graql;
import ai.grakn.graql.InsertQuery;
import ai.grakn.graql.answer.ConceptMap;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

import static ai.grakn.graql.Graql.var;

public class Variant {
    public static void migrate(Grakn.Session session) {
        try {
            BufferedReader reader = Files.newBufferedReader(Paths.get("dataset/disgenet/variants.csv"));
            CSVParser csvParser = new CSVParser(reader, CSVFormat.DEFAULT);

            for (CSVRecord csvRecord: csvParser) {

                // skip header
                if (csvRecord.getRecordNumber() == 1) {
                    continue;
                }

                String snpId = csvRecord.get(0);
                String variantSymbol = csvRecord.get(1);

                InsertQuery insertQuery = Graql.insert(var("v").isa("variant")
                        .has("snp-id", snpId)
                        .has("variant-symbol", variantSymbol));

                Grakn.Transaction writeTransaction = session.transaction(GraknTxType.WRITE);
                List<ConceptMap> insertedIds = insertQuery.withTx(writeTransaction).execute();
                System.out.println("Inserted a variant with ID: " + insertedIds.get(0).get("v").id());
                writeTransaction.commit();
            }

            System.out.println("-----variants have been migrated-----");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
