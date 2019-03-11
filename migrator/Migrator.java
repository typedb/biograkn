package grakn.biograkn.migrator;



import edu.stanford.nlp.coref.data.CorefChain;
import edu.stanford.nlp.ie.util.RelationTriple;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.*;
import edu.stanford.nlp.semgraph.SemanticGraph;
import edu.stanford.nlp.trees.Tree;
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
import grakn.core.concept.answer.ConceptMap;
import graql.lang.Graql;
import graql.lang.query.GraqlGet;
import graql.lang.query.GraqlInsert;

import java.util.List;
import java.util.Map;
import java.util.Properties;

import static graql.lang.Graql.var;


public class Migrator {

    public static void main(String[] args) {
//        migratePrecisionMedicine();
        migrateTextMining();
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

    @SuppressWarnings("Duplicates")
    public static void migrateTextMining() {

        GraknClient graknClient = new GraknClient("127.0.0.1:48555");
        GraknClient.Session session = graknClient.session("text_mining");

        try {
            String abstractText = "Joe Smith was born in California. In 2017, he went to Paris, France in the summer. His flight left at 3:00pm on July 10th, 2017. " +
                    "After eating some escargot for the first time, Joe said, He sent a postcard to his sister Jane Smith. After hearing about Joe's trip, Jane decided she might go to France one day.";

            GraknClient.Transaction writeTransaction = session.transaction().write();
            GraqlInsert graqlInsert = Graql.insert(var("a").isa("annotated-abstract").val(abstractText));
            List<ConceptMap> insertedAbstractId = writeTransaction.execute(graqlInsert);
            writeTransaction.commit();
            System.out.println("Inserted an abstract");

            // create a document object
            CoreDocument document = new CoreDocument(abstractText);

            // set up pipeline properties
            Properties props = new Properties();

            // set the list of annotators to run
            props.setProperty("annotators", "tokenize,ssplit,pos,lemma,ner,parse,kbp,coref,relation,sentiment");

            // set a property for an annotator
            props.setProperty("coref.algorithm", "neural");
            props.setProperty("ner.useSUTime", "false");

            // build pipeline
            StanfordCoreNLP pipeline = new StanfordCoreNLP(props);

            // annnotate the document
            pipeline.annotate(document);

            List<CoreSentence> sentences = document.sentences();

            for (CoreSentence sentence : sentences) {

                writeTransaction = session.transaction().write();

                graqlInsert = Graql.insert(var("s").isa("sentence").has("text", sentence.text()).has("sentiment", sentence.sentiment()));

                List<ConceptMap> insertedSentenceId = writeTransaction.execute(graqlInsert);

                graqlInsert = Graql.match(
                        var("s").id(insertedSentenceId.get(0).get("s").id().toString()),
                        var("a").id(insertedAbstractId.get(0).get("a").id().toString()))
                        .insert(var("acs").isa("abstract-containing-sentence").rel("contained-sentence", "s").rel("abstract-container", "a"));

                writeTransaction.execute(graqlInsert);

                writeTransaction.commit();
                System.out.println("Inserted a sentence");

                List<CoreLabel> tokens = sentence.tokens();

                writeTransaction = session.transaction().write();
                for (CoreLabel token : tokens) {
                    String tag = token.tag();
                    String lemma = token.lemma();
                    double startPosition = token.beginPosition();
                    double endPosition = token.endPosition();
                    String ner = token.ner();
                    Map<String, Double> nerConfidence = token.nerConfidence();


                    graqlInsert = Graql.match(var("s").id(insertedSentenceId.get(0).get("s").id().toString()))
                            .insert(var("t").isa("token")
                                        .has("tag", tag)
                                        .has("lemma", lemma)
                                        .has("start-position", startPosition)
                                        .has("end-position", endPosition)
                                        .has("ner", ner),
                                    var("sct").isa("sentence-containing-token").rel("sentence-container", "s").rel("contained-token", "t"));

                    writeTransaction.execute(graqlInsert);
                }
                writeTransaction.commit();
                System.out.println("Inserted tokens");

                List<RelationTriple> relationships = sentence.relations();

                writeTransaction = session.transaction().write();

                for (RelationTriple relation : relationships) {
                    List<CoreLabel> subjects = relation.subject;
                    List<CoreLabel> objects = relation.object;
                    String relationshipType = relation.relation.get(0).toString();
                    double relationConfidence = relation.confidence;

                    String query = "match $s id " + insertedSentenceId.get(0).get("s").id() + ";";

                    for (CoreLabel subject : subjects) {
                        String subjectLemma = subject.lemma();
                        query += " $" + subject.toString() + " isa token, has lemma '" + subjectLemma + "';";
                    }

                    for (CoreLabel object : objects) {
                        String objectLemma = object.lemma();
                        query += " $" + object.toString() + " isa token, has lemma '" + objectLemma + "';";
                    }

                    query += " insert $r (mentioning-sentence: $s,";

                    for (CoreLabel subject : subjects) {
                        query += "subject: $" + subject.toString() + ",";
                    }

                    for (CoreLabel object : objects) {
                        query += "object: $" + object.toString() + ",";
                    }

                    query = query.substring(0, query.length() - 1);
                    query += ") isa relationship, has confidence " + relationConfidence + ", has relationship-type '" + relationshipType + "';";

                    graqlInsert = Graql.parse(query);
                    writeTransaction.execute(graqlInsert);
                }

                List<CoreEntityMention> entityMentions = sentence.entityMentions();

                for (CoreEntityMention entityMention : entityMentions) {
                    String entityMentioned = entityMention.toString();

                    graqlInsert = Graql.match(
                            var("s").id(insertedSentenceId.get(0).get("s").id().toString()))
                            .insert(var("e").isa("entity-mention").has("value", entityMentioned),
                                    var("m").isa("mention").rel("mentioned-entity", "e").rel("mentioning-sentence", "s"));
                    writeTransaction.execute(graqlInsert);
                }
                writeTransaction.commit();
                System.out.println("Inserted relationships and mentions");
            }

            Map<Integer, CorefChain> corefChains = document.corefChains();


            corefChains.forEach((key, value) -> {

                GraknClient.Transaction writeTx = session.transaction().write();


                int chainId = value.getChainID();
                String representativeMention = value.getRepresentativeMention().mentionSpan;

                List<CorefChain.CorefMention> mentions = value.getMentionsInTextualOrder();

                String matchQuery = "match $a id " + insertedAbstractId.get(0).get("a").id().toString() + ";";

                String insertQuery = "insert $r (owner: $a,";

                for (CorefChain.CorefMention mention : mentions) {
                    String sentenceText = document.sentences().get(mention.sentNum - 1).text();
                    String mentionedEntity = mention.mentionSpan;

                    GraqlGet getMentionedEntity = Graql.match(var("e").isa("entity-mention").has("value", mentionedEntity)).get();

                    List<ConceptMap> getEntity = writeTx.execute(getMentionedEntity);

                    if (getEntity.isEmpty()) {
                        GraqlInsert ins = Graql.insert(var("e").isa("entity-mention").has("value", mentionedEntity));
                        writeTx.execute(getMentionedEntity);
                        writeTx.commit();
                        writeTx = session.transaction().write();
                    }

                    matchQuery += " $" + mentionedEntity.replaceAll("[^a-zA-Z]", "") + mention.mentionID
                            + " isa entity-mention, has value \"" + mentionedEntity + "\"; $" + mention.mentionID + mention.sentNum
                            + " isa sentence, has text \"" + sentenceText + "\"; $" + mention.mentionID
                            + " ($" + mentionedEntity.replaceAll("[^a-zA-Z]", "") + mention.mentionID+ ", $" + mention.mentionID + mention.sentNum + ");";

                    if (representativeMention != mentionedEntity) {
                        insertQuery += "owned: $" + mention.mentionID + ",";
                    } else {
                        insertQuery += "representative: $" + mention.mentionID + ",";
                    }
                }
                insertQuery = insertQuery.substring(0, insertQuery.length() - 1);
                String query = matchQuery + insertQuery + ") isa coref-chain, has chain-id " + chainId + ";";

                GraqlInsert insert = Graql.parse(query);

                writeTx.execute(insert);

                writeTx.commit();
            });
            System.out.println("asdasd");

            session.close();
        } catch (Exception e) {
            e.printStackTrace();
            session.close();
        }
    }
}