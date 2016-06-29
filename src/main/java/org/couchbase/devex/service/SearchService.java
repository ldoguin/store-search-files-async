package org.couchbase.devex.service;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.couchbase.client.java.Bucket;
import com.couchbase.client.java.query.AsyncN1qlQueryResult;
import com.couchbase.client.java.query.N1qlQuery;
import com.couchbase.client.java.query.consistency.ScanConsistency;
import com.couchbase.client.java.search.SearchQuery;
import com.couchbase.client.java.search.queries.MatchQuery;

import rx.Observable;

@Service
public class SearchService {

	@Autowired
	private Bucket bucket;

	public Observable<Map<String, Object>> getFiles() {
		N1qlQuery query = N1qlQuery
				.simple("SELECT binaryStoreLocation, binaryStoreDigest FROM `default` WHERE type = 'file'");
		query.params().consistency(ScanConsistency.STATEMENT_PLUS);
		return bucket.async().query(query).flatMap(AsyncN1qlQueryResult::rows).map(r -> r.value().toMap());
	}

	public Observable<Map<String, Object>> searchN1QLFiles(String whereClause) {
		N1qlQuery query = N1qlQuery.simple(
				"SELECT binaryStoreLocation, binaryStoreDigest FROM `default` WHERE type= 'file' " + whereClause);
		query.params().consistency(ScanConsistency.STATEMENT_PLUS);
		return bucket.async().query(query).flatMap(AsyncN1qlQueryResult::rows).map(r -> r.value().toMap());
	}

	public Observable<Map<String, Object>> searchFulltextFiles(String term) {
		MatchQuery ftq = SearchQuery.match(term).fuzziness(2);
		SearchQuery sq = new SearchQuery("file_fulltext", ftq);
		sq.fields("binaryStoreDigest", "binaryStoreLocation");
		return bucket.async().query(sq).flatMap(res -> res.hits()).doOnNext(next -> System.out.println(next))
				.map(row -> {
					Map<String, Object> m = new HashMap<String, Object>();
					m.put("binaryStoreDigest", row.fields().get("binaryStoreDigest"));
					m.put("binaryStoreLocation", row.fields().get("binaryStoreLocation"));
					m.put("fragment", row.fragments().get("fulltext"));
					return m;
				});
	}

}
