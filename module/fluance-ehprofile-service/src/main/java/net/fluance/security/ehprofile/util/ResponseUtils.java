package net.fluance.security.ehprofile.util;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URLConnection;

import org.springframework.http.HttpHeaders;
import org.springframework.http.InvalidMediaTypeException;
import org.springframework.http.MediaType;

public class ResponseUtils {
	
	/**
	 * Given a byte[], extract the ContentType. If the Content Type does no exist, returns an IllegalArgumentException
	 * @param thumbnail
	 * @return
	 * @throws IOException
	 */
	public static HttpHeaders setContentTypeFromArray(byte[] thumbnail) throws IOException{
		String contentType = URLConnection.guessContentTypeFromStream(new ByteArrayInputStream(thumbnail));
		HttpHeaders headers = new HttpHeaders();
		try {
			headers.setContentType(MediaType.parseMediaType(contentType));
		} catch (InvalidMediaTypeException imte){
			throw new IllegalArgumentException(imte.getMessage());
		}
		return headers;
	}
}
