package net.floodlightcontroller.SFCC.web;

import java.io.IOException;
import java.util.Iterator;

import net.floodlightcontroller.core.internal.IOFSwitchService;
import net.floodlightcontroller.staticflowentry.IStaticFlowEntryPusherService;

import org.restlet.resource.Delete;
import org.restlet.resource.Post;
import org.restlet.resource.ServerResource;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class SFCCDelFlowResource extends ServerResource {
	@Post
	public String del_flow(String fmJson) throws IOException{
		
		IStaticFlowEntryPusherService StaticFlowPusher =
				(IStaticFlowEntryPusherService)getContext().getAttributes().
				get(IStaticFlowEntryPusherService.class.getCanonicalName());
		IOFSwitchService switchService =
				(IOFSwitchService)getContext().getAttributes().
				get(IOFSwitchService.class.getCanonicalName());
		ObjectMapper mapper = new ObjectMapper();
		JsonNode root = mapper.readTree(fmJson);
		JsonNode nodelink = root.get("nodelink");
		Iterator<JsonNode> it = nodelink.elements();
		while(it.hasNext()){
			JsonNode eachlink = it.next();
			StaticFlowPusher.deleteFlow(eachlink.path("link_uuid").textValue());
			
		}
		
		

		
		return null;
		
	}

}
