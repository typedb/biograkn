package grakn.biograkn.migrator;

import ai.grakn.Keyspace;
import ai.grakn.client.Grakn;
import ai.grakn.util.SimpleURI;
import grakn.biograkn.migrator.clinicaltrial.ClinicalTrial;
import grakn.biograkn.migrator.diagnosis.Diagnosis;
import grakn.biograkn.migrator.disease.Disease;
import grakn.biograkn.migrator.diseasedrug.DiseaseDrugAssociation;
import grakn.biograkn.migrator.drug.Drug;
import grakn.biograkn.migrator.drugvariant.DrugVariantAssociation;
import grakn.biograkn.migrator.experimentaltreatement.ExperimentalTreatment;
import grakn.biograkn.migrator.gene.Gene;
import grakn.biograkn.migrator.geneidentification.GeneIdentification;
import grakn.biograkn.migrator.geneticvariation.GeneticVariation;
import grakn.biograkn.migrator.person.Person;
import grakn.biograkn.migrator.pubmedarticle.PubmedArticle;
import grakn.biograkn.migrator.variant.Variant;
import grakn.biograkn.migrator.variantidentification.VariantIdentification;

public class Migrator {
    public static void main(String[] args) {

        System.out.println("~~~~~~~~~~Starting Migration~~~~~~~~~~");

        SimpleURI localGrakn = new SimpleURI("127.0.0.1", 48555);
        Keyspace keyspace = Keyspace.of("precision_medicine");
        Grakn grakn = new Grakn(localGrakn);
        Grakn.Session session = grakn.session(keyspace);

        Person.migrate(session);
        Disease.migrate(session);
        Diagnosis.migrate(session);
        Gene.migrate(session);
        Variant.migrate(session);
        ClinicalTrial.migrate(session);
        GeneIdentification.migrate(session);
        VariantIdentification.migrate(session);
        GeneticVariation.migrate(session);
//        Drug.migrate(session);
//        DiseaseDrugAssociation.migrate(session);
//        DrugVariantAssociation.migrate(session);
        ExperimentalTreatment.migrate(session);
//        PubmedArticle.migrate(session);

        session.close();

        System.out.println("~~~~~~~~~~Migration Completed~~~~~~~~~~");
    }
}