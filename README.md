# Pitclipse

[![Build Status](https://travis-ci.com/pitest/pitclipse.svg?branch=master)](https://travis-ci.com/pitest/pitclipse) [![Technical Debt](https://sonarcloud.io/api/project_badges/measure?project=org.pitest%3Aorg.pitest.pitclipse&metric=sqale_index)](https://sonarcloud.io/dashboard?id=org.pitest%3Aorg.pitest.pitclipse) [![Coverage](https://sonarcloud.io/api/project_badges/measure?project=org.pitest%3Aorg.pitest.pitclipse&metric=coverage)](https://sonarcloud.io/dashboard?id=org.pitest%3Aorg.pitest.pitclipse) [![Lines of Code](https://sonarcloud.io/api/project_badges/measure?project=org.pitest%3Aorg.pitest.pitclipse&metric=ncloc)](https://sonarcloud.io/dashboard?id=org.pitest%3Aorg.pitest.pitclipse) [ ![Download](https://api.bintray.com/packages/kazejiyu/Pitclipse/releases/images/download.svg) ](https://bintray.com/kazejiyu/Pitclipse/releases/_latestVersion)

Provides support for [PIT (Pitest)](http://pitest.org) within the Eclipse IDE. Allows to compute the mutation coverage of your code and shows the result within dedicated views.

[Notable previous versions are described here](OLD_MILESTONES.md)

## How to use Pitclipse?

First of all, you need to install Pitclipse in your Eclipse IDE (see `Installation` below).

Once the plug-in is installed, you can run Pitest. To this end:
- Right-clic on a Java project defining unit tests
- `Run As` > `PIT Mutation Test`

Wait a few seconds, two views should open to show the results:
- **PIT Summary**: shows the percentage of mutation coverage
- **PIT Mutations**: shows the detected mutations and their location in code

It is also possible to run a single JUnit test class. Specific PIT options can be configured from the Launch Configuration window:
- `Run` > `Run Configurations...`
- Double-click on `PIT Mutation Test`
- Specify the options
- Press `Run`

> **/!\\** JUnit 5 is not supported at the moment

## Installation

### From the Eclipse Marketplace

The plug-in is available in the [Eclipse Marketplace](https://marketplace.eclipse.org/content/pitclipse).

Drag the following button to your running Eclipse workspace to start the installation:
<div align="center">
  <a href="http://marketplace.eclipse.org/marketplace-client-intro?mpc_install=1426461" class="drag" title="Drag to your running Eclipse* workspace. *Requires Eclipse Marketplace Client"><img typeof="foaf:Image" class="img-responsive" src="https://marketplace.eclipse.org/sites/all/themes/solstice/public/images/marketplace/btn-install.png" alt="Drag to your running Eclipse* workspace. *Requires Eclipse Marketplace Client" /></a>
</div>

> **/!\\** Not up-to-date at the moment, please use the update site below

### From the update site
Alternatively, the plug-in can also be installed from the following (temporary) update site:

- [https://dl.bintray.com/kazejiyu/Pitclipse/updates/](https://dl.bintray.com/kazejiyu/Pitclipse/updates/)

To use it from Eclipse IDE, click on `Help` > `Install new software...` and then paste the above URL.

> **Note**: this URL is not viewable from a browser.

## How to contribute?

### Requirements

You will need [Maven 3.x](https://maven.apache.org/download.cgi), [Java 8 JDK](https://adoptopenjdk.net/upstream.html) and the latest [Eclipse IDE for RCP](https://www.eclipse.org/downloads/packages/) release.

### Setup the environment

First of all, clone the repository:

```
git clone https://github.com/pitest/pitclipse.git
```

Then:

1. Import all the plug-ins within your Eclipse IDE workspace
2. Open the `releng/org.pitest.pitclipse.target/org.pitest.pitclipse.target.target` file
3. Click on "_Set as Active Target Platform_"
4. Wait for the dependencies to be loaded (may take a while)

### Commit your changes

Make your changes, then make sure the tests still pass:
```
mvn clean verify
```
Commit your changes, then submit a PR.

> See [CONTRIBUTING.md](CONTRIBUTING.md) for further details.