package org.mtkachev.eclipse.plugins.multilauncher.ui;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationType;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.internal.ui.DebugUIPlugin;
import org.eclipse.debug.internal.ui.launchConfigurations.LaunchConfigurationFilteredTree;
import org.eclipse.debug.internal.ui.launchConfigurations.LaunchConfigurationManager;
import org.eclipse.debug.internal.ui.launchConfigurations.LaunchGroupFilter;
import org.eclipse.debug.ui.ILaunchGroup;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StackLayout;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.ui.activities.WorkbenchActivityHelper;
import org.eclipse.ui.dialogs.FilteredTree;
import org.eclipse.ui.dialogs.PatternFilter;
import org.mtkachev.eclipse.plugins.multilauncher.PluginMessages;
import org.mtkachev.eclipse.plugins.multilauncher.internal.MultilauncherConfigurationDelegate;
import org.mtkachev.eclipse.plugins.multilauncher.internal.SublaunchConfiguration;

public class LaunchConfigurationDialog extends TitleAreaDialog implements ISelectionChangedListener {
	private ViewerFilter[] filters = null;
	private ISelection selection;
	private ILaunchGroup[] launchGroups;
	private String mode;
	private boolean isDefaultMode;
	private ViewerFilter emptyTypeFilter;
	private IStructuredSelection initialSelection;
	private LaunchConfigurationSelectionComposite selectionComposite;
	private Text pauseBeforeNextText;
	private Integer pauseBeforeNextInSecs = 0;
	private boolean waitForTerminate;
	private boolean isForEditing;
	
	public LaunchConfigurationDialog(Shell shell, String initMode, boolean forEditing) {
		super(shell);
		LaunchConfigurationManager manager = DebugUIPlugin.getDefault().getLaunchConfigurationManager();
		launchGroups = manager.getLaunchGroups();
		mode = initMode;
		isForEditing = forEditing;
		filters = null;
		setShellStyle(getShellStyle() | SWT.RESIZE);
		emptyTypeFilter = new ViewerFilter() {
			@Override
			public boolean select(Viewer viewer, Object parentElement, Object element) {
				if (element instanceof ILaunchConfigurationType) {
					try {
						ILaunchConfigurationType type = (ILaunchConfigurationType) element;
						return getLaunchManager().getLaunchConfigurations(type).length > 0;
					} catch (CoreException e) {
						return false;
					}
				} else if (element instanceof ILaunchConfiguration) {
					ILaunchConfiguration config = (ILaunchConfiguration)element;
					return DebugUIPlugin.doLaunchConfigurationFiltering(config) && !WorkbenchActivityHelper.filterItem(config);
				}
				return true;
			}
		};
	}

	protected ILaunchManager getLaunchManager() {
		return DebugPlugin.getDefault().getLaunchManager();
	}

	@Override
	protected Control createContents(Composite parent) {
		Control x = super.createContents(parent);
		validate();
		setErrorMessage(null);
		return x;
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		Composite comp = (Composite) super.createDialogArea(parent);
		
		getShell().setText(isForEditing ?
				PluginMessages.LaunchConfigurationDialog_Titile_Edit :
				PluginMessages.LaunchConfigurationDialog_Titile_Create);
		
		setTitle(isForEditing ?
				PluginMessages.LaunchConfigurationDialog_Titile_Edit :
				PluginMessages.LaunchConfigurationDialog_Titile_Create);
		
		HashMap<String, ILaunchGroup> modes = new HashMap<String, ILaunchGroup>();
		for (ILaunchGroup launchGroup : launchGroups) {
			if (!modes.containsKey(launchGroup.getMode())) {
				modes.put(launchGroup.getMode(), launchGroup);
			}
		}
		if (this.mode.equals(MultilauncherConfigurationDelegate.DEFAULT_MODE)) {
			try {
				this.mode = "run";
				ILaunchConfiguration[] configs = getSelectedLaunchConfigurations();
				if (configs.length > 0) {
					// we care only about the first selected element
					for (Iterator<String> iterator = modes.keySet().iterator(); iterator.hasNext();) {
						String mode = iterator.next();
						if (configs[0].supportsMode(mode)) {
							this.mode = mode;
							break;
						}
					}
				}
			} catch (Exception e) {
			}
		}
		
		selectionComposite = new LaunchConfigurationSelectionComposite(
				comp,
				PluginMessages.LaunchConfigurationDialog_LaunchModeLabel,
				SWT.NONE);
		
		for (Iterator<String> iterator = modes.keySet().iterator(); iterator.hasNext();) {
			String mode = iterator.next();
			ILaunchGroup launchGroup = modes.get(mode);
			LaunchConfigurationFilteredTree launchTree = new LaunchConfigurationFilteredTree(
					selectionComposite.getStackParent(), 
					SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER, 
					new PatternFilter(), launchGroup, filters);
			
			String label = mode;
			selectionComposite.addItem(label, launchTree);
			launchTree.createViewControl();
			ViewerFilter[] filters = launchTree.getViewer().getFilters();
			for (ViewerFilter viewerFilter : filters) {
				if (viewerFilter instanceof LaunchGroupFilter) {
					launchTree.getViewer().removeFilter(viewerFilter);
				}
			}
			launchTree.getViewer().addFilter(emptyTypeFilter);
			launchTree.getViewer().addSelectionChangedListener(this);
			if (launchGroup.getMode().equals(this.mode)) {
				selectionComposite.setSelection(label);
			}
			if (initialSelection!=null) {
				launchTree.getViewer().setSelection(initialSelection, true);
			}
		}
		selectionComposite.pack();
		Rectangle bounds = selectionComposite.getBounds();
		GridData data = ((GridData) selectionComposite.getLayoutData());
		if (data == null) {
			data = new GridData(GridData.FILL_BOTH);
			selectionComposite.setLayoutData(data);
		}
		data.heightHint = Math.max(convertHeightInCharsToPixels(15), bounds.height);
		data.widthHint = Math.max(convertWidthInCharsToPixels(40), bounds.width);
		selectionComposite.getCombo().addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				mode = ((Combo) e.widget).getText();
			}
		});
		
		Composite checkboxComp = new Composite(comp, SWT.NONE);
		checkboxComp.setLayout(new GridLayout(1, false));
		checkboxComp.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		Button checkBox = new Button(checkboxComp, SWT.CHECK);
		checkBox.setText(PluginMessages.LaunchConfigurationDialog_DefaultModeCheckbox); 
		checkBox.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				isDefaultMode = ((Button) e.widget).getSelection();
			}
		});
		checkBox.setSelection(isDefaultMode);

		// wait for terminate option
		Composite waitForTerminateComposite = new Composite(comp, SWT.NONE);
		waitForTerminateComposite.setLayout(new GridLayout(1, false));
		waitForTerminateComposite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		Button waitForTerminateCheckBox = new Button(waitForTerminateComposite, SWT.CHECK);
		waitForTerminateCheckBox.setText(PluginMessages.LaunchConfigurationDialog_WaitForTerminateCheckbox); 
		waitForTerminateCheckBox.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				waitForTerminate = ((Button) e.widget).getSelection();
			}
		});
		waitForTerminateCheckBox.setSelection(waitForTerminate);

		Composite pauseBeforeNextInSecsComposite = new Composite(comp, SWT.NONE);
		pauseBeforeNextInSecsComposite.setLayout(new GridLayout(2, false));
		pauseBeforeNextInSecsComposite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		Label pauseBeforeNextTextlabel = new Label(pauseBeforeNextInSecsComposite, SWT.NONE);
		pauseBeforeNextTextlabel.setText(PluginMessages.LaunchConfigurationDialog_PauseBeforeNext); 
		pauseBeforeNextText = new Text(pauseBeforeNextInSecsComposite, SWT.SINGLE | SWT.BORDER);
		pauseBeforeNextText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		pauseBeforeNextText.addModifyListener(new ModifyListener(){
			@Override
			public void modifyText(ModifyEvent e) {
				String text = ((Text)e.widget).getText();
				try {
					pauseBeforeNextInSecs = new Integer(Integer.parseInt(text));
				} catch (NumberFormatException exc) {
				}
				validate();
			}
		});
		if(pauseBeforeNextInSecs != null) {
			pauseBeforeNextText.setText(pauseBeforeNextInSecs.toString());
		}
		return comp;
	}
	
	public ILaunchConfiguration[] getSelectedLaunchConfigurations() {
		ArrayList<ILaunchConfiguration> configs = new ArrayList<ILaunchConfiguration>(); 
		if (selection != null && !selection.isEmpty()) {
			for (Iterator<?> iter = ((IStructuredSelection)selection).iterator(); iter.hasNext();) {
				Object selection = iter.next();
				if (selection instanceof ILaunchConfiguration) {
					configs.add((ILaunchConfiguration)selection);
				}
			}
		}
		return configs.toArray(new ILaunchConfiguration[configs.size()]);
	}

	public String getMode() {
		return isDefaultMode ? MultilauncherConfigurationDelegate.DEFAULT_MODE : mode;
	}
	
	public static LaunchConfigurationDialog createDialog(Shell shell, String groupId, boolean forEditing) {
		return new LaunchConfigurationDialog(shell, groupId, forEditing);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.ISelectionChangedListener#selectionChanged(org.eclipse.jface.viewers.SelectionChangedEvent)
	 */
	@Override
	public void selectionChanged(SelectionChangedEvent event) {
		Tree topTree = null;
		final Control topControl = selectionComposite.getTopControl();
		if (topControl instanceof FilteredTree) {
			final TreeViewer viewer = ((FilteredTree)topControl).getViewer();
			if (viewer != null) {
				topTree = viewer.getTree();
			}
		}
		if (topTree == null) {
			return;
		}
		
		boolean selectionIsForVisibleViewer = false;
		final Object src = event.getSource();
		if (src instanceof Viewer) {
			final Control viewerControl = ((Viewer)src).getControl();
			if (viewerControl == topTree) {
				selectionIsForVisibleViewer = true;
			}
		}
		
		if (!selectionIsForVisibleViewer) {
			return;
		}
		
		selection = event.getSelection();
		validate();
	}

	protected void validate() {
		Button ok_button = getButton(IDialogConstants.OK_ID);
		boolean isValid = true;
		if (isForEditing) {
			if (getSelectedLaunchConfigurations().length != 1) {
				setErrorMessage(PluginMessages.LaunchConfigurationDialog_Invalid_NotOneLaunchConfigurationsSelected); 
				isValid = false;
			} else {
				setErrorMessage(null);
			}
		} else {
			if (getSelectedLaunchConfigurations().length < 1) {
				setErrorMessage(PluginMessages.LaunchConfigurationDialog_Invalid_NoLaunchSelected); 
				isValid = false;
			} else {
				setErrorMessage(null);
			}
		}

		if (isValid) {
			String delayStr = pauseBeforeNextText.getText();
			boolean badDelay = false;
			Integer delay = 0;
			try {
 				delay = Integer.parseInt(delayStr);
			} catch (NumberFormatException nfe) {
				badDelay = true;
			}
			if(delay < 0) {
				badDelay = true;
			}
			if(badDelay) {
				setErrorMessage(PluginMessages.LaunchConfigurationDialog_Invalid_DelayNotNumeric); 
			}
		}
		
		if (ok_button != null) {
			ok_button.setEnabled(isValid);
		}
	}
	

	public void setInitialSelection(SublaunchConfiguration conf) {
		isDefaultMode = conf.getMode().equals(MultilauncherConfigurationDelegate.DEFAULT_MODE);
	    initialSelection = new StructuredSelection(conf.getLaunchConfiguration());   
	    selection = initialSelection;
    }
	
	private class LaunchConfigurationSelectionComposite extends Composite {
		private final LinkedHashMap<String, LaunchConfigurationFilteredTree> treeMap = 
				new LinkedHashMap<String, LaunchConfigurationFilteredTree>();

		private final Composite area;
		private final StackLayout layout;
		private final Combo combo;
		private final Label label;
		
	
		public LaunchConfigurationSelectionComposite(
				Composite parent, 
				String labelText,
				int style) {
			
			super(parent, style);

			this.treeMap.putAll(treeMap);

			setLayout(new GridLayout(2, false));
			layout = new StackLayout();
			label = createLabel(this, labelText);
			combo = createCombo(this);
			GridData cgd = new GridData(GridData.FILL_HORIZONTAL);

			combo.setLayoutData(cgd);
			area = createTabArea(this);
			GridData agd = new GridData(GridData.FILL_BOTH);
			agd.horizontalSpan = 2;
			area.setLayoutData(agd);
		}
		
		protected Composite createTabArea(Composite parent) {
			Composite comp = new Composite(parent, SWT.NONE);
			comp.setLayout(layout);

			return comp;
		}

		protected Label createLabel(Composite parent, String labelText) {
			Label label = new Label(parent, SWT.WRAP);
			label.setText(labelText);
		    return label;
	    }
		
		protected Combo createCombo(Composite parent) {
			Combo box = new Combo(parent, SWT.READ_ONLY);
			box.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					String name = combo.getText();
					layout.topControl = treeMap.get(name);
					area.layout();
				}
			});
			return box;
		}

		public void addItem(String label, LaunchConfigurationFilteredTree tree) {
			treeMap.put(label, tree);
			combo.add(label);
			if (layout.topControl == null) {
				layout.topControl = tree;
				combo.setText(label);
			}
		}
		
		public Composite getStackParent() {
			return area;
		}

		public Combo getCombo() {
			return combo;
		}

		public Control getTopControl() {
			return layout != null ? layout.topControl : null; 
		}

		public void setSelection(String label) {
			combo.setText(label);
			chooseTree(label);
		}

		private void chooseTree(String label) {
			layout.topControl = treeMap.get(label);
			getStackParent().layout();
		}
	}
}
