package br.com.cmabreu.services;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import javax.annotation.PreDestroy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import br.com.cmabreu.FederateAmbassador;
import br.com.cmabreu.FederateExecutorThread;
import br.com.cmabreu.fr.FlightRadarCollectorThread;
import br.com.cmabreu.misc.EncoderDecoder;
import br.com.cmabreu.models.FlightRadarAircraft;
import hla.rti1516e.CallbackModel;
import hla.rti1516e.RTIambassador;
import hla.rti1516e.ResignAction;
import hla.rti1516e.RtiFactoryFactory;
import hla.rti1516e.exceptions.CallNotAllowedFromWithinCallback;
import hla.rti1516e.exceptions.FederatesCurrentlyJoined;
import hla.rti1516e.exceptions.FederationExecutionAlreadyExists;
import hla.rti1516e.exceptions.FederationExecutionDoesNotExist;
import hla.rti1516e.exceptions.NotConnected;
import hla.rti1516e.exceptions.RTIinternalError;

@Service
public class FederateService {
	private RTIambassador rtiamb;
	private FederateAmbassador fedamb;  			
	private Logger logger = LoggerFactory.getLogger( FederateService.class );
	private EncoderDecoder encoder;	
	private Runnable frCollectorThread;
	private Runnable federateExecutorThread;
	private ScheduledFuture<?> scheduled;
	private ScheduledExecutorService scheduler;
	
    @Value("${federation.fomfolder}")
    String fomFolder;	

    @Value("${federation.name}")
    String federationName;	
    
    @Value("${federation.federateName}")
    String federateName;	

    @Value("${frcollector.firstrun}")
    Integer frFirstRun; 
    
    @Value("${frcollector.interval}")
    Integer frInterval; 

    
    @PreDestroy
	public void onExit() {
		logger.info("Encerando Federado...");
		this.quit();
	}
    
    public void startRti() throws Exception {
    	
    	if( !fomFolder.endsWith("/") ) fomFolder = fomFolder + "/";
    	
		/////////////////////////////////////////////////
		// 1 & 2. create the RTIambassador and Connect //
		/////////////////////////////////////////////////    	
		createRtiAmbassador();
		connect();
		
		//////////////////////////////
		// 3. create the federation //
		//////////////////////////////		
		createFederation( federationName );
		
		////////////////////////////
		// 4. join the federation //
		////////////////////////////		
		joinFederation( federationName, federateName);
		

		///////////////////////////////
		// 5. Inicia o Gerenciador //
		///////////////////////////////	
		FlightRadarAircraftManager.startInstance( rtiamb );
		
		//////////////////////////////
		// 8. publish               //
		//////////////////////////////
		// in this section we tell the RTI of all the data we are going to
		// produce, and all the data we want to know about
		publish();
		logger.info( "Published" );		

		
		/////////////////////////////////////
		// 10. do the main simulation loop //
		/////////////////////////////////////
		// Do not block the web browser interface!
		this.federateExecutorThread = new FederateExecutorThread( this );
		new Thread( this.federateExecutorThread ).start();
		
    }


	// Inicia o coletor FlightRadar24
	// Acionado somente pelo Controller REST
    public void startCollector() {
		this.scheduler = Executors.newSingleThreadScheduledExecutor();
		this.frCollectorThread = new FlightRadarCollectorThread( );
        this.scheduled =  this.scheduler.scheduleAtFixedRate( this.frCollectorThread, this.frFirstRun, this.frInterval , TimeUnit.SECONDS );
    }
    
    public void evokeCallBacks() {
    	try {
			rtiamb.evokeMultipleCallbacks( 0.1, 0.2 );
		} catch ( CallNotAllowedFromWithinCallback e ) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch ( RTIinternalError e ) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }
    
	// This now can be called by a REST endpoint. See FederateController.quit()
    public void quit()  {
	
    	( (FederateExecutorThread)this.federateExecutorThread ).finish();
    	( (FlightRadarCollectorThread)this.frCollectorThread ).finish();
    	this.scheduled.cancel( false );
    	
    	
		////////////////////////////////////
		// 12. resign from the federation //
		////////////////////////////////////
		try {
			resignFederationExecution();
			destroyFederation();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	
    }
    
	// This now can be called by a REST endpoint. See FederateController.deleteObjectInstance()
	public void deleteObjectInstance( int objectHandle ) throws Exception {
		rtiamb.deleteObjectInstance( encoder.getObjectHandle( objectHandle ), generateTag() );
		logger.info( "Deleted Object, handle=" + objectHandle );
	}

	public void resignFederationExecution() throws Exception {
		rtiamb.resignFederationExecution( ResignAction.DELETE_OBJECTS );
	}	

	
	// This method now will be fired by a REST endpoint. See FederateController.destroyFederation()
	public void destroyFederation() {
		////////////////////////////////////////
		// 13. try and destroy the federation //
		////////////////////////////////////////
		// NOTE: we won't die if we can't do this because other federates
		//       remain. in that case we'll leave it for them to clean up
		try	{
			rtiamb.destroyFederationExecution( federationName );
			logger.info( "Destroyed Federation" );
		} catch( FederationExecutionDoesNotExist dne ) {
			logger.warn( "No need to destroy federation, it doesn't exist" );
		} catch( FederatesCurrentlyJoined fcj ) 	{
			logger.warn( "Didn't destroy federation, federates still joined" );
		} catch (NotConnected e) {
			logger.error( "Not Connected" );
		} catch (RTIinternalError e) {
			logger.error( e.getMessage() + " : " + e.getCause() );
		}
		
		// disconnect
		try {
			rtiamb.disconnect();
			logger.info( "Disconnected" );
		} catch( Exception e ) {
			logger.error( e.getMessage() + " : " + e.getCause() );
			e.printStackTrace();
		}			
		
	}
    
    
	private void createRtiAmbassador() throws Exception {
		logger.info( "Creating RTIambassador." );
		this.rtiamb = RtiFactoryFactory.getRtiFactory().getRtiAmbassador();
		encoder = new EncoderDecoder();
	}   
	
	private void connect() throws Exception {
		logger.info( "Connecting..." );
		fedamb = new FederateAmbassador( this );
		rtiamb.connect( fedamb, CallbackModel.HLA_IMMEDIATE );
	}	
	
	private void createFederation( String federationName ) throws Exception {
		// We attempt to create a new federation with the first three of the
		// restaurant FOM modules covering processes, food and drink		
		logger.info( "Creating Federation " + federationName );
		try	{
			URL[] modules = new URL[]{
				// The MIM file MUST be present	
				(new File( fomFolder + "HLAstandardMIM.xml")).toURI().toURL(),
				(new File( fomFolder + "RPR_FOM_v2.0_1516-2010.xml")).toURI().toURL(),
				(new File( fomFolder + "NETN-Base_v1.0.2.xml")).toURI().toURL(),
				(new File( fomFolder + "NETN-Aggregate_v1.0.4.xml")).toURI().toURL(),
				(new File( fomFolder + "NETN-Physical_v1.1.2.xml")).toURI().toURL(),
			};
			rtiamb.createFederationExecution( federationName, modules );
			logger.info( "Created Federation. HLA Version " + rtiamb.getHLAversion() );
		} catch( FederationExecutionAlreadyExists exists ) {
			logger.error( "Didn't create federation, it already existed." );
		} catch( MalformedURLException urle )	{
			logger.error( "Exception loading one of the FOM modules from disk: " + urle.getMessage() );
			urle.printStackTrace();
			return;
		}
	}	

	
	private void joinFederation( String federationName, String federateName ) throws Exception  {
		rtiamb.joinFederationExecution( federateName, "ExampleFederateType", federationName );   
		logger.info( "Joined Federation as " + federateName );
	}	

	
	public EncoderDecoder getEncoder() {
		return encoder;
	}
	
	private byte[] generateTag() {
		return ( "XPLANE_" + System.currentTimeMillis()).getBytes();
	}	
	
	public void publish() throws Exception {
		// Quem vai publicar uma aeronave eh o software X-Plane quando enviar dados.
		// Até la, nada de aviao por aqui.
	}

	public FlightRadarAircraft spawn( String identificador ) throws Exception {
		FlightRadarAircraft aircraft = FlightRadarAircraftManager.getInstance().spawn( identificador );
		return aircraft;
		
	}

	public FlightRadarAircraft update( String identificador, float lat, float lon, float alt, float head, float pitch, float roll ) throws Exception {
		return FlightRadarAircraftManager.getInstance().update( identificador, lat, lon, alt, head, pitch, roll);
	}
	
	
}
