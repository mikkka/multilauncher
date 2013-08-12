package org.mtkachev.eclipse.plugins.multilauncher.ui;

import java.util.Iterator;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.internal.ui.DebugPluginImages;
import org.eclipse.debug.ui.AbstractLaunchConfigurationTab;
import org.eclipse.jface.viewers.BaseLabelProvider;
import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.CheckboxTreeViewer;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.jface.viewers.ICheckStateProvider;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.*;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DragSource;
import org.eclipse.swt.dnd.DragSourceEvent;
import org.eclipse.swt.dnd.DragSourceListener;
import org.eclipse.swt.dnd.DropTarget;
import org.eclipse.swt.dnd.DropTargetAdapter;
import org.eclipse.swt.dnd.DropTargetEvent;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.*;
import org.eclipse.swt.widgets.*;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;
import org.mtkachev.eclipse.plugins.multilauncher.PluginMessages;
import org.mtkachev.eclipse.plugins.multilauncher.internal.MultilauncherConfigurationDelegate;
import org.mtkachev.eclipse.plugins.multilauncher.internal.SublaunchConfiguration;
import org.mtkachev.eclipse.plugins.multilauncher.internal.SublaunchConfigurationRepo;

public class LaunchConfigurationTab extends AbstractLaunchConfigurationTab {

	private final SublaunchConfigurationRepo sublaunchConfigurationRepo;
	private final String mode;

	private CheckboxTreeViewer treeViewer;
	private List<SublaunchConfiguration> sublaunchConfigurationsList;
	
	private Button addButton;
	private Button editButton;
	private Button removeButton;
	
	private final SelectionAdapter addButtonSelectListener = new SelectionAdapter() {
	    @Override
	    public void widgetSelected(SelectionEvent e) {
			LaunchConfigurationDialog dialog = 
					LaunchConfigurationDialog.createDialog(
							treeViewer.getControl().getShell(), mode, false);
				if (dialog.open() == Dialog.OK) {
					ILaunchConfiguration[] configs = dialog.getSelectedLaunchConfigurations();
					if (configs.length < 1) 
						return;
					for (ILaunchConfiguration config : configs) {
						SublaunchConfiguration subConfiguration = new SublaunchConfiguration(
								config.getName(), 
								dialog.getMode(), 
								config, 
								true, 
								dialog.isWaitForTerminate(), 
								dialog.getPauseBeforeNextInSecs()
						);
						sublaunchConfigurationsList.add(subConfiguration);
						treeViewer.refresh(true);
						treeViewer.setChecked(subConfiguration, true);
					}
					treeViewer.refresh();
					updateButtonsState();
					updateLaunchConfigurationDialog();
				}
	    }
	};
	
	private final SelectionListener editButtonSelectListener = new SelectionAdapter() {
	    @Override
	    public void widgetSelected(SelectionEvent e) {
			StructuredSelection selection = (StructuredSelection) treeViewer.getSelection();
			if(selection.size() == 1) {
				SublaunchConfiguration conf = (SublaunchConfiguration) selection.iterator().next();
				LaunchConfigurationDialog dialog = 
						LaunchConfigurationDialog.createDialog(treeViewer.getControl().getShell(), conf);
				if (dialog.open() == Dialog.OK) {
					ILaunchConfiguration[] confs = dialog.getSelectedLaunchConfigurations();
					if (confs.length == 1) {
						//TODO: use directly SublaunchConfiguration in dialog 
						conf.setLaunchRef(confs[0].getName());
						conf.setLaunchConfiguration(confs[0]);
						conf.setMode(dialog.getMode());
						conf.setWaitForTerminateAfetrLaunch(dialog.isWaitForTerminate());
						conf.setPauseBeforeNextInSecs(dialog.getPauseBeforeNextInSecs());
						treeViewer.refresh(true);
						updateButtonsState();
						updateLaunchConfigurationDialog();
					}
				}
			}
	    }
	};
	
	private final SelectionListener removeButtonSelectListener = new SelectionAdapter() {
	    @Override
	    public void widgetSelected(SelectionEvent e) {
			StructuredSelection selection = (StructuredSelection) treeViewer.getSelection();
			for (Iterator<?> iter = selection.iterator(); iter.hasNext(); ) {
				sublaunchConfigurationsList.remove(iter.next());
			}
			treeViewer.refresh();
			updateButtonsState();
			updateLaunchConfigurationDialog();
	    }
	};
	
	private final ISelectionChangedListener treeSelectionChangeListener = new ISelectionChangedListener() {
		@Override
		public void selectionChanged(SelectionChangedEvent event) {
			updateButtonsState();
		}
	};
	
	private final ICheckStateListener treeChecksListener = new ICheckStateListener(){
		@Override
		public void checkStateChanged(CheckStateChangedEvent event) {
			((SublaunchConfiguration)event.getElement()).setEnabled(event.getChecked());
			updateLaunchConfigurationDialog();
		}
	};

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
	
	class CheckStateProvider implements ICheckStateProvider {

		/* (non-Javadoc)
		 * @see org.eclipse.jface.viewers.ICheckStateProvider#isChecked(java.lang.Object)
		 */
		@Override
		public boolean isChecked(Object element) {
			if (element instanceof SublaunchConfiguration) {
				return ((SublaunchConfiguration)element).getEnabled();
			}
			return false;
		}

		/* (non-Javadoc)
		 * @see org.eclipse.jface.viewers.ICheckStateProvider#isGrayed(java.lang.Object)
		 */
		@Override
		public boolean isGrayed(Object element) {
			return false;
		}
	}
	
	static class LabelProvider extends BaseLabelProvider implements ITableLabelProvider {
		@Override
		public Image getColumnImage(Object element, int columnIndex) {
			if (element instanceof SublaunchConfiguration) {
				if (columnIndex == 0) {
					SublaunchConfiguration el = (SublaunchConfiguration) element;
					if (el.getLaunchConfiguration() == null || 
							!MultilauncherConfigurationDelegate.isValidLaunchReference(el.getLaunchConfiguration())) {
						Image errorImage = PlatformUI.getWorkbench().getSharedImages().getImage(ISharedImages.IMG_OBJS_ERROR_TSK);
						return errorImage;
					}
					
					try {
		                String key = el.getLaunchConfiguration().getType().getIdentifier();
		                return DebugPluginImages.getImage(key);
	                } catch (CoreException e) {
	                	Image errorImage = PlatformUI.getWorkbench().getSharedImages().getImage(ISharedImages.IMG_OBJS_ERROR_TSK);
						return errorImage;
	                }
				}
			}
			return null;
		}

		@Override
		public String getColumnText(Object element, int columnIndex) {
			if ((element instanceof SublaunchConfiguration)) {
				SublaunchConfiguration el = (SublaunchConfiguration) element;
				
				// launch name
				if (columnIndex == 0) {
					try {
						return (el.getLaunchConfiguration() != null) ? 
								el.getLaunchConfiguration().getType().getName() + "/" + el.getLaunchRef() : el.getLaunchRef();
					} catch (CoreException e) {
						return el.getLaunchRef();
					}
				}
	
				// launch mode 
				if (columnIndex == 1)
					return el.getMode();
				
				// launch post action
				if (columnIndex == 2) {
					String retval = "";
					if(el.isWaitForTerminateAfetrLaunch()) {
						retval = PluginMessages.LaunchConfigurationTab_Table_Mode_WT;
					}
					if(el.getPauseBeforeNextInSecs() != null && el.getPauseBeforeNextInSecs() > 0) {
						if(!retval.equals("")) {
							retval = retval + ", ";
						}
						retval = retval + 
								NLS.bind(PluginMessages.LaunchConfigurationTab_Table_Mode_Pause, el.getPauseBeforeNextInSecs());
					}
					return retval;
				}
			}
			return null;
		}
	}
	
	private int getSelectionCount() {
		return ((StructuredSelection)treeViewer.getSelection()).size();
	}

	private void updateButtonsState() {
		int selectionCount = getSelectionCount();
		editButton.setEnabled(selectionCount == 1);
		removeButton.setEnabled(selectionCount > 0);
	}

	@Override
	public void createControl(Composite parent) {
		final Composite comp = new Composite(parent, SWT.NONE);
		setControl(comp);
		comp.setLayout(new GridLayout(2, false));

		treeViewer = new CheckboxTreeViewer(comp, SWT.BORDER | SWT.MULTI | SWT.V_SCROLL | SWT.H_SCROLL);
		final Tree table = treeViewer.getTree();
		table.setFont(parent.getFont());
		
		treeViewer.setContentProvider(new ContentProvider());
		treeViewer.setCheckStateProvider(new CheckStateProvider());
		treeViewer.setLabelProvider(new LabelProvider());
		treeViewer.addCheckStateListener(treeChecksListener);
		
		table.setHeaderVisible(true);
		table.setLayoutData(new GridData(GridData.FILL_BOTH));
		TreeColumn col1 = new TreeColumn(table, SWT.NONE);
		col1.setText(PluginMessages.LaunchConfigurationTab_Table_Name); 
		col1.setWidth(300);
		TreeColumn col2 = new TreeColumn(table, SWT.NONE);
		col2.setText(PluginMessages.LaunchConfigurationTab_Table_Mode); 
		col2.setWidth(100);
		TreeColumn col3 = new TreeColumn(table, SWT.NONE);
		col3.setText(PluginMessages.LaunchConfigurationTab_Table_AfterLaunch); 
		col3.setWidth(100);
	
		treeViewer.setInput(sublaunchConfigurationsList);
		treeViewer.addSelectionChangedListener(treeSelectionChangeListener);
		
		Composite butComposite = new Composite(comp, SWT.NONE);
		butComposite.setLayout(new GridLayout());
		
		addButton = new Button(butComposite, SWT.PUSH);
		addButton.setText(PluginMessages.LaunchConfigurationTab_AddButton);
		addButton.setFont(parent.getFont());
		addButton.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		addButton.addSelectionListener(addButtonSelectListener); 

		editButton = new Button(butComposite, SWT.PUSH);
		editButton.setText(PluginMessages.LaunchConfigurationTab_EditButton);
		editButton.setFont(parent.getFont());
		editButton.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		editButton.addSelectionListener(editButtonSelectListener); 

		removeButton = new Button(butComposite, SWT.PUSH);
		removeButton.setText(PluginMessages.LaunchConfigurationTab_RemoveButton);
		removeButton.setFont(parent.getFont());
		removeButton.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		removeButton.addSelectionListener(removeButtonSelectListener); 

		GridData layoutData = new GridData(GridData.GRAB_VERTICAL);
		layoutData.verticalAlignment = SWT.BEGINNING;
		butComposite.setLayoutData(layoutData);
		
		addDragAndDropBehaviour(table);
		
		updateButtonsState();
	}
	
	private void addDragAndDropBehaviour(final Tree table) {
		Transfer[] types = new Transfer[] {TextTransfer.getInstance()};
		int operations = DND.DROP_MOVE;
		
		final DragSource source = new DragSource (table, operations);
		source.setTransfer(types);
		final TreeItem[] dragSourceItem = new TreeItem[1];
		source.addDragListener (new DragSourceListener () {
			public void dragStart(DragSourceEvent event) {
				TreeItem[] selection = table.getSelection();
				if (selection.length > 0 && selection[0].getItemCount() == 0) {
					event.doit = true;
					dragSourceItem[0] = selection[0];
				} else {
					event.doit = false;
				}
			};
			public void dragSetData (DragSourceEvent event) {
				event.data = dragSourceItem[0].getText();
			}
			public void dragFinished(DragSourceEvent event) {
				if (event.detail == DND.DROP_MOVE)
					dragSourceItem[0] = null;
			}
		});

		DropTarget target = new DropTarget(table, operations);
		target.setTransfer(types);
		target.addDropListener (new DropTargetAdapter() {
			public void dragOver(DropTargetEvent event) {
				event.feedback = DND.FEEDBACK_SCROLL;
				if (event.item != null) {
					TreeItem item = (TreeItem)event.item;
					Point pt = table.getDisplay().map(null, table, event.x, event.y);
					Rectangle bounds = item.getBounds();
					if (pt.y < bounds.y + bounds.height/3) {
						event.feedback |= DND.FEEDBACK_INSERT_BEFORE;
					} else if (pt.y > bounds.y + 2*bounds.height/3) {
						event.feedback |= DND.FEEDBACK_INSERT_AFTER;
					}
				}
			}
			
			private int idxByItem(TreeItem[] items, TreeItem item) {
				for (int i = 0; i < items.length; i++) {
					if (items[i] == item) {
						return i;
					}
				
				}
				return 0;
			}
			
			public void drop(DropTargetEvent event) {
				if (event.data == null) {
					event.detail = DND.DROP_NONE;
					return;
				}

				TreeItem[] items = table.getItems();
				TreeItem item = event.item != null ? (TreeItem)event.item : items[items.length - 1];
				TreeItem sourceItem = dragSourceItem[0];

				Point pt = table.getDisplay().map(null, table, event.x, event.y);
				Rectangle bounds = item.getBounds();

				int index = idxByItem(items, item);
				int indexSource = idxByItem(items, sourceItem);
				if(indexSource <= index) {
					index--;
				}
				
				if (pt.y < bounds.y + bounds.height/3) {
					sublaunchConfigurationsList.remove(indexSource);
					sublaunchConfigurationsList.add(index, (SublaunchConfiguration)sourceItem.getData());
				}  else if (pt.y > bounds.y + 2*bounds.height/3) {
					sublaunchConfigurationsList.remove(indexSource);
					sublaunchConfigurationsList.add(index + 1, (SublaunchConfiguration)sourceItem.getData());
				} else {
					SublaunchConfiguration from = sublaunchConfigurationsList.get(indexSource);
					SublaunchConfiguration to = sublaunchConfigurationsList.get(index + 1);
					sublaunchConfigurationsList.set(indexSource, to);
					sublaunchConfigurationsList.set(index + 1, from);
				}

				treeViewer.refresh();
				updateButtonsState();
				updateLaunchConfigurationDialog();
			}
		});	
	}

	@Override
	public void setDefaults(ILaunchConfigurationWorkingCopy configuration) {
	}

	@Override
	public void initializeFrom(ILaunchConfiguration configuration) {
		sublaunchConfigurationsList = sublaunchConfigurationRepo.loadSublaunchConfigList(configuration);
		if (treeViewer != null) {
			treeViewer.setInput(sublaunchConfigurationsList);
		}
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
		// all sub configs are valid
		setMessage(null);
		setErrorMessage(null);
		for (SublaunchConfiguration conf : sublaunchConfigurationsList) {
			if(conf.getLaunchConfiguration() == null) {
				return false;
			}
			if(!MultilauncherConfigurationDelegate.isValidLaunchReference(conf.getLaunchConfiguration())) {
				return false;
			}
		}
		return true;
	}

	public LaunchConfigurationTab(String mode, SublaunchConfigurationRepo sublaunchConfigurationRepo) {
		super();
		this.sublaunchConfigurationRepo = sublaunchConfigurationRepo;
		this.mode = mode;
	}
}
