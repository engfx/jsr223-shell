package jsr223.shell;

import java.io.Writer;
import java.util.ArrayList;
import java.util.List;

import javax.script.SimpleScriptContext;

public class AnsDepScriptContext extends SimpleScriptContext {

	private List<Writer> destinations = new ArrayList<Writer>();
	private List<Writer> errorDestinations = new ArrayList<Writer>();
	
	
	public AnsDepScriptContext(List<Writer> dests, List<Writer> errorDests) {
		super();
		this.destinations.addAll(dests);
		this.errorDestinations.addAll(errorDests);
	}


	public List<Writer> getDestinations() {
		return destinations;
	}


	public void setDestinations(List<Writer> destinations) {
		this.destinations = destinations;
	}


	public List<Writer> getErrorDestinations() {
		return errorDestinations;
	}


	public void setErrorDestinations(List<Writer> errorDestinations) {
		this.errorDestinations = errorDestinations;
	}
	
	
	
}
