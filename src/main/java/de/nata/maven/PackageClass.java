package de.nata.maven;

public class PackageClass {
	
	public String getVersion() {
		return getClass().getPackage().getImplementationVersion();
	}

}
