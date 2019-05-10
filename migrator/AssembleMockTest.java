package grakn.biograkn.migrator;

import org.junit.Test;
import org.junit.Before;

import static grakn.biograkn.precisionmedicine.migrator.Migrator.migratePrecisionMedicine;

import grakn.client.GraknClient;
import graql.lang.Graql;
import java.util.List;
import grakn.core.concept.answer.ConceptMap;
import graql.lang.query.GraqlCompute;

import grakn.core.concept.answer.Numeric;



public class AssembleMockTest {

    @Before
    public void before() {
        migratePrecisionMedicine("mock");
    }

    @Test
    public void assembleMockTest() {
        GraknClient graknClient = new GraknClient("127.0.0.1:48555");
        GraknClient.Session session = graknClient.session("precision_medicine");

        GraknClient.Transaction readTransaction = session.transaction().read();

        GraqlCompute.Statistics query = Graql.compute().count().in("gene");

        List<Numeric> insertedIds = readTransaction.execute(query);

        System.out.println(insertedIds);
    }
}