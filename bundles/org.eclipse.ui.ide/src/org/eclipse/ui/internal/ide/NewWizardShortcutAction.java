/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal.ide;


import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.actions.ActionFactory;
import org.eclipse.ui.help.WorkbenchHelp;
//@issue illegal reference to org.eclipse.ui.internal.dialogs.WorkbenchWizardElement
import org.eclipse.ui.internal.dialogs.WorkbenchWizardElement;
import org.eclipse.ui.internal.registry.IPluginContribution;

/**
 *	Instances can launch arbitrary resource creation wizards
 *	that have been marked as being available as workbench shortcut
 *	items
 */
public class NewWizardShortcutAction extends Action implements IPluginContribution {
	private WorkbenchWizardElement wizardElement;
	private IWorkbenchWindow window;
	/**
	 *	Create an instance of this class.  Use this constructor if you do
	 *	not wish to pre-specify the selection that should be provided to
	 *	launched shortcut wizards.
	 *
	 *	@param element WorkbenchWizardElement
	 */
	public NewWizardShortcutAction(IWorkbenchWindow window, WorkbenchWizardElement element) {
		super(element.getLabel(element));
		setToolTipText(element.getDescription());
		setImageDescriptor(element.getImageDescriptor());
		setId(ActionFactory.NEW.getId());
		wizardElement = element;
		this.window = window;
	}
	/**
	 *	This action has been invoked by the user
	 *
	 *	@param context Window
	 */
	public void run() {
		// create instance of target wizard

		INewWizard wizard;
		try {
			wizard = (INewWizard) wizardElement.createExecutableExtension();
		} catch (CoreException e) {
			ErrorDialog.openError(
				window.getShell(),
				IDEWorkbenchMessages.getString("NewWizardShortcutAction.errorTitle"), //$NON-NLS-1$
				IDEWorkbenchMessages.getString("NewWizardShortcutAction.errorMessage"), //$NON-NLS-1$
				e.getStatus());
			return;
		}

		ISelection selection = window.getSelectionService().getSelection();
		IStructuredSelection selectionToPass = StructuredSelection.EMPTY;
		if (selection instanceof IStructuredSelection) {
			selectionToPass = wizardElement.adaptedSelection((IStructuredSelection) selection);
		} else {
			// Build the selection from the IFile of the editor
			IWorkbenchPart part = window.getPartService().getActivePart();
			if (part instanceof IEditorPart) {
				IEditorInput input = ((IEditorPart) part).getEditorInput();
				if (input instanceof IFileEditorInput) {
					selectionToPass = new StructuredSelection(((IFileEditorInput) input).getFile());
				}
			}
		}

		wizard.init(window.getWorkbench(), selectionToPass);

		Shell parent = window.getShell();
		WizardDialog dialog = new WizardDialog(parent, wizard);
		dialog.create();
		WorkbenchHelp.setHelp(dialog.getShell(), IHelpContextIds.NEW_WIZARD_SHORTCUT);
		dialog.open();
	}
	/* (non-Javadoc)
	 * @see org.eclipse.ui.internal.registry.IPluginContribution#fromPlugin()
	 */
	public boolean fromPlugin() {		
		return wizardElement.fromPlugin();
	}
	/* (non-Javadoc)
	 * @see org.eclipse.ui.internal.registry.IPluginContribution#getLocalId()
	 */
	public String getLocalId() {
		return wizardElement.getLocalId();
	}
	/* (non-Javadoc)
	 * @see org.eclipse.ui.internal.registry.IPluginContribution#getPluginId()
	 */
	public String getPluginId() {		
		return wizardElement.getPluginId();
	}
}
