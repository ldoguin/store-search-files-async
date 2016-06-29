package org.couchbase.devex.domain;

import ratpack.handling.Context;
import ratpack.http.internal.HttpHeaderConstants;
import ratpack.render.RendererSupport;

public class StoredFileRenderer extends RendererSupport<StoredFile> {

	@Override
	public void render(Context context, StoredFile storedFile) throws Exception {
		StoredFileDocument doc = storedFile.getStoredFileDocument();
		String mimeType = doc.getMimeType();
		String fileName = String.format("inline; filename=\"" + doc.getBinaryStoreFilename() + "\"");
		Integer size = doc.getSize();
		context.getResponse().contentType(mimeType).getHeaders().set(HttpHeaderConstants.CONTENT_LENGTH, size)
				.set("Content-Disposition", fileName);
		context.getResponse().sendFile(storedFile.getFile().toPath());
	}

}
