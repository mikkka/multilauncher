package org.mtkachev.eclipse.plugins.multilauncher;

import org.eclipse.osgi.util.NLS;

public class PluginMessages extends NLS {
	public static String LaunchUIPlugin_Error;
	public static String MultiLaunchConfigurationDelegate_0;
	public static String MultiLaunchConfigurationDelegate_Cannot;
	public static String MultiLaunchConfigurationDelegate_Loop;

	private PluginMessages() {
	}

	static {
        // Load message values from bundle file
        NLS.initializeMessages(PluginMessages.class.getCanonicalName(), PluginMessages.class);
    }
	
}
