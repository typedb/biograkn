package grakn.biograkn.migrator;


import grakn.biograkn.migrator.clinicaltrial.ClinicalTrial;
import grakn.biograkn.migrator.clinicaltrialrelationships.ClinicalTrialRelationship;
import grakn.biograkn.migrator.disease.Disease;
import grakn.biograkn.migrator.drug.Drug;
import grakn.biograkn.migrator.drugdisease.DrugDiseaseAssociation;
import grakn.biograkn.migrator.gene.Gene;
import grakn.biograkn.migrator.genedisease.GeneDiseaseAssociation;
import grakn.biograkn.migrator.variant.Variant;
import grakn.biograkn.migrator.variantdisease.VariantDiseaseAssociation;
import grakn.core.client.GraknClient;


public class Migrator {

    public static void main(String[] args) {
        migratePrecisionMedicine();
    }

    public static void migratePrecisionMedicine() {
        System.out.println("~~~~~~~~~~Starting Precision Medicine Migration~~~~~~~~~~");

        GraknClient graknClient = new GraknClient("127.0.0.1:48555");
        GraknClient.Session session = graknClient.session("precision_medicine");

        // entities
        Gene.migrate(session);
        Variant.migrate(session);
        Disease.migrate(session);
        Drug.migrate(session);
        ClinicalTrial.migrate(session);

        // relationships
        GeneDiseaseAssociation.migrate(session);
        VariantDiseaseAssociation.migrate(session);
        DrugDiseaseAssociation.migrate(session);
        ClinicalTrialRelationship.migrate(session);

        session.close();
        System.out.println("~~~~~~~~~~Precision Medicine Migration Completed~~~~~~~~~~");
    }
}