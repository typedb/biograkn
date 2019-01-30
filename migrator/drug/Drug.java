package grakn.biograkn.migrator.drug;

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

public class Drug {

    public static void migrate(Grakn.Session session) {
        try {
            BufferedReader reader = Files.newBufferedReader(Paths.get("dataset/drugs_melanoma.csv"));
            CSVParser csvParser = new CSVParser(reader, CSVFormat.DEFAULT);

            for (CSVRecord csvRecord: csvParser) {

                // skip header
                if (csvRecord.getRecordNumber() == 1) {
                    continue;
                }

                String drugName = csvRecord.get(0);
                String meshId = csvRecord.get(1);
                String drugBankId = csvRecord.get(2);
                String pubChemCid = csvRecord.get(3);

                InsertQuery insertQuery = Graql.insert(var("dr").isa("drug")
                        .has("drug-name", drugName)
                        .has("mesh-id", meshId)
                        .has("drug-bank-id", drugBankId)
                        .has("pub-chem-cid", pubChemCid));

                Grakn.Transaction writeTransaction = session.transaction(GraknTxType.WRITE);
                List<ConceptMap> insertedId = insertQuery.withTx(writeTransaction).execute();
                System.out.println("Inserted a drug with ID: " + insertedId.get(0).get("dr").id());
                writeTransaction.commit();
            }

            System.out.println("-----drugs have been migrated-----");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
