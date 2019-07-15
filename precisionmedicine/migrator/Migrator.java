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

        // loadSchema("precisionmedicine/schema/precision-medicine-schema.gql", session);

        // entities
        // Gene.migrate(session, dataset);
        // Variant.migrate(session, dataset);
        // Disease.migrate(session, dataset);
        // Drug.migrate(session, dataset);
        ClinicalTrial.migrate(session, dataset);

        // relationships
        // GeneDiseaseAssociation.migrate(session, dataset);
        // VariantDiseaseAssociation.migrate(session, dataset);
        // DrugDiseaseAssociation.migrate(session, dataset);
        // ClinicalTrialRelationship.migrate(session);

        session.close();
        graknClient.close();
    }
}
