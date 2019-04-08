package grakn.biograkn.textmining.migrator;


import grakn.biograkn.textmining.migrator.corenlp.CoreNLP;
import grakn.biograkn.textmining.migrator.pubmedarticle.PubmedArticle;
import grakn.core.client.GraknClient;
import graql.lang.Graql;
import graql.lang.query.GraqlQuery;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

@SuppressWarnings("Duplicates")
public class Migrator {

    public static void migrateTextMining() {
        System.out.println("~~~~~~~~~~Starting Text Mining Migration~~~~~~~~~~");

        GraknClient graknClient = new GraknClient("127.0.0.1:48555");
        GraknClient.Session session = graknClient.session("text_mining");

        try {
            loadSchema(session);
            PubmedArticle.migrate(session);
            CoreNLP.migrate(session);
        } catch (Exception e) {
            e.printStackTrace();
            session.close();
        }
        session.close();
        System.out.println("~~~~~~~~~~Text Mining Migration Completed~~~~~~~~~~");
    }

    private static void loadSchema(GraknClient.Session session) {
        GraknClient.Transaction transaction = session.transaction().write();

        try {
            byte[] encoded = Files.readAllBytes(Paths.get("textmining/schema/text-mining-schema.gql"));
            String query = new String(encoded, StandardCharsets.UTF_8);
            transaction.execute((GraqlQuery) Graql.parse(query));
            transaction.commit();
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("-----text mining schema loaded-----");
    }
}
