package ext;

import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;


public class ParseUrlQuery {
	
	public static HashMap<String,String> parseUrl(String url) throws URISyntaxException {
		List<NameValuePair> params = URLEncodedUtils.parse(new URI(url), "UTF-8");
		HashMap<String, String> mapped = (HashMap) params.stream().collect(
		        Collectors.toMap(NameValuePair::getName, NameValuePair::getValue));
		return mapped;
	}
}
