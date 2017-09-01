package org.osgl.inject;

import java.io.IOException;
import java.util.Properties;

/**
 * Provide library version at runtime
 */
public final class Version {

    static final String UNKNOWN = "unknown";

    private static String version = UNKNOWN;
    private static String build = UNKNOWN;

    static {
        loadVersionInfo();
    }

    /**
     * Returns the version of the library. The version string shall
     * be the same as `${project.version}` defined in project `pom.xml`
     * file
     *
     * @return the library version
     */
    public static String version() {
        return version;
    }

    /**
     * Returns the build number of the library. The build number shall
     * be the same as `${buildNumber}` set by
     * [Build Number Maven Plugin ](http://www.mojohaus.org/buildnumber-maven-plugin/)
     *
     * @return the library build number
     */
    public static String buildNumber() {
        return build;
    }

    /**
     * Returns {@link #versionTag()} concatenated with {@link #buildNumber()}
     *
     * @return the full version tag
     */
    public static String versionTag() {
        return version() + "-" + buildNumber();
    }

    private static void loadVersionInfo() {
        try {
            Properties properties = new Properties();
            properties.load(Version.class.getResourceAsStream(".version"));
            version = properties.getProperty("version");
            build = properties.getProperty("build");
        } catch (IOException e) {
            Genie.logger.warn(e, "unable to load version info");
        }
    }
}
