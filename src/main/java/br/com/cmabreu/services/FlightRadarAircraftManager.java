package br.com.cmabreu.services;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import br.com.cmabreu.models.FlightRadarAircraft;
import hla.rti1516e.AttributeHandle;
import hla.rti1516e.AttributeHandleSet;
import hla.rti1516e.InteractionClassHandle;
import hla.rti1516e.ObjectClassHandle;
import hla.rti1516e.ObjectInstanceHandle;
import hla.rti1516e.RTIambassador;

public class FlightRadarAircraftManager {
	private RTIambassador rtiAmb;
	
	private InteractionClassHandle interactionHandle;   

	// caches of handle types - set once we join a federation
	private AttributeHandleSet attributes;
	private ObjectClassHandle entityHandle;
	private AttributeHandle entityTypeHandle;
	private AttributeHandle spatialHandle;
	private AttributeHandle forceIdentifierHandle;
	private AttributeHandle markingHandle;	
	private AttributeHandle isConcealedHandle;
	private AttributeHandle entityIdentifierHandle;
	private AttributeHandle damageStateHandle;
	private static FlightRadarAircraftManager instance;
	private List<FlightRadarAircraft> aircrafts;
	
	private Logger logger = LoggerFactory.getLogger( FlightRadarAircraftManager.class );
	
	public static FlightRadarAircraftManager getInstance() {
		return instance;
	}
	
	public static void startInstance( RTIambassador rtiAmb ) throws Exception {
		instance = new FlightRadarAircraftManager( rtiAmb );
	}
	
	private FlightRadarAircraftManager( RTIambassador rtiAmb ) throws Exception {
		this.aircrafts = new ArrayList<FlightRadarAircraft>();
		logger.info("FlightRadar Aircraft Manager ativo");
		this.rtiAmb = rtiAmb;
		this.publish();
	}
	
	private void publish() throws Exception {
		// get all the handle information for the attributes
		logger.info("Objetos publicados.");
		this.entityHandle = this.rtiAmb.getObjectClassHandle("HLAobjectRoot.BaseEntity.PhysicalEntity.Platform.Aircraft");
		this.entityTypeHandle = this.rtiAmb.getAttributeHandle(entityHandle, "EntityType");
		this.spatialHandle = this.rtiAmb.getAttributeHandle(entityHandle, "Spatial");
		this.forceIdentifierHandle = this.rtiAmb.getAttributeHandle(entityHandle, "ForceIdentifier");
		this.markingHandle = this.rtiAmb.getAttributeHandle(entityHandle, "Marking");
		this.isConcealedHandle = this.rtiAmb.getAttributeHandle(entityHandle, "IsConcealed");
		this.entityIdentifierHandle = this.rtiAmb.getAttributeHandle(entityHandle, "EntityIdentifier");
		this.damageStateHandle = this.rtiAmb.getAttributeHandle(entityHandle, "DamageState");
		
		// package the information into a handle set
		attributes = this.rtiAmb.getAttributeHandleSetFactory().create();
		attributes.add(entityTypeHandle);
		attributes.add(spatialHandle);
		attributes.add(forceIdentifierHandle);
		attributes.add(markingHandle);
		attributes.add(isConcealedHandle);
		attributes.add(entityIdentifierHandle);
		attributes.add(damageStateHandle);
        this.rtiAmb.publishObjectClassAttributes( this.entityHandle, attributes );   
        
        this.interactionHandle = this.rtiAmb.getInteractionClassHandle("Acknowledge");
        this.rtiAmb.publishInteractionClass(interactionHandle);
	}

	/* GETTERS e SETTERS */
	
	public RTIambassador getRtiAmb() {
		return rtiAmb;
	}

	public AttributeHandle getEntityIdentifierHandle() {
		return entityIdentifierHandle;
	}

	public InteractionClassHandle getInteractionHandle() {
		return interactionHandle;
	}

	public ObjectClassHandle getEntityHandle() {
		return entityHandle;
	}

	public AttributeHandle getEntityTypeHandle() {
		return entityTypeHandle;
	}

	public AttributeHandle getSpatialHandle() {
		return spatialHandle;
	}

	public AttributeHandle getForceIdentifierHandle() {
		return forceIdentifierHandle;
	}

	public AttributeHandle getMarkingHandle() {
		return markingHandle;
	}

	public AttributeHandle getIsConcealedHandle() {
		return isConcealedHandle;
	}

	public AttributeHandle getDamageStateHandle() {
		return damageStateHandle;
	}

	public FlightRadarAircraft spawn( String identificador ) throws Exception {
		FlightRadarAircraft temp = new FlightRadarAircraft( this, identificador );
		this.aircrafts.add( temp );
		return temp;
	}
	
	public void update( List<FlightRadarAircraft> aircrafts ) throws Exception {
		for( FlightRadarAircraft ac : aircrafts  ) {
			ac.sendSpatialVariant();
		}
	}

	public synchronized FlightRadarAircraft sendToRTI(String identificador, float lat, float lon, float alt, float head, float pitch, float roll, float speed) throws Exception {
		// Nao tentar otimizar!
		// O objeto "ac" deve ser modificado dentro do loop para
		// que o mesmo objeto da lista reflita as alteracoes
		
		logger.info(identificador + " " + lat + "," + lon + " ( alt=" + alt + ", head=" + head + " )" );
		
    	// Verifica se já tenho esta aeronave
		for( FlightRadarAircraft ac : this.aircrafts ) {
			if( ac.getIdentificador().equals( identificador ) ) {
				// Preenche os atributos da aeronave com os dados do FlightRadar24
				// O numero do voo identifica unicamente uma aeronave
				// Envia as atualizacoes para a RTI
				ac.setAltitude( alt );
				ac.setLongitude( lon );
				ac.setLatitude( lat );
				ac.setOrientationPsi( head );
				ac.setOrientationTheta( pitch );
				ac.setOrientationPhi( roll );
				ac.setVelocityX(speed);
				// Manda a atualizacao para a RTI
				ac.sendSpatialVariant();
				return ac;
			}
		}
		
		// se não tiver eu crio na minha lista
		FlightRadarAircraft ac = new FlightRadarAircraft( this, identificador );
		ac.setAltitude( alt );
		ac.setLongitude( lon );
		ac.setLatitude( lat );
		ac.setOrientationPsi( head );
		ac.setOrientationTheta( pitch );
		ac.setOrientationPhi( roll );
		ac.setVelocityX(speed);
		this.aircrafts.add( ac );
		// Manda a atualizacao para a RTI
		ac.sendSpatialVariant();		
		return ac;
	}
	
	public void sendAircraftToRTI( String keyName, JSONArray aircraftJsonData ) throws Exception {
    	// ["E49590",-12.39,-38.28,35,39025,470,"4502","F-SBSV5","A20N","PR-YSD",1586829614,"VCP","REC","AD2202",0,0,"AZU2202",0,"AZU"]
		try {
			String arID = keyName;//aircraftJsonData.getString(0);
	    	double lat = aircraftJsonData.getDouble(1);
	    	double lon = aircraftJsonData.getDouble(2);
	    	double heading = aircraftJsonData.getDouble(3);
	    	double alt = aircraftJsonData.getDouble(4) * 0.3048 ;
	    	float speed = (float)aircraftJsonData.getDouble(5);
	    	this.sendToRTI(arID, (float)lat, (float)lon, (float)alt, (float)heading, 0, 0, speed);
		} catch ( Exception e ) {
			e.printStackTrace();
		}
	}

	public void provideAttributeValueUpdate(ObjectInstanceHandle theObject, AttributeHandleSet theAttributes) throws Exception {
		List<FlightRadarAircraft> copyList = new ArrayList<FlightRadarAircraft>( aircrafts );
		
		for( FlightRadarAircraft aircraft : copyList ) {
			if( aircraft.getObjectInstanceHandle().equals( theObject) ) aircraft.updateAllValues();
		}
		
		copyList.clear();
	}
	
}
