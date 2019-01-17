package grakn.template.java.migrator;


import ai.grakn.GraknTxType;
import ai.grakn.Keyspace;
import ai.grakn.client.Grakn;
import ai.grakn.graql.Graql;
import ai.grakn.graql.InsertQuery;
import ai.grakn.graql.answer.ConceptMap;
import ai.grakn.util.SimpleURI;
import java.util.List;

import static ai.grakn.graql.Graql.var;


public class Migrator {

    public void migrateGene() {



//        SimpleURI localGrakn = new SimpleURI("localhost", 48555);
//        Keyspace keyspace = Keyspace.of("precision-medicine");
//        Grakn grakn = new Grakn(localGrakn);
//        Grakn.Session session = grakn.session(keyspace);
//
//
//        Grakn.Transaction writeTransaction = session.transaction(GraknTxType.WRITE);
//        InsertQuery insertQuery = Graql.insert(var("p").isa("person").has("first-name", "Elizabeth"));
//        List<ConceptMap> insertedId = insertQuery.withTx(writeTransaction).execute();
//        System.out.println("Inserted a person with ID: " + insertedId.get(0).get("p").id());
//
//        writeTransaction.commit();
    }
}