package grakn.biograkn.precisionmedicine.migrator.clinicaltrialrelationships;

import grakn.client.GraknClient;
import grakn.client.answer.ConceptMap;
import graql.lang.Graql;
import graql.lang.query.GraqlGet;
import graql.lang.query.GraqlInsert;

import java.util.List;
import java.util.stream.Stream;

import static graql.lang.Graql.var;



@SuppressWarnings("Duplicates")
public class ClinicalTrialRelationship {

        public static void migrate(GraknClient.Session session) {

            GraknClient.Transaction readTransaction = session.transaction().read();

            GraqlGet graqlGet = Graql.match(var("ct").isa("clinical-trial").has("intervention-name", var("i")).has("condition", var("c")).has("brief-title", var("b")).has("official-title", var("o"))).get();
            Stream<ConceptMap> clinicalTrialConcepts = readTransaction.stream(graqlGet);

            System.out.println("loaded clinical-trials");

            graqlGet = Graql.match(var("d").isa("drug").has("name", var("n"))).get("n");
            Stream<ConceptMap> drugNameConcepts = readTransaction.stream(graqlGet);

            System.out.println("loaded drugs");

            graqlGet = Graql.match(var("d").isa("disease").has("name", var("n"))).get("n");
            Stream<ConceptMap> diseaseNameConcepts = readTransaction.stream(graqlGet);

            System.out.println("loaded diseases");

//
//            graqlGet = Graql.match(var("g").isa("gene").has("symbol", var("s"))).get("s");
//            List<ConceptMap> geneSymbolConcepts = readTransaction.execute(graqlGet);
//
//            System.out.println("loaded genes");

            clinicalTrialConcepts.forEach(clinicalTrial -> {
                String interventionName = clinicalTrial.get("i").asAttribute().value().toString();
                String condition = clinicalTrial.get("c").asAttribute().value().toString();
                String briefTitle = clinicalTrial.get("b").asAttribute().value().toString();
                String officialTitle = clinicalTrial.get("o").asAttribute().value().toString();

                if (interventionName.length() > 0) {
                    drugNameConcepts.forEach(map -> {
                        String drugName = map.get("n").asAttribute().value().toString();

                        if (drugName.length() > 0 && interventionName.contains(drugName)) {

                            GraqlInsert GraqlInsert = Graql.match(
                                    var("dn").isa("name").id(map.get("n").id().toString()),
                                    var("in").isa("intervention-name").val(interventionName))
                                    .insert(var("con").isa("containing").rel("contained", "dn").rel("container", "in"));

                            GraknClient.Transaction writeTransaction = session.transaction().write();
                            List<ConceptMap> insertedIds = writeTransaction.execute(GraqlInsert);
                            System.out.println("committed!");
                            writeTransaction.commit();
                        }
                    });
                }

                if (condition.length() > 0) {
                    diseaseNameConcepts.forEach(map -> {
                        String diseaseName = map.get("n").asAttribute().value().toString();

                        if (diseaseName.length() > 0 && condition.contains(diseaseName)) {

                            GraqlInsert GraqlInsert = Graql.match(
                                    var("dn").isa("name").id(map.get("n").id().toString()),
                                    var("c").isa("condition").val(condition))
                                    .insert(var("con").isa("containing").rel("contained", "dn").rel("container", "c"));

                            GraknClient.Transaction writeTransaction = session.transaction().write();
                            List<ConceptMap> insertedIds = writeTransaction.execute(GraqlInsert);
                            System.out.println("committed!");
                            writeTransaction.commit();
                        }
                    });
                }

//                if (briefTitle.length() > 0 || officialTitle.length() > 0) {
//                    for (ConceptMap map : geneSymbolConcepts) {
//                        String geneSymbol = map.get("s").asAttribute().value().toString();
//
//                        if (geneSymbol.length() > 0 && (briefTitle.contains(geneSymbol))) {
//
//                            GraqlInsert GraqlInsert = Graql.match(
//                                    var("s").isa("symbol").id(map.get("s").id().toString()),
//                                    var("bt").isa("brief-title").val(briefTitle))
//                                    .insert(var("con").isa("containing").rel("contained", "s").rel("container", "bt"));
//
//                            GraknClient.Transaction writeTransaction = session.transaction().write();
//                            List<ConceptMap> insertedIds = writeTransaction.execute(GraqlInsert);
//
//                            writeTransaction.commit();
//                        } else if (geneSymbol.length() > 0 && officialTitle.contains(geneSymbol)) {
//
//                            GraqlInsert GraqlInsert = Graql.match(
//                                    var("s").isa("symbol").id(map.get("s").id().toString()),
//                                    var("ot").isa("official-title").val(officialTitle))
//                                    .insert(var("con").isa("containing").rel("contained", "s").rel("container", "ot"));
//
//                            GraknClient.Transaction writeTransaction = session.transaction().write();
//                            List<ConceptMap> insertedIds = writeTransaction.execute(GraqlInsert);
//
//                            writeTransaction.commit();
//                        }
//                    }
//                }
            });
            System.out.println("inserted trial relationships");
            readTransaction.close();
        }
}
