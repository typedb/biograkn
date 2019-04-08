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
import grakn.core.client.GraknClient;
import graql.lang.Graql;
import graql.lang.query.GraqlQuery;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

@SuppressWarnings("Duplicates")
public class Migrator {

    public static void migratePrecisionMedicine() {
        System.out.println("~~~~~~~~~~Starting Precision Medicine Migration~~~~~~~~~~");

        GraknClient graknClient = new GraknClient("127.0.0.1:48555");
        GraknClient.Session session = graknClient.session("precision_medicine");

        loadSchema(session);

        // entities
        // Gene.migrate(session);
        // Variant.migrate(session);
        // Disease.migrate(session);
        // Drug.migrate(session);
        // ClinicalTrial.migrate(session);

        // relationships
        // GeneDiseaseAssociation.migrate(session);
        // VariantDiseaseAssociation.migrate(session);
        // DrugDiseaseAssociation.migrate(session);
        ClinicalTrialRelationship.migrate(session);

        session.close();
        System.out.println("~~~~~~~~~~Precision Medicine Migration Completed~~~~~~~~~~");
    }

    private static void loadSchema(GraknClient.Session session) {
        GraknClient.Transaction transaction = session.transaction().write();

        try {
            byte[] encoded = Files.readAllBytes(Paths.get("precisionmedicine/schema/precision-medicine-schema.gql"));
            String query = new String(encoded, StandardCharsets.UTF_8);
            transaction.execute((GraqlQuery) Graql.parse(query));
            transaction.commit();
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("-----precision medicine schema loaded-----");
    }
}
