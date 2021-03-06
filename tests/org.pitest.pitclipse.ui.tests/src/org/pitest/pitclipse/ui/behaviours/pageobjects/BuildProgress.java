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
import org.eclipse.swtbot.swt.finder.utils.SWTBotPreferences;
import org.pitest.pitclipse.ui.swtbot.WaitForBuildCondition;

public class BuildProgress {

    private static final long BUILD_TIMEOUT = SWTBotPreferences.TIMEOUT;
    private final SWTWorkbenchBot bot;
    private WaitForBuildCondition buildCompleteCondition;

    public BuildProgress(SWTWorkbenchBot bot) {
        this.bot = bot;
    }

    public void waitForBuild() {
        try {
            bot.waitUntil(buildCompleteCondition, BUILD_TIMEOUT);
        } finally {
            buildCompleteCondition.unsubscribe();
            buildCompleteCondition = null;
        }
    }

    public void listenForBuild() {
        buildCompleteCondition = new WaitForBuildCondition();
        buildCompleteCondition.subscribe();
    }

}
