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

package org.pitest.pitclipse.ui.swtbot;

import java.io.IOException;

import org.pitest.pitclipse.core.extension.point.ResultNotifier;
import org.pitest.pitclipse.runner.PitResults;
import org.pitest.pitclipse.ui.swtbot.ResultsParser.Summary;

public class PitResultNotifier implements ResultNotifier<PitResults> {
    public enum PitSummary {
        INSTANCE;

        private Summary summary;

        /**
         * @return covered classes or 0 if no summary is present
         */
        public int getClasses() {
            if (summary != null) {
                return summary.getClasses();
            }
            return 0;
        }

        /**
         * @return code coverage or 100 if no summary is present
         */
        public double getCodeCoverage() {
            if (summary != null) {
                return summary.getCodeCoverage();
            }
            return 100;
        }

        /**
         * @return mutation coverage or 100 if no summary is present
         */
        public double getMutationCoverage() {
            if (summary != null) {
                return summary.getMutationCoverage();
            }
            return 100;
        }

        public void reset() {
            summary = null;
        }

        void setSummary(Summary summary) {
            this.summary = summary;
        }
    }

    @Override
    public void handleResults(PitResults results) {
        try {
            // file only exists, if mutations were done
            if (results.getHtmlResultFile() != null) {
                PitSummary.INSTANCE.setSummary(new ResultsParser(results.getHtmlResultFile()).getSummary());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
