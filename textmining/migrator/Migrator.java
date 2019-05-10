package grakn.biograkn.textmining.migrator;


import grakn.biograkn.textmining.migrator.corenlp.CoreNLP;
import grakn.biograkn.textmining.migrator.pubmedarticle.PubmedArticle;
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

    public static void migrateTextMining() {
        System.out.println("~~~~~~~~~~Starting Text Mining Migration~~~~~~~~~~");

        GraknClient graknClient = new GraknClient("127.0.0.1:48555");
        GraknClient.Session session = graknClient.session("text_mining");

        try {
            loadSchema("textmining/schema/text-mining-schema.gql", session);
            PubmedArticle.migrate(session);
            CoreNLP.migrate(session);
        } catch (Exception e) {
            e.printStackTrace();
            session.close();
        }
        session.close();
        System.out.println("~~~~~~~~~~Text Mining Migration Completed~~~~~~~~~~");
    }
}
