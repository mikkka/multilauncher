package org.mtkachev.eclipse.plugins.multilauncher;

import org.eclipse.osgi.util.NLS;

public class PluginMessages extends NLS {
	public static String LaunchUIPlugin_Error;
	public static String MultiLaunchConfigurationDelegate_0;
	public static String MultiLaunchConfigurationDelegate_Cannot;
	public static String MultiLaunchConfigurationDelegate_Loop;
	public static String MultiLaunchConfigurationDelegate_Action_WaitUntilTerminated;
	public static String MultiLaunchConfigurationDelegate_Action_Delay;
	public static String MultiLaunchConfigurationDelegate_Action_WaitingForTermination;
	public static String MultiLaunchConfigurationDelegate_Action_Delaying;
	public static String LaunchConfigurationTab_Title;
	public static String MultiLaunchConfigurationTabGroup_Table_Enable;
	public static String MultiLaunchConfigurationTabGroup_Table_Name;
	public static String MultiLaunchConfigurationTabGroup_Table_Mode;

	private PluginMessages() {
	}

	static {
        // Load message values from bundle file
        NLS.initializeMessages(PluginMessages.class.getCanonicalName(), PluginMessages.class);
    }
	
}
