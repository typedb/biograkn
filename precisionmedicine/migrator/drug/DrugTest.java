package grakn.biograkn.precisionmedicine.migrator.drug;

import grakn.client.GraknClient;
import grakn.core.concept.answer.ConceptMap;
import graql.lang.Graql;
import graql.lang.query.GraqlGet;
import org.junit.Test;

import java.util.List;

import static grakn.biograkn.precisionmedicine.migrator.drug.Drug.*;
import static grakn.biograkn.utils.Utils.loadSchema;
import static graql.lang.Graql.var;
import static org.junit.Assert.assertEquals;

public class DrugTest {

    @Test
    public void drugTest() {
        GraknClient graknClient = new GraknClient("127.0.0.1:48555");
        GraknClient.Session session = graknClient.session("drug_test");

        loadSchema("precisionmedicine/schema/precision-medicine-schema.gql", session);

        migrateFromDrugsAtFda(session, "precisionmedicine/dataset/drugsatfda/Products_mock.csv");
        migrateFromPharmgkb(session, "precisionmedicine/dataset/pharmgkb/drugs_mock.csv");
        migrateFromCtdbase(session, "precisionmedicine/dataset/ctdbase/CTD_chemicals_mock.csv");

        GraknClient.Transaction readTransaction = session.transaction().read();

        GraqlGet graqlGet = Graql.match(var("d").isa("drug")).get();

        List<ConceptMap> getIds = readTransaction.execute(graqlGet);

        assertEquals(getIds.size(), 300);

        graknClient.keyspaces().delete("drug_test");

        readTransaction.close();
        session.close();
    }
}
