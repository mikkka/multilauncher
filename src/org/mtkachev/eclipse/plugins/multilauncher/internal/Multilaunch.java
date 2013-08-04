package org.mtkachev.eclipse.plugins.multilauncher.internal;

import java.util.LinkedList;

import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchesListener2;
import org.eclipse.debug.core.Launch;

public class Multilaunch extends Launch implements ILaunchesListener2 {
	private final LinkedList<ILaunch> sublaunches = new LinkedList<ILaunch>();

	public Multilaunch(ILaunchConfiguration launchConfiguration, String mode) {
		super(launchConfiguration, mode, null);
	}

	@Override
	public void launchesRemoved(ILaunch[] launches) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void launchesAdded(ILaunch[] launches) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void launchesChanged(ILaunch[] launches) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void launchesTerminated(ILaunch[] launches) {
		// TODO Auto-generated method stub
		
	}

	public void addSubLaunch(ILaunch sublaunch) {
		sublaunches.add(sublaunch);
	}
}
