package org.couchbase.devex.service;

import static com.couchbase.client.core.time.Delay.exponential;
import static com.couchbase.client.core.time.Delay.fixed;

import java.io.File;
import java.io.FileOutputStream;
import java.util.concurrent.TimeUnit;

import org.couchbase.devex.BinaryStoreConfiguration;
import org.couchbase.devex.domain.StoredFile;
import org.couchbase.devex.domain.StoredFileDocument;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.couchbase.client.core.BackpressureException;
import com.couchbase.client.core.RequestCancelledException;
import com.couchbase.client.java.Bucket;
import com.couchbase.client.java.ReplicaMode;
import com.couchbase.client.java.document.JsonDocument;
import com.couchbase.client.java.error.TemporaryFailureException;
import com.couchbase.client.java.util.retry.RetryBuilder;

import ratpack.exec.Blocking;
import ratpack.exec.Promise;
import ratpack.form.UploadedFile;
import ratpack.func.Pair;
import ratpack.rx.RxRatpack;

@Service
public class BinaryStoreService {

	@Autowired
	private Bucket bucket;
	@Autowired
	private SHA1Service sha1Service;
	@Autowired
	private DataExtractionService dataExtractionService;
	@Autowired
	private BinaryStoreConfiguration configuration;

	public Promise<StoredFile> findFile(String digest) {
		File f = new File(configuration.getBinaryStoreRoot() + File.separator + digest);
		if (!f.exists()) {
			return null;
		}
		return RxRatpack.promiseSingle(
				bucket.async().get(digest).switchIfEmpty(bucket.async().getFromReplica(digest, ReplicaMode.ALL)).first()
						.map(doc -> new StoredFileDocument(doc)).map(sfd -> new StoredFile(f, sfd))
						.timeout(2, TimeUnit.SECONDS));
	}

	public Promise<Pair<JsonDocument, Boolean>> deleteFile(String digest) {
		Promise<JsonDocument> pDoc = RxRatpack.promiseSingle(bucket.async().remove(digest));
		Promise<Boolean> deletetFilePromise = Blocking.get(() -> {
			File f = new File(configuration.getBinaryStoreRoot() + File.separator + digest);
			if (!f.exists()) {
				throw new IllegalArgumentException("Can't delete file that does not exist");
			}
			return f.delete();
		});
		return pDoc.right(deletetFilePromise);
	}

	public Promise<JsonDocument> storeFile(String name, UploadedFile uploadedFile) throws Exception {
		Promise<JsonDocument> docPromise = sha1Service.getSha1Digest(uploadedFile.getInputStream()).flatMap(digest -> {
			return writeFile(digest, uploadedFile).flatMap(file -> {
				return dataExtractionService.extractData(file).map(jo -> {
					jo.put("binaryStoreDigest", digest);
					jo.put("type", "file");
					jo.put("binaryStoreLocation", name);
					jo.put("binaryStoreFilename", uploadedFile.getFileName());
					String mimeType = jo.getString("MIMEType");
					if ("application/pdf".equals(mimeType)) {
						Promise<String> fulltextContent = dataExtractionService.extractText(file);
						fulltextContent.then(textContent -> jo.put("fulltext", textContent));
					}
					return jo;
				}).map(metadata -> JsonDocument.create(digest, metadata));
			});
		});
		return docPromise
				.flatMap(doc -> RxRatpack.promiseSingle(bucket.async().upsert(doc).timeout(500, TimeUnit.MILLISECONDS)
						.retryWhen(RetryBuilder.anyOf(RequestCancelledException.class)
								.delay(fixed(31000, TimeUnit.MILLISECONDS)).max(100).build())
						.retryWhen(RetryBuilder.anyOf(TemporaryFailureException.class, BackpressureException.class)
								.delay(exponential(TimeUnit.MILLISECONDS, 1)).max(100).build())));

	}

	private Promise<File> writeFile(String digest, UploadedFile uploadedFile) {
		return Blocking.get(() -> {
			File file = new File(configuration.getBinaryStoreRoot() + File.separator + digest);
			FileOutputStream fos = new FileOutputStream(file);
			uploadedFile.writeTo(fos);
			fos.flush();
			fos.close();
			return file;
		});
	}
}
