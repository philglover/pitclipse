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

import java.util.List;

import org.eclipse.swtbot.eclipse.finder.SWTWorkbenchBot;
import org.eclipse.swtbot.eclipse.finder.widgets.SWTBotView;
import org.eclipse.swtbot.swt.finder.SWTBot;
import org.eclipse.swtbot.swt.finder.waits.ICondition;

public class Views {

    private final SWTWorkbenchBot bot;

    public Views(SWTWorkbenchBot bot) {
        this.bot = bot;
    }

    public void closeConsole() {
        List<SWTBotView> allViews = bot.views();
        for (SWTBotView view : allViews) {
            if ("Console".equals(view.getTitle())) {
                view.close();
            }
        }
    }

    /**
     * If the Console view can be found then clear it, to make sure we don't get
     * output from previous test runs
     */
    public void clearConsole() {
        List<SWTBotView> allViews = bot.views();
        for (SWTBotView view : allViews) {
            if ("Console".equals(view.getTitle())) {
                view.show();
                if (!view.bot().styledText().getText().isEmpty()) {
                    // use the toolbar button instead of .bot().styledText().setText("")
                    // which does not seem to work synchronously
                    view.toolbarButton("Clear Console").click();
                }
                return;
            }
        }
    }

    public void waitForTestsAreRunOnConsole() {
        System.out.println("Waiting for PIT to finish on Console...");
        bot.waitUntil(new ICondition() {
            static final String EXPECTED_END_STRING = "tests per mutation)";
            String currentText = "";
            long start = System.currentTimeMillis();

            @Override
            public boolean test() {
                currentText = showConsole().bot()
                    .styledText().getText()
                    .trim();
                final String end = currentText
                    .substring(
                        currentText.length() - EXPECTED_END_STRING.length(),
                        currentText.length());
                System.out.print("Console ends with: " + end);
                boolean matched = EXPECTED_END_STRING.equals(end);
                System.out.println
                    ("... " +
                     (System.currentTimeMillis() - start) + "ms" +
                     (matched ? " OK!" : ""));
                return matched;
            }

            @Override
            public void init(SWTBot bot) {
            }

            @Override
            public String getFailureMessage() {
                return "Console View does not end with '" + EXPECTED_END_STRING + "'\n:"
                        + "CURRENT CONSOLE TEXT:\n"
                        + currentText;
            }
        });
    }

    public SWTBotView showConsole() {
        SWTBotView consoleView = bot.viewByPartName("Console");
        consoleView.show();
        return consoleView;
    }

}
