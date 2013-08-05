package org.mtkachev.eclipse.plugins.multilauncher.ui;

import org.eclipse.debug.ui.AbstractLaunchConfigurationTabGroup;
import org.eclipse.debug.ui.ILaunchConfigurationDialog;
import org.eclipse.debug.ui.ILaunchConfigurationTab;
import org.mtkachev.eclipse.plugins.multilauncher.internal.SublaunchConfigurationRepo;

public class MultilauncherTabGroup extends AbstractLaunchConfigurationTabGroup {
	private final SublaunchConfigurationRepo sublaunchConfigurationRepo;
	
	public MultilauncherTabGroup() {
		super();
		sublaunchConfigurationRepo = new SublaunchConfigurationRepo();
	}

	@Override
	public void createTabs(ILaunchConfigurationDialog dialog, String mode) {
		ILaunchConfigurationTab[] tabs = new ILaunchConfigurationTab[] {//
				new LaunchConfigurationTab(mode, sublaunchConfigurationRepo)
		};
		setTabs(tabs);
	}
}
