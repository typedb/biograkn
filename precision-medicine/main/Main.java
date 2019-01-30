package grakn.template.java;

import ai.grakn.Keyspace;
import ai.grakn.client.Grakn;
import ai.grakn.util.SimpleURI;
import grakn.template.java.migrator.Migrator;

public class Main {
    public static void main(String[] args) {

        System.out.println("~~~~~~~~~~Starting Migration~~~~~~~~~~");

        SimpleURI localGrakn = new SimpleURI("127.0.0.1", 48555);
        Keyspace keyspace = Keyspace.of("precision_medicine");
        Grakn grakn = new Grakn(localGrakn);
        Grakn.Session session = grakn.session(keyspace);

        Migrator migrator = new Migrator();

        //common


        // precision-medicine

        migrator.migratePersons(session);
        migrator.migrateDiseases(session);
        migrator.migrateDiagnoses(session);

        migrator.migrateGenes(session);
        migrator.migrateVariants(session);
        migrator.migrateClinicalTrials(session);

        migrator.migrateGeneIdentifications(session);
        migrator.migrateVariantIdentifications(session);
        migrator.migrateGeneticVariations(session);
        migrator.migrateDrugs(session);
        migrator.migrateDiseaseDrugAssociations(session);

        // text mining



        session.close();

        System.out.println("~~~~~~~~~~Migration Completed~~~~~~~~~~");
    }
}