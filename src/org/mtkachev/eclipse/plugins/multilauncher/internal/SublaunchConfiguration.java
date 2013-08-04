package org.mtkachev.eclipse.plugins.multilauncher.internal;

import org.eclipse.debug.core.ILaunchConfiguration;

public class SublaunchConfiguration {
	private String launchRef;
	private String mode;
	private ILaunchConfiguration launchConfiguration;
	private Boolean enabled;

	private Boolean waitForTerminateAfetrLaunch;
	private Integer pauseBeforeNextInSecs;
	
	public SublaunchConfiguration(String launchRef, String mode,
			ILaunchConfiguration launchConfiguration, Boolean enabled,
			boolean waitForTerminateAfetrLaunch, int pauseBeforeNextInSecs) {
		super();
		this.launchRef = launchRef;
		this.mode = mode;
		this.launchConfiguration = launchConfiguration;
		this.enabled = enabled;
		this.waitForTerminateAfetrLaunch = waitForTerminateAfetrLaunch;
		this.pauseBeforeNextInSecs = pauseBeforeNextInSecs;
	}
	
	public String getLaunchRef() {
		return launchRef;
	}
	
	public void setLaunchRef(String launchRef) {
		this.launchRef = launchRef;
	}
	
	public String getMode() {
		return mode;
	}
	
	public void setMode(String mode) {
		this.mode = mode;
	}
	
	public ILaunchConfiguration getLaunchConfiguration() {
		return launchConfiguration;
	}
	
	public void setLaunchConfiguration(ILaunchConfiguration launchConfiguration) {
		this.launchConfiguration = launchConfiguration;
	}
	
	public Boolean getEnabled() {
		return enabled;
	}

	public void setEnabled(Boolean enabled) {
		this.enabled = enabled;
	}

	public Boolean isWaitForTerminateAfetrLaunch() {
		return waitForTerminateAfetrLaunch;
	}
	
	public void setWaitForTerminateAfetrLaunch(Boolean waitForTerminateAfetrLaunch) {
		this.waitForTerminateAfetrLaunch = waitForTerminateAfetrLaunch;
	}
	
	public Integer getPauseBeforeNextInSecs() {
		return pauseBeforeNextInSecs;
	}

	public void setPauseBeforeNextInSecs(Integer pauseBeforeNextInSecs) {
		this.pauseBeforeNextInSecs = pauseBeforeNextInSecs;
	}
}
