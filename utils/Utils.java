package grakn.biograkn.utils;

import grakn.client.GraknClient;
import grakn.core.concept.answer.ConceptMap;
import graql.lang.query.GraqlInsert;

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

                    for (GraqlInsert insertQuery : queryList) {
                        GraknClient.Transaction writeTransaction = session.transaction().write();
                        List<ConceptMap> insertedIds = writeTransaction.execute(insertQuery);
                        writeTransaction.commit();
                    }
                    return null;
                }, executorService);

                asyncInsertions.add(asyncInsert);
            });

            CompletableFuture.allOf(asyncInsertions.toArray(new CompletableFuture[]{})).get();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
    }

    public static <T> List<List<T>> splitList(List<T> list, final int L) {
        List<List<T>> parts = new ArrayList<>();
        final int N = list.size();
        for (int i = 0; i < N; i += L) {
            parts.add(new ArrayList<T>(
                    list.subList(i, Math.min(N, i + L)))
            );
        }
        return parts;
    }
}
