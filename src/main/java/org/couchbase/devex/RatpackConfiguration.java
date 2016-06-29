package org.couchbase.devex;

import java.util.Collections;
import java.util.List;

import org.couchbase.devex.domain.StoredFileRenderer;
import org.springframework.context.annotation.Configuration;

import com.google.common.collect.ImmutableMap;

import ratpack.func.Action;
import ratpack.guice.BindingsSpec;
import ratpack.handlebars.HandlebarsModule;
import ratpack.handling.Chain;
import ratpack.server.BaseDir;
import ratpack.server.ServerConfigBuilder;
import ratpack.spring.config.RatpackServerCustomizer;

@Configuration
public class RatpackConfiguration implements RatpackServerCustomizer {

	@Override
	public List<Action<Chain>> getHandlers() {
		return Collections.emptyList();
	}

	@Override
	public Action<ServerConfigBuilder> getServerConfig() {
		return config -> config.baseDir(BaseDir.find())
				.props(ImmutableMap.of("server.maxContentLength", "100000000", "app.name", "Search Store File"));

	}

	@Override
	public Action<BindingsSpec> getBindings() {
		return bindingConfig -> bindingConfig.module(HandlebarsModule.class).bind(FileHandler.class)
				.bind(StoredFileRenderer.class).bind(ErrorHandlerImpl.class).bind(ClientHandlerImpl.class);
	}
}