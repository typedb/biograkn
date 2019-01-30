package grakn.biograkn.migrator.diseasedrug;

import ai.grakn.GraknTxType;
import ai.grakn.client.Grakn;
import ai.grakn.concept.ConceptId;
import ai.grakn.graql.GetQuery;
import ai.grakn.graql.Graql;
import ai.grakn.graql.InsertQuery;
import ai.grakn.graql.answer.ConceptMap;

import java.util.List;

import static ai.grakn.graql.Graql.var;

public class DiseaseDrugAssociation {

    public static void migrate(Grakn.Session session) {

        Grakn.Transaction readTransaction = session.transaction(GraknTxType.READ);

        GetQuery getQuery = Graql.match(Graql.var("dr").isa("drug")).get();
        List<ConceptMap> clinicalTrials = getQuery.withTx(readTransaction).execute();

        for (ConceptMap map: clinicalTrials) {
            String conceptId = map.get("dr").id().getValue();

            InsertQuery insertQuery = Graql.match(
                    var("d").isa("disease").has("disease-id", "C0025202"),
                    var("dr").isa("drug").id(ConceptId.of(conceptId)))
                    .insert(var("dda").isa("disease-drug-association").rel("associated-disease", "d").rel("associated-drug", "dr"));

            Grakn.Transaction writeTransaction = session.transaction(GraknTxType.WRITE);
            List<ConceptMap> insertedId = insertQuery.withTx(writeTransaction).execute();
            System.out.println("Inserted a disease drug association with ID: " + insertedId.get(0).get("dda").id());
            writeTransaction.commit();
        }
        System.out.println("-----disease drug associations have been migrated-----");
    }
}
