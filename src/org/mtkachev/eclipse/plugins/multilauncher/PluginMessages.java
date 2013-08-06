package org.mtkachev.eclipse.plugins.multilauncher;

import org.eclipse.osgi.util.NLS;

public class PluginMessages extends NLS {
	public static String MultiLaunchPlugin_Error;
	public static String MultiLaunchConfigurationDelegate_Task;
	public static String MultiLaunchConfigurationDelegate_Cannot;
	public static String MultiLaunchConfigurationDelegate_Loop;
	public static String MultiLaunchConfigurationDelegate_Action_WaitUntilTerminated;
	public static String MultiLaunchConfigurationDelegate_Action_Delay;
	public static String MultiLaunchConfigurationDelegate_Action_WaitingForTermination;
	public static String MultiLaunchConfigurationDelegate_Action_Delaying;
	public static String LaunchConfigurationTab_Title;
	public static String LaunchConfigurationTab_Table_Name;
	public static String LaunchConfigurationTab_Table_Mode;
	public static String LaunchConfigurationTab_Table_AfterLaunch;
	public static String LaunchConfigurationTab_Table_Mode_WT;
	public static String LaunchConfigurationTab_Table_Mode_Pause;
	public static String LaunchConfigurationTab_AddButton;
	public static String LaunchConfigurationTab_EditButton;
	public static String LaunchConfigurationTab_RemoveButton;
	public static String LaunchConfigurationDialog_Titile_Edit;
	public static String LaunchConfigurationDialog_Titile_Create;
	public static String LaunchConfigurationDialog_LaunchModeLabel;
	public static String LaunchConfigurationDialog_DefaultModeCheckbox;
	public static String LaunchConfigurationDialog_WaitForTerminateCheckbox;
	public static String LaunchConfigurationDialog_PauseBeforeNext;
	public static String LaunchConfigurationDialog_Invalid_NotOneLaunchConfigurationsSelected;
	public static String LaunchConfigurationDialog_Invalid_NoLaunchSelected;
	public static String LaunchConfigurationDialog_Invalid_DelayNotNumeric;

	private PluginMessages() {
	}

	static {
        // Load message values from bundle file
        NLS.initializeMessages(PluginMessages.class.getCanonicalName(), PluginMessages.class);
    }
	
}
