package br.com.cmabreu.fr;


import java.util.Iterator;

import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import br.com.cmabreu.models.FlightRadarAircraft;

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
	
	public void finish() {
		this.running = false;
	}
	
    public FlightRadarCollectorThread( ) {
    	logger.info("Coletor Iniciado");
    	//
    }  
    
    public void run() {
        this.running = true;

        while ( this.running ) {
        	
        	try {
        		// Coleta dados da WEB.
        		// Acessa o FlightRadar e pega as aeronaves
        		String aircrafts = getAircrafts();
        		
        		JSONObject json = new JSONObject( aircrafts );
        		
                Iterator<String> itr1 = json.keys(); 
                while (itr1.hasNext()) { 
                    String pair = itr1.next(); 
                    Object oo = json.get( pair );
                    if ( oo instanceof JSONArray ) {
                    	JSONArray frAircraft = (JSONArray)oo;
                    	String arID = frAircraft.getString(0);
                    	double lon = frAircraft.getDouble(1);
                    	double lat = frAircraft.getDouble(2);
                    	double alt = frAircraft.getDouble(4) * 0.3048;
                    	double heading = frAircraft.getDouble(3);
                		// Para cada aeronave encontrada
                		FlightRadarAircraft ac = new FlightRadarAircraft();
                		// Preenche os atributos da aeronave com os dados do FlightRadar24
                		// O numero do voo identifica unicamente uma aeronave
                		// Envia as atualizacoes para a RTI
                		ac.sendSpatialVariant();

                    	
                    	
                    }
                }         		
        		
        		
        		
        	} catch( Exception se ) {
        		logger.error( se.getMessage() );
        	}
        	
        }
        
        logger.info("Coletor finalizado.");
        
    }  
    

	private String getAircrafts() {
		RestTemplate restTemplate = new RestTemplate();
		String responseBody;
		String url = "https://data-live.flightradar24.com/zones/fcgi/feed.js?bounds=-20,-24,-45,-40&faa=1&mlat=1&flarm=1&adsb=1&gnd=0&air=1&vehicles=1&estimated=1&maxage=1000&gliders=1&stats=1";
		try {
			ResponseEntity<String> result = restTemplate.getForEntity( url , String.class);
			responseBody = result.getBody().toString();
		} catch (HttpClientErrorException e) {
		    responseBody = e.getResponseBodyAsString();
		}	
		return responseBody;
	}
	

		
  
}
