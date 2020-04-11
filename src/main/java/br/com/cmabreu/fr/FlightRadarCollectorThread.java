package br.com.cmabreu.fr;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import br.com.cmabreu.models.FlightRadarAircraft;

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
        		
        		// Para cada aeronave encontrada
        		FlightRadarAircraft ac = new FlightRadarAircraft();
        		// Preenche os atributos da aeronave com os dados do FlightRadar24
        		// O numero do voo identifica unicamente uma aeronave
        		// Envia as atualizacoes para a RTI
        		ac.sendSpatialVariant();
        		
        		
        	} catch( Exception se ) {
        		logger.error( se.getMessage() );
        	}
        	
        }
        
        logger.info("Coletor finalizado.");
        
    }  
    

	

		
  
}
