package net.floodlightcontroller.SFCC;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.projectfloodlight.openflow.protocol.OFMessage;
import org.projectfloodlight.openflow.protocol.OFType;

import net.floodlightcontroller.core.FloodlightContext;
import net.floodlightcontroller.core.IFloodlightProviderService;
import net.floodlightcontroller.core.IOFMessageListener;
import net.floodlightcontroller.core.IOFSwitch;
import net.floodlightcontroller.core.internal.IOFSwitchService;
import net.floodlightcontroller.core.module.FloodlightModuleContext;
import net.floodlightcontroller.core.module.FloodlightModuleException;
import net.floodlightcontroller.core.module.IFloodlightModule;
import net.floodlightcontroller.core.module.IFloodlightService;
import net.floodlightcontroller.devicemanager.IDevice;
import net.floodlightcontroller.devicemanager.IDeviceListener;
import net.floodlightcontroller.devicemanager.IDeviceService;
import net.floodlightcontroller.devicemanager.SwitchPort;
import net.floodlightcontroller.restserver.IRestApiService;
import net.floodlightcontroller.staticflowentry.IStaticFlowEntryPusherService;
import net.floodlightcontroller.storage.IStorageSourceService;

import net.floodlightcontroller.SFCC.web.SFCCRoutable;

public class SFCC implements IFloodlightModule,IDeviceListener,ISFCCService {
	//mac to dpid and port
	static protected Map<String, SwitchPort> mactoswitch;
	//Map<Dpid, Map<port,mac>>
	static protected Map<String, HashMap<Integer,String>> dpidtomac;
	
	
	protected IFloodlightProviderService floodlightProviderService;
	protected IRestApiService restApiService;
	protected IDeviceService deviceManager;
	
	@Override
	public String getName() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean isCallbackOrderingPrereq(String type, String name) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isCallbackOrderingPostreq(String type, String name) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void deviceAdded(IDevice device) {
		// TODO Auto-generated method stub
		System.out.println("device added : "+device.getMACAddressString());
		SwitchPort sw = device.getAttachmentPoints()[0];
		if(!mactoswitch.containsKey(device.getMACAddressString())){
			mactoswitch.put(device.getMACAddressString(), sw);
		}
		else{
			mactoswitch.remove(device.getMACAddressString());
			mactoswitch.put(device.getMACAddressString(), sw);
		}
		if(!dpidtomac.containsKey(sw.getSwitchDPID().toString())){
			HashMap<Integer,String> tmp = new HashMap<Integer,String>(); 
			tmp.put(sw.getPort().getPortNumber(), device.getMACAddressString());
			dpidtomac.put(sw.getSwitchDPID().toString(), tmp);
		}
		else{
			HashMap<Integer,String> tmp = dpidtomac.remove(sw.getSwitchDPID().toString());
			tmp.put(sw.getPort().getPortNumber(), device.getMACAddressString());
			dpidtomac.put(sw.getSwitchDPID().toString(), tmp);
			
			
		}
			
	}

	@Override
	public void deviceRemoved(IDevice device) {
		// TODO Auto-generated method stub
		if(mactoswitch.containsKey(device.getMACAddressString())){
			mactoswitch.remove(device.getMACAddressString());
		}
		if(dpidtomac.containsKey(device.getAttachmentPoints()[0].getSwitchDPID().toString())){
			dpidtomac.remove(device.getAttachmentPoints()[0].getSwitchDPID().toString());
		}
	}

	@Override
	public void deviceMoved(IDevice device) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void deviceIPV4AddrChanged(IDevice device) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void deviceVlanChanged(IDevice device) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public Collection<Class<? extends IFloodlightService>> getModuleServices() {
		// TODO Auto-generated method stub
		Collection<Class<? extends IFloodlightService>> l =
				new ArrayList<Class<? extends IFloodlightService>>();
		l.add(ISFCCService.class);
		return l;
	}

	@Override
	public Map<Class<? extends IFloodlightService>, IFloodlightService> getServiceImpls() {
		// TODO Auto-generated method stub
		Map<Class<? extends IFloodlightService>,
		IFloodlightService> m =
		new HashMap<Class<? extends IFloodlightService>,
		IFloodlightService>();
		m.put(ISFCCService.class, this);
		return m;
	}

	@Override
	public Collection<Class<? extends IFloodlightService>> getModuleDependencies() {
		// TODO Auto-generated method stub
		Collection<Class<? extends IFloodlightService>> l =
				new ArrayList<Class<? extends IFloodlightService>>();
		l.add(IFloodlightProviderService.class);
		l.add(IDeviceService.class);
		l.add(IRestApiService.class);
		return l;
	}

	@Override
	public void init(FloodlightModuleContext context)
			throws FloodlightModuleException {
		// TODO Auto-generated method stub
		floodlightProviderService = context.getServiceImpl(IFloodlightProviderService.class);
		deviceManager = context.getServiceImpl(IDeviceService.class);
		restApiService = context.getServiceImpl(IRestApiService.class);
		
		
	}

	@Override
	public void startUp(FloodlightModuleContext context)
			throws FloodlightModuleException {
		// TODO Auto-generated method stub
		deviceManager.addListener(this);
		mactoswitch = new HashMap<String,SwitchPort>();
		dpidtomac = new HashMap<String,HashMap<Integer,String>>();
		restApiService.addRestletRoutable(new SFCCRoutable());
		Collection<? extends IDevice> allDevice = deviceManager.getAllDevices();
		for(IDevice d : allDevice){
			String mac = d.getMACAddressString();
			SwitchPort sw = d.getAttachmentPoints()[0];
			mactoswitch.put(mac,sw);
			if(!dpidtomac.containsKey(sw.getSwitchDPID().toString())){
				HashMap<Integer,String> tmp = new HashMap<Integer,String>();
				tmp.put(sw.getPort().getPortNumber(), mac);
				dpidtomac.put(sw.getSwitchDPID().toString(),tmp);
			}
			else{
				HashMap<Integer,String> tmp = dpidtomac.remove(sw.getSwitchDPID().toString());
				tmp.put(sw.getPort().getPortNumber(), mac);
				dpidtomac.put(sw.getSwitchDPID().toString(), tmp);
			}
			
		}
		
		
	}

	@Override
	public SwitchPort getSwitchbyMAC(String mac) {
		// TODO Auto-generated method stub
		return mactoswitch.get(mac);
	}

	@Override
	public Map<Integer, String> getmacTable(String dpid) {
		// TODO Auto-generated method stub
		return dpidtomac.get(dpid);
	}

}
