package org.couchbase.devex;

import java.io.File;
import java.util.Map;

import org.couchbase.devex.service.SearchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import ratpack.form.Form;
import ratpack.func.Action;
import ratpack.handlebars.Template;
import ratpack.handling.Chain;
import ratpack.rx.RxRatpack;
import ratpack.spring.config.EnableRatpack;
import rx.Observable;

@SpringBootApplication
@EnableRatpack
public class Application {

	@Autowired
	BinaryStoreConfiguration configuration;

	@Bean
	CommandLineRunner init() {
		return (String[] args) -> {
			new File(configuration.getBinaryStoreRoot()).mkdirs();
		};
	}

	public static void main(String... args) throws Exception {
		SpringApplication.run(Application.class, args);
	}

	@Bean
	public Action<Chain> home() {
		return chain -> chain.prefix("file", FileHandler.class).post("fulltext", ctx -> {
			ctx.parse(Form.class).then(form -> {
				String queryString = form.get("queryString");
				SearchService searchService = ctx.get(SearchService.class);
				Observable<Map<String, Object>> files = searchService.searchFulltextFiles(queryString);
				RxRatpack.promise(files).then(response -> ctx
						.render(Template.handlebarsTemplate("uploadForm", "text/html", m -> m.put("files", response))));
			});
		}).post("n1ql", ctx -> {
			ctx.parse(Form.class).then(form -> {
				String queryString = form.get("queryString");
				SearchService searchService = ctx.get(SearchService.class);
				Observable<Map<String, Object>> files = searchService.searchN1QLFiles(queryString);
				RxRatpack.promise(files).then(response -> ctx
						.render(Template.handlebarsTemplate("uploadForm", "text/html", m -> m.put("files", response))));
			});
		});
	}
}