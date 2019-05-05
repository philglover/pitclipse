/*******************************************************************************
 * Copyright 2012-2019 Phil Glover and contributors
 *  
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License.  You may obtain a copy
 * of the License at
 *  
 *  http://www.apache.org/licenses/LICENSE-2.0
 *  
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the
 * License for the specific language governing permissions and limitations under
 * the License.
 ******************************************************************************/

package org.pitest.pitclipse.preferences.ui;

import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.RadioGroupFieldEditor;
import org.eclipse.jface.preference.StringFieldEditor;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.pitest.pitclipse.core.PitCoreActivator;
import org.pitest.pitclipse.runner.config.PitExecutionMode;

import static org.pitest.pitclipse.core.preferences.PitPreferences.AVOID_CALLS_FROM_PIT;
import static org.pitest.pitclipse.core.preferences.PitPreferences.AVOID_CALLS_TO;
import static org.pitest.pitclipse.core.preferences.PitPreferences.EXCLUDED_CLASSES;
import static org.pitest.pitclipse.core.preferences.PitPreferences.EXCLUDED_METHODS;
import static org.pitest.pitclipse.core.preferences.PitPreferences.EXCLUDE_CLASSES_FROM_PIT;
import static org.pitest.pitclipse.core.preferences.PitPreferences.EXCLUDE_METHODS_FROM_PIT;
import static org.pitest.pitclipse.core.preferences.PitPreferences.INCREMENTAL_ANALYSIS;
import static org.pitest.pitclipse.core.preferences.PitPreferences.MUTATION_TESTS_RUN_IN_PARALLEL;
import static org.pitest.pitclipse.core.preferences.PitPreferences.PIT_EXECUTION_MODE;
import static org.pitest.pitclipse.core.preferences.PitPreferences.PIT_TIMEOUT;
import static org.pitest.pitclipse.core.preferences.PitPreferences.PIT_TIMEOUT_FACTOR;
import static org.pitest.pitclipse.core.preferences.PitPreferences.RUN_IN_PARALLEL;
import static org.pitest.pitclipse.core.preferences.PitPreferences.TIMEOUT;
import static org.pitest.pitclipse.core.preferences.PitPreferences.TIMEOUT_FACTOR;
import static org.pitest.pitclipse.core.preferences.PitPreferences.USE_INCREMENTAL_ANALYSIS;
import static org.pitest.pitclipse.runner.config.PitExecutionMode.values;

/**
 * This class represents a preference page that is contributed to the
 * Preferences dialog. By subclassing <samp>FieldEditorPreferencePage</samp>, we
 * can use the field support built into JFace that allows us to create a page
 * that is small and knows how to save, restore and apply itself.
 * <p>
 * This page is used to modify preferences only. They are stored in the
 * preference store that belongs to the main plug-in class. That way,
 * preferences can be accessed directly via the preference store.
 */

public class PitPreferencePage extends FieldEditorPreferencePage implements IWorkbenchPreferencePage {

    public PitPreferencePage() {
        super(GRID);
        setPreferenceStore(PitCoreActivator.getDefault().getPreferenceStore());
        setDescription("Pitclipse Preferences");
    }

    /**
     * Creates the field editors. Field editors are abstractions of the common
     * GUI blocks needed to manipulate various types of preferences. Each field
     * editor knows how to save and restore itself.
     */
    @Override
    public void createFieldEditors() {
        createExecutionModeRadioButtons();
        createRunInParallelOption();
        createUseIncrementalAnalysisOption();
        createExcludeClassesField();
        createExcludeMethodsField();
        createAvoidCallsToField();
        createPitTimeoutField();
        createPitTimeoutFactorField();
    }

    private void createAvoidCallsToField() {
        addField(new StringFieldEditor(AVOID_CALLS_TO, AVOID_CALLS_FROM_PIT, getFieldEditorParent()));
    }

    private void createExcludeClassesField() {
        addField(new StringFieldEditor(EXCLUDED_CLASSES, EXCLUDE_CLASSES_FROM_PIT, getFieldEditorParent()));
    }

    private void createExcludeMethodsField() {
        addField(new StringFieldEditor(EXCLUDED_METHODS, EXCLUDE_METHODS_FROM_PIT, getFieldEditorParent()));
    }

    private void createUseIncrementalAnalysisOption() {
        addField(new BooleanFieldEditor(INCREMENTAL_ANALYSIS, USE_INCREMENTAL_ANALYSIS, getFieldEditorParent()));
    }

    private void createRunInParallelOption() {
        addField(new BooleanFieldEditor(RUN_IN_PARALLEL, MUTATION_TESTS_RUN_IN_PARALLEL, getFieldEditorParent()));
    }

    private void createPitTimeoutField() {
        addField(new StringFieldEditor(TIMEOUT, PIT_TIMEOUT, getFieldEditorParent()));
    }

    private void createPitTimeoutFactorField() {
        addField(new StringFieldEditor(TIMEOUT_FACTOR, PIT_TIMEOUT_FACTOR, getFieldEditorParent()));
    }

    private void createExecutionModeRadioButtons() {
        PitExecutionMode[] values = values();
        String[][] executionModeValues = new String[values.length][2];
        for (int i = 0; i < values.length; i++) {
            executionModeValues[i] = new String[] { values[i].getLabel(), values[i].getId() };
        }
        addField(new RadioGroupFieldEditor(PIT_EXECUTION_MODE, "Pit execution scope", 1, executionModeValues, getFieldEditorParent()));
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.eclipse.ui.IWorkbenchPreferencePage#init(org.eclipse.ui.IWorkbench)
     */
    @Override
    public void init(IWorkbench workbench) {
        // Not implemented - not special initialising needed
    }
}
