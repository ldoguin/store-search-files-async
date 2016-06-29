package org.couchbase.devex;

import java.util.Map;

import org.couchbase.devex.service.BinaryStoreService;
import org.couchbase.devex.service.SearchService;

import com.couchbase.client.java.document.JsonDocument;

import ratpack.exec.Promise;
import ratpack.form.Form;
import ratpack.form.UploadedFile;
import ratpack.func.Action;
import ratpack.handlebars.Template;
import ratpack.handling.Chain;
import ratpack.rx.RxRatpack;
import ratpack.util.MultiValueMap;
import rx.Observable;

public class FileHandler implements Action<Chain> {

	@Override
	public void execute(Chain chain) throws Exception {
		chain.path(":digest", ctx -> {
			BinaryStoreService binaryStoreService = ctx.get(BinaryStoreService.class);
			String digest = ctx.getPathTokens().get("digest");
			ctx.byMethod(methodSpec -> {
				methodSpec.get(() -> {
					binaryStoreService.findFile(digest).then(response -> ctx.render(response));
				}).put(() -> {

				}).delete(() -> {
					binaryStoreService.deleteFile(digest).then(d -> ctx.getResponse().send("ok"));
				});
			});
		}).all(ctx -> {
			SearchService searchService = ctx.get(SearchService.class);
			ctx.byMethod(methodSpec -> {
				methodSpec.get(() -> {
					Observable<Map<String, Object>> files = searchService.getFiles();
					RxRatpack.promise(files).then(response -> ctx.render(
							Template.handlebarsTemplate("uploadForm", "text/html", m -> m.put("files", response))));
				}).post(() -> {
					BinaryStoreService fileStore = ctx.get(BinaryStoreService.class);
					ctx.parse(Form.class).then(form -> {
						MultiValueMap<String, UploadedFile> files = form.files();
						files.values().forEach(uploadedFile -> {
							String name = form.get("name");
							if (name == null || name.isEmpty()) {
								Observable<Map<String, Object>> storefiles = searchService.getFiles();
								RxRatpack.promise(storefiles)
										.then(response -> ctx.render(Template.handlebarsTemplate("uploadForm",
												"text/html",
												m -> m.put("files", response).put("message", "Cannot be Empty"))));
							}
							try {
								Promise<JsonDocument> doc = fileStore.storeFile(name, uploadedFile);
								doc.then(d -> ctx.redirect("file"));
							} catch (Exception e) {
								throw new RuntimeException(e);
							}
						});

					});
				});
			});
		});
	}

}
