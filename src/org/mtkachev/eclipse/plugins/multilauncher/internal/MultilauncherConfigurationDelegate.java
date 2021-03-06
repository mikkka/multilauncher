package org.mtkachev.eclipse.plugins.multilauncher.internal;

import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.model.*;
import org.eclipse.debug.internal.ui.DebugUIPlugin;
import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.osgi.util.NLS;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.activities.WorkbenchActivityHelper;
import org.mtkachev.eclipse.plugins.multilauncher.PluginMessages;

public class MultilauncherConfigurationDelegate extends LaunchConfigurationDelegate implements ILaunchConfigurationDelegate2 {
	public static final String DEFAULT_MODE = "default";

	private final SublaunchConfigurationRepo sublaunchConfigurationRepo = new SublaunchConfigurationRepo();
	
	public MultilauncherConfigurationDelegate() {
	}
	
	@Override
	public ILaunch getLaunch(ILaunchConfiguration configuration, String mode)
			throws CoreException {
		return new Multilaunch(configuration, mode);
	}

	@Override
	public void launch(ILaunchConfiguration configuration, String mode,
			ILaunch launch, IProgressMonitor monitor) throws CoreException {
		final IPreferenceStore prefStore = DebugUIPlugin.getDefault().getPreferenceStore();
		boolean dstore = prefStore.getBoolean(IDebugUIConstants.PREF_AUTO_REMOVE_OLD_LAUNCHES);

		try {
			monitor.beginTask(PluginMessages.MultiLaunchConfigurationDelegate_Task + configuration.getName(), 1000); 
			
			prefStore.setValue(IDebugUIConstants.PREF_AUTO_REMOVE_OLD_LAUNCHES,	false);
			
			List<SublaunchConfiguration> confList = sublaunchConfigurationRepo.loadSublaunchConfigList(configuration);
			for (final SublaunchConfiguration conf : confList) {
				final String localMode;
				if (conf.getMode() != null && !conf.getMode().equals(DEFAULT_MODE)) {
					localMode = conf.getMode();
				} else {
					localMode = mode;
				}
				
				if(conf.getEnabled()) {
					if (!isConfCompatinleWithMode(conf, localMode)) {
						showNotCompatibeModeError(conf, localMode);
					} else {
						if (isTheSameConf(configuration, conf)) {
							showLoopError(conf);
						} else {
							ILaunch subLaunch = DebugUIPlugin.buildAndLaunch(
									conf.getLaunchConfiguration(), 
									localMode, 
									new SubProgressMonitor(monitor, 1000 / confList.size()));
							
							((Multilaunch)launch).addSubLaunch(subLaunch);
							((Multilaunch)launch).launchChanged(subLaunch);
	
							postLaunchAction(subLaunch, conf, monitor);
						}
					}
				}
			}
			if (!launch.hasChildren()) {
				DebugPlugin.getDefault().getLaunchManager().removeLaunch(launch);
			}
		} finally {
			prefStore.setValue(IDebugUIConstants.PREF_AUTO_REMOVE_OLD_LAUNCHES, dstore);
			monitor.done();
		}
	}

	private void showLoopError(final SublaunchConfiguration conf) {
		PlatformUI.getWorkbench().getDisplay().asyncExec(new Runnable() {
			@Override
			public void run() {
				MessageDialog.openError(
						PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(),
						PluginMessages.MultiLaunchPlugin_Error, 
						NLS.bind(
								PluginMessages.MultiLaunchConfigurationDelegate_Loop, 
								conf.toString()
						)
				);
			}
		});
	}

	private void showNotCompatibeModeError(final SublaunchConfiguration conf,
			final String localMode) {
		PlatformUI.getWorkbench().getDisplay().asyncExec(new Runnable() {
			@Override
			public void run() {
				MessageDialog.openError(
						PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(),
						PluginMessages.MultiLaunchPlugin_Error,  
						NLS.bind(
								PluginMessages.MultiLaunchConfigurationDelegate_Cannot, 
								conf.getLaunchConfiguration().toString(), 
								localMode
						)
				);
			}
		});
	}

	private boolean isTheSameConf(ILaunchConfiguration configuration,
			final SublaunchConfiguration conf) {
		return configuration.getName().equals(conf.getLaunchRef());
	}

	private boolean isConfCompatinleWithMode(final SublaunchConfiguration conf,
			final String localMode) throws CoreException {
		return conf.getLaunchConfiguration().supportsMode(localMode);
	}

	private void postLaunchAction(ILaunch sublaunch, SublaunchConfiguration conf, IProgressMonitor monitor) {
		if(conf.isWaitForTerminateAfetrLaunch()) {
			monitor.subTask(
					PluginMessages.MultiLaunchConfigurationDelegate_Action_WaitingForTermination + 
					" " + 
					sublaunch.getLaunchConfiguration().getName());
			while (!sublaunch.isTerminated() && !monitor.isCanceled()) {
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					break;
				}
			}
			monitor.subTask("");
		}
		
		Integer waitTimeInSecs = conf.getPauseBeforeNextInSecs(); 
		if(waitTimeInSecs > 0) {
			monitor.subTask(
					NLS.bind(PluginMessages.MultiLaunchConfigurationDelegate_Action_Delaying, 
							waitTimeInSecs.toString()));			
			try {
				Thread.sleep(waitTimeInSecs * 1000);
			} catch (InterruptedException e) {
			}
		}
	}

	public static boolean isValidLaunchReference(ILaunchConfiguration config) {
		return DebugUIPlugin.doLaunchConfigurationFiltering( config) && !WorkbenchActivityHelper.filterItem(config);
	}

	public boolean buildForLaunch(ILaunchConfiguration configuration, String mode, IProgressMonitor monitor)
			throws CoreException {
		return false;
	}

	@Override
	protected void buildProjects(IProject[] projects, IProgressMonitor monitor) throws CoreException {
	}
}
