package org.couchbase.devex;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
public class BinaryStoreConfiguration {

	@Value("${binaryStore.root:upload-dir}")
	private String binaryStoreRoot;

	public String getBinaryStoreRoot() {
		return binaryStoreRoot;
	}

}
