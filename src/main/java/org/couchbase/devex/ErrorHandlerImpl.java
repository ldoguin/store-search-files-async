package org.couchbase.devex;

import ratpack.error.ServerErrorHandler;
import ratpack.handlebars.Template;
import ratpack.handling.Context;

public class ErrorHandlerImpl implements ServerErrorHandler {

	@Override
	public void error(Context context, Throwable throwable) throws Exception {
		context.render(
				Template.handlebarsTemplate("error", "text/html", m -> m.put("message", throwable.getMessage())));
	}

}
