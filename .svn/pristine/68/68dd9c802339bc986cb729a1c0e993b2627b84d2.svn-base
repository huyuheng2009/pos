package com.yogapay.core.server.jpos;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;

import org.jpos.core.Configuration;
import org.jpos.core.ConfigurationException;
import org.jpos.iso.ISOException;
import org.jpos.iso.packager.GenericPackager;

public class Packager extends GenericPackager {
	public Packager() throws ISOException {
		super();
	}

	/**
	 * Create a GenericPackager with the field descriptions from an XML File
	 * 
	 * @param filename
	 *            The XML field description file
	 */
	public Packager(String filename) throws ISOException {
		super(filename);
	}

	/**
	 * Create a GenericPackager with the field descriptions from an XML
	 * InputStream
	 * 
	 * @param input
	 *            The XML field description InputStream
	 */
	public Packager(InputStream input) throws ISOException {
		super(input);
	}

	/**
	 * Packager Configuration.
	 * <p/>
	 * <ul>
	 * <li>packager-config
	 * <li>packager-logger
	 * <li>packager-realm
	 * </ul>
	 * 
	 * @param cfg
	 *            Configuration
	 */
	@Override
	public void setConfiguration(Configuration cfg)
			throws ConfigurationException {
		try {
			cfg.put("packager-config",
					getURL(cfg.get("packager-config")).getPath());

		} catch (Exception e) {
			throw new ConfigurationException(e);
		}
		super.setConfiguration(cfg);
	}

	public static final String CLASSPATH_URL_PREFIX = "classpath:";

	public static URL getURL(String resourceLocation)
			throws FileNotFoundException {
		if (resourceLocation.startsWith(CLASSPATH_URL_PREFIX)) {
			String path = resourceLocation.substring(CLASSPATH_URL_PREFIX
					.length());
			URL url = getDefaultClassLoader().getResource(path);
			if (url == null) {
				String description = "class path resource [" + path + "]";
				throw new FileNotFoundException(
						description
								+ " cannot be resolved to URL because it does not exist");
			}
			return url;
		}
		try {
			// try URL
			return new URL(resourceLocation);
		} catch (MalformedURLException ex) {
			// no URL -> treat as file path
			try {
				return new File(resourceLocation).toURI().toURL();
			} catch (MalformedURLException ex2) {
				throw new FileNotFoundException("Resource location ["
						+ resourceLocation
						+ "] is neither a URL not a well-formed file path");
			}
		}
	}

	public static ClassLoader getDefaultClassLoader() {
		ClassLoader cl = null;
		try {
			cl = Thread.currentThread().getContextClassLoader();
		} catch (Throwable ex) {
			// Cannot access thread context ClassLoader - falling back to system
			// class loader...
		}
		if (cl == null) {
			// No thread context class loader -> use class loader of this class.
			cl = Package.class.getClassLoader();
		}
		return cl;
	}
	
//	public static void main(String[] args) throws Exception {
//		Packager p = new Packager();
//		SimpleConfiguration cfg = new SimpleConfiguration();
//		cfg.put("packager-config", "classpath:packager/eptok.xml");
//		p.setConfiguration(cfg);
//		
//	}

}
