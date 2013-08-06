package org.mtkachev.eclipse.plugins.multilauncher.internal;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.core.ILaunchManager;
import org.mtkachev.eclipse.plugins.multilauncher.MultilauncherPlugin;

public class SublaunchConfigurationRepo {
	private static String PREFIX = "org.mtkachev.eclipse.plugins.multilauncher.";
	
	private static String PROP_IDX = "idx";
	private static String PROP_REF = "lauchRef";
	private static String PROP_MODE = "mode";
	private static String PROP_ENABLED = "enabled";
	private static String PROP_WAIT_TERMINATE = "wait_term";
	private static String PROP_PAUSE = "pause";
	
	public List<SublaunchConfiguration> loadSublaunchConfigList(ILaunchConfiguration configuration) {
		LinkedList<SublaunchConfiguration> sublaunchList = new LinkedList<SublaunchConfiguration>();

		try {
			Map<?,?> attrs = configuration.getAttributes();

			String idxStr = (String)attrs.get(propKey(PROP_IDX));
			if(idxStr != null) {
				int maxIdx = Integer.parseInt(idxStr);
				
				for(int idx = 0; idx <= maxIdx; idx++) {
					String launchRef = (String) attrs.get(propKey(PROP_REF, idx));
					ILaunchConfiguration conf = findLaunch(launchRef);
					sublaunchList.add(new SublaunchConfiguration(
							launchRef,
							(String)attrs.get(propKey(PROP_MODE, idx)),
							conf,
							"true".equals(attrs.get(propKey(PROP_ENABLED, idx))),
							"true".equals(attrs.get(propKey(PROP_WAIT_TERMINATE, idx))),
							Integer.parseInt((String)attrs.get(propKey(PROP_PAUSE, idx)))
					));
				}
			}
		} catch (CoreException e) {
			MultilauncherPlugin.log(e);
		} catch (Exception e) {
			MultilauncherPlugin.log(e);
		}
		
		return sublaunchList;
	}

	public void storeSublaunchConfigList(
			ILaunchConfigurationWorkingCopy configuration, List<SublaunchConfiguration> configList) {
		int idx = 0;
		for(SublaunchConfiguration conf : configList) {
			if(conf != null) {
				configuration.setAttribute(propKey(PROP_REF, idx), conf.getLaunchRef());
				configuration.setAttribute(propKey(PROP_MODE, idx), conf.getMode());
				configuration.setAttribute(propKey(PROP_ENABLED, idx), 
						conf.getEnabled().toString());
				configuration.setAttribute(propKey(PROP_WAIT_TERMINATE, idx), 
						conf.isWaitForTerminateAfetrLaunch().toString());
				configuration.setAttribute(propKey(PROP_PAUSE, idx), 
						conf.getPauseBeforeNextInSecs().toString());
				idx++;
			}
		}
		configuration.setAttribute(propKey(PROP_IDX), String.valueOf(idx - 1));
	}
	
	private String propKey(String propName) {
		return PREFIX + propName;
	}

	private String propKey(String propName, Integer idx) {
		return PREFIX + propName + "." + idx;
	}
	
	//TODO: handle null configuration
	private ILaunchConfiguration findLaunch(String launchRef) throws CoreException {
		ILaunchManager launchManager = DebugPlugin.getDefault().getLaunchManager();
		ILaunchConfiguration[] launchConfigurations = launchManager.getLaunchConfigurations();
		for (ILaunchConfiguration conf : launchConfigurations) {
			if (conf.getName().equals(launchRef)) return conf;
		}
		return null;
	}
}
