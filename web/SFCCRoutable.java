package net.floodlightcontroller.SFCC.web;

import org.restlet.Context;
import org.restlet.Restlet;
import org.restlet.routing.Router;

import net.floodlightcontroller.restserver.RestletRoutable;
import net.floodlightcontroller.staticflowentry.web.StaticFlowEntryPusherResource;

public class SFCCRoutable implements RestletRoutable {

	@Override
	public Restlet getRestlet(Context context) {
		// TODO Auto-generated method stub
		Router router = new Router(context);
		router.attach("/genrule", SFCCAddFlowResource.class);
		router.attach("/delrule", SFCCDelFlowResource.class);

		return router;
	}

	@Override
	public String basePath() {
		// TODO Auto-generated method stub
		return "/simpleswitch";
	}

}
