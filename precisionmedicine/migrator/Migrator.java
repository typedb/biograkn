package grakn.biograkn.precisionmedicine.migrator;


import grakn.biograkn.precisionmedicine.migrator.clinicaltrial.ClinicalTrial;
import grakn.biograkn.precisionmedicine.migrator.clinicaltrialrelationships.ClinicalTrialRelationship;
import grakn.biograkn.precisionmedicine.migrator.disease.Disease;
import grakn.biograkn.precisionmedicine.migrator.drug.Drug;
import grakn.biograkn.precisionmedicine.migrator.drugdisease.DrugDiseaseAssociation;
import grakn.biograkn.precisionmedicine.migrator.gene.Gene;
import grakn.biograkn.precisionmedicine.migrator.genedisease.GeneDiseaseAssociation;
import grakn.biograkn.precisionmedicine.migrator.variant.Variant;
import grakn.biograkn.precisionmedicine.migrator.variantdisease.VariantDiseaseAssociation;
import grakn.client.GraknClient;
import graql.lang.Graql;
import graql.lang.query.GraqlQuery;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

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
        System.out.println("Gene.migrate(session, dataset) took " + (System.currentTimeMillis() - start) + "ms");
//        Variant.migrate(session, dataset); // 2
//        Disease.migrate(session, dataset); // 2
//        Drug.migrate(session, dataset); // 2
//
//        session.close();
//        session = graknClient.session("precision_medicine");
//
//        ClinicalTrial.migrate(session, dataset); // 3
//
//        session.close();
//        session = graknClient.session("precision_medicine");
//
//        // relationships
//        GeneDiseaseAssociation.migrate(session, dataset); // 4
//        VariantDiseaseAssociation.migrate(session, dataset); // 4
//        DrugDiseaseAssociation.migrate(session, dataset); // 4
//        ClinicalTrialRelationship.migrate(session); // 4

        session.close();
        graknClient.close();
    }
}
