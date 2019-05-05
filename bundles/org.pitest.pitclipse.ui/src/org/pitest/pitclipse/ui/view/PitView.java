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

package org.pitest.pitclipse.ui.view;

import org.eclipse.swt.SWT;
import org.eclipse.swt.SWTError;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.ui.part.ViewPart;

import java.io.File;

public class PitView extends ViewPart implements SummaryView {
    private Browser browser = null;
    private PitUiUpdatePublisher publisher = null;

    @Override
    public synchronized void createPartControl(Composite parent) {
        try {
            browser = new Browser(parent, SWT.NONE);
            publisher = new PitUiUpdatePublisher(browser);
            browser.addProgressListener(publisher);
        } catch (SWTError e) {
            MessageBox messageBox = new MessageBox(parent.getShell(), SWT.ICON_ERROR | SWT.OK);
            messageBox.setMessage("Browser cannot be initialized.");
            messageBox.setText("Exit");
            messageBox.open();
        }
    }

    @Override
    public void setFocus() {
    }

    @Override
    public synchronized void update(File result) {
        if (result == null) {
            browser.setText("<html/>");
        } else {
            browser.setUrl(result.toURI().toString());
        }
    }
}
