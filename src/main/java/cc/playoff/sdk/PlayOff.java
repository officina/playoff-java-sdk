package cc.playoff.sdk;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import com.squareup.okhttp.HttpUrl.Builder;
import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;
import com.squareup.okhttp.HttpUrl;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.auth0.jwt.Algorithm;
import com.auth0.jwt.JWTSigner;

public class PlayOff {

	private String version;
	private String client_id;
	private String client_secret;
	private String type;
	private String redirect_uri;
	private String code;
	private PersistAccessToken pac;

	private static final MediaType MEDIA_TYPE_JSON = MediaType.parse("application/json; charset=utf-8");
	private final OkHttpClient client = new OkHttpClient();
	private final Gson gson = new Gson();

	public static String createJWT(String client_id, String client_secret, String player_id, String[] scopes, int expires) {
		JWTSigner signer = new JWTSigner(client_secret);
		HashMap<String, Object> claims = new HashMap<String, Object>();
		claims.put("player_id", player_id);
		claims.put("scopes", scopes);
		String token = signer.sign(claims, new JWTSigner.Options().setExpirySeconds(expires).setAlgorithm(Algorithm.HS256));
		token = client_id + ':' + token;
		return token;
	}

	/* Use this to initialize the PlayOff sdk in client credentials flow
	 * @params String client_id Your client id
	 * @params String client_secret Your client secret
	 * @params PersistAccessToken pac Your implementation to store and load the access token from a database
	 */
    public PlayOff(String client_id, String client_secret, PersistAccessToken pac, String version) {
    	this.client_id = client_id;
    	this.client_secret = client_secret;
    	this.type = "client";
    	this.pac = pac;
    	this.version = version;
    	client.setConnectTimeout(10, TimeUnit.SECONDS);
        client.setWriteTimeout(10, TimeUnit.SECONDS);
        client.setReadTimeout(30, TimeUnit.SECONDS);
    }

    public PlayOff(String client_id, String client_secret, PersistAccessToken pac) {
    	this(client_id, client_secret, pac, "v2");
    }

    /* Use this to initialize the PlayOff sdk in authorization code flow
	 * @param String client_id Your client id
	 * @params String client_secret Your client secret
	 * @params String redirect_uri The redirect_uri
	 * @params PersistAccessToken pac Your implementation to store and load the access token from a database
	 */
    public PlayOff(String client_id, String client_secret, String redirect_uri, PersistAccessToken pac) {
    	this(client_id, client_secret, redirect_uri, pac, "v2");
    }

    public PlayOff(String client_id, String client_secret, String redirect_uri, PersistAccessToken pac, String version) {
    	this.client_id = client_id;
    	this.client_secret = client_secret;
    	this.redirect_uri = redirect_uri;
    	this.type = "code";
    	this.pac = pac;
    	this.version =  version;
    }
    
    public void getAccessTokenAsync(final Callback cb) {
    	System.out.println("Getting Access Token");
    	JsonObject json = new JsonObject();
	    json.addProperty("client_id", client_id);
	    json.addProperty("client_secret", client_secret);
    	if(type.equals("client")) {
    		json.addProperty("grant_type",  "client_credentials");
    	}
    	else {
    		json.addProperty("grant_type",  "authorization_code");
    		json.addProperty("code",  code);
    		json.addProperty("redirect_uri", redirect_uri);
    	}
    	Request request = new Request.Builder()
        .url("https://playoff.cc/auth/token")
        .post(RequestBody.create(MEDIA_TYPE_JSON, json.toString()))
        .build();
    	client.newCall(request).enqueue(new com.squareup.okhttp.Callback() {
 			@Override 
 			public void onResponse(Response response) throws IOException {
				try {
					final Map<String, Object> token = (Map<String, Object>) parseJson(response.body().string());
					Long expires_at = System.currentTimeMillis() + (((Double) token.get("expires_in")).longValue() * 1000);
			  		token.remove("expires_in");
			  		token.put("expires_at", expires_at);
			  		if(pac == null) {
		    			pac = new PersistAccessToken(){
		    				@Override
		    				public void store(Map<String, Object> token) {
		    					System.out.println("Storing Access Token");
		    				}

		    				@Override
		    				public Map<String, Object> load() {
		    					return token;
		    				}

		    			};
			  		}
			  		pac.store(token);
			  		cb.onSuccess(null);
				} catch (PlayOffException e) {
					cb.onPlayOffError(e);
				}
 			}

			@Override
			public void onFailure(Request request, IOException e) {
				cb.onIOError(e);
			}
 		});
    }

    public void getAccessToken() throws IOException, PlayOffException {
    	System.out.println("Getting Access Token");
    	JsonObject json = new JsonObject();
	    json.addProperty("client_id", client_id);
	    json.addProperty("client_secret", client_secret);
    	if(type.equals("client")) {
    		json.addProperty("grant_type",  "client_credentials");
    	}
    	else {
    		json.addProperty("grant_type",  "authorization_code");
    		json.addProperty("code",  code);
    		json.addProperty("redirect_uri", redirect_uri);
    	}
    	Request request = new Request.Builder()
        .url("https://playoff.cc/auth/token")
        .post(RequestBody.create(MEDIA_TYPE_JSON, json.toString()))
        .build();
    	Response response = client.newCall(request).execute();
  		final Map<String, Object> token = (Map<String, Object>) parseJson(response.body().string());
  		Long expires_at = System.currentTimeMillis() + (((Double) token.get("expires_in")).longValue() * 1000);
  		token.remove("expires_in");
  		token.put("expires_at", expires_at);
  		if(pac == null) {
    			pac = new PersistAccessToken(){
    				@Override
    				public void store(Map<String, Object> token) {
    					System.out.println("Storing Access Token");
    				}

    				@Override
    				public Map<String, Object> load() {
    					return token;
    				}

    			};
  		}
  		pac.store(token);
    }

    /* Use this to make a request to the PlayOff API
    * @params String method The type of request ['GET', 'POST', 'PUT', 'PATCH', 'DELETE']
    * @params String route The PlayOff API route
    * @params Map<String, String> The query params for the request
    * @params Object body The data you would like to send in your POST, PUT, PATCH requests
    * @params boolean raw  Whether you would like the response to be string or a Map (Useful for images)
    */
    public Object api(String method, String route, Map<String, String> query, Object body, final boolean raw) throws IOException, PlayOffException {
    	Builder urlBuilder = new HttpUrl.Builder()
    	.scheme("https")
    	.host("api.playoff.cc")
    	.encodedPath("/"+this.version+route);
    	
    	if (query != null) {
    		for (Map.Entry<String, String> entry : query.entrySet())
        	{
    			urlBuilder.addQueryParameter(entry.getKey(), entry.getValue());
        	}
    	}
    	
    	Map<String, Object> token = null;
    	if (pac != null) {
    		token = pac.load();
    	}
    	if(pac == null || token == null ||System.currentTimeMillis() >= ((Long) token.get("expires_at"))){
    		getAccessToken();
    		token = pac.load();
    	}
    	urlBuilder.addQueryParameter("access_token", token.get("access_token").toString());
    	HttpUrl url = urlBuilder.build();
    	//System.out.println(url);
    	String req_body = "";
    	if (body != null) { 
    		req_body = gson.toJson(body);
    	}
    	Request request;
    	if(method.equalsIgnoreCase("GET")) {
    		request = new Request.Builder()
    		.url(url)
    		.build();
    	}
    	else if(method.equalsIgnoreCase("POST")) {
    		request = new Request.Builder()
    		.url(url)
    		.post(RequestBody.create(MEDIA_TYPE_JSON, req_body))
    		.build();
    	}
    	else if(method.equalsIgnoreCase("PUT")) {
    		request = new Request.Builder()
    		.url(url)
    		.put(RequestBody.create(MEDIA_TYPE_JSON, req_body))
    		.build();
    	}
    	else if(method.equalsIgnoreCase("PATCH")) {
    		request = new Request.Builder()
    		.url(url)
    		.patch(RequestBody.create(MEDIA_TYPE_JSON, req_body))
    		.build();
    	}
    	else if(method.equalsIgnoreCase("Delete")) {
    		request = new Request.Builder()
    		.url(url)
    		.delete()
    		.build();
    	}
    	else {
    		request = new Request.Builder()
    		.url(url)
    		.build();
    	}
		Response response = client.newCall(request).execute();
    	if(raw == true){
    		return response.body().bytes();
    	}
    	else {
			return parseJson(response.body().string());
    	}
    }
    
     public void apiAsync(final String method, final String route, final Map<String, String> query, final Object body, final boolean raw, final Callback cb) {
    	Map<String, Object> token = null;
     	if (pac != null) {
     		token = pac.load();
     	};
     	if(pac == null || token == null || System.currentTimeMillis() >= ((Long) token.get("expires_at"))){
     		getAccessTokenAsync(new Callback(){
				@Override
				public void onSuccess(Object data) {
					makeRequestAsync(pac.load(), method, route, query, body, raw, cb);
				}

				@Override
				public void onPlayOffError(PlayOffException e) {
					cb.onPlayOffError(e);
				}

				@Override
				public void onIOError(IOException e) {
					cb.onIOError(e);
				}
     			
     		});
     	}
     	else {
     		makeRequestAsync(token, method, route, query, body, raw, cb);
     	}
     }
     
     public void makeRequestAsync(Map<String, Object> token, final String method, String route, Map<String, String> query, final Object body, final boolean raw, final Callback cb) {
     	final Builder urlBuilder = new HttpUrl.Builder()
     	.scheme("https")
     	.host("api.playoff.cc")
     	.encodedPath("/"+this.version+route);
     	
     	if (query != null) {
     		for (Map.Entry<String, String> entry : query.entrySet())
         	{
     			urlBuilder.addQueryParameter(entry.getKey(), entry.getValue());
         	}
     	}
     	urlBuilder.addQueryParameter("access_token", token.get("access_token").toString());
     	HttpUrl url = urlBuilder.build();
     	//System.out.println(url);
     	String req_body = "";
     	if (body != null) { 
     		req_body = gson.toJson(body);
     	}
     	Request request;
     	if(method.equalsIgnoreCase("GET")) {
     		request = new Request.Builder()
     		.url(url)
     		.build();
     	}
     	else if(method.equalsIgnoreCase("POST")) {
     		request = new Request.Builder()
     		.url(url)
     		.post(RequestBody.create(MEDIA_TYPE_JSON, req_body))
     		.build();
     	}
     	else if(method.equalsIgnoreCase("PUT")) {
     		request = new Request.Builder()
     		.url(url)
     		.put(RequestBody.create(MEDIA_TYPE_JSON, req_body))
     		.build();
     	}
     	else if(method.equalsIgnoreCase("PATCH")) {
     		request = new Request.Builder()
     		.url(url)
     		.patch(RequestBody.create(MEDIA_TYPE_JSON, req_body))
     		.build();
     	}
     	else if(method.equalsIgnoreCase("Delete")) {
     		request = new Request.Builder()
     		.url(url)
     		.delete()
     		.build();
     	}
     	else {
     		request = new Request.Builder()
     		.url(url)
     		.build();
     	}
 		client.newCall(request).enqueue(new com.squareup.okhttp.Callback() {
 			@Override 
 			public void onResponse(Response response) throws IOException {
 				if(raw == true) {
 					cb.onSuccess((byte[]) response.body().bytes());
 				}
 				else {
 					try {
 						Object data = parseJson(response.body().string());
 						cb.onSuccess(data);
					} catch (PlayOffException e) {
						cb.onPlayOffError(e);
					}
 				}
 			}

			@Override
			public void onFailure(Request request, IOException e) {
				cb.onIOError(e);
			}
 		});
    }

    public Object get(String route, Map<String, String> query) throws IOException, PlayOffException {
	    return api("GET", route, query, null, false);
    }

    public byte[] getRaw(String route, Map<String, String> query) throws IOException, PlayOffException {
	    return (byte[]) api("GET", route, query, null, true);
    }

    public Object post(String route, Map<String, String> query, Object body) throws  IOException, PlayOffException {
    	return api("POST", route, query, body, false);
    }

    public Object put(String route, Map<String, String> query, Object body) throws IOException, PlayOffException {
    	return api("PUT", route, query, body, false);
    }

    public Object patch(String route, Map<String, String> query, Object body) throws IOException, PlayOffException {
    	return api("PATCH", route, query, body, false);
    }

    public Object delete(String route, Map<String, String> query) throws IOException, PlayOffException {
	    return api("DELETE", route, query, null, false);
    }
    
    public void getAsync(String route, Map<String, String> query, Callback cb) {
	    apiAsync("GET", route, query, null, false, cb);
    }

    public void getRawAsync(String route, Map<String, String> query, Callback cb) {
	    apiAsync("GET", route, query, null, true, cb);
    }

    public void postAsync(String route, Map<String, String> query, Object body, Callback cb) {
    	apiAsync("POST", route, query, body, false, cb);
    }

    public void putAsync(String route, Map<String, String> query, Object body, Callback cb) {
    	apiAsync("PUT", route, query, body, false, cb);
    }

    public void patchAsync(String route, Map<String, String> query, Object body, Callback cb) {
    	apiAsync("PATCH", route, query, body, false, cb);
    }

    public void deleteAsync(String route, Map<String, String> query, Callback cb) {
	    apiAsync("DELETE", route, query, null, false, cb);
    }

    private Object parseJson(String content) throws PlayOffException {
    	if(content.contains("error") && content.contains("error_description")) {
    		Map<String, String> errors = (Map<String, String>) gson.fromJson(content, Object.class);
    		throw new PlayOffException(errors.get("error"), errors.get("error_description"));
    	}
    	else {
    		return gson.fromJson(content, Object.class);
    	}
    }

    public String get_login_url() throws URISyntaxException {
        return "https://playoff.cc/auth?response_type=code&client_id="+client_id + "&redirect_uri="+redirect_uri;
	 }

	public void exchange_code(String code) throws  IOException, PlayOffException {
		this.code = code;
	}
	
	public static class PlayOffException extends Exception {
		private String name;
		
		 public PlayOffException(String name, String message) {
	        super(message);
	        this.name = name;
	     }
		 
		 public String getName() {
			 return name;
		 }
	}
	
	public static interface PersistAccessToken {
		public void store(Map<String, Object> token);
		public Map<String, Object> load();
	}
	
	public static interface Callback {
		void onSuccess(Object data);
		void onPlayOffError(PlayOffException e);
		void onIOError(IOException e);
	}
}
