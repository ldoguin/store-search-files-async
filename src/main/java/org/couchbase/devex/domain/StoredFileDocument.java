package org.couchbase.devex.domain;

import com.couchbase.client.java.document.JsonDocument;
import com.couchbase.client.java.document.json.JsonObject;

public class StoredFileDocument {

	public static String BINARY_STORE_DIGEST_PROPERTY = "binaryStoreDigest";

	public static String BINARY_STORE_LOCATION_PROPERTY = "binaryStoreLocation";

	public static String BINARY_STORE_FILENAME_PROPERTY = "binaryStoreFilename";

	public static String BINARY_STORE_METADATA_SIZE_PROPERTY = "FileSize";

	public static String BINARY_STORE_METADATA_MIMETYPE_PROPERTY = "MIMEType";

	private JsonDocument jsonDocument;
	private JsonObject content;

	private String type = "file";

	public StoredFileDocument(JsonDocument jsonDocument) {
		this.jsonDocument = jsonDocument;
		this.content = jsonDocument.content();
	}

	public String getMimeType() {
		String mimeType = content.getString(BINARY_STORE_METADATA_MIMETYPE_PROPERTY);
		if (mimeType == null) {
			mimeType = "application/octet-stream";
		}
		return mimeType;
	}

	public Integer getSize() {
		return content.getInt(BINARY_STORE_METADATA_SIZE_PROPERTY);
	}

	public String getBinaryStoreLocation() {
		return content.getString(BINARY_STORE_LOCATION_PROPERTY);
	}

	public String getBinaryStoreFilename() {
		return content.getString(BINARY_STORE_FILENAME_PROPERTY);
	}

	public String getBinaryStoreDigest() {
		return content.getString(BINARY_STORE_DIGEST_PROPERTY);
	}

	public String getType() {
		return type;
	}

	public JsonDocument getJsonDocument() {
		return jsonDocument;
	}
}
