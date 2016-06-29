package org.couchbase.devex.service;

import java.io.InputStream;

import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.stereotype.Service;

import ratpack.exec.Blocking;
import ratpack.exec.Promise;

@Service
public class SHA1Service {

	public Promise<String> getSha1Digest(InputStream is) {
		return Blocking.get(() -> DigestUtils.sha1Hex(is));
	}

}