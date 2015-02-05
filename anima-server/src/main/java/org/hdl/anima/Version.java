package org.hdl.anima;

/**
 * Version
 * @author qiuhd
 * @since  2014-8-1
 * @version V1.0.0
 */
public class Version {
	 /**
     * The major number (ie 1.x.x).
     */
    private int major;

    /**
     * The minor version number (ie x.1.x).
     */
    private int minor;

    /**
     * The micro version number (ie x.x.1).
     */
    private int micro;

    /**
     * A status release number or -1 to indicate none.
     */
    private int statusVersion;

    /**
     * Cached version string information
     */
    private String versionString;
    
    /**
     * Create a new version information object.
     *
     * @param major the major release number.
     * @param minor the minor release number.
     * @param micro the micro release number.
     */
    public Version(int major, int minor, int micro) {
        this.major = major;
        this.minor = minor;
        this.micro = micro;
        versionString = major + "." + minor + "." + micro;
    }

    /**
     * Returns the version number of this instance of Openfire as a
     * String (ie major.minor.revision).
     *
     * @return The version as a string
     */
    public String getVersionString() {
        return versionString;
    }


    /**
     * Obtain the major release number for this product.
     *
     * @return The major release number 1.x.x
     */
    public int getMajor() {
        return major;
    }

    /**
     * Obtain the minor release number for this product.
     *
     * @return The minor release number x.1.x
     */
    public int getMinor() {
        return minor;
    }

    /**
     * Obtain the micro release number for this product.
     *
     * @return The micro release number x.x.1
     */
    public int getMicro() {
        return micro;
    }

    /**
     * Obtain the status relase number for this product. For example, if
     * the release status is <strong>alpha</strong> the release may be <strong>5</strong>
     * resulting in a release status of <strong>Alpha 5</strong>.
     *
     * @return The status version or -1 if none is set.
     */
    public int getStatusVersion() {
        return statusVersion;
    }
}

