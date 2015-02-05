package org.hdl.anima.common;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;
import org.xml.sax.SAXException;
/**
 * Configuration 
 * @author qiuhd
 */
public class Configuration implements Iterable<Map.Entry<String, String>> {

	private static final Logger LOG = LoggerFactory.getLogger(Configuration.class);
	/**
	 * List of configuration resources.
	 */
	private ArrayList<Object> resources = new ArrayList<Object>();

	private boolean loadDefaults = true;
	
	private boolean quietmode = true;

	/**
	 * Configuration objects
	 */
	private static final WeakHashMap<Configuration, Object> REGISTRY = new WeakHashMap<Configuration, Object>();

	/**
	 * The value reported as the setting resource when a key is set by code
	 * rather than a file resource.
	 */
	static final String UNKNOWN_RESOURCE = "Unknown";

	/**
	 * List of configuration parameters marked <b>final</b>.
	 */
	private Set<String> finalParameters = new HashSet<String>();
	
	/**
	 * List of default Resources. Resources are loaded in the order of the list
	 * entries
	 */
	private static final CopyOnWriteArrayList<String> defaultResources = new CopyOnWriteArrayList<String>();

	/**
	 * Stores the mapping of key to the resource which modifies or loads the key
	 * most recently
	 */
	private HashMap<String, String> updatingResource;

	private Properties properties;
	private Properties overlay;

	private ClassLoader classLoader;
	{
		classLoader = Thread.currentThread().getContextClassLoader();
		if (classLoader == null) {
			classLoader = Configuration.class.getClassLoader();
		}
	}

	public Configuration() {
		this(true);
	}

	public Configuration(boolean loadDefaults) {
		this.loadDefaults = loadDefaults;
		updatingResource = new HashMap<String, String>();
		synchronized (Configuration.class) {
			REGISTRY.put(this, null);
		}
	}

	@SuppressWarnings({ "unchecked"})
	public Configuration(Configuration other) {
		this.resources = (ArrayList<Object>) other.resources.clone();
		synchronized (other) {
			if (other.properties != null) {
				this.properties = (Properties) other.properties.clone();
			}

			if (other.overlay != null) {
				this.overlay = (Properties) other.overlay.clone();
			}

			this.updatingResource = new HashMap<String, String>(other.updatingResource);
		}

		this.finalParameters = new HashSet<String>(other.finalParameters);
		synchronized (Configuration.class) {
			REGISTRY.put(this, null);
		}
	}
	
	/**
	 * Add a default resource. Resources are loaded in the order of the
	 * resources added.
	 * 
	 * @param name
	 *            file name. File should be present in the classpath.
	 */
	public static synchronized void addDefaultResource(String name) {
		if (!defaultResources.contains(name)) {
			defaultResources.add(name);
			for (Configuration conf : REGISTRY.keySet()) {
				if (conf.loadDefaults) {
					conf.reloadConfiguration();
				}
			}
		}
	}
	
	/**
	 * Add a configuration resource.
	 * 
	 * The properties of this resource will override properties of previously
	 * added resources, unless they were marked <a href="#Final">final</a>.
	 * 
	 * @param name
	 *            resource to be added, the classpath is examined for a file
	 *            with that name.
	 */
	public void addResource(String name) {
		addResourceObject(name);
	}
	
	/**
	 * Add a configuration resource.
	 * 
	 * The properties of this resource will override properties of previously
	 * added resources, unless they were marked <a href="#Final">final</a>.
	 * 
	 * @param in
	 *            InputStream to deserialize the object from.
	 */
	public void addResource(InputStream in) {
		addResourceObject(in);
	}
	
	/**
	 * Add a configuration resource.
	 * 
	 * The properties of this resource will override properties of previously
	 * added resources, unless they were marked <a href="#Final">final</a>.
	 * 
	 * @param url
	 *            url of the resource to be added, the local filesystem is
	 *            examined directly to find the resource, without referring to
	 *            the classpath.
	 */
	public void addResource(URL url) {
		addResourceObject(url);
	}
	
	private synchronized void addResourceObject(Object resource) {
		resources.add(resource); // add to resources
		reloadConfiguration();
	}
	
	/**
	 * Get the {@link URL} for the named resource.
	 * 
	 * @param name
	 *            resource name.
	 * @return the url for the named resource.
	 */
	public URL getResource(String name) {
		return classLoader.getResource(name);
	}
	
	/**
	 * Get the value of the <code>name</code> property, <code>null</code> if no
	 * such property exists.
	 * 
	 * Values are processed for <a href="#VariableExpansion">variable
	 * expansion</a> before being returned.
	 * 
	 * @param name
	 *            the property name.
	 * @return the value of the <code>name</code> property, or null if no such
	 *         property exists.
	 */
	public String get(String name) {
		return substituteVars(getProps().getProperty(name));
	}
	
	/**
	 * Get the value of the <code>name</code> property. If no such property
	 * exists, then <code>defaultValue</code> is returned.
	 * 
	 * @param name
	 *            property name.
	 * @param defaultValue
	 *            default value.
	 * @return property value, or <code>defaultValue</code> if the property
	 *         doesn't exist.
	 */
	public String get(String name, String defaultValue) {
		return substituteVars(getProps().getProperty(name, defaultValue));
	}
	
	/**
	 * Get the value of the <code>name</code> property, without doing <a
	 * href="#VariableExpansion">variable expansion</a>.
	 * 
	 * @param name
	 *            the property name.
	 * @return the value of the <code>name</code> property, or null if no such
	 *         property exists.
	 */
	public String getRaw(String name) {
		return getProps().getProperty(name);
	}
	
	private synchronized Properties getProps() {
		if (properties == null) {
			properties = new Properties();
			loadResources(properties, resources, quietmode);
			if (overlay != null) {
				properties.putAll(overlay);
				for (Map.Entry<Object, Object> item : overlay.entrySet()) {
					updatingResource.put((String) item.getKey(),
							UNKNOWN_RESOURCE);
				}
			}
		}
		return properties;
	}
	
	/**
	 * Set the <code>value</code> of the <code>name</code> property.
	 * 
	 * @param name
	 *            property name.
	 * @param value
	 *            property value.
	 */
	public void set(String name, String value) {
		getOverlay().setProperty(name, value);
		getProps().setProperty(name, value);
		this.updatingResource.put(name, UNKNOWN_RESOURCE);
	}
	
	/**
	 * Sets a property if it is currently unset.
	 * 
	 * @param name
	 *            the property name
	 * @param value
	 *            the new value
	 */
	public void setIfUnset(String name, String value) {
		if (get(name) == null) {
			set(name, value);
		}
	}
	
	/**
	 * Get the value of the <code>name</code> property as an <code>int</code>.
	 * 
	 * If no such property exists, or if the specified value is not a valid
	 * <code>int</code>, then <code>defaultValue</code> is returned.
	 * 
	 * @param name
	 *            property name.
	 * @param defaultValue
	 *            default value.
	 * @return property value as an <code>int</code>, or
	 *         <code>defaultValue</code>.
	 */
	public int getInt(String name, int defaultValue) {
		String valueString = get(name);
		if (valueString == null)
			return defaultValue;
		try {
			String hexString = getHexDigits(valueString);
			if (hexString != null) {
				return Integer.parseInt(hexString, 16);
			}
			return Integer.parseInt(valueString);
		} catch (NumberFormatException e) {
			return defaultValue;
		}
	}
	
	/**
	 * Get the value of the <code>name</code> property as a <code>long</code>.
	 * If no such property is specified, or if the specified value is not a
	 * valid <code>long</code>, then <code>defaultValue</code> is returned.
	 * 
	 * @param name
	 *            property name.
	 * @param defaultValue
	 *            default value.
	 * @return property value as a <code>long</code>, or
	 *         <code>defaultValue</code>.
	 */
	public long getLong(String name, long defaultValue) {
		String valueString = get(name);
		if (valueString == null)
			return defaultValue;
		try {
			String hexString = getHexDigits(valueString);
			if (hexString != null) {
				return Long.parseLong(hexString, 16);
			}
			return Long.parseLong(valueString);
		} catch (NumberFormatException e) {
			return defaultValue;
		}
	}
	
	/**
	 * Get the value of the <code>name</code> property as a <code>float</code>.
	 * If no such property is specified, or if the specified value is not a
	 * valid <code>float</code>, then <code>defaultValue</code> is returned.
	 * 
	 * @param name
	 *            property name.
	 * @param defaultValue
	 *            default value.
	 * @return property value as a <code>float</code>, or
	 *         <code>defaultValue</code>.
	 */
	public float getFloat(String name, float defaultValue) {
		String valueString = get(name);
		if (valueString == null)
			return defaultValue;
		try {
			return Float.parseFloat(valueString);
		} catch (NumberFormatException e) {
			return defaultValue;
		}
	}

	/**
	 * Set the value of the <code>name</code> property to a <code>float</code>.
	 * 
	 * @param name
	 *            property name.
	 * @param value
	 *            property value.
	 */
	public void setFloat(String name, float value) {
		set(name, Float.toString(value));
	}
	
	/**
	 * Get the value of the <code>name</code> property as a <code>boolean</code>
	 * . If no such property is specified, or if the specified value is not a
	 * valid <code>boolean</code>, then <code>defaultValue</code> is returned.
	 * 
	 * @param name
	 *            property name.
	 * @param defaultValue
	 *            default value.
	 * @return property value as a <code>boolean</code>, or
	 *         <code>defaultValue</code>.
	 */
	public boolean getBoolean(String name, boolean defaultValue) {
		String valueString = get(name);
		if ("true".equals(valueString))
			return true;
		else if ("false".equals(valueString))
			return false;
		else
			return defaultValue;
	}

	/**
	 * Set the value of the <code>name</code> property to a <code>boolean</code>
	 * .
	 * 
	 * @param name
	 *            property name.
	 * @param value
	 *            <code>boolean</code> value of the property.
	 */
	public void setBoolean(String name, boolean value) {
		set(name, Boolean.toString(value));
	}
	
	/**
	 * Set the value of the <code>name</code> property to the given type. This
	 * is equivalent to <code>set(&lt;name&gt;, value.toString())</code>.
	 * 
	 * @param name
	 *            property name
	 * @param value
	 *            new value
	 */
	public <T extends Enum<T>> void setEnum(String name, T value) {
		set(name, value.toString());
	}

	/**
	 * Return value matching this enumerated type.
	 * 
	 * @param name
	 *            Property name
	 * @param defaultValue
	 *            Value returned if no mapping exists
	 * @throws IllegalArgumentException
	 *             If mapping is illegal for the type provided
	 */
	public <T extends Enum<T>> T getEnum(String name, T defaultValue) {
		final String val = get(name);
		return null == val ? defaultValue : Enum.valueOf(
				defaultValue.getDeclaringClass(), val);
	}

	
	/**
	 * Set the value of the <code>name</code> property to a <code>long</code>.
	 * 
	 * @param name
	 *            property name.
	 * @param value
	 *            <code>long</code> value of the property.
	 */
	public void setLong(String name, long value) {
		set(name, Long.toString(value));
	}
	
	private String getHexDigits(String value) {
		boolean negative = false;
		String str = value;
		String hexString = null;
		if (value.startsWith("-")) {
			negative = true;
			str = value.substring(1);
		}
		if (str.startsWith("0x") || str.startsWith("0X")) {
			hexString = str.substring(2);
			if (negative) {
				hexString = "-" + hexString;
			}
			return hexString;
		}
		return null;
	}

	/**
	 * Set the value of the <code>name</code> property to an <code>int</code>.
	 * 
	 * @param name
	 *            property name.
	 * @param value
	 *            <code>int</code> value of the property.
	 */
	public void setInt(String name, int value) {
		set(name, Integer.toString(value));
	}

	public Iterator<Entry<String, String>> iterator() {
		Map<String, String> result = new HashMap<String, String>();
		for (Map.Entry<Object, Object> item : getProps().entrySet()) {
			if (item.getKey() instanceof String
					&& item.getValue() instanceof String) {
				result.put((String) item.getKey(), (String) item.getValue());
			}
		}
		return result.entrySet().iterator();
	}

	/**
	 * Reload configuration from previously added resources.
	 * 
	 * This method will clear all the configuration read from the added
	 * resources, and final parameters. This will make the resources to be read
	 * again before accessing the values. Values that are added via set methods
	 * will overlay values read from the resources.
	 */
	public synchronized void reloadConfiguration() {
		properties = null; // trigger reload
		finalParameters.clear(); // clear site-limits
	}
	
	private synchronized Properties getOverlay() {
		if (overlay == null) {
			overlay = new Properties();
		}
		return overlay;
	}
	
	private void loadResources(Properties properties, ArrayList<?> resources,
			boolean quiet) {
		if (loadDefaults) {
			for (String resource : defaultResources) {
				loadResource(properties, resource, quiet);
			}
		}

		for (Object resource : resources) {
			loadResource(properties, resource, quiet);
		}
	}
	
	private static Pattern varPat = Pattern.compile("\\$\\{[^\\}\\$\u0020]+\\}");
	private static int MAX_SUBST = 20;

	private String substituteVars(String expr) {
		if (expr == null) {
			return null;
		}
		Matcher match = varPat.matcher("");
		String eval = expr;
		for (int s = 0; s < MAX_SUBST; s++) {
			match.reset(eval);
			if (!match.find()) {
				return eval;
			}
			String var = match.group();
			var = var.substring(2, var.length() - 1); // remove ${ .. }
			String val = null;
			try {
				val = System.getProperty(var);
			} catch (SecurityException se) {
				LOG.warn("Unexpected SecurityException in Configuration", se);
			}
			if (val == null) {
				val = getRaw(var);
			}
			if (val == null) {
				return eval; // return literal ${var}: var is unbound
			}
			// substitute
			eval = eval.substring(0, match.start()) + val
					+ eval.substring(match.end());
		}
		throw new IllegalStateException(
				"Variable substitution depth too large: " + MAX_SUBST + " "
						+ expr);
	}
	
	private void loadResource(Properties properties, Object name, boolean quiet) {
		try {
			DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory
					.newInstance();
			// ignore all comments inside the xml file
			docBuilderFactory.setIgnoringComments(true);

			// allow includes in the xml file
			docBuilderFactory.setNamespaceAware(true);
			try {
				docBuilderFactory.setXIncludeAware(true);
			} catch (UnsupportedOperationException e) {
				LOG.error("Failed to set setXIncludeAware(true) for parser "
						+ docBuilderFactory + ":" + e, e);
			}
			DocumentBuilder builder = docBuilderFactory.newDocumentBuilder();
			Document doc = null;
			Element root = null;

			if (name instanceof URL) { // an URL resource
				URL url = (URL) name;
				if (url != null) {
					if (!quiet) {
						LOG.info("parsing " + url);
					}
					doc = builder.parse(url.toString());
				}
			} else if (name instanceof String) { // a CLASSPATH resource
				URL url = getResource((String) name);
				if (url != null) {
					if (!quiet) {
						LOG.info("parsing " + url);
					}
					doc = builder.parse(url.toString());
				}
			} else if (name instanceof InputStream) {
				try {
					doc = builder.parse((InputStream) name);
				} finally {
					((InputStream) name).close();
				}
			} else if (name instanceof Element) {
				root = (Element) name;
			}

			if (doc == null && root == null) {
				if (quiet)
					return;
				throw new RuntimeException(name + " not found");
			}

			if (root == null) {
				root = doc.getDocumentElement();
			}
			if (!"configuration".equals(root.getTagName()))
				LOG.error("bad conf file: top-level element not <configuration>");
			NodeList props = root.getChildNodes();
			for (int i = 0; i < props.getLength(); i++) {
				Node propNode = props.item(i);
				if (!(propNode instanceof Element))
					continue;
				Element prop = (Element) propNode;
				if ("configuration".equals(prop.getTagName())) {
					loadResource(properties, prop, quiet);
					continue;
				}
				if (!"property".equals(prop.getTagName()))
					LOG.warn("bad conf file: element not <property>");
				NodeList fields = prop.getChildNodes();
				String attr = null;
				String value = null;
				boolean finalParameter = false;
				for (int j = 0; j < fields.getLength(); j++) {
					Node fieldNode = fields.item(j);
					if (!(fieldNode instanceof Element))
						continue;
					Element field = (Element) fieldNode;
					if ("name".equals(field.getTagName())
							&& field.hasChildNodes())
						attr = ((Text) field.getFirstChild()).getData().trim();
					if ("value".equals(field.getTagName())
							&& field.hasChildNodes())
						value = ((Text) field.getFirstChild()).getData();
					if ("final".equals(field.getTagName())
							&& field.hasChildNodes())
						finalParameter = "true".equals(((Text) field
								.getFirstChild()).getData());
				}

				// Ignore this parameter if it has already been marked as
				// 'final'
				if (attr != null) {
					if (value != null) {
						if (!finalParameters.contains(attr)) {
							properties.setProperty(attr, value);
							updatingResource.put(attr, name.toString());
						} else if (!value.equals(properties.getProperty(attr))) {
							LOG.warn(name
									+ ":a attempt to override final parameter: "
									+ attr + ";  Ignoring.");
						}
					}
					if (finalParameter) {
						finalParameters.add(attr);
					}
				}
			}

		} catch (IOException e) {
			LOG.error("error parsing conf file", e);
			throw new RuntimeException(e);
		} catch (DOMException e) {
			LOG.error("error parsing conf file", e);
			throw new RuntimeException(e);
		} catch (SAXException e) {
			LOG.error("error parsing conf file", e);
			throw new RuntimeException(e);
		} catch (ParserConfigurationException e) {
			LOG.error("error parsing conf file", e);
			throw new RuntimeException(e);
		}
	}

	public void setClassLoader(ClassLoader classLoader) {
		this.classLoader = classLoader;
	}

	public ClassLoader getClassLoader() {
		return this.classLoader;
	}

	/**
	 * Write out the non-default properties in this configuration to the given
	 * {@link OutputStream}.
	 * 
	 * @param out
	 *            the output stream to write to.
	 */
	public void writeXml(OutputStream out) throws IOException {
		writeXml(new OutputStreamWriter(out));
	}

	/**
	 * Write out the non-default properties in this configuration to the given
	 * {@link Writer}.
	 * 
	 * @param out
	 *            the writer to write to.
	 */
	public void writeXml(Writer out) throws IOException {
		Document doc = asXmlDocument();
		try {
			DOMSource source = new DOMSource(doc);
			StreamResult result = new StreamResult(out);
			TransformerFactory transFactory = TransformerFactory.newInstance();
			Transformer transformer = transFactory.newTransformer();
			transformer.transform(source, result);
		} catch (TransformerException te) {
			throw new IOException(te);
		}
	}
	
	/**
	 * Set the quietness-mode.
	 * 
	 * In the quiet-mode, error and informational messages might not be logged.
	 * 
	 * @param quietmode
	 *            <code>true</code> to set quiet-mode on, <code>false</code> to
	 *            turn it off.
	 */
	public synchronized void setQuietMode(boolean quietmode) {
		this.quietmode = quietmode;
	}

	/**
	 * Return the XML DOM corresponding to this Configuration.
	 */
	private synchronized Document asXmlDocument() throws IOException {
		Document doc;
		Properties properties = getProps();
		try {
			doc = DocumentBuilderFactory.newInstance().newDocumentBuilder()
					.newDocument();
		} catch (ParserConfigurationException pe) {
			throw new IOException(pe);
		}
		Element conf = doc.createElement("configuration");
		doc.appendChild(conf);
		conf.appendChild(doc.createTextNode("\n"));
		for (Enumeration<Object> e = properties.keys(); e.hasMoreElements();) {
			String name = (String) e.nextElement();
			Object object = properties.get(name);
			String value = null;
			if (object instanceof String) {
				value = (String) object;
			} else {
				continue;
			}
			Element propNode = doc.createElement("property");
			conf.appendChild(propNode);
			if (updatingResource != null) {
				org.w3c.dom.Comment commentNode = doc.createComment("Loaded from "
						+ updatingResource.get(name));
				propNode.appendChild(commentNode);
			}
			Element nameNode = doc.createElement("name");
			nameNode.appendChild(doc.createTextNode(name));
			propNode.appendChild(nameNode);

			Element valueNode = doc.createElement("value");
			valueNode.appendChild(doc.createTextNode(value));
			propNode.appendChild(valueNode);

			conf.appendChild(doc.createTextNode("\n"));
		}
		return doc;
	}
}
