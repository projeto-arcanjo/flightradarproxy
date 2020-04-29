package br.com.cmabreu;

import br.com.cmabreu.services.FederateService;
import hla.rti1516e.AttributeHandleSet;
import hla.rti1516e.InteractionClassHandle;
import hla.rti1516e.NullFederateAmbassador;
import hla.rti1516e.ObjectInstanceHandle;
import hla.rti1516e.OrderType;
import hla.rti1516e.ParameterHandleValueMap;
import hla.rti1516e.TransportationTypeHandle;
import hla.rti1516e.FederateAmbassador.SupplementalReceiveInfo;
import hla.rti1516e.exceptions.FederateInternalError;

public class FederateAmbassador extends NullFederateAmbassador {
	private FederateService federateService;

	public FederateAmbassador( FederateService federateService ){
		this.federateService = federateService;
	}

	@Override
	public void receiveInteraction(InteractionClassHandle arg0, ParameterHandleValueMap arg1, byte[] arg2,
			OrderType arg3, TransportationTypeHandle arg4, SupplementalReceiveInfo arg5 ) throws FederateInternalError {
		// 
	}	

	@Override
	public void provideAttributeValueUpdate(ObjectInstanceHandle theObject,	AttributeHandleSet theAttributes, byte[] userSuppliedTag) throws FederateInternalError {
		federateService.provideAttributeValueUpdate( theObject, theAttributes, userSuppliedTag );
	}	
	

		
	

}
