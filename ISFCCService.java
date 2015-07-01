package net.floodlightcontroller.SFCC;

import java.util.Map;

import net.floodlightcontroller.core.module.IFloodlightService;
import net.floodlightcontroller.devicemanager.SwitchPort;

public interface ISFCCService extends IFloodlightService {
	
	public SwitchPort getSwitchbyMAC(String mac);
	public Map<Integer , String> getmacTable(String dpid);

}
