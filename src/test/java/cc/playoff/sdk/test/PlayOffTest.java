package cc.playoff.sdk.test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import cc.playoff.sdk.PlayOff;
import cc.playoff.sdk.PlayOff.Callback;
import cc.playoff.sdk.PlayOff.PersistAccessToken;
import cc.playoff.sdk.PlayOff.PlayOffException;
import cc.playoff.sdk.PlayOffGraphQL;

import java.util.prefs.Preferences;

public class PlayOffTest {

	public static Preferences prefs;

	public static void testv1() {
		PlayOff pl = null;
		HashMap<String, String> player_id = new HashMap<String, String>();
		player_id.put("player_id", "student1");
		try {
			pl = new PlayOff(
				"Zjc0MWU0N2MtODkzNS00ZWNmLWEwNmYtY2M1MGMxNGQ1YmQ4",
				"YzllYTE5NDQtNDMwMC00YTdkLWFiM2MtNTg0Y2ZkOThjYTZkMGIyNWVlNDAtNGJiMC0xMWU0LWI2NGEtYjlmMmFkYTdjOTI3",
				null,
				"v1"
			);
			Map<String, Object> players = (Map<String, Object>) pl.get("/game/players", null);
			System.out.println(players.get("total"));
			ArrayList data = (ArrayList) players.get("data");
			Map<String, Object> player = (Map<String, Object>) data.get(0);
			System.out.println(player.get("id"));
			System.out.println(player.get("alias"));

			Map<String, Object> student1 = (Map<String, Object>) pl.get("/player", player_id);
			System.out.println(student1.get("id"));
			System.out.println(student1.get("alias"));


			pl.get ("/definitions/processes", player_id);
			pl.get ("/definitions/teams", player_id);
			pl.get ("/processes", player_id);
			pl.get ("/teams", player_id);

			Map<String, String> new_process = (Map<String, String>) pl.post ("/definitions/processes/module1", player_id, null);

			HashMap<String, String> body = new HashMap<String, String>();
			body.put("name", "patched_process");
			body.put("access", "PUBLIC");
			pl.patch ("/processes/" + new_process.get("id"), player_id, body);

			pl.delete("/processes/" + new_process.get("id"), player_id);

			pl.getRaw("/player", player_id);

		} catch (IOException e) {
			e.printStackTrace();
		} catch (PlayOffException e) {
			e.printStackTrace();
		}
	}

	public static void testv2() {
		PlayOff pl = null;
		HashMap<String, String> player_id = new HashMap<String, String>();
		player_id.put("player_id", "student1");
		try {
			pl = new PlayOff(
				"Zjc0MWU0N2MtODkzNS00ZWNmLWEwNmYtY2M1MGMxNGQ1YmQ4",
				"YzllYTE5NDQtNDMwMC00YTdkLWFiM2MtNTg0Y2ZkOThjYTZkMGIyNWVlNDAtNGJiMC0xMWU0LWI2NGEtYjlmMmFkYTdjOTI3",
				null,
				"v2"
			);
			Map<String, Object> players = (Map<String, Object>) pl.get("/runtime/players", player_id);
			System.out.println(players.get("total"));
			ArrayList data = (ArrayList) players.get("data");
			Map<String, Object> player = (Map<String, Object>) data.get(0);
			System.out.println(player.get("id"));
			System.out.println(player.get("alias"));

			Map<String, Object> student1 = (Map<String, Object>) pl.get("/runtime/player", player_id);
			System.out.println(student1.get("id"));
			System.out.println(student1.get("alias"));


			pl.get ("/runtime/definitions/processes", player_id);
			pl.get ("/runtime/definitions/teams", player_id);
			pl.get ("/runtime/processes", player_id);
			pl.get ("/runtime/teams", player_id);

			HashMap<String, String> body = new HashMap<String, String>();
			body.put("definition", "module1");

			Map<String, String> new_process = (Map<String, String>) pl.post ("/runtime/processes", player_id, body);

			body = new HashMap<String, String>();
			body.put("name", "patched_process");
			body.put("access", "PUBLIC");
			pl.patch ("/runtime/processes/" + new_process.get("id"), player_id, body);

			pl.delete("/runtime/processes/" + new_process.get("id"), player_id);

			System.out.println(pl.getRaw("/runtime/player", player_id));
			System.out.println(new String(pl.getRaw("/runtime/player", player_id), "UTF-8"));
		} catch (IOException e) {
			e.printStackTrace();
		} catch (PlayOffException e) {
			e.printStackTrace();
		}
	}

	public static void testExpiresAt() {
    try {
    	final HashMap<String, Object> access_token = new HashMap<String, Object>();
    	PlayOff pl = new PlayOff(
	        "Zjc0MWU0N2MtODkzNS00ZWNmLWEwNmYtY2M1MGMxNGQ1YmQ4",
	        "YzllYTE5NDQtNDMwMC00YTdkLWFiM2MtNTg0Y2ZkOThjYTZkMGIyNWVlNDAtNGJiMC0xMWU0LWI2NGEtYjlmMmFkYTdjOTI3",
	        new PersistAccessToken(){
	          @Override
	          public void store(Map<String, Object> token) {
	              System.out.println("Storing Access Token");
	              access_token.put("expires_at", token.get("expires_at"));
	              access_token.put("access_token", token.get("access_token"));
	          }
	          @Override
	          public Map<String, Object> load() {
	              System.out.println("Loading Access Token");
	              System.out.println("Current Time: "+System.currentTimeMillis());
	              System.out.println("Expires At: "+access_token.get("expires_at"));
	              //access_token.put("expires_at", System.currentTimeMillis() - 100);
	              if (access_token.get("expires_at") == null) {
	            	  return null;
	              }
	              else {
	            	  return access_token;
	              }
	          }
	        },
	        "playoffgenerali.it"
      );
      HashMap<String, String> player_id = new HashMap<String, String>();
  	  player_id.put("player_id", "student1");
  	  pl.get("/runtime/players", player_id);
  	  pl.get("/runtime/players", player_id);
  	  pl.get("/runtime/players", player_id);
    } catch (IOException e) {
      e.printStackTrace();
    } catch (PlayOffException e) {
      e.printStackTrace();
    }
  }

  	public static void testWrongRoute() {
	  try {
		  PlayOff pl = new PlayOff(
			"Zjc0MWU0N2MtODkzNS00ZWNmLWEwNmYtY2M1MGMxNGQ1YmQ4",
		    "YzllYTE5NDQtNDMwMC00YTdkLWFiM2MtNTg0Y2ZkOThjYTZkMGIyNWVlNDAtNGJiMC0xMWU0LWI2NGEtYjlmMmFkYTdjOTI3",
			null,
			"v2"
		  );
		  HashMap<String, String> player_id = new HashMap<String, String>();
		  player_id.put("player_id", "student1");
		  pl.get ("/unkown", player_id);
		} catch (IOException e) {
			e.printStackTrace();
		} catch (PlayOffException e) {
			System.out.println(e.getName());
			System.out.println(e.getMessage());
		}
  }

   public static void testJWT() {
      String[] scopes = {"player.runtime.read", "player.runtime.write"};
      String token = PlayOff.createJWT("MWYwZGYzNTYtZGIxNy00OGM5LWExZGMtZjBjYTFiN2QxMTlh",  "NmM2YTcxOGYtNGE2ZC00ZDdhLTkyODQtYTIwZTE4ZDc5YWNjNWFiNzBiYjAtZmZiMC0xMWU0LTg5YzctYzc5NWNiNzA1Y2E4", "student1", scopes , 3600);
      System.out.println(token);
   }
   
   public static void testAsync() {
	    PlayOff pl = null;
		pl = new PlayOff(
		    "Zjc0MWU0N2MtODkzNS00ZWNmLWEwNmYtY2M1MGMxNGQ1YmQ4",
			"YzllYTE5NDQtNDMwMC00YTdkLWFiM2MtNTg0Y2ZkOThjYTZkMGIyNWVlNDAtNGJiMC0xMWU0LWI2NGEtYjlmMmFkYTdjOTI3",
			null,
			"v2"
		);
	   HashMap<String, String> player_id = new HashMap<String, String>();
	   player_id.put("player_id", "student1");
       pl.getAsync("/unkown", player_id, new Callback() {
		@Override
		public void onSuccess(Object data) {
		}
		@Override
		public void onPlayOffError(PlayOffException e) {
			System.out.println(e.getName());
			System.out.println(e.getMessage());
		}
		@Override
		public void onIOError(IOException e) {	
		}
       });
       pl.getAsync("/runtime/player", player_id, new Callback() {
   		@Override
   		public void onSuccess(Object data) {
   			Map<String, Object> student1 = (Map<String, Object>) data;
			System.out.println(student1.get("id"));
			System.out.println(student1.get("alias"));
			System.out.println("All Tests Finished");
			System.exit(0);
   		}
   		@Override
   		public void onPlayOffError(PlayOffException e) {
   			System.out.println("Last Test Failed");
   		}
   		@Override
   		public void onIOError(IOException e) {	
   			System.out.println("Last Test Failed");
   		}
      });
   }
   
   public static void testv3() throws IOException, PlayOffException {
	   PlayOffGraphQL pl = null;
	   String token = "";
	   String query =  "query K {"
		   		+ " root {"
		   		+ "    games { "
		   		+ "      edges { "
		   		+ "        node {"
		   		+ "          id"
		   		+ "          name"
		   		+ "        }"
		   		+ "      }"
		   		+ "    }"
		   		+ " }}";
	   pl = new PlayOffGraphQL(
			   "wrong_secret",
			   "http://localhost:3212/graphql"
	    );
	   token = pl.createJWT("wrong_user_id", 3600);
	   try {
		   Map<String, Object> games = (Map<String, Object>) pl.graphql(token, query, null);
		   System.out.println(games);
	   } catch(PlayOffException e) {
		   System.out.println(e);
	   }
	   
	   pl = new PlayOffGraphQL(
			   "wrong_secret",
			   "http://localhost:3212/graphql"
	    );
	   token = pl.createJWT("db55271e-1e5e-11e6-8369-201a06e4e14a", 3600);
	   try {
		   Map<String, Object> games = (Map<String, Object>) pl.graphql(token, query, null);
		   System.out.println(games);
	   } catch(PlayOffException e) {
		   System.out.println(e);
	   }
	   
	   pl = new PlayOffGraphQL(
			   "OThjMTE5M2QtZTViYS00YzJjLTg5ZDctNzg3ODg2YzE0Mjcx",
			   "http://localhost:3212/graphql"
	    );
	   token = pl.createJWT("db55271e-1e5e-11e6-8369-201a06e4e14a", 3600);
	   try {
		   Map<String, Object> games = (Map<String, Object>) pl.graphql(token, query, null);
		   System.out.println(games);
	   } catch(PlayOffException e) {
		   System.out.println(e);
	   }
	   
	   try {
	   pl = new PlayOffGraphQL(
			   "OThjMTE5M2QtZTViYS00YzJjLTg5ZDctNzg3ODg2YzE0Mjcx",
			   "http://localhost:3212/graphql"
	    );
	   token = pl.createJWT("db55271e-1e5e-11e6-8369-201a06e4e14a", 3600);
	   Map<String, Object> input =  new HashMap<String, Object>();
       input.put("input", "{\"id\":\"test\",\"type\":\"Game\"}");
	   Map<String, Object> games = (Map<String, Object>) pl.graphql(token, ""
			+ "query Game($input:PLGlobalID!) {"
	        + "  node(id: $input) {"
	        + "    ... on Game {"
	        + "      id "
	        + "      name "
	        + "    } "
	        + "  } "
	        + " }"
	   , input);
	   System.out.println(games);
	   } catch(PlayOffException e) {
		   System.out.println(e);
	   }
   }

    public static void main(String[] args) throws IOException, PlayOffException {
//		testWrongRoute();
//		testv1();
//		testv2();
//		testExpiresAt();
//		testJWT();
//		testAsync();
    	testv3();
	}

}
