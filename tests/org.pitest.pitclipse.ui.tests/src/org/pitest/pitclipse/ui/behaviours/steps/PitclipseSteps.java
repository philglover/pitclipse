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

package org.pitest.pitclipse.ui.behaviours.steps;

import static com.google.common.collect.ImmutableSet.copyOf;
import static java.lang.Integer.parseInt;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.collection.IsEmptyCollection.empty;
import static org.hamcrest.number.OrderingComparison.greaterThan;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.pitest.pitclipse.ui.behaviours.pageobjects.PageObjects.PAGES;
import static org.pitest.pitclipse.ui.util.AssertUtil.assertDoubleEquals;

import java.io.File;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.swtbot.swt.finder.exceptions.WidgetNotFoundException;
import org.eclipse.swtbot.swt.finder.widgets.TimeoutException;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;
import org.pitest.pitclipse.runner.PitOptions;
import org.pitest.pitclipse.runner.results.DetectionStatus;
import org.pitest.pitclipse.ui.behaviours.pageobjects.PackageContext;
import org.pitest.pitclipse.ui.swtbot.PitResultNotifier.PitSummary;

import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableList;

import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import io.cucumber.datatable.DataTable;

public class PitclipseSteps {

    @When("test {word} in package {word} is run for project {word}")
    public void runTest(final String testClassName, final String packageName, final String projectName)
            throws CoreException {
        // No need to do a full build: we should be now synchronized with building
        // Build the whole workspace to prevent random compilation failures
        // ResourcesPlugin.getWorkspace().build(IncrementalProjectBuilder.FULL_BUILD,
        // new NullProgressMonitor());
        System.out.println(String.format("Run PIT on: %s %s.%s", projectName, packageName, testClassName));
        runPit(new SelectTestClass(testClassName, packageName, projectName));
    }

    @Then("a coverage report is generated with {int} class/classes tested with overall coverage of {int}% and mutation coverage of {int}%")
    public void coverageReportGenerated(int classes, double totalCoverage, double mutationCoverage) {
        PAGES.views().waitForTestsAreRunOnConsole();
        try {
            assertEquals("Number of tested classes mismatch", classes, PitSummary.INSTANCE.getClasses());
            assertDoubleEquals("Total coverage mismatch", totalCoverage, PitSummary.INSTANCE.getCodeCoverage());
            assertDoubleEquals("Mutation coverage mismatch", mutationCoverage,
                    PitSummary.INSTANCE.getMutationCoverage());
        } catch (Error e) {
            e.printStackTrace();
            throw e;
        }
    }

    @Then("the mutation results are")
    public void mutationsAre(DataTable tableOfMutations) {
        List<PitMutation> expectedMutations = mutationsFromExampleTable(tableOfMutations);
        List<PitMutation> actualMutations = PAGES.getPitMutationsView().getMutations();
        assertThat(actualMutations, is(equalTo(expectedMutations)));
    }

    @When("the following mutation is selected")
    public void mutationIsSelected(DataTable tableOfMutations) {
        PitMutation mutation = mutationsFromExampleTable(tableOfMutations).get(0);
        doubleClickMutationInMutationsView(mutation);
    }

    public void doubleClickMutationInMutationsView(PitMutation mutation) {
        PAGES.getPitMutationsView()
             .select(mutation);
    }

    @Then("the file {string} is opened at line number {int}")
    public void mutationIsOpened(String fileName, int lineNumber) {
        FilePosition position = PAGES.getPitMutationsView().getLastSelectedMutation(fileName);
        assertThat(position.className, is(equalTo(fileName)));
        assertThat(position.lineNumber, is(equalTo(lineNumber)));
    }

    @When("the PIT views are opened")
    public void thePitViewsAreOpened() {
        PAGES.getWindowsMenu().openPitSummaryView();
        PAGES.getWindowsMenu().openPitMutationsView();
    }

    @When("the Console view is closed")
    public void closeTheConsole() {
        PAGES.views().closeConsole();
    }

    @When("tests in package {word} are run for project {word}")
    public void runPackageTest(final String packageName, final String projectName) {
        System.out.println
            (String.format("Run PIT on: %s %s",
                projectName, packageName));
        runPit(new SelectPackage(packageName, projectName));
    }

    @When("tests in source root {word} are run for project {word}")
    public void runPackageRootTest(final String packageRoot, final String projectName) {
        System.out.println
            (String.format("Run PIT on: %s %s",
                projectName, packageRoot));
        runPit(new SelectPackageRoot(packageRoot, projectName));
    }

    @When("tests are run for project {word}")
    public void runProjectTest(String projectName) {
        System.out.println
            (String.format("Run PIT on: %s",
                projectName));
        runPit(new SelectProject(projectName));
    }

    @Then("the options passed to Pit match(:)")
    public void runtimeOptionsMatch(DataTable configTable) {
        PitOptions options = PAGES.getRunMenu().getLastUsedPitOptions();
        assertThat(configTable.height(), is(greaterThan(0)));
        assertThat(options, match(configTable.asMaps().get(0)));
    }

    public void runtimeOptionsMatch(Map<String, String> configMap) {
        PitOptions options = PAGES.getRunMenu().getLastUsedPitOptions();
        assertThat(options, match(configMap));
    }

    private void runPit(Runnable runnable) {
        assertNoErrorsInWorkspace();
        // make sure to clear the console to avoid interferences
        // with the output of previous runs
        PAGES.views().clearConsole();
        // make sure summary is cleared
        PitSummary.INSTANCE.reset();
        int retryCount = 20;
        int counter = 0;
        while (counter < retryCount) {
            try {
                runnable.run();
                PAGES.getRunMenu().runPit();
                return;
            } catch (TimeoutException te) {
                counter++;
            } catch (WidgetNotFoundException wfne) {
                counter++;
            }
        }
    }
    
    public void assertNoErrorsInWorkspace() {
        Set<String> errors = errorsInWorkspace();
        assertThat(errors, empty());
    }
    
    /**
     * Makes sure there are no errors in the Workspace, by accessing the error
     * markers directly.
     * 
     * IMPORTANT: do not retrieve errors from the "Problems" view because that view
     * might be updated asynchronously: we might lose errors and when running the
     * tests an error Dialog will popup, making the tests flaky.
     * 
     * @return
     */
    private final Set<String> errorsInWorkspace() {
        try {
            return getErrorMarkers().stream()
                .map(it -> {
                    try {
                        return it.getAttribute(IMarker.MESSAGE).toString();
                    } catch (CoreException e) {
                        return "";
                    }
                })
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toSet());
        } catch (CoreException e) {
            throw new IllegalStateException(e);
        }
    }
    
    public List<IMarker> getErrorMarkers() throws CoreException {
        return Stream.of(getMarkers())
            .filter(it -> it.getAttribute(IMarker.SEVERITY, IMarker.SEVERITY_INFO) == IMarker.SEVERITY_ERROR)
            .collect(Collectors.toList());
    }
    
    private IMarker[] getMarkers() throws CoreException {
        return ResourcesPlugin.getWorkspace().getRoot().findMarkers(IMarker.PROBLEM, true, IResource.DEPTH_INFINITE);
    }
    
    private List<PitMutation> mutationsFromExampleTable(DataTable tableOfMutations) {
        List<Map<String, String>> asMaps = tableOfMutations.asMaps();
        ImmutableList.Builder<PitMutation> projectsBuilder = ImmutableList.builder();
        for (Map<String, String> mutationRow : asMaps) {
            DetectionStatus status = DetectionStatus.valueOf(mutationRow.get("status"));
            String project = mutationRow.get("project");
            String pkg = mutationRow.get("package");
            String className = mutationRow.get("class");
            int line = parseInt(mutationRow.get("line"));
            String mutation = mutationRow.get("mutation");
            PitMutation pitMutation = PitMutation.builder().withStatus(status).withProject(project).withPackage(pkg)
                    .withClassName(className).withLineNumber(line).withMutation(mutation).build();
            projectsBuilder.add(pitMutation);
        }
        return projectsBuilder.build();
    }

    private Matcher<PitOptions> match(final Map<String, String> optionRow) {
        return new TypeSafeMatcher<PitOptions>() {
            @Override
            public void describeTo(Description description) {
                description.appendText("Matches: ").appendValue(optionRow);
            }

            @Override
            public boolean matchesSafely(PitOptions options) {
                boolean match = true;
                match &= checkStringMatch(match, optionRow, "classUnderTest", options.getClassUnderTest());
                match &= checkSetMatch(match, optionRow, "classesToMutate", options.getClassesToMutate());
                match &= checkClasspathMatch(match, optionRow, "projects", options.getSourceDirectories());
                match &= checkSetMatch(match, optionRow, "excludedClasses", options.getExcludedClasses());
                match &= checkSetMatch(match, optionRow, "excludedMethods", options.getExcludedMethods());
                match &= checkSetMatch(match, optionRow, "packagesUnderTest", options.getPackages());
                match &= checkBooleanMatch(match, optionRow, "runInParallel",
                        options.getThreads() == Runtime.getRuntime().availableProcessors());
                match &= checkBooleanMatch(match, optionRow, "incrementalAnalysis",
                        options.getHistoryLocation() != null);
                match &= checkIntMatch(match, optionRow, "timeoutConst", options.getTimeout());
                match &= checkBigDecimalMatch(match, optionRow, "timeoutFactor", options.getTimeoutFactor());
                return match;
            }

            private boolean checkClasspathMatch(boolean match, Map<String, String> optionRow, String key,
                    List<File> sourceDirectories) {
                if (match && optionRow.containsKey(key)) {
                    List<String> paths = ImmutableList
                            .copyOf(Splitter.on(',').trimResults().omitEmptyStrings().split(optionRow.get(key)));
                    if (paths.size() == sourceDirectories.size()) {
                        boolean allMatch = true;
                        for (int i = 0; i < paths.size(); i++) {
                            allMatch &= sourceDirectories.get(i).toString().contains(paths.get(i));
                        }
                        return allMatch;
                    } else {
                        return false;
                    }
                }
                return match;
            }

            private boolean checkStringMatch(boolean match, Map<String, String> optionRow, String key, String value) {
                if (match && optionRow.containsKey(key)) {
                    return optionRow.get(key).equals(value);
                }
                return match;
            }

            private boolean checkBooleanMatch(boolean match, Map<String, String> optionRow, String key,
                    boolean actualValue) {
                if (match && optionRow.containsKey(key)) {
                    boolean expectedValue = Boolean.valueOf(optionRow.get(key));
                    return expectedValue == actualValue;
                }
                return match;
            }

            private boolean checkIntMatch(boolean match, Map<String, String> optionRow, String key, int actualValue) {
                if (match && optionRow.containsKey(key)) {
                    int expectedValue = Integer.valueOf(optionRow.get(key));
                    return expectedValue == actualValue;
                }
                return match;
            }

            private boolean checkBigDecimalMatch(boolean match, Map<String, String> optionRow, String key,
                    BigDecimal actualValue) {
                if (match && optionRow.containsKey(key)) {
                    BigDecimal expectedValue = new BigDecimal(optionRow.get(key));
                    return expectedValue.compareTo(actualValue) == 0;
                }
                return match;
            }

            private boolean checkSetMatch(boolean match, Map<String, String> optionRow, String key,
                    List<String> actualValues) {
                if (match && optionRow.containsKey(key)) {
                    Set<String> expectedValues = copyOf(
                            Splitter.on(',').trimResults().omitEmptyStrings().split(optionRow.get(key)));
                    return expectedValues.equals(copyOf(actualValues));
                }
                return match;
            }
        };
    }

    private static class SelectProject implements Runnable {
        private final String projectName;

        public SelectProject(String projectName) {
            this.projectName = projectName;
        }

        @Override
        public void run() {
            PAGES.getPackageExplorer().selectProject(projectName);
        }
    }

    private static final class SelectPackageRoot implements Runnable {
        private final PackageRootSelector context;

        private SelectPackageRoot(String packageRoot, String projectName) {
            context = new PackageRootSelector(projectName, packageRoot);
        }

        @Override
        public void run() {
            PAGES.getPackageExplorer().selectPackageRoot(context);
        }
        
        private static final class PackageRootSelector implements PackageContext {
            private final String projectName;
            private final String packageRoot;

            public PackageRootSelector(String projectName, String packageRoot) {
                this.projectName = projectName;
                this.packageRoot = packageRoot;
            }

            @Override
            public String getPackageName() {
                return null;
            }

            @Override
            public String getProjectName() {
                return projectName;
            }

            @Override
            public String getSourceDir() {
                return packageRoot;
            }
        }
    }

    private static final class SelectPackage implements Runnable {
        private final PackageSelector context;

        private SelectPackage(String packageName, String projectName) {
            context = new PackageSelector(projectName, packageName);
        }

        @Override
        public void run() {
            PAGES.getPackageExplorer().selectPackage(context);
        }
        
        private static final class PackageSelector implements PackageContext {
            private final String projectName;
            private final String packageName;

            public PackageSelector(String projectName, String packageName) {
                this.projectName = projectName;
                this.packageName = packageName;
            }

            @Override
            public String getPackageName() {
                return packageName;
            }

            @Override
            public String getProjectName() {
                return projectName;
            }

            @Override
            public String getSourceDir() {
                return null;
            }
        }
    }

    private static final class SelectTestClass implements Runnable {
        private final String testClassName;
        private final String packageName;
        private final String projectName;

        private SelectTestClass(String testClassName, String packageName, String projectName) {
            this.testClassName = testClassName;
            this.packageName = packageName;
            this.projectName = projectName;
        }

        @Override
        public void run() {
            PAGES.getPackageExplorer().selectClass(testClassName, packageName, projectName);
        }
    }
}
