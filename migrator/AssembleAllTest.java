package grakn.biograkn.migrator;

import grakn.client.GraknClient;
import grakn.client.answer.Numeric;
import graql.lang.Graql;
import graql.lang.query.GraqlCompute;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

import static grakn.biograkn.precisionmedicine.migrator.Migrator.migratePrecisionMedicine;
import static org.junit.Assert.assertEquals;


@SuppressWarnings("Duplicates")
public class AssembleAllTest {

    @Before
    public void before() {
        migratePrecisionMedicine("precisionmedicine/dataset/all");
    }


    @Test
    public void assembleAllTest() {
        GraknClient graknClient = new GraknClient("127.0.0.1:48555");
        GraknClient.Session session = graknClient.session("precision_medicine");

        GraknClient.Transaction readTransaction = session.transaction().read();

        GraqlCompute.Statistics query = Graql.compute().count().in("gene");

        List<Numeric> insertedIds = readTransaction.execute(query);

        assertEquals(insertedIds.get(0).number().intValue(), 200);

        query = Graql.compute().count().in("variant");

        insertedIds = readTransaction.execute(query);

        assertEquals(insertedIds.get(0).number().intValue(), 100);

        query = Graql.compute().count().in("disease");

        insertedIds = readTransaction.execute(query);

        assertEquals(insertedIds.get(0).number().intValue(), 194);

        query = Graql.compute().count().in("drug");

        insertedIds = readTransaction.execute(query);

        assertEquals(insertedIds.get(0).number().intValue(), 300);

        query = Graql.compute().count().in("clinical-trial");

        insertedIds = readTransaction.execute(query);

        assertEquals(insertedIds.get(0).number().intValue(), 44);

        readTransaction.close();
        session.close();
        graknClient.close();
    }
}
