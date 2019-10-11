package grakn.biograkn.textmining.migrator.corenlp;

import edu.stanford.nlp.coref.data.CorefChain;
import edu.stanford.nlp.ie.crf.CRFClassifier;
import edu.stanford.nlp.ie.util.RelationTriple;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.CoreDocument;
import edu.stanford.nlp.pipeline.CoreEntityMention;
import edu.stanford.nlp.pipeline.CoreSentence;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.sequences.SeqClassifierFlags;
import edu.stanford.nlp.util.StringUtils;
import grakn.client.GraknClient;
import grakn.client.answer.ConceptMap;
import graql.lang.Graql;
import graql.lang.query.GraqlDefine;
import graql.lang.query.GraqlGet;
import graql.lang.query.GraqlInsert;

import java.util.List;
import java.util.Map;
import java.util.Properties;

import static graql.lang.Graql.type;
import static graql.lang.Graql.var;

@SuppressWarnings("Duplicates")
public class CoreNLP {

    public static void migrate(GraknClient.Session session) {


        trainModel("textmining/dataset/model", "textmining/dataset/properties.prop", "textmining/dataset/training_file.txt");

        GraknClient.Transaction readTransaction = session.transaction().read();

        GraqlGet getAbstractsQuery = Graql.match(var("a").isa("annotated-abstract")).get();

        List<ConceptMap> abstracts = readTransaction.execute(getAbstractsQuery);

        for (ConceptMap conceptMap : abstracts) {

            String abstractId = conceptMap.get("a").id().toString();
            String abstractText = conceptMap.get("a").asAttribute().value().toString();

            readTransaction.close();

            CoreDocument document = mineText(abstractText);

            migrateSentences(document, abstractId, session);

            migrateCorefChains(document, abstractId, session);
        }
    }

    private static void trainModel(String modelOutPath, String prop, String trainingFilepath) {
        Properties props = StringUtils.propFileToProperties(prop);
        props.setProperty("serializeTo", modelOutPath);
        props.setProperty("trainFile", trainingFilepath);

        SeqClassifierFlags flags = new SeqClassifierFlags(props);
        CRFClassifier<CoreLabel> crf = new CRFClassifier<>(flags);
        crf.train();
        crf.serializeClassifier(modelOutPath);
    }

    private static CoreDocument mineText(String text) {
        // create a document object
        CoreDocument document = new CoreDocument(text);

        // set up pipeline properties
        Properties props = new Properties();

        // set the list of annotators to run
        props.setProperty("annotators", "tokenize,ssplit,pos,lemma,ner,parse,kbp,coref,relation,sentiment");

        // set a property for annotators
        props.setProperty("coref.algorithm", "neural");
        props.setProperty("ner.useSUTime", "false");
        props.setProperty("ner.model", "textmining/dataset/model");
        props.setProperty("ner.applyFineGrained", "false");
        props.setProperty("ner.applyNumericClassifiers", "false");

        // build pipeline
        StanfordCoreNLP pipeline = new StanfordCoreNLP(props);

        // annnotate the document
        pipeline.annotate(document);
        return document;
    }

    private static void migrateSentences(CoreDocument document, String abstractId, GraknClient.Session session) {
        List<CoreSentence> sentences = document.sentences();

        for (CoreSentence sentence : sentences) {

            GraknClient.Transaction writeTransaction = session.transaction().write();

            GraqlInsert graqlInsert = Graql.insert(var("s").isa("sentence").has("text", sentence.text()).has("sentiment", sentence.sentiment()));

            String insertedSentenceId = writeTransaction.execute(graqlInsert).get(0).get("s").id().toString();

            graqlInsert = Graql.match(
                    var("s").id(insertedSentenceId),
                    var("a").id(abstractId))
                    .insert(var("acs").isa("abstract-containing-sentence").rel("contained-sentence", "s").rel("abstract-container", "a"));

            List<ConceptMap> insertedIds = writeTransaction.execute(graqlInsert);

            if (insertedIds.isEmpty()) {
                throw new IllegalStateException("abstract-containing-sentence insertion failed");
            }

            writeTransaction.commit();

            migrateTokens(sentence, insertedSentenceId, session);

            migrateMinedRelationsDummy(session);

            migrateMentions(sentence, insertedSentenceId, session, abstractId);
        }
        System.out.println("inserted sentences");
    }

    private static void migrateTokens(CoreSentence sentence, String sentenceId, GraknClient.Session session) {
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

            List<ConceptMap> insertedIds = writeTransaction.execute(graqlInsert);

            if (insertedIds.isEmpty()) {
                throw new IllegalStateException("sentence-containing-token insertion failed");
            }
        }
        writeTransaction.commit();
        System.out.println("Inserted tokens");
    }

    // To be used for a trained KBPAnnotator
    private static void migrateMinedRelations(CoreSentence sentence, String sentenceId, GraknClient.Session session) {
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
            List<ConceptMap> insertedIds = writeTransaction.execute(graqlInsert);

            if (insertedIds.isEmpty()) {
                throw new IllegalStateException("mined-relation insertion failed");
            }
        }
        writeTransaction.commit();
        System.out.println("Inserted mined-relations");
    }

    private static void migrateMinedRelationsDummy(GraknClient.Session session) {
        GraknClient.Transaction writeTransaction = session.transaction().write();

        String query = "match $s isa sentence, has text 'Thus, dabrafenib plus trametinib provides an important treatment option for patients with BRAF (V600) mutation-positive unresectable or metastatic melanoma.'; " +
                "$subjectToken isa token, has lemma 'melanoma', has start-position 1720.0, has end-position 1728.0; " +
                "$objectToken1 isa token, has lemma 'trametinib', has start-position 1595.0, has end-position 1605.0; " +
                "$objectToken2 isa token, has lemma 'dabrafenib', has start-position 1579.0, has end-position 1589.0; " +
                "insert $r (mentioning-sentence: $s, subject: $subjectToken, object: $objectToken1, object: $objectToken2) isa mined-relation, has confidence 1.0, has relation-type 'treatment';";

        GraqlInsert graqlInsert = Graql.parse(query);
        List<ConceptMap> insertedIds = writeTransaction.execute(graqlInsert);

        writeTransaction.commit();
        System.out.println("Inserted mined-relations");
    }

    private static void migrateMentions(CoreSentence sentence, String sentenceId, GraknClient.Session session, String abstractId) {
        List<CoreEntityMention> entityMentions = sentence.entityMentions();

        for (CoreEntityMention entityMention : entityMentions) {
            GraknClient.Transaction writeTransaction = session.transaction().write();

            String entityMentioned = entityMention.toString();
            String entityType = entityMention.entityType();

            GraqlInsert graqlInsert = Graql.match(
                    var("s").id(sentenceId))
                    .insert(var("e").isa("entity-mention").has("value", entityMentioned).has("entity-type", entityType),
                            var("m").isa("mention").rel("mentioned-entity", "e").rel("mentioning-sentence", "s"));

            List<ConceptMap> insertedIds = writeTransaction.execute(graqlInsert);

            writeTransaction.commit();

            if (insertedIds.isEmpty()) {
                throw new IllegalStateException("entity-mention and mention insertion failed");
            }
            defineAndMigrateType(entityMentioned, entityType, session, abstractId);

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

                insertedIds = writeTransaction.execute(graqlInsert);

                if (insertedIds.isEmpty()) {
                    throw new IllegalStateException("entity-containing-token insertion failed");
                }
            }
            writeTransaction.commit();
        }
        System.out.println("Inserted mentions");
    }

    private static void defineAndMigrateType(String entityMentioned, String entityType, GraknClient.Session session, String abstractId) {
        GraknClient.Transaction writeTransaction = session.transaction().write();


        GraqlGet getMentionedEntityType = Graql.match(var("x").type(entityType)).get();

        try {
            List<ConceptMap> getEntityType = writeTransaction.execute(getMentionedEntityType);
        } catch (Exception e) {
            writeTransaction = session.transaction().write();
            GraqlDefine defineType;
            defineType = Graql.define(type(entityType).sub("entity").has("value").plays("extracted-entity"));
            writeTransaction.execute(defineType);
        }

        GraqlGet getMentionedEntity = Graql.match(var("x").isa(entityType).has("value", entityMentioned)).get();

        List<ConceptMap> getEntity = writeTransaction.execute(getMentionedEntity);

        if (getEntity.isEmpty()) {
            GraqlInsert graqlInsert = Graql.insert(var("e").isa(entityType).has("value", entityMentioned));
            writeTransaction.execute(graqlInsert);
        }

        getMentionedEntity = Graql.match(
                var("e").isa(entityType).has("value", entityMentioned),
                var("p").isa("pubmed-article").has("annotated-abstract", var("a")),
                var("a").id(abstractId),
                var("ee").isa("entity-extraction").rel("extracted-entity", "e").rel("mined-text", "p")).get();

        getEntity = writeTransaction.execute(getMentionedEntity);

        if (getEntity.isEmpty()) {

            GraqlInsert insert = Graql.match(
                    var("e").isa(entityType).has("value", entityMentioned),
                    var("p").isa("pubmed-article").has("annotated-abstract", var("a")),
                    var("a").id(abstractId))
                    .insert(var("ee").isa("entity-extraction").rel("extracted-entity", "e").rel("mined-text", "p"));
            List<ConceptMap> insertedIds = writeTransaction.execute(insert);
            if (insertedIds.isEmpty()) {
                throw new IllegalStateException("entity-extraction insertion failed");
            }
        }
        writeTransaction.commit();
    }

    private static void migrateCorefChains(CoreDocument document, String abstractId, GraknClient.Session session) {
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

                    List<ConceptMap> insertedIds = writeTx.execute(ins);

                    if (insertedIds.isEmpty()) {
                        throw new IllegalStateException("entity-mention and mention insertion failed in coref-chain");
                    }

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

                        List<ConceptMap> insertedIds = writeTx.execute(ins);

                        if (insertedIds.isEmpty()) {
                            throw new IllegalStateException("mention insertion failed in coref-chain");
                        }

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

            List<ConceptMap> insertedIds = writeTx.execute(insert);

            writeTx.commit();
        });
        System.out.println("migrated coref-chains");
    }
}
