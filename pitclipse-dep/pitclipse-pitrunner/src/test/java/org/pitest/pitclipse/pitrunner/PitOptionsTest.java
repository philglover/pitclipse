package org.pitest.pitclipse.pitrunner;

import static com.google.common.collect.ImmutableList.of;
import static java.lang.Integer.toHexString;
import static org.apache.commons.lang3.SystemUtils.IS_OS_WINDOWS;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Random;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;
import org.pitest.pitclipse.example.ExampleTest;
import org.pitest.pitclipse.pitrunner.PitOptions.PitLaunchException;
import org.pitest.pitclipse.pitrunner.PitOptions.PitOptionsBuilder;

import com.google.common.collect.Lists;

public class PitOptionsTest {

	private static final File TMP_DIR = new File(
			System.getProperty("java.io.tmpdir"));
	private final Random random = new Random();
	private final File testTmpDir = randomDir();
	private final File testSrcDir = randomDir();
	private final File anotherTestSrcDir = randomDir();
	private static final File REALLY_BAD_PATH = getBadPath();

	private static File getBadPath() {
		if (IS_OS_WINDOWS) {
			return new File("BADDRIVE:\\");
		}
		return new File("/HOPEFULLY/DOES/NOT/EXIST/SO/IS/BAD/");
	}

	private static final String TEST_CLASS1 = PitOptionsTest.class
			.getCanonicalName();
	private static final String TEST_CLASS2 = PitRunner.class
			.getCanonicalName();
	private static final List<String> CLASS_PATH = of(TEST_CLASS1, TEST_CLASS2);
	private static final String PACKAGE_1 = PitOptionsTest.class.getPackage()
			.getName() + ".*";
	private static final String PACKAGE_2 = ExampleTest.class.getPackage()
			.getName() + ".*";
	private static final List<String> PACKAGES = of(PACKAGE_1, PACKAGE_2);

	@Before
	public void setup() {
		for (File dir : of(testTmpDir, testSrcDir, anotherTestSrcDir)) {
			dir.mkdirs();
			dir.deleteOnExit();
		}
	}

	@AfterClass
	public static void cleanupFiles() {

	}

	@Test(expected = PitLaunchException.class)
	public void defaultOptionsThrowException() throws IOException {
		new PitOptionsBuilder().build();
	}

	@Test(expected = PitLaunchException.class)
	public void validSourceDirButNoTestClassThrowsException()
			throws IOException {
		new PitOptionsBuilder().withSourceDirectory(testSrcDir).build();
	}

	@Test
	public void minimumOptionsSet() throws IOException {
		PitOptions options = new PitOptionsBuilder()
				.withSourceDirectory(testSrcDir)
				.withClassUnderTest(TEST_CLASS1).build();
		File reportDir = options.getReportDirectory();
		assertTrue(reportDir.isDirectory());
		assertTrue(reportDir.exists());
		assertEquals(TMP_DIR, reportDir.getParentFile());
		assertEquals(TEST_CLASS1, options.getClassUnderTest());
		assertArrayEquals(expectedArgs(reportDir, testSrcDir, TEST_CLASS1),
				options.toCLIArgs());
		assertEquals(expectedArgsAsString(reportDir, testSrcDir, TEST_CLASS1),
				options.toCLIArgsAsString());
	}

	@Test(expected = PitLaunchException.class)
	public void sourceDirectoryDoesNotExist() throws IOException {
		new PitOptionsBuilder().withSourceDirectory(randomDir())
				.withClassUnderTest(TEST_CLASS1).build();
	}

	@Test(expected = PitLaunchException.class)
	public void multipleSourceDirectoriesOneDoesNotExist() throws IOException {
		new PitOptionsBuilder()
				.withSourceDirectories(of(testSrcDir, randomDir()))
				.withClassUnderTest(TEST_CLASS1).build();
	}

	@Test
	public void multipleSourceDirectoriesExist() throws IOException {
		List<File> srcDirs = of(testSrcDir, anotherTestSrcDir);
		PitOptions options = new PitOptionsBuilder()
				.withSourceDirectories(srcDirs).withClassUnderTest(TEST_CLASS1)
				.build();
		File reportDir = options.getReportDirectory();
		assertTrue(reportDir.isDirectory());
		assertTrue(reportDir.exists());
		assertEquals(TMP_DIR, reportDir.getParentFile());
		assertEquals(TEST_CLASS1, options.getClassUnderTest());
		assertArrayEquals(expectedArgs(reportDir, srcDirs, TEST_CLASS1),
				options.toCLIArgs());
		assertEquals(expectedArgsAsString(reportDir, srcDirs, TEST_CLASS1),
				options.toCLIArgsAsString());
	}

	@Test
	public void useDifferentReportDirectory() {
		File expectedDir = new File(testTmpDir, randomString());
		assertFalse(expectedDir.exists());
		PitOptions options = new PitOptionsBuilder()
				.withReportDirectory(expectedDir)
				.withSourceDirectory(testSrcDir)
				.withClassUnderTest(TEST_CLASS1).build();
		File actualDir = options.getReportDirectory();
		assertTrue(actualDir.isDirectory());
		assertEquals(expectedDir, actualDir);
		assertTrue(expectedDir.exists());
		assertArrayEquals(expectedArgs(expectedDir, testSrcDir, TEST_CLASS1),
				options.toCLIArgs());
		assertEquals(
				expectedArgsAsString(expectedDir, testSrcDir, TEST_CLASS1),
				options.toCLIArgsAsString());
	}

	@Test(expected = PitLaunchException.class)
	public void useInvalidReportDirectory() {
		new PitOptionsBuilder().withReportDirectory(REALLY_BAD_PATH)
				.withSourceDirectory(testSrcDir)
				.withClassUnderTest(TEST_CLASS1).build();
	}

	@Test(expected = PitLaunchException.class)
	public void useInvalidSourceDirectory() {
		new PitOptionsBuilder().withSourceDirectory(REALLY_BAD_PATH)
				.withClassUnderTest(TEST_CLASS1).build();
	}

	@Test
	public void useClasspath() throws IOException {
		PitOptions options = new PitOptionsBuilder()
				.withSourceDirectory(testSrcDir)
				.withClassUnderTest(TEST_CLASS1)
				.withClassesToMutate(CLASS_PATH).build();
		File reportDir = options.getReportDirectory();
		assertTrue(reportDir.isDirectory());
		assertTrue(reportDir.exists());
		assertEquals(TMP_DIR, reportDir.getParentFile());
		assertEquals(TEST_CLASS1, options.getClassUnderTest());
		assertArrayEquals(
				expectedArgs(reportDir, testSrcDir, TEST_CLASS1, TEST_CLASS1,
						TEST_CLASS2), options.toCLIArgs());
		assertEquals(
				expectedArgsAsString(reportDir, testSrcDir, TEST_CLASS1,
						TEST_CLASS1, TEST_CLASS2), options.toCLIArgsAsString());
	}

	@Test
	public void testPackagesSupplied() {
		PitOptions options = new PitOptionsBuilder()
				.withSourceDirectory(testSrcDir).withPackagesToTest(PACKAGES)
				.withClassesToMutate(CLASS_PATH).build();
		File reportDir = options.getReportDirectory();
		assertTrue(reportDir.isDirectory());
		assertTrue(reportDir.exists());
		assertEquals(TMP_DIR, reportDir.getParentFile());
		assertNull(options.getClassUnderTest());
		assertEquals(PACKAGES, options.getTestPackages());
		assertArrayEquals(
				expectedArgs(reportDir, testSrcDir,
						PACKAGE_1 + "," + PACKAGE_2, TEST_CLASS1, TEST_CLASS2),
				options.toCLIArgs());
		assertEquals(
				expectedArgsAsString(reportDir, testSrcDir, PACKAGE_1 + ","
						+ PACKAGE_2, TEST_CLASS1, TEST_CLASS2),
				options.toCLIArgsAsString());
	}

	private String randomString() {
		return toHexString(random.nextInt());
	}

	private Object[] expectedArgs(File reportDir, File sourceDir,
			String classUnderTest, String... classpath) {
		return expectedArgs(reportDir, of(sourceDir), classUnderTest, classpath);
	}

	private Object[] expectedArgs(File reportDir, List<File> sourceDirs,
			String classUnderTest, String... classpath) {
		List<String> args = Lists.newArrayList("--failWhenNoMutations",
				"false", "--outputFormats", "HTML", "--reportDir",
				reportDir.getPath(), "--targetTests", classUnderTest,
				"--targetClasses");
		if (null != classpath) {
			String result = "";
			for (int i = 0; i < classpath.length; i++) {
				if (i == classpath.length - 1) {
					result += classpath[i];
				} else {
					result += classpath[i] + ",";
				}
			}
			args.add(result);
		}
		args.add("--sourceDirs");
		if (null != sourceDirs) {
			String result = "";
			for (int i = 0; i < sourceDirs.size(); i++) {
				if (i == sourceDirs.size() - 1) {
					result += sourceDirs.get(i).getPath();
				} else {
					result += sourceDirs.get(i).getPath() + ",";
				}
			}
			args.add(result);
		}
		args.add("--verbose");
		return args.toArray();
	}

	private String expectedArgsAsString(File reportDir, File sourceDir,
			String classUnderTest, String... classpath) {
		return expectedArgsAsString(reportDir, of(sourceDir), classUnderTest,
				classpath);
	}

	private String expectedArgsAsString(File reportDir, List<File> sourceDirs,
			String classUnderTest, String... classpath) {
		Object[] args = expectedArgs(reportDir, sourceDirs, classUnderTest,
				classpath);
		StringBuilder argsBuilder = new StringBuilder();
		for (Object arg : args) {
			argsBuilder.append(' ').append(arg);
		}
		return argsBuilder.toString().trim();
	}

	private File randomDir() {
		File randomDir = new File(TMP_DIR, randomString());
		randomDir.deleteOnExit();
		return randomDir;
	}
}