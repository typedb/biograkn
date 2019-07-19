package grakn.biograkn.utils;

import grakn.client.GraknClient;
import grakn.core.concept.answer.ConceptMap;
import graql.lang.Graql;
import graql.lang.query.GraqlInsert;
import graql.lang.query.GraqlQuery;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Utils {

    public static void executeQueriesConcurrently(GraknClient.Session session, List<GraqlInsert>  insertQueries) {
        try {
            List<List<GraqlInsert>> queryLists = splitList(insertQueries, 8);

            ExecutorService executorService = Executors.newFixedThreadPool(8);

            List<CompletableFuture<Void>> asyncInsertions = new ArrayList<>();

            queryLists.forEach((queryList) -> {
                CompletableFuture<Void> asyncInsert = CompletableFuture.supplyAsync(() -> {

                    int counter = 0;
                    GraknClient.Transaction writeTransaction = session.transaction().write();

                    for (GraqlInsert insertQuery : queryList) {
                        if (counter % 300 == 0) {
                            writeTransaction.commit();
                            writeTransaction = session.transaction().write();
                            System.out.print('.');
                        }
                        List<ConceptMap> insertedIds = writeTransaction.execute(insertQuery);
                        counter++;
                    }
                    writeTransaction.commit();
                    return null;
                }, executorService);

                asyncInsertions.add(asyncInsert);
            });

            CompletableFuture.allOf(asyncInsertions.toArray(new CompletableFuture[]{})).get();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
    }

    private static <T> List<List<T>> splitList(List<T> list, final int L) {
        List<List<T>> parts = new ArrayList<>();
        final int N = list.size();
        for (int i = 0; i < N; i += L) {
            parts.add(new ArrayList<T>(
                    list.subList(i, Math.min(N, i + L)))
            );
        }
        return parts;
    }

    public static void loadSchema(String path, GraknClient.Session session) {
        System.out.print("\tMigrating Schema");

        GraknClient.Transaction transaction = session.transaction().write();

        try {
            byte[] encoded = Files.readAllBytes(Paths.get(path));
            String query = new String(encoded, StandardCharsets.UTF_8);
            transaction.execute((GraqlQuery) Graql.parse(query));
            transaction.commit();
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println(" - [DONE]");
    }
}
