package br.com.cmabreu;

import br.com.cmabreu.services.FederateService;
import hla.rti1516e.NullFederateAmbassador;

public class FederateAmbassador extends NullFederateAmbassador {
	private FederateService federateService;

	public FederateAmbassador( FederateService federateService ){
		this.federateService = federateService;
	}

	

}
