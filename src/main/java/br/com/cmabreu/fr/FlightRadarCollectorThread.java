package br.com.cmabreu.fr;


import java.util.Arrays;
import java.util.Iterator;

import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import br.com.cmabreu.services.FlightRadarAircraftManager;

/*
{
"full_count": 3454,
"version": 4,
"2458aaaa": [
	"E48C4D",
	-23.263,
	-46.282,
	22,
	9100,
	299,
	"0000",
	"F-SBBP2",
	"A320",
	"PR-MYV",
	1586575676,
	"GRU",
	"THE",
	"LA4650",
	0,
	2176,
	"TAM4650",
	0,
	"LAN"
],
"stats": {}
}

*/

public class FlightRadarCollectorThread implements Runnable {
    private boolean running;
	private Logger logger = LoggerFactory.getLogger( FlightRadarCollectorThread.class );
	private FlightRadarAircraftManager manager;
	
	public void finish() {
		this.running = false;
	}
	
    public FlightRadarCollectorThread( ) {
    	logger.info("Coletor Iniciado");
    	this.running = true;
    	this.manager = FlightRadarAircraftManager.getInstance();
    }  
    
    public void run() {

    	if( !this.running ) {
    		return;
    	}
    	
    	System.out.println("Coletando...");
    	
    	try {
    		// Coleta dados da WEB.
    		// Acessa o FlightRadar e pega as aeronaves
    		String aircrafts = getAircrafts();
    		
    		JSONObject json = new JSONObject( aircrafts );
    		int count = 0;
            Iterator<String> itr1 = json.keys(); 
            while (itr1.hasNext()) { 
                String pair = itr1.next();
                
                Object oo = json.get( pair );
                if ( oo instanceof JSONArray ) {
                	manager.updateAircraft( (JSONArray)oo );
                	count++;	
                	
                }
            }         		
    		
    		logger.info( count + " aeronaves coletadas.");
    		
    	} catch( Exception se ) {
    		se.printStackTrace();
    		logger.error( se.getMessage() );
    	}
        	
        
        
    }  
    

	private String getAircrafts() {
		RestTemplate restTemplate = new RestTemplate();
		String responseBody;
		String url = "https://data-live.flightradar24.com/zones/fcgi/feed.js?bounds=-10,-24,-45,-30&faa=1&mlat=1&flarm=1&adsb=1&gnd=0&air=1&vehicles=1&estimated=1&maxage=1000&gliders=1&stats=1";
		try {
			HttpHeaders headers = new HttpHeaders();
            headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
            headers.add("user-agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/54.0.2840.99 Safari/537.36");
            HttpEntity<String> entity = new HttpEntity<String>("parameters", headers);
			
			
            ResponseEntity<String> result = restTemplate.exchange( url , HttpMethod.GET, entity, String.class);
			responseBody = result.getBody().toString();
		
			
		} catch (HttpClientErrorException e) {
		    responseBody = e.getResponseBodyAsString();
		}	
		return responseBody;
	}
	

		
  
}
