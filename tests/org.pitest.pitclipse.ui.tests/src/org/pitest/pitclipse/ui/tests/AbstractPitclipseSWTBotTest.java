/*******************************************************************************
 * Copyright 2021 Lorenzo Bettini and contributors
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
package org.pitest.pitclipse.ui.tests;

import static java.lang.Integer.parseInt;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.pitest.pitclipse.ui.behaviours.pageobjects.PageObjects.PAGES;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationType;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swtbot.eclipse.finder.SWTWorkbenchBot;
import org.eclipse.swtbot.eclipse.finder.widgets.SWTBotEditor;
import org.eclipse.swtbot.eclipse.finder.widgets.SWTBotView;
import org.eclipse.swtbot.swt.finder.junit.SWTBotJunit4ClassRunner;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.WorkbenchException;
import org.eclipse.ui.intro.IIntroManager;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.pitest.pitclipse.core.PitCoreActivator;
import org.pitest.pitclipse.launch.ui.PitLaunchShortcut;
import org.pitest.pitclipse.runner.results.DetectionStatus;
import org.pitest.pitclipse.ui.behaviours.pageobjects.ConcreteClassContext;
import org.pitest.pitclipse.ui.behaviours.pageobjects.PitRunConfiguration;
import org.pitest.pitclipse.ui.behaviours.steps.LaunchConfigurationSteps;
import org.pitest.pitclipse.ui.behaviours.steps.PitMutation;
import org.pitest.pitclipse.ui.behaviours.steps.PitclipseSteps;
import org.pitest.pitclipse.ui.util.ProjectImportUtil;

/**
 * @author Lorenzo Bettini
 * 
 */
@RunWith(SWTBotJunit4ClassRunner.class)
public abstract class AbstractPitclipseSWTBotTest {
    protected static SWTWorkbenchBot bot;
    private static int statusIndex = 0;
    private static int projectIndex = 1;
    private static int packageIndex = 2;
    private static int classIndex = 3;
    private static int lineIndex = 4;
    private static int mutationIndex = 5;

    @BeforeClass
    public static void beforeClass() throws Exception {
        bot = new SWTWorkbenchBot();

        closeWelcomePage();
        openJavaPerspective();
    }

    @AfterClass
    public static void afterClass() {
        deleteAllProjects();
        // DON'T CALL bot.resetWorkbench();
        // otherwise the PIT views will be closed and will not
        // be opened again on the next test
        // (maybe because it saves the state of the closed views?)
        bot.closeAllShells();
        bot.saveAllEditors();
        bot.closeAllEditors();
    }

    protected static void closeWelcomePage() throws InterruptedException {
        Display.getDefault().syncExec(new Runnable() {
            public void run() {
                IIntroManager introManager = PlatformUI.getWorkbench().getIntroManager();
                if (introManager.getIntro() != null) {
                    introManager
                            .closeIntro(introManager.getIntro());
                }
            }
        });
    }

    protected static void openJavaPerspective() throws InterruptedException {
        Display.getDefault().syncExec(new Runnable() {
            public void run() {
                IWorkbench workbench = PlatformUI.getWorkbench();
                try {
                    workbench.showPerspective("org.eclipse.jdt.ui.JavaPerspective",
                            workbench.getActiveWorkbenchWindow());
                } catch (WorkbenchException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    protected static void openViewById(String viewId) throws InterruptedException {
        Display.getDefault().syncExec(new Runnable() {
            public void run() {
                IWorkbench workbench = PlatformUI.getWorkbench();
                try {
                    workbench.getActiveWorkbenchWindow().getActivePage().showView(viewId);
                } catch (WorkbenchException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    protected static IProject importTestProject(String projectName) throws CoreException {
        PAGES.getBuildProgress().listenForBuild();
        IProject importProject = ProjectImportUtil.importProject(projectName);
        PAGES.getBuildProgress().waitForBuild();
        verifyProjectExists(projectName);
        assertNoErrorsInWorkspace();
        return importProject;
    }

    protected static void createJavaProjectWithJUnit4(String projectName) {
        PAGES.getBuildProgress().listenForBuild();
        PAGES.getFileMenu().newJavaProject(projectName);
        PAGES.getAbstractSyntaxTree().addJUnitToClassPath(projectName);
        PAGES.getBuildProgress().waitForBuild();
        assertNoErrorsInWorkspace();
    }

    protected static void createJavaProjectWithJUnit5(String projectName) {
        PAGES.getBuildProgress().listenForBuild();
        PAGES.getFileMenu().newJavaProject(projectName);
        PAGES.getAbstractSyntaxTree().addJUnit5ToClassPath(projectName);
        PAGES.getBuildProgress().waitForBuild();
        assertNoErrorsInWorkspace();
    }

    protected static void assertNoErrorsInWorkspace() {
        new PitclipseSteps().assertNoErrorsInWorkspace();
    }

    protected static void verifyProjectExists(String projectName) {
        for (String project : PAGES.getPackageExplorer().getProjectsInWorkspace()) {
            if (projectName.equals(project)) {
                // Project does indeed exist
                return;
            }
        }
        fail("Project: " + projectName + " not found.");
    }

    protected static void deleteAllProjects() {
        PAGES.getBuildProgress().listenForBuild();
        for (String project : PAGES.getPackageExplorer().getProjectsInWorkspace()) {
            PAGES.getAbstractSyntaxTree().deleteProject(project);
        }
        PAGES.getBuildProgress().waitForBuild();
        File historyFile = PitCoreActivator.getDefault().getHistoryFile();
        if (historyFile.exists()) {
            assertTrue(historyFile.delete());
        }
    }

    protected static void deleteSrcContents(String projectName) throws CoreException {
        PAGES.getBuildProgress().listenForBuild();
        IJavaProject javaProject = PAGES.getAbstractSyntaxTree().getJavaProject(projectName);
        javaProject.getProject().getFolder("src").delete(true, null);
        javaProject.getProject().getFolder("src").create(true, true, null);
        PAGES.getBuildProgress().waitForBuild();
    }

    protected static void addToBuildPath(String dependentProject, String projectName) {
        PAGES.getPackageExplorer().selectProject(projectName);
        PAGES.getAbstractSyntaxTree().addProjectToClassPathOfProject(projectName, dependentProject);
    }

    protected static void openEditor(String className, String packageName, String projectName) {
        PAGES.getPackageExplorer()
            .openClass(
                new ConcreteClassContext.Builder()
                    .withProjectName(projectName)
                    .withPackageName(packageName)
                    .withClassName(className)
                    .build());
    }

    /**
     * See {@link #setClassContents(String, String, String, String)} for the
     * method specification.
     * 
     * @param className
     * @param packageName
     * @param projectName
     * @param method
     */
    protected static void createClassWithMethod(String className, String packageName, String projectName,
            String method) {
        createClass(className, packageName, projectName);
        createMethod(className, packageName, projectName, method);
    }

    protected static void createClass(String className, String packageName, String projectName) {
        PAGES.getBuildProgress().listenForBuild();
        PAGES.getPackageExplorer().selectPackageRoot(projectName, "src");
        // Cannot use the Package explorer right click context menu
        // to create a class due to SWTBot bug 261360
        PAGES.getFileMenu().createClass(packageName, className);
        PAGES.getBuildProgress().waitForBuild();
    }

    /**
     * See {@link #setClassContents(String, String, String, String)} for the
     * method specification.
     * 
     * @param className
     * @param packageName
     * @param projectName
     * @param method
     */
    protected static void createMethod(String className, String packageName, String projectName,
            String method) {
        setClassContents(className, packageName, projectName, method);
    }

    /**
     * Sets the class contents: the package declaration and the class declaration
     * are generated automatically, and only the contents to be inserted inside the class
     * must be specified (thus, all types must be fully qualified since there's no way
     * of specifying the imports).
     * 
     * Example:
     * 
     * <pre>
     * setClassContents("FooTest", "foo.bar", TEST_PROJECT,
     *          "@org.junit.Test\n"
     *        + "public void fooTest3() {\n"
     *        + "    org.junit.Assert.assertEquals(2,\n"
     *        + "            new Foo().doFoo(1));\n"
     *        + "}");
     * </pre>
     * 
     * @param className
     * @param packageName
     * @param project
     * @param contents
     */
    protected static void setClassContents(String className, String packageName, String project,
            String contents) {
        PAGES.getBuildProgress().listenForBuild();
        SWTBotEditor editor = bot.editorByTitle(className + ".java");
        editor.setFocus();
        editor.toTextEditor().setText(
        "package " + packageName + ";\n\n" +
        "public class " + className + " {\n\n" +
            indent(contents) + "\n\n" +
        "}\n"
        );
        editor.save();
        PAGES.getBuildProgress().waitForBuild();
    }

    protected static void removeMethods(String className, String packageName, String project) {
        setClassContents(className, packageName, project, "\n\n");
    }

    private static String indent(String contents) {
        return Stream.of(contents.split("\n"))
            .map(s -> "    " + s)
            .collect(Collectors.joining("\n"));
    }

    protected static void runTest(final String testClassName, final String packageName, final String projectName) throws CoreException {
        new PitclipseSteps().runTest(testClassName, packageName, projectName);
    }

    protected static void runPackageTest(final String packageName, final String projectName) throws CoreException {
        new PitclipseSteps().runPackageTest(packageName, projectName);
    }

    protected static void runPackageRootTest(final String packageRoot, final String projectName) throws CoreException {
        new PitclipseSteps().runPackageRootTest(packageRoot, projectName);
    }

    protected static void runProjectTest(final String projectName) throws CoreException {
        new PitclipseSteps().runProjectTest(projectName);
    }

    protected static void consoleContains(int generatedMutants, int killedMutants,
            int killedPercentage,
            int testsRun,
            int testsPerMutations) {
        PAGES.views().waitForTestsAreRunOnConsole();
        SWTBotView consoleView = bot.viewByPartName("Console");
        consoleView.show();
        String consoleText = consoleView.bot()
                .styledText().getText()
                .replace("\r", "");
        // System.out.println(consoleText);

        if (generatedMutants == 0) {
            // in newer versions of PIT the Console will simply end with a warning
            assertThat(consoleText,
                containsString(
                    "INFO : Created  0 mutation test units in pre scan"
                )
            );
            assertThat(consoleText,
                containsString(
                    "WARNING : No mutations found."
                )
            );
        } else {
            // we are interested in two strings appearing in the Console
            // newer versions of PIT generate other contents in between
            // e.g., ">> Mutations with no coverage 0. Test strength 100%"
            // so we must run the checks separately
            assertThat(consoleText,
                containsString(
                    String.format(
                      ">> Generated %d mutations Killed %d (%d%%)",
                      generatedMutants, killedMutants,
                      killedPercentage)
                )
            );
            assertThat(consoleText,
                containsString(
                    String.format(
                      ">> Ran %d tests (%d tests per mutation)",
                      testsRun, testsPerMutations)
                )
            );
        }
    }

    /**
     * The expectedMutationsTable String argument represents the expected
     * mutations table, this is an example of String:
     * (the headers of the table are the following ones:)
     * <pre>
     *  status      | project  | package | class       | line | mutation
     * </pre>
     * 
     * <pre>
     * "NO_COVERAGE | project1 | foo.bar | foo.bar.Foo | 6 | Replaced integer addition with subtraction       \n" +
     * "NO_COVERAGE | project1 | foo.bar | foo.bar.Foo | 6 | replaced int return with 0 for foo/bar/Foo::doFoo "
     * </pre>
     * 
     * @param expectedMutationsTable
     */
    protected static void mutationsAre(String expectedMutationsTable) {
        List<PitMutation> expectedMutations = new ArrayList<>();
        String[] lines = expectedMutationsTable.split("\n");
        for (String line : lines) {
            PitMutation pitMutation = fromMutationLine(line);
            expectedMutations.add(pitMutation);
        }
        mutationsAre(expectedMutations);
    }

    protected static PitMutation fromMutationLine(String line) {
        String[] mutationRow = line.split("\\|");
        DetectionStatus status = DetectionStatus.valueOf(mutationRow[statusIndex].trim());
        String project = mutationRow[projectIndex].trim();
        String pkg = mutationRow[packageIndex].trim();
        String className = mutationRow[classIndex].trim();
        int lineNum = parseInt(mutationRow[lineIndex].trim());
        String mutation = mutationRow[mutationIndex].trim();
        PitMutation pitMutation = PitMutation.builder().withStatus(status).withProject(project).withPackage(pkg)
                .withClassName(className).withLineNumber(lineNum).withMutation(mutation).build();
        return pitMutation;
    }

    protected static void mutationsAre(List<PitMutation> expectedMutations) {
        List<PitMutation> actualMutations = PAGES.getPitMutationsView().getMutations();
        assertThat(actualMutations, equalTo(expectedMutations));
    }

    protected static void coverageReportGenerated(int classes, double totalCoverage, double mutationCoverage) {
        new PitclipseSteps().coverageReportGenerated(classes, totalCoverage, mutationCoverage);
    }

    /**
     * The configTable String argument represents the expected
     * runtime options, a two row table is expected with the same number
     * of columns; the first row contains the keys and the second row contains the values.
     * Example (the alignment is optional and spaces are trimmed):
     * 
     * <pre>
     * "classUnderTest  | classesToMutate              | excludedClasses | excludedMethods | runInParallel | incrementalAnalysis | avoidCallsTo                                                               \n" +
     * "foo.bar.FooTest | foo.bar.BarTest, foo.bar.Foo | *Test           |                 | true          | false               | java.util.logging, org.apache.log4j, org.slf4j, org.apache.commons.logging, org.apache.logging.log4j"
     * </pre>
     * 
     * @param configTable
     */
    protected static void runtimeOptionsMatch(String configTable) {
        new PitclipseSteps().runtimeOptionsMatch(fromTwoRowTableToMap(configTable));
    }

    protected static Map<String, String> fromTwoRowTableToMap(String table) {
        Map<String, String> map = new HashMap<>();
        String[] lines = table.split("\n");
        assertThat("You must specify a row with keys and a row with values",
            lines.length, equalTo(2));
        String[] keyRow = lines[0].split("\\|");
        String[] valueRow = lines[1].split("\\|");
        assertThat("Keys and values do not match in number",
            keyRow.length, equalTo(valueRow.length));
        for (int i = 0; i < keyRow.length; ++i) {
            map.put(keyRow[i].trim(), valueRow[i].trim());
        }
        return map;
    }

    /**
     * The configTable String argument represents the expected
     * launch configurations elements, a table with at least two rows is expected with the same number
     * of columns; the first row contains the keys and other ones contain the values.
     * Example (the alignment is optional and spaces are trimmed):
     * 
     * <pre>
     *   "name    | runInParallel | useIncrementalAnalysis | excludedClasses | excludedMethods | avoidCallsTo                                                               \n"
     * + "BarTest | true          | false                  | *Test           |                 | java.util.logging, org.apache.log4j, org.slf4j, org.apache.commons.logging, org.apache.logging.log4j \n"
     * + "FooTest | true          | false                  | *Test           |                 | java.util.logging, org.apache.log4j, org.slf4j, org.apache.commons.logging, org.apache.logging.log4j"
     * </pre>
     * 
     * @param configTable
     */
    protected static void launchConfigurationsMatch(String configTable) {
        List<PitRunConfiguration> launchConfigurations = PAGES.getRunMenu().runConfigurations();
        new LaunchConfigurationSteps().configurationsMatch
            (fromTableToMap(configTable), launchConfigurations);
    }

    protected static List<Map<String, String>> fromTableToMap(String table) {
        List<Map<String, String>> maps = new ArrayList<>();
        String[] lines = table.split("\n");
        assertThat("You must specify at least a row with keys and rows with values",
            lines.length, greaterThanOrEqualTo(2));
        String[] keyRow = lines[0].split("\\|");
        for (int i = 1; i < lines.length; ++i) {
            Map<String, String> map = new HashMap<>();
            maps.add(map);
            String[] valueRow = lines[i].split("\\|");
            assertThat("Keys and values do not match in number",
                keyRow.length, equalTo(valueRow.length));
            for (int j = 0; j < keyRow.length; ++j) {
                map.put(keyRow[j].trim(), valueRow[j].trim());
            }
        }
        return maps;
    }

    protected static void removePitLaunchConfigurations() throws CoreException {
        ILaunchManager manager = DebugPlugin.getDefault().getLaunchManager();
        ILaunchConfigurationType type =
            manager.getLaunchConfigurationType(PitLaunchShortcut.PIT_CONFIGURATION_TYPE);
        ILaunchConfiguration[] lcs = manager.getLaunchConfigurations(type);
        for (ILaunchConfiguration launchConfiguration : lcs) {
            launchConfiguration.delete();
        }
    }

}
