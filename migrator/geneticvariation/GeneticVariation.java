package grakn.biograkn.migrator.geneticvariation;

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

public class GeneticVariation {

    public static void migrate(Grakn.Session session) {
        try {
            BufferedReader reader = Files.newBufferedReader(Paths.get("dataset/geneticVariations.csv"));
            CSVParser csvParser = new CSVParser(reader, CSVFormat.DEFAULT);

            for (CSVRecord csvRecord: csvParser) {

                // skip header
                if (csvRecord.getRecordNumber() == 1) {
                    continue;
                }

                String geneSymbol = csvRecord.get(0);
                String snpId = csvRecord.get(1);

                InsertQuery insertQuery = Graql.match(
                        var("g").isa("gene").has("gene-symbol", geneSymbol),
                        var("a").isa("allele").has("snp-id", snpId))
                        .insert(var("gv").isa("genetic-variation").rel("varied-gene", "g").rel("genetic-variant", "a"));

                Grakn.Transaction writeTransaction = session.transaction(GraknTxType.WRITE);
                List<ConceptMap> insertedId = insertQuery.withTx(writeTransaction).execute();
                System.out.println("Inserted a genetic variation with ID: " + insertedId.get(0).get("gv").id());
                writeTransaction.commit();
            }

            System.out.println("-----genetic variations have been migrated-----");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
