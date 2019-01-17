package grakn.template.java;

import grakn.template.java.migrator.Migrator;

public class Main {
    public static void main(String[] args) {

        Migrator migrator = new Migrator();

        migrator.migrateGene();
    }
}