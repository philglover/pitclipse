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

package org.pitest.pitclipse.ui.behaviours.pageobjects;

import org.eclipse.swtbot.eclipse.finder.SWTWorkbenchBot;
import org.eclipse.swtbot.eclipse.finder.widgets.SWTBotView;
import org.eclipse.swtbot.swt.finder.finders.UIThreadRunnable;
import org.eclipse.swtbot.swt.finder.results.VoidResult;
import static org.pitest.pitclipse.ui.view.PitView.BACK_BUTTON_TEXT;
import static org.pitest.pitclipse.ui.view.PitView.FORWARD_BUTTON_TEXT;
import static org.pitest.pitclipse.ui.view.PitView.HOME_BUTTON_TEXT;

public class PitSummaryView {
    private static String SUMMARY_VIEW_TITLE = "PIT Summary";
    private SWTWorkbenchBot bot;
    private SWTBotView summaryView;

    public PitSummaryView(SWTWorkbenchBot bot) {
        this.bot = bot;
    }

    public void showViewIfNotOpen() {
        if (summaryView == null) {
            summaryView = bot.viewByTitle(SUMMARY_VIEW_TITLE);
        }
        summaryView.show();
    }

    public void clickButtonWithText(String text) {
        showViewIfNotOpen();
        bot.toolbarButton(text).click();
    }

    public String getCurrentBrowserUrl() {
        showViewIfNotOpen();
        return summaryView.bot().browser().getUrl();
    }

    public String clickBack() {
        clickButtonWithText(BACK_BUTTON_TEXT);
        summaryView.bot().browser().waitForPageLoaded();
        return getCurrentBrowserUrl();
    }

    public String clickHome() {
        clickButtonWithText(HOME_BUTTON_TEXT);
        summaryView.bot().browser().waitForPageLoaded();
        return getCurrentBrowserUrl();
    }

    public String clickForward() {
        clickButtonWithText(FORWARD_BUTTON_TEXT);
        summaryView.bot().browser().waitForPageLoaded();
        return getCurrentBrowserUrl();
    }

    /**
     * Sets the link in the browser via javaScript, because bot can't click links in
     * browser
     * @param hyperLinkText link which to set as url, can be relative
     * @return new url of browser
     */
    public String setLink(String hyperLinkText) {
        showViewIfNotOpen();
        // use this instead of the SWTBrowser.execute(), because we need syncExec
        final String link = "window.location.href = '" + hyperLinkText + "'";
        summaryView.bot().browser().waitForPageLoaded();
        UIThreadRunnable.syncExec(new VoidResult() {
            @Override
            public void run() {
                summaryView.bot().browser().widget.execute(link);
            }
        });
        summaryView.bot().browser().waitForPageLoaded();
        return getCurrentBrowserUrl();
    }

    /**
     * Closes the view, if it is opened.
     */
    public void closeView() {
        if (summaryView != null) {
            showViewIfNotOpen();
            summaryView.close();
        }
    }
}
