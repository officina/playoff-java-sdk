package cc.playoff.sdk;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.concurrent.TimeUnit;

import com.auth0.jwt.Algorithm;
import com.auth0.jwt.JWTSigner;
import com.google.gson.Gson;
import com.squareup.okhttp.HttpUrl;
import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;

public class PlayOffGraphQL {
	
	class GraphQLRequest {
		String query;
		Object variables;
		
		GraphQLRequest(String query, Object variables) {
			this.query= query;
			this.variables = variables;
		}
	}
	
	public String secret;
	public String endPoint;
	
	private static final MediaType MEDIA_TYPE_JSON = MediaType.parse("application/json; charset=utf-8");
	private final OkHttpClient client = new OkHttpClient();
	private final Gson gson = new Gson();
	
	/* Creates a new PlayOffGraphQL SDK client
	 * @params String secret Your user secret
	 * @params String endPoint The GraphQL API server endPoint
	 */
    public PlayOffGraphQL(String secret, String endPoint) {
    	this.secret = secret;
    	this.endPoint = endPoint;
    	client.setConnectTimeout(10, TimeUnit.SECONDS);
        client.setWriteTimeout(10, TimeUnit.SECONDS);
        client.setReadTimeout(30, TimeUnit.SECONDS);
    }
    
    /* Creates a jwt token
	 * @params String user_id the id of the user who is going to make the requests
	 * @params int expires the expiration time for the token in seconds
	 */
    public String createJWT(String user_id, int expires) {
		JWTSigner signer = new JWTSigner(this.secret);
		HashMap<String, Object> claims = new HashMap<String, Object>();
		String token = signer.sign(claims, new JWTSigner.Options().setExpirySeconds(expires).setAlgorithm(Algorithm.HS256));
		token = user_id + ':' + token;
		return token;
	}
    
    /* Makes a graphql request to the given endpoint
	 * @params String query the query you would like to make
	 * @params Object variables the input parameters for use by the query
	 */
    public Object graphql(String token, String query, Object variables)  throws IOException, PlayOff.PlayOffException {
    	HttpUrl url = HttpUrl.parse(this.endPoint + "?access_token="+token);
    	System.out.println(url);
    	String req_body = gson.toJson(new GraphQLRequest(query, variables));
    	Request request = new Request.Builder()
        .url(url)
        .post(RequestBody.create(MEDIA_TYPE_JSON, req_body))
        .build();
    	Response response = client.newCall(request).execute();
		return parseJson(response.body().string());
    }
    
    private Object parseJson(String content) throws PlayOff.PlayOffException {
    	if(content.contains("error") && content.contains("error_description")) {
    		Map<String, String> errors = (Map<String, String>) gson.fromJson(content, Object.class);
    		throw new PlayOff.PlayOffException(errors.get("error"), errors.get("error_description"));
    	}
    	else if (content.contains("errors")) {
    		Map<String, Object> response = (Map<String, Object>) gson.fromJson(content, Object.class);
    		List<Object> errors = (List<Object>)response.get("errors");
    		Map<String, Object> err = (Map<String, Object>) errors.get(0);
    		throw new PlayOff.PlayOffException((String)err.get("code"), (String)err.get("message"));
    	}
    	else {
    		return gson.fromJson(content, Object.class);
    	}
    }
}