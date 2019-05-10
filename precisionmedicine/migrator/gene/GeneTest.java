package grakn.biograkn.precisionmedicine.migrator.gene;

import grakn.client.GraknClient;
import grakn.core.concept.answer.ConceptMap;
import graql.lang.Graql;
import graql.lang.query.GraqlGet;
import org.junit.Test;

import java.util.List;

import static grakn.biograkn.precisionmedicine.migrator.gene.Gene.migrateFromCtdbase;
import static grakn.biograkn.precisionmedicine.migrator.gene.Gene.migrateFromHgnc;
import static grakn.biograkn.utils.Utils.loadSchema;
import static graql.lang.Graql.var;
import static org.junit.Assert.assertEquals;

public class GeneTest {

    @Test
    public void geneTest() {
        GraknClient graknClient = new GraknClient("127.0.0.1:48555");
        GraknClient.Session session = graknClient.session("gene_test");

        loadSchema("precisionmedicine/schema/precision-medicine-schema.gql", session);

        migrateFromHgnc(session, "precisionmedicine/dataset/hgnc/custom_mock.csv");
        migrateFromCtdbase(session, "precisionmedicine/dataset/ctdbase/CTD_genes_mock.csv");

        GraknClient.Transaction readTransaction = session.transaction().read();

        GraqlGet graqlGet = Graql.match(var("g").isa("gene")).get();

        List<ConceptMap> getIds = readTransaction.execute(graqlGet);

        assertEquals(getIds.size(), 200);

        graknClient.keyspaces().delete("gene_test");

        readTransaction.close();
        session.close();
    }
}
