package grakn.biograkn.precisionmedicine.migrator;

import grakn.biograkn.precisionmedicine.migrator.clinicaltrial.ClinicalTrial;
import grakn.biograkn.precisionmedicine.migrator.disease.Disease;
import grakn.biograkn.precisionmedicine.migrator.drug.Drug;
import grakn.biograkn.precisionmedicine.migrator.drugdisease.DrugDiseaseAssociation;
import grakn.biograkn.precisionmedicine.migrator.gene.Gene;
import grakn.biograkn.precisionmedicine.migrator.genedisease.GeneDiseaseAssociation;
import grakn.biograkn.precisionmedicine.migrator.variant.Variant;
import grakn.biograkn.precisionmedicine.migrator.variantdisease.VariantDiseaseAssociation;
import grakn.client.GraknClient;

import static grakn.biograkn.utils.Utils.loadSchema;

@SuppressWarnings("Duplicates")
public class Migrator {

    public static void migratePrecisionMedicine(String dataset) {
        System.out.println("Migrating Precision Medicine");

        GraknClient graknClient = new GraknClient("127.0.0.1:48555");
        GraknClient.Session session = graknClient.session("precision_medicine");

        loadSchema("precisionmedicine/schema/precision-medicine-schema.gql", session);

        long start;

        // entities
        start = System.currentTimeMillis();
        Gene.migrate(session, dataset); // 1
        System.out.println("Gene.migrate(...) took " + (System.currentTimeMillis() - start) + "ms");

        start = System.currentTimeMillis();
        Variant.migrate(session, dataset); // 2
        Disease.migrate(session, dataset); // 2
        Drug.migrate(session, dataset); // 2
        System.out.println("{Variant,Disease,Drug}.migrate(...) took " + (System.currentTimeMillis() - start) + "ms");

        session.close();
        session = graknClient.session("precision_medicine");

        start = System.currentTimeMillis();
        ClinicalTrial.migrate(session, dataset); // 3
        System.out.println("ClinicalTrial.migrate(...) took " + (System.currentTimeMillis() - start) + "ms");

        session.close();

        // relationships
        session = graknClient.session("precision_medicine");
        start = System.currentTimeMillis();
        GeneDiseaseAssociation.migrate(session, dataset); // 4
        VariantDiseaseAssociation.migrate(session, dataset); // 4
        DrugDiseaseAssociation.migrate(session, dataset); // 4
        System.out.println("{GeneDiseaseAssociation,VariantDiseaseAssociation,DrugDiseaseAssociation}.migrate(...) took " + (System.currentTimeMillis() - start) + "ms");
        session.close();

//        session = graknClient.session("precision_medicine");
//        start = System.currentTimeMillis();
//        ClinicalTrialRelationship.migrate(session); // 5
//        System.out.println("{ClinicalTrialRelationship}.migrate(...) took " + (System.currentTimeMillis() - start) + "ms");
//        graknClient.close();
    }
}
