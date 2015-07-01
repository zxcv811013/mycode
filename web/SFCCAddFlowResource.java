package net.floodlightcontroller.SFCC.web;

import java.io.IOException;

import java.util.Iterator;

import net.floodlightcontroller.SFCC.ISFCCService;
import net.floodlightcontroller.core.internal.IOFSwitchService;
import net.floodlightcontroller.devicemanager.SwitchPort;
import net.floodlightcontroller.staticflowentry.IStaticFlowEntryPusherService;
import net.floodlightcontroller.staticflowentry.StaticFlowEntries;
import net.floodlightcontroller.staticflowentry.StaticFlowEntryPusher;
import net.floodlightcontroller.storage.IStorageSourceService;
import net.floodlightcontroller.util.ActionUtils;
import net.floodlightcontroller.util.FlowModUtils;
import net.floodlightcontroller.util.MatchUtils;

import org.projectfloodlight.openflow.protocol.action.OFActionSetField;
import org.projectfloodlight.openflow.protocol.OFFactories;
import org.projectfloodlight.openflow.protocol.OFFlowMod;
import org.projectfloodlight.openflow.protocol.OFMatchV3;
import org.projectfloodlight.openflow.types.DatapathId;
import org.projectfloodlight.openflow.types.OFPort;
import org.restlet.Context;
import org.restlet.resource.ServerResource;
import org.restlet.resource.Delete;
import org.restlet.resource.Post;
import org.restlet.resource.ServerResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.MappingJsonFactory;
import com.fasterxml.jackson.databind.ObjectMapper;

public class SFCCAddFlowResource extends ServerResource {
	protected static Logger log = LoggerFactory.getLogger(StaticFlowEntryPusher.class);

	
	@Post
	public String add_flow(String fmJson) throws IOException{
		
		ISFCCService SFCCService =
				(ISFCCService)getContext().getAttributes().
				get(ISFCCService.class.getCanonicalName());
		IStaticFlowEntryPusherService StaticFlowPusher =
				(IStaticFlowEntryPusherService)getContext().getAttributes().
				get(IStaticFlowEntryPusherService.class.getCanonicalName());
		IOFSwitchService switchService =
				(IOFSwitchService)getContext().getAttributes().
				get(IOFSwitchService.class.getCanonicalName());
		//fmJson ="{\"ip\" : \"127.0.0.1\"}";
		OFFlowMod.Builder fmb = null;
		//fmJson = "{\"nodelink\": [    {        \"head\": {            \"instance_id\": \"FW=de685cda-1354-4ae0-80ea-d0590c9b32ca\",            \"tcp_port\": 80,            \"vif_uuid\": \"11968fde-5842-4d64-ad49-274e704f00b9\",            \"type\": \"transparent\",            \"zone\": \"tw-nctu-1\",            \"fixed_ip\": \"192.168.0.7\",            \"floating_ip\": null,            \"mac_addr\": \"00:00:00:00:00:01\",            \"port_id\": \"23f0e426-b5a2-4364-bc4a-0aaed12e9c78\",            \"net_id\": \"41183288-6249-44f4-9f04-eb5e11ba2dd2\"        },        \"set\": {            \"ipv4_dst\": \"10.0.1.24\"        },        \"tail\": {            \"instance_id\": \"FW=5039354c-1398-4009-8269-3d51cb4ba99c\",            \"tcp_port\": 80,            \"vif_uuid\": \"ef154f01-a872-405d-a9ea-37b73c19e58e\",            \"type\": \"transparent\",            \"zone\": \"tw-nctu-1\",            \"fixed_ip\": \"192.168.0.8\",            \"floating_ip\": null,            \"mac_addr\": \"00:00:00:00:00:01\",            \"port_id\": \"23f0e426-b532-4364-bc4a-0aaed12e9c78\",            \"net_id\": \"41183288-623e-44f4-9f04-eb5e11ba2dd2\"        },        \"link_uuid\": \"70f8c6c1-1a0d-46cb-bc9e-4914eb5ddcba\",        \"action\": \"output\",        \"match\": {            \"eth_dst\": \"fa:16:3e:18:b2:89\",            \"eth_type\": 2048        }    }]}";
		ObjectMapper mapper = new ObjectMapper();
		JsonNode root = mapper.readTree(fmJson);
		JsonNode nodelink = root.get("nodelink");
		Iterator<JsonNode> it = nodelink.elements();
		while(it.hasNext()){
			JsonNode eachlink = it.next();
			String head_mac = eachlink.path("head").path("mac_addr").textValue();
			SwitchPort head_switch = SFCCService.getSwitchbyMAC(head_mac);
			String tail_mac = eachlink.path("tail").path("mac_addr").textValue();
			SwitchPort tail_switch = SFCCService.getSwitchbyMAC(tail_mac);
			String flow_name = eachlink.path("link_uuid").textValue();
			String matchString = new String() ;
			String actionString = new String() ;
			//Iterator<JsonNode> JsonMatch = eachlink.path("match").elements();
			JsonNode JsonMatch = eachlink.path("match");
			Iterator<String> field_name = eachlink.path("match").fieldNames();
			//get match
			while(field_name.hasNext()){
				String name =field_name.next(); 
				if(matchString.length() > 0){
					matchString = matchString + ",";
				}
				JsonNode match_field = JsonMatch.path(name);
				if(match_field.isInt())
					matchString = matchString + name +"=" +match_field.asInt();	
				else
					matchString = matchString + name +"=" +match_field.textValue();		
			}
			if(matchString.length() > 0){
				matchString = matchString + ",";
			}
			matchString = matchString +"in_port="+head_switch.getPort().getPortNumber();
			//get action
			Iterator<String> action_name = eachlink.path("set").fieldNames();
			JsonNode JsonSet = eachlink.path("set");
			while(action_name.hasNext()){
				String name = action_name.next();
				if(actionString.length() > 0){
					actionString = actionString + ",";
				}
				actionString = actionString + "set_field="+name+"->"+JsonSet.path(name).textValue();
			}
			if(actionString.length() > 0){
				actionString = actionString + ",";
			}
			actionString = actionString +"output=";
			if(head_switch.getSwitchDPID().getLong() == tail_switch.getSwitchDPID().getLong() )
				actionString = actionString + tail_switch.getPort().getPortNumber();
			else
				actionString = actionString +OFPort.NORMAL;
			
			fmb = OFFactories.getFactory(switchService.getSwitch(DatapathId.of(head_switch.getSwitchDPID().getLong())).getOFFactory().getVersion()).buildFlowModify();
			StaticFlowEntries.initDefaultFlowMod(fmb, flow_name);
			System.out.println(matchString);
			try {
				fmb.setMatch(MatchUtils.fromString(matchString, fmb.getVersion()));
				ActionUtils.fromString(fmb, actionString, log);
			} catch (IllegalArgumentException e) {

				return null;
			} catch (Exception e) {

				e.printStackTrace();
				return null;
			}
			OFFlowMod newFlowMod = fmb.build();
			newFlowMod = FlowModUtils.toFlowAdd(newFlowMod);
			//switchService.getSwitch(head_switch.getSwitchDPID()).write(newFlowMod);
			StaticFlowPusher.addFlow(flow_name, newFlowMod, head_switch.getSwitchDPID());
			//switchService.getSwitch(head_switch.getSwitchDPID()).flush();
		}

		
		
			
		
		return null;		
	}

	

}
