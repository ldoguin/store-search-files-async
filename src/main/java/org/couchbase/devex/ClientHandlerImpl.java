package org.couchbase.devex;

import ratpack.error.ClientErrorHandler;
import ratpack.handlebars.Template;
import ratpack.handling.Context;

public class ClientHandlerImpl implements ClientErrorHandler {

	@Override
	public void error(Context context, int statusCode) throws Exception {
		context.render(Template.handlebarsTemplate("error", "text/html", m -> m.put("message", "Error " + statusCode)));
	}

}
