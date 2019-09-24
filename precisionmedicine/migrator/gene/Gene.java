package grakn.biograkn.precisionmedicine.migrator.gene;

import grakn.client.GraknClient;
import grakn.core.concept.answer.ConceptMap;
import graql.lang.Graql;
import graql.lang.query.GraqlGet;
import graql.lang.query.GraqlInsert;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import grakn.biograkn.utils.Utils;
import static graql.lang.Graql.var;


@SuppressWarnings("Duplicates")
public class Gene {
    public static void migrate(GraknClient.Session session, String dataset) {
        System.out.print("\tMigrating Genes");

        migrateFromHgnc(session, dataset + "/hgnc/custom.csv");
        migrateFromCtdbase(session, dataset + "/ctdbase/CTD_genes.csv");

        System.out.println(" - [DONE]");
    }

    static void migrateFromHgnc(GraknClient.Session session, String path) {
        try {
            BufferedReader reader = Files.newBufferedReader(Paths.get(path));
            CSVParser csvParser = new CSVParser(reader, CSVFormat.DEFAULT);

            GraknClient.Transaction tx = session.transaction().write();
            int counter = 0;

            for (CSVRecord csvRecord : csvParser) {

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

     static void migrateFromCtdbase(GraknClient.Session session, String path) {

        try {
            BufferedReader reader = Files.newBufferedReader(Paths.get(path));
            CSVParser csvParser = new CSVParser(reader, CSVFormat.DEFAULT);

            int counter = 0;
            GraknClient.Transaction tx = session.transaction().write();
            for (CSVRecord csvRecord : csvParser) {

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
                    GraqlInsert graqlInsert = Graql.insert(var("g").isa("gene")
                            .has("symbol", symbol)
                            .has("name", name)
                            .has("ncbi-id", ncbiId));
                    System.out.print(".");
                    tx.execute(graqlInsert);
                    if (counter % 50 == 0) {
                        tx.commit();
                        System.out.println("committed!");
                        tx = session.transaction().write();
                    }
                    counter++;
                }
            }
            tx.commit();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

