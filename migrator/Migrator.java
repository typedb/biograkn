package grakn.biograkn.migrator;


import static grakn.biograkn.precisionmedicine.migrator.Migrator.migratePrecisionMedicine;
import static grakn.biograkn.textmining.migrator.Migrator.migrateTextMining;

@SuppressWarnings("Duplicates")
public class Migrator {

    public static void main(String[] args) {
        migratePrecisionMedicine("precisionmedicine/dataset/all");
         migrateTextMining();
    }
}
