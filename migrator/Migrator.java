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

@SuppressWarnings("Duplicates")
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
        System.out.println("~~~~~~~~~~Starting Text Mining Migration~~~~~~~~~~");

        GraknClient graknClient = new GraknClient("127.0.0.1:48555");
        GraknClient.Session session = graknClient.session("text_mining");

        try {
            String abstractText = "Joe Smith was born in California. In 2017, he went to Paris, France in the summer. His flight left at 3:00pm on July 10th, 2017. " +
                    "After eating some escargot for the first time, Joe said, He sent a postcard to his sister Jane Smith. After hearing about Joe's trip, Jane decided she might go to France one day.";

            GraknClient.Transaction writeTransaction = session.transaction().write();
            GraqlInsert graqlInsert = Graql.insert(var("a").isa("annotated-abstract").val(abstractText));
            String insertedAbstractId = writeTransaction.execute(graqlInsert).get(0).get("a").id().toString();
            writeTransaction.commit();
            System.out.println("Inserted an abstract");

            CoreDocument document = parseText(abstractText);

            migrateSentences(document, insertedAbstractId, session);

            migrateCorefChains(document, insertedAbstractId, session);

            System.out.println("~~~~~~~~~~Text Mining Migration Completed~~~~~~~~~~");
            session.close();
        } catch (Exception e) {
            e.printStackTrace();
            session.close();
        }
    }

    public static void migrateSentences(CoreDocument document, String abstractId, GraknClient.Session session) {
        List<CoreSentence> sentences = document.sentences();

        for (CoreSentence sentence : sentences) {

            GraknClient.Transaction writeTransaction = session.transaction().write();

            GraqlInsert graqlInsert = Graql.insert(var("s").isa("sentence").has("text", sentence.text()).has("sentiment", sentence.sentiment()));

            String insertedSentenceId = writeTransaction.execute(graqlInsert).get(0).get("s").id().toString();

            graqlInsert = Graql.match(
                    var("s").id(insertedSentenceId),
                    var("a").id(abstractId))
                    .insert(var("acs").isa("abstract-containing-sentence").rel("contained-sentence", "s").rel("abstract-container", "a"));

            writeTransaction.execute(graqlInsert);
            writeTransaction.commit();

            migrateTokens(sentence, insertedSentenceId, session);

            migrateMinedRelations(sentence, insertedSentenceId, session);

            migrateMentions(sentence, insertedSentenceId, session);
        }
        System.out.println("inserted sentences");
    }

    public static void migrateTokens(CoreSentence sentence, String sentenceId, GraknClient.Session session) {
        List<CoreLabel> tokens = sentence.tokens();

        GraknClient.Transaction writeTransaction = session.transaction().write();
        for (CoreLabel token : tokens) {
            String tag = token.tag();
            String lemma = token.lemma();
            double startPosition = token.beginPosition();
            double endPosition = token.endPosition();
            String ner = token.ner();
            Map<String, Double> nerConfidence = token.nerConfidence();


            GraqlInsert graqlInsert = Graql.match(var("s").id(sentenceId))
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
    }

    public static void migrateMinedRelations(CoreSentence sentence, String sentenceId, GraknClient.Session session) {
        List<RelationTriple> minedRelations = sentence.relations();

        GraknClient.Transaction writeTransaction = session.transaction().write();
        for (RelationTriple minedRelation : minedRelations) {
            List<CoreLabel> subjects = minedRelation.subject;
            List<CoreLabel> objects = minedRelation.object;
            String relationType = minedRelation.relation.get(0).toString();
            double relationConfidence = minedRelation.confidence;

            String query = "match $s id " + sentenceId + ";";

            for (CoreLabel subject : subjects) {
                String subjectLemma = subject.lemma();
                double startPostion = subject.beginPosition();
                double endPosition = subject.endPosition();

                query += " $" + subject.toString() + " isa token, has lemma '" + subjectLemma + "'" + ", has start-position '" + startPostion + "'" + ", has end-position '" + endPosition + "';";
            }

            for (CoreLabel object : objects) {
                String objectLemma = object.lemma();
                double startPostion = object.beginPosition();
                double endPosition = object.endPosition();

                query += " $" + object.toString() + " isa token, has lemma '" + objectLemma + "'" + ", has start-position '" + startPostion + "'" + ", has end-position '" + endPosition + "';";
            }

            query += " insert $r (mentioning-sentence: $s,";

            for (CoreLabel subject : subjects) {
                query += "subject: $" + subject.toString() + ",";
            }

            for (CoreLabel object : objects) {
                query += "object: $" + object.toString() + ",";
            }

            query = query.substring(0, query.length() - 1);
            query += ") isa mined-relation, has confidence " + relationConfidence + ", has relation-type '" + relationType + "';";

            GraqlInsert graqlInsert = Graql.parse(query);
            writeTransaction.execute(graqlInsert);
        }
        writeTransaction.commit();
        System.out.println("Inserted mined-relations");
    }

    public static void migrateMentions(CoreSentence sentence, String sentenceId, GraknClient.Session session) {
        List<CoreEntityMention> entityMentions = sentence.entityMentions();

        for (CoreEntityMention entityMention : entityMentions) {
            GraknClient.Transaction writeTransaction = session.transaction().write();

            String entityMentioned = entityMention.toString();
            String entityType = entityMention.entityType();

            GraqlInsert graqlInsert = Graql.match(
                    var("s").id(sentenceId))
                    .insert(var("e").isa("entity-mention").has("value", entityMentioned).has("entity-type", entityType),
                            var("m").isa("mention").rel("mentioned-entity", "e").rel("mentioning-sentence", "s"));
            writeTransaction.execute(graqlInsert);
            writeTransaction.commit();

            writeTransaction = session.transaction().write();

            List<CoreLabel> tokens = entityMention.tokens();

            for (CoreLabel token : tokens) {
                String lemma = token.lemma();
                double startPostion = token.beginPosition();
                double endPosition = token.endPosition();

                graqlInsert = Graql.match(
                        var("e").isa("entity-mention").has("value", entityMentioned),
                        var("t").isa("token").has("lemma", lemma).has("start-position", startPostion).has("end-position", endPosition))
                        .insert(var("ect").isa("entity-containing-token").rel("containing-entity", "e").rel("contained-token", "t"));
                writeTransaction.execute(graqlInsert);
            }
            writeTransaction.commit();
        }
        System.out.println("Inserted mentions");
    }

    public static void migrateCorefChains(CoreDocument document, String abstractId, GraknClient.Session session) {
        Map<Integer, CorefChain> corefChains = document.corefChains();

        corefChains.forEach((key, value) -> {

            GraknClient.Transaction writeTx = session.transaction().write();

            double chainId = value.getChainID();
            String representativeMention = value.getRepresentativeMention().mentionSpan;

            List<CorefChain.CorefMention> mentions = value.getMentionsInTextualOrder();

            String matchQuery = "match $a id " + abstractId + ";";

            String insertQuery = "insert $r (owner: $a,";

            for (CorefChain.CorefMention mention : mentions) {
                String sentenceText = document.sentences().get(mention.sentNum - 1).text();
                String mentionedEntity = mention.mentionSpan;


                GraqlGet getMentionedEntity = Graql.match(var("e").isa("entity-mention").has("value", mentionedEntity)).get();

                List<ConceptMap> getEntity = writeTx.execute(getMentionedEntity);

                if (getEntity.isEmpty()) {
                    GraqlInsert ins = Graql.match(var("s").isa("sentence").has("text", sentenceText))
                            .insert(var("e").isa("entity-mention").has("value", mentionedEntity),
                                    var("m").isa("mention").rel("mentioned-entity", "e").rel("mentioning-sentence", "s"));

                    writeTx.execute(ins);
                    writeTx.commit();
                    writeTx = session.transaction().write();
                } else {
                    GraqlGet getMention = Graql.match(var("e").isa("entity-mention").has("value", mentionedEntity),
                            var("s").isa("sentence").has("text", sentenceText),
                            var("m").isa("mention").rel("e").rel("s")).get();

                    getEntity = writeTx.execute(getMention);

                    if (getEntity.isEmpty()) {
                        GraqlInsert ins = Graql.match(var("s").isa("sentence").has("text", sentenceText),var("e").isa("entity-mention").has("value", mentionedEntity))
                                .insert(var("m").isa("mention").rel("mentioned-entity", "e").rel("mentioning-sentence", "s"));

                        writeTx.execute(ins);
                        writeTx.commit();
                        writeTx = session.transaction().write();
                    }
                }


                matchQuery += " $" + mentionedEntity.replaceAll("[^a-zA-Z0-9]", "") + mention.mentionID
                        + " isa entity-mention, has value \"" + mentionedEntity + "\"; $" + mention.mentionID + mention.sentNum
                        + " isa sentence, has text \"" + sentenceText + "\"; $" + mention.mentionID
                        + " ($" + mentionedEntity.replaceAll("[^a-zA-Z0-9]", "") + mention.mentionID+ ", $" + mention.mentionID + mention.sentNum + ");";

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
        System.out.println("migrated coref-chains");
    }

    public static CoreDocument parseText(String text) {
        // create a document object
        CoreDocument document = new CoreDocument(text);

        // set up pipeline properties
        Properties props = new Properties();

        // set the list of annotators to run
        props.setProperty("annotators", "tokenize,ssplit,pos,lemma,ner,parse,kbp,coref,relation,sentiment");

        // set a property for annotators
        props.setProperty("coref.algorithm", "neural");
        props.setProperty("ner.useSUTime", "false");

        // build pipeline
        StanfordCoreNLP pipeline = new StanfordCoreNLP(props);

        // annnotate the document
        pipeline.annotate(document);
        return document;
    }
}