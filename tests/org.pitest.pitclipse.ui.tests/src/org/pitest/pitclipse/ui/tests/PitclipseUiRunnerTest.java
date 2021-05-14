package org.pitest.pitclipse.ui.tests;

import static org.pitest.pitclipse.ui.behaviours.pageobjects.PageObjects.PAGES;

import java.util.Collections;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.swtbot.swt.finder.junit.SWTBotJunit4ClassRunner;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.pitest.pitclipse.ui.util.ProjectImportUtil;

/**
 * @author Lorenzo Bettini
 * 
 */
@RunWith(SWTBotJunit4ClassRunner.class)
public class PitclipseUiRunnerTest extends AbstractPitclipseSWTBotTest {

    private static final String TEST_PROJECT = "org.pitest.pitclipse.testprojects.emptyclasses";
    private static final String FOO_BAR_PACKAGE = "foo.bar";
    private static final String FOO_CLASS = "Foo";
    private static final String FOO_TEST_CLASS = "FooTest";

    @BeforeClass
    public static void setupJavaProject() throws CoreException {
        PAGES.getBuildProgress().listenForBuild();
        ProjectImportUtil.importProject(TEST_PROJECT);
        PAGES.getBuildProgress().waitForBuild();
        verifyProjectExists(TEST_PROJECT);
        assertNoErrorsInWorkspace();
        openEditor(FOO_CLASS, FOO_BAR_PACKAGE, TEST_PROJECT);
        openEditor(FOO_TEST_CLASS, FOO_BAR_PACKAGE, TEST_PROJECT);
    }

    @Test
    public void emptyClassAndEmptyTest() throws CoreException {
        removeMethods(FOO_CLASS, FOO_BAR_PACKAGE, TEST_PROJECT);
        removeMethods(FOO_TEST_CLASS, FOO_BAR_PACKAGE, TEST_PROJECT);
        runTest(FOO_TEST_CLASS, FOO_BAR_PACKAGE, TEST_PROJECT);
        consoleContains(0, 0, 100, 0, 0);
        mutationsAre(Collections.emptyList());
        coverageReportGenerated(0, 100, 100);
    }

    @Test
    public void emptyClassAndEmptyTestMethod() throws CoreException {
        removeMethods(FOO_CLASS, FOO_BAR_PACKAGE, TEST_PROJECT);
        createMethod(FOO_TEST_CLASS, FOO_BAR_PACKAGE, TEST_PROJECT,
                "@org.junit.Test\n"
              + "public void fooTest1() {\n"
              + "    Foo foo = new Foo();\n"
              + "}");
        runTest(FOO_TEST_CLASS, FOO_BAR_PACKAGE, TEST_PROJECT);
        consoleContains(0, 0, 100, 0, 0);
        mutationsAre(Collections.emptyList());
        coverageReportGenerated(0, 100, 100);
    }

    @Test
    public void classWithMethodAndNoCoverageTestMethod() throws CoreException {
        createMethod(FOO_CLASS, FOO_BAR_PACKAGE, TEST_PROJECT,
                "public int doFoo(int i) {\n"
              + "    return i + 1;\n"
              + "}");
        createMethod(FOO_TEST_CLASS, FOO_BAR_PACKAGE, TEST_PROJECT,
                "@org.junit.Test\n"
              + "public void fooTest1() {\n"
              + "    Foo foo = new Foo();\n"
              + "}");
        runTest(FOO_TEST_CLASS, FOO_BAR_PACKAGE, TEST_PROJECT);
        consoleContains(2, 0, 0, 0, 0);
        mutationsAre(
        "NO_COVERAGE | " + TEST_PROJECT + " | foo.bar | foo.bar.Foo |    6 | Replaced integer addition with subtraction       \n" +
        "NO_COVERAGE | " + TEST_PROJECT + " | foo.bar | foo.bar.Foo |    6 | replaced int return with 0 for foo/bar/Foo::doFoo ");
        coverageReportGenerated(1, 50, 0);
    }

    @Test
    public void classWithMethodAndBadTestMethod() throws CoreException {
        createMethod(FOO_CLASS, FOO_BAR_PACKAGE, TEST_PROJECT,
                "public int doFoo(int i) {\n"
              + "    return i + 1;\n"
              + "}");
        createMethod(FOO_TEST_CLASS, FOO_BAR_PACKAGE, TEST_PROJECT,
                "@org.junit.Test\n"
              + "public void fooTest2() {\n"
              + "    new Foo().doFoo(1);\n"
              + "}");
        runTest(FOO_TEST_CLASS, FOO_BAR_PACKAGE, TEST_PROJECT);
        consoleContains(2, 0, 0, 2, 1);
        mutationsAre(
        "SURVIVED | " + TEST_PROJECT + " | foo.bar | foo.bar.Foo |    6 | Replaced integer addition with subtraction       \n" +
        "SURVIVED | " + TEST_PROJECT + " | foo.bar | foo.bar.Foo |    6 | replaced int return with 0 for foo/bar/Foo::doFoo ");
        coverageReportGenerated(1, 100, 0);
    }

    @Test
    public void classWithMethodAndBetterTestMethod() throws CoreException {
        createMethod(FOO_CLASS, FOO_BAR_PACKAGE, TEST_PROJECT,
                "public int doFoo(int i) {\n"
              + "    return i + 1;\n"
              + "}");
        createMethod(FOO_TEST_CLASS, FOO_BAR_PACKAGE, TEST_PROJECT,
                "@org.junit.Test\n"
              + "public void fooTest3() {\n"
              + "    org.junit.Assert.assertEquals(2,\n"
              + "            new Foo().doFoo(1));\n"
              + "}");
        runTest(FOO_TEST_CLASS, FOO_BAR_PACKAGE, TEST_PROJECT);
        consoleContains(2, 2, 100, 2, 1);
        mutationsAre(
        "KILLED | " + TEST_PROJECT + " | foo.bar | foo.bar.Foo |    6 | Replaced integer addition with subtraction       \n" +
        "KILLED | " + TEST_PROJECT + " | foo.bar | foo.bar.Foo |    6 | replaced int return with 0 for foo/bar/Foo::doFoo ");
        coverageReportGenerated(1, 100, 100);
    }

    @Test
    public void runPitAtPackageAndPackageRootAndProjectLevel() throws CoreException {
        createMethod(FOO_CLASS, FOO_BAR_PACKAGE, TEST_PROJECT,
                "public int doFoo(int i) {\n"
              + "    return i + 1;\n"
              + "}");
        createMethod(FOO_TEST_CLASS, FOO_BAR_PACKAGE, TEST_PROJECT,
                "@org.junit.Test\n"
              + "public void fooTest3() {\n"
              + "    org.junit.Assert.assertEquals(2,\n"
              + "            new Foo().doFoo(1));\n"
              + "}");
        runPackageTest(FOO_BAR_PACKAGE, TEST_PROJECT);
        consoleContains(2, 2, 100, 2, 1);
        coverageReportGenerated(1, 100, 100);
        runPackageRootTest("src", TEST_PROJECT);
        consoleContains(2, 2, 100, 2, 1);
        coverageReportGenerated(1, 100, 100);
        runProjectTest(TEST_PROJECT);
        consoleContains(2, 2, 100, 2, 1);
        coverageReportGenerated(1, 100, 100);
    }

}