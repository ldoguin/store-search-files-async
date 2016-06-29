package org.couchbase.devex.domain;

import java.io.File;

public class StoredFile {

	private File file;

	private StoredFileDocument storedFileDocument;

	public StoredFile(File file, StoredFileDocument storedFileDocument) {
		this.file = file;
		this.storedFileDocument = storedFileDocument;
	}

	public File getFile() {
		return file;
	}

	public StoredFileDocument getStoredFileDocument() {
		return storedFileDocument;
	}

}
