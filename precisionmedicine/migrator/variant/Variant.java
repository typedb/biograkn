package grakn.biograkn.precisionmedicine.migrator.variant;

import grakn.core.client.GraknClient;import graql.lang.Graql;
import graql.lang.query.GraqlInsert;
import grakn.core.concept.answer.ConceptMap;

import static graql.lang.Graql.var;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;


@SuppressWarnings("Duplicates")
public class Variant {
    public static void migrate(GraknClient.Session session) {
        try {
            BufferedReader reader = Files.newBufferedReader(Paths.get("precisionmedicine/dataset/pharmgkb/variants.csv"));

            CSVParser csvParser = new CSVParser(reader, CSVFormat.DEFAULT);

            for (CSVRecord csvRecord: csvParser) {

                // skip header
                if (csvRecord.getRecordNumber() == 1) {
                    continue;
                }

                String pharmgkbId = csvRecord.get(0);
                String snpId = csvRecord.get(1);
                String geneSymbols = csvRecord.get(4);
                String location = csvRecord.get(5);

                GraqlInsert GraqlInsert = Graql.insert(var("v").isa("variant")
                        .has("pharmgkb-id", pharmgkbId)
                        .has("snp-id", snpId)
                        .has("gene-symbols", geneSymbols)
                        .has("location", location));

                GraknClient.Transaction writeTransaction = session.transaction().write();
                List<ConceptMap> insertedIds = writeTransaction.execute(GraqlInsert);
                System.out.println("Inserted a variant at record number: " + csvRecord.getRecordNumber());
                writeTransaction.commit();
            }

            System.out.println("-----variants have been migrated-----");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
