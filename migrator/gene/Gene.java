package grakn.biograkn.migrator.gene;

import grakn.core.client.GraknClient;
import grakn.core.concept.answer.ConceptMap;
import graql.lang.Graql;
import graql.lang.query.GraqlGet;
import graql.lang.query.GraqlInsert;
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
public class Gene {
    public static void migrate(GraknClient.Session session) {
        migrateFromHgnc(session);
        migrateFromCtdbase(session);
        System.out.println("-----genes have been migrated-----");
    }

    private static void migrateFromHgnc(GraknClient.Session session) {
        try {
            BufferedReader reader = Files.newBufferedReader(Paths.get("dataset/hgnc/custom.csv"));
            CSVParser csvParser = new CSVParser(reader, CSVFormat.DEFAULT);

            for (CSVRecord csvRecord: csvParser) {

                // skip header
                if (csvRecord.getRecordNumber() == 1) {
                    continue;
                }

                String symbol = csvRecord.get(0);
                String name = csvRecord.get(1);
                String chromosome = csvRecord.get(2);
                String locusType = csvRecord.get(3);
                String locusGroup = csvRecord.get(4);
                String ensemblId = csvRecord.get(5);
                String ncbiId = csvRecord.get(6);

                GraqlInsert graqlInsert = Graql.insert(var("g").isa("gene")
                        .has("symbol", symbol)
                        .has("name", name)
                        .has("chromosome", chromosome)
                        .has("locus-type", locusType)
                        .has("locus-group", locusGroup)
                        .has("ensembl-id", ensemblId)
                        .has("ncbi-id", ncbiId));

                GraknClient.Transaction writeTransaction = session.transaction().write();
                List<ConceptMap> insertedIds = writeTransaction.execute(graqlInsert);
                System.out.println("Inserted a gene at record number: " + csvRecord.getRecordNumber() + " of hgnc");
                writeTransaction.commit();
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void migrateFromCtdbase(GraknClient.Session session) {
        try {
            BufferedReader reader = Files.newBufferedReader(Paths.get("dataset/ctdbase/CTD_genes.csv"));
            CSVParser csvParser = new CSVParser(reader, CSVFormat.DEFAULT);

            for (CSVRecord csvRecord: csvParser) {

                // skip header
                if (csvRecord.getRecordNumber() < 30) {
                    continue;
                }

                String symbol = csvRecord.get(0);
                String name = csvRecord.get(1);
                String ncbiId = csvRecord.get(2);

                GraknClient.Transaction readTransaction = session.transaction().read();

                GraqlGet graqlGet = Graql.match(
                        var("g").isa("gene").has("ncbi-id", ncbiId)).get();

                List<ConceptMap> getIds = readTransaction.execute(graqlGet);

                readTransaction.close();

                // check if the gene has already been inserted
                if(getIds.size() == 0){
                    GraqlInsert GraqlInsert = Graql.insert(var("g").isa("gene")
                            .has("symbol", symbol)
                            .has("name", name)
                            .has("ncbi-id", ncbiId));

                    GraknClient.Transaction writeTransaction = session.transaction().write();
                    List<ConceptMap> insertedIds = writeTransaction.execute(GraqlInsert);

                    System.out.println("Inserted a gene at record number: " + csvRecord.getRecordNumber() + " of ctdbase");
                    writeTransaction.commit();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
