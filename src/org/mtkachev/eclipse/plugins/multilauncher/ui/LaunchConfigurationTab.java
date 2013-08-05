package org.mtkachev.eclipse.plugins.multilauncher.ui;

import java.util.List;

import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.ui.AbstractLaunchConfigurationTab;

import org.eclipse.jface.viewers.CheckboxTreeViewer;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;

import org.eclipse.swt.*;
import org.eclipse.swt.layout.*;
import org.eclipse.swt.widgets.*;

import org.mtkachev.eclipse.plugins.multilauncher.PluginMessages;
import org.mtkachev.eclipse.plugins.multilauncher.internal.SublaunchConfiguration;
import org.mtkachev.eclipse.plugins.multilauncher.internal.SublaunchConfigurationRepo;

public class LaunchConfigurationTab extends AbstractLaunchConfigurationTab {

	private final SublaunchConfigurationRepo sublaunchConfigurationRepo;
	private final String mode;

	protected CheckboxTreeViewer treeViewer;
	protected List<SublaunchConfiguration> sublaunchConfigurationsList;

	class ContentProvider implements IStructuredContentProvider, ITreeContentProvider {
		@Override
		public Object[] getElements(Object inputElement) {
			return getChildren(inputElement);
		}

		@Override
		public void dispose() {
			sublaunchConfigurationsList = null;
		}

		@Override
		@SuppressWarnings("unchecked")
		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
			if (newInput instanceof List<?>)
				sublaunchConfigurationsList = (List<SublaunchConfiguration>) newInput;
		}

		@Override
		public Object[] getChildren(Object parentElement) {
			return (parentElement == sublaunchConfigurationsList) ? sublaunchConfigurationsList.toArray() : null;
		}

		@Override
		public Object getParent(Object element) {
			return (element == sublaunchConfigurationsList) ? null : sublaunchConfigurationsList;
		}

		@Override
		public boolean hasChildren(Object element) {
			return (element == sublaunchConfigurationsList) ? (sublaunchConfigurationsList.size() > 0) : false;
		}
	}

	@Override
	public void createControl(Composite parent) {
		final Composite comp = new Composite(parent, SWT.NONE);
		setControl(comp);
		comp.setLayout(new GridLayout(2, false));

		treeViewer = new CheckboxTreeViewer(comp, SWT.BORDER | SWT.MULTI | SWT.V_SCROLL | SWT.H_SCROLL);
		final Tree tree = treeViewer.getTree();
		Tree table = treeViewer.getTree();
		table.setFont(parent.getFont());
		
		treeViewer.setContentProvider(new ContentProvider());
		//treeViewer.setLabelProvider(new LabelProvider());
		//treeViewer.setCheckStateProvider(new CheckStateProvider());
		
		table.setHeaderVisible(true);
		table.setLayoutData(new GridData(GridData.FILL_BOTH));
		TreeColumn col1 = new TreeColumn(table, SWT.NONE);
		col1.setText(PluginMessages.MultiLaunchConfigurationTabGroup_Table_Enable); 
		col1.setWidth(300);
		TreeColumn col2 = new TreeColumn(table, SWT.NONE);
		col2.setText(PluginMessages.MultiLaunchConfigurationTabGroup_Table_Name); 
		col2.setWidth(100);
		TreeColumn col3 = new TreeColumn(table, SWT.NONE);
		col3.setText(PluginMessages.MultiLaunchConfigurationTabGroup_Table_Mode); 
		col3.setWidth(100);
	
		treeViewer.setInput(sublaunchConfigurationsList);
		
		Composite butComposite = new Composite(comp, SWT.NONE);
		butComposite.setLayout(new GridLayout());
		Button butt1 = new Button(butComposite, SWT.PUSH);
		butt1.setText("ADD");
		butt1.setFont(parent.getFont());
		butt1.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		Button butt2 = new Button(butComposite, SWT.PUSH);
		butt2.setText("REMOVE");
		butt2.setFont(parent.getFont());
		butt2.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		GridData layoutData = new GridData(GridData.GRAB_VERTICAL);
		layoutData.verticalAlignment = SWT.BEGINNING;
		butComposite.setLayoutData(layoutData);
	}
	
	@Override
	public void setDefaults(ILaunchConfigurationWorkingCopy configuration) {
	}

	@Override
	public void initializeFrom(ILaunchConfiguration configuration) {
		sublaunchConfigurationsList = sublaunchConfigurationRepo.loadSublaunchConfigList(configuration);
	}

	@Override
	public void performApply(ILaunchConfigurationWorkingCopy configuration) {
		sublaunchConfigurationRepo.storeSublaunchConfigList(configuration, sublaunchConfigurationsList);
	}

	@Override
	public String getName() {
		return PluginMessages.LaunchConfigurationTab_Title; 
	}

	@Override
	public boolean isValid(ILaunchConfiguration launchConfig) {
		return false;
	}

	public LaunchConfigurationTab(String mode, SublaunchConfigurationRepo sublaunchConfigurationRepo) {
		super();
		this.sublaunchConfigurationRepo = sublaunchConfigurationRepo;
		this.mode = mode;
	}
}
