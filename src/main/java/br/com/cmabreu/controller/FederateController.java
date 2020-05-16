package br.com.cmabreu.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import br.com.cmabreu.models.FlightRadarAircraft;
import br.com.cmabreu.services.FederateService;

@RestController
public class FederateController {
	
    @Autowired
    private FederateService federateService;	
	

    @RequestMapping(value = "/quit", method = RequestMethod.GET, produces=MediaType.APPLICATION_JSON_UTF8_VALUE )
	public @ResponseBody String quit() {
    	federateService.quit();
    	return "ok";
	}
	

    @RequestMapping(value = "/start", method = RequestMethod.GET, produces=MediaType.APPLICATION_JSON_UTF8_VALUE )
	public @ResponseBody String startCollector( @RequestParam(value = "interval", required = false) Integer interval  ) {
    	
    	System.out.println( interval );
    	
    	if( interval != null ) {
    		federateService.startCollector( interval );
    	} else {
    		federateService.startCollector();
    	}
    	return "ok";
	}
    
    
	@RequestMapping(value = "/deleteobjectinstance", method = RequestMethod.GET, produces=MediaType.APPLICATION_JSON_UTF8_VALUE )
	public @ResponseBody String deleteObjectInstance( @RequestParam(value = "handle", required = true) Integer objectInstanceHandle ) {
		try {
			federateService.deleteObjectInstance( objectInstanceHandle );
		} catch ( Exception e ) {
			//
		}
		return "ok";
	}
	
	
	@RequestMapping(value = "/spawn", method = RequestMethod.GET, produces=MediaType.APPLICATION_JSON_UTF8_VALUE )
	public @ResponseBody FlightRadarAircraft spawn( @RequestParam(value = "identificador", required = true) String identificador ) {
		try {
			FlightRadarAircraft aircraft = federateService.spawn( identificador );
			return aircraft;
		} catch ( Exception e ) {
			e.printStackTrace();
			return null;
		}
	}


	@RequestMapping(value = "/bbox/{minlat}/{minlon}/{maxlat}/{maxlon}", method = RequestMethod.GET )
	public void bbox( @PathVariable("minlat") Float minLat,
			@PathVariable("minlon") Float minLon,
			@PathVariable("maxlat") Float maxLat,
			@PathVariable("maxlon") Float maxLon) {
		federateService.changeBBox(minLat, minLon, maxLat, maxLon);
	}

	
	@RequestMapping(value = "/lastdata", method = RequestMethod.GET, produces=MediaType.APPLICATION_JSON_UTF8_VALUE )
	public @ResponseBody String lastData( ) {
		return federateService.getLastData();
	}

	
	
	@RequestMapping(value = "/update", method = RequestMethod.GET, produces=MediaType.APPLICATION_JSON_UTF8_VALUE )
	public @ResponseBody FlightRadarAircraft update( @RequestParam(value = "identificador", required = true) String identificador,
			@RequestParam(value = "lat", required = true) float lat,
			@RequestParam(value = "lon", required = true) float lon, 
			@RequestParam(value = "alt", required = true) float alt,
			@RequestParam(value = "head", required = true) float head, 
			@RequestParam(value = "pitch", required = true) float pitch,
			@RequestParam(value = "roll", required = true) float roll,
			@RequestParam(value = "speed", required = true) float speed) {
		try {
			FlightRadarAircraft aircraft = federateService.update( identificador, lat, lon, alt, head, pitch, roll, speed );
			return aircraft;
		} catch ( Exception e ) {
			e.printStackTrace();
			return null;
		}
	}
	
	
}

