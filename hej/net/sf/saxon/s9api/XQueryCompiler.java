package net.sf.saxon.s9api;

import net.sf.saxon.query.ModuleURIResolver;
import net.sf.saxon.query.StaticQueryContext;
import net.sf.saxon.trans.XPathException;

import javax.xml.transform.ErrorListener;
import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;

/**
 *  An XQueryCompiler object allows XQuery 1.0 queries to be compiled. The compiler holds information that
 *  represents the static context for the compilation.
 *
  * <p>To construct an XQueryCompiler, use the factory method {@link Processor#newXQueryCompiler}.</p>
 *
 * <p>An XQueryCompiler may be used repeatedly to compile multiple queries. Any changes made to the
 * XQueryCompiler (that is, to the static context) do not affect queries that have already been compiled.
 * An XQueryCompiler may be used concurrently in multiple threads, but it should not then be modified once
 * initialized.</p>
 *
 * @since 9.0
 */

public class XQueryCompiler {

    private Processor processor;
    private StaticQueryContext env;
    private ItemType requiredContextItemType;
    private String encoding;

    /**
     * Protected constructor
     * @param processor the Saxon Processor
     */

    protected XQueryCompiler(Processor processor) {
        this.processor = processor;
        this.env = processor.getUnderlyingConfiguration().newStaticQueryContext();
    }

    /**
     * Set the static base URI for the query
     * @param baseURI the static base URI
     */

    public void setBaseURI(URI baseURI) {
        if (!baseURI.isAbsolute()) {
            throw new IllegalArgumentException("Base URI must be an absolute URI");
        }
        env.setBaseURI(baseURI.toString());
    }

    /**
     * Get the static base URI for the query
     * @return the static base URI
     */

    public URI getBaseURI() {
        try {
            return new URI(env.getBaseURI());
        } catch (URISyntaxException err) {
            throw new IllegalStateException(err);
        }
    }

    /**
     * Set the ErrorListener to be used during this query compilation episode
     * @param listener The error listener to be used. This is notified of all errors detected during the
     * compilation.
     */

    public void setErrorListener(ErrorListener listener) {
        env.setErrorListener(listener);
    }

    /**
     * Get the ErrorListener being used during this compilation episode
     * @return listener The error listener in use. This is notified of all errors detected during the
     * compilation. If no user-supplied ErrorListener has been set, returns the system-supplied
     * ErrorListener.
     */

    public ErrorListener getErrorListener() {
        return env.getErrorListener();
    }

    /**
     * Set whether trace hooks are to be included in the compiled code. To use tracing, it is necessary
     * both to compile the code with trace hooks included, and to supply a TraceListener at run-time
     * @param option true if trace code is to be compiled in, false otherwise
     */

    public void setCompileWithTracing(boolean option) {
        env.setCompileWithTracing(option);
    }

    /**
     * Ask whether trace hooks are included in the compiled code.
     * @return true if trace hooks are included, false if not.
     */

    public boolean isCompileWithTracing() {
        return env.isCompileWithTracing();
    }

    /**
     * Set a user-defined ModuleURIResolver for resolving URIs used in <code>import module</code>
     * declarations in the XQuery prolog.
     * This will override any ModuleURIResolver that was specified as part of the configuration.
     * @param resolver the ModuleURIResolver to be used
     */

    public void setModuleURIResolver(ModuleURIResolver resolver) {
        env.setModuleURIResolver(resolver);
    }

    /**
     * Get the user-defined ModuleURIResolver for resolving URIs used in <code>import module</code>
     * declarations in the XQuery prolog; returns null if none has been explicitly set either
     * here or in the Saxon Configuration.
     * @return the registered ModuleURIResolver
     */

    public ModuleURIResolver getModuleURIResolver() {
        return env.getModuleURIResolver();
    }

    /**
     * Set the encoding of the supplied query. This is ignored if the query is supplied
     * in character form, that is, as a <code>String</code> or as a <code>Reader</code>. If no value
     * is set, the query processor will attempt to infer the encoding, defaulting to UTF-8 if no
     * information is available.
     * @param encoding the encoding of the supplied query, for example "iso-8859-1"
     * @since 9.1
     */

    public void setEncoding(String encoding) {
        this.encoding = encoding;
    }

    /**
     * Get the encoding previously set for the supplied query.
     * @return the encoding previously set using {@link #setEncoding(String)}, or null
     * if no value has been set. Note that this is not necessarily the actual encoding of the query.
     * @since 9.2
     */

    public String getEncoding() {
        return encoding;
    }

    /**
     * Say whether the query is allowed to be updating. XQuery update syntax will be rejected
     * during query compilation unless this flag is set. XQuery Update is supported only under Saxon-EE.
     * @param updating true if the query is allowed to use the XQuery Update facility
     * (requires Saxon-EE). If set to false, the query must not be an updating query. If set
     * to true, it may be either an updating or a non-updating query.
     * @since 9.1
     */

    public void setUpdatingEnabled(boolean updating) {
        env.setUpdatingEnabled(updating);
    }

    /**
     * Ask whether the query is allowed to use XQuery Update syntax
     * @return true if the query is allowed to use the XQuery Update facility. Note that this
     * does not necessarily mean that the query is an updating query; but if the value is false,
     * the it must definitely be non-updating.
     * @since 9.1
     */

    public boolean isUpdatingEnabled() {
        return env.isUpdatingEnabled();
    }

    /**
     * Say that the query must be compiled to be schema-aware, even if it contains no
     * "import schema" declarations. Normally a query is treated as schema-aware
     * only if it contains one or more "import schema" declarations. If it is not schema-aware,
     * then all input documents must be untyped (or xs:anyType), and validation of temporary nodes is disallowed
     * (though validation of the final result tree is permitted). Setting the argument to true
     * means that schema-aware code will be compiled regardless.
     * @param schemaAware If true, the stylesheet will be compiled with schema-awareness
     * enabled even if it contains no xsl:import-schema declarations. If false, the stylesheet
     * is treated as schema-aware only if it contains one or more xsl:import-schema declarations.
     * @since 9.2
     */

    public void setSchemaAware(boolean schemaAware) {
        env.getExecutable().setSchemaAware(schemaAware);
    }

    /**
     * Ask whether schema-awareness has been requested either by means of a call on
     * {@link #setSchemaAware}
     * @return true if schema-awareness has been requested
     * @since 9.2
     */

    public boolean isSchemaAware() {
        return env.getExecutable().isSchemaAware();
    }

    /**
     * Say whether an XQuery 1.0 or XQuery 1.1 processor is required.
     * @param version Must be "1.0" or "1.1". At present very limited support
     * for XQuery 1.1 is available. This functionality is available only in Saxon-EE, and it cannot
     * be used in conjunction with XQuery Updates. To use XQuery 1.1 features,
     * the query prolog must also specify version="1.1".
     * @throws IllegalArgumentException if the version is not 1.0 or 1.1.
     * @since 9.2
     */

    public void setLanguageVersion(String version) {
        if (!"1.0".equals(version) && !"1.1".equals(version)) {
            throw new IllegalArgumentException("LanguageVersion " + version);
        }
        env.setLanguageVersion(version);
    }

    /**
     * Ask whether an XQuery 1.0 or XQuery 1.1 processor is being used
     * @return version: "1.0" or "1.1"
     * @since 9.2
     */

    public String getLanguageVersion() {
        return env.getLanguageVersion();
    }

    /**
     * Declare a namespace binding as part of the static context for queries compiled using this
     * XQueryCompiler. This binding may be overridden by a binding that appears in the query prolog.
     * The namespace binding will form part of the static context of the query, but it will not be copied
     * into result trees unless the prefix is actually used in an element or attribute name.
     *
     * @param prefix The namespace prefix. If the value is a zero-length string, this method sets the default
     *               namespace for elements and types.
     * @param uri    The namespace URI. It is possible to specify a zero-length string to "undeclare" a namespace;
     *               in this case the prefix will not be available for use, except in the case where the prefix
     *               is also a zero length string, in which case the absence of a prefix implies that the name
     *               is in no namespace.
     * @throws NullPointerException if either the prefix or uri is null.
     * @throws IllegalArgumentException in the event of an invalid declaration of the XML namespace
     */

    public void declareNamespace(String prefix, String uri) {
        env.declareNamespace(prefix, uri);
    }

    /**
     * Declare the static type of the context item. If this type is declared, and if a context item
     * is supplied when the query is invoked, then the context item must conform to this type (no
     * type conversion will take place to force it into this type).
     * @param type the required type of the context item
     */

    public void setRequiredContextItemType(ItemType type) {
        requiredContextItemType = type;
        env.setRequiredContextItemType(type.getUnderlyingItemType());
    }

    /**
     * Get the required type of the context item. If no type has been explicitly declared for the context
     * item, an instance of AnyItemType (representing the type item()) is returned.
     * @return the required type of the context item
     */

    public ItemType getRequiredContextItemType() {
        return requiredContextItemType;
    }

    /**
     * Compile a library module supplied as a string. The code generated by compiling the library is available
     * for importing by all subsequent compilations using the same XQueryCompiler; it is identified by an
     * "import module" declaration that specifies the module URI of the library module. No module location
     * hint is required, and if one is present, it is ignored.
     * <p>The base URI of the query should be supplied by calling {@link #setBaseURI(java.net.URI)} </p>
     * <p>Separate compilation of library modules is supported only under Saxon-EE</p>
     * @param query the text of the query
     * @throws SaxonApiException if the query compilation fails with a static error
     * @since 9.2
     */

     public void compileLibrary(String query) throws SaxonApiException {
        try {
            env.compileLibrary(query);
        } catch (XPathException e) {
            throw new SaxonApiException(e);
        }
     }

    /**
     * Compile a library module  supplied as a file. The code generated by compiling the library is available
     * for importing by all subsequent compilations using the same XQueryCompiler; it is identified by an
     * "import module" declaration that specifies the module URI of the library module. No module location
     * hint is required, and if one is present, it is ignored.
     * <p>The encoding of the input stream may be specified using {@link #setEncoding(String)} </p>
     * <p>Separate compilation of library modules is supported only under Saxon-EE</p>
     * @param query the file containing the query. The URI corresponding to this file will be used as the
     * base URI of the query, overriding any URI supplied using {@link #setBaseURI(java.net.URI)} (but not
     * overriding any base URI specified within the query prolog)
     * @throws SaxonApiException if the query compilation fails with a static error
     * @throws IOException if the file does not exist or cannot be read
     * @since 9.2
     */

    public void compileLibrary(File query) throws SaxonApiException, IOException {
        try {
            String savedBaseUri = env.getBaseURI();
            env.setBaseURI(query.toURI().toString());
            env.compileQuery(new FileInputStream(query), encoding);
            env.setBaseURI(savedBaseUri);
        } catch (XPathException e) {
            throw new SaxonApiException(e);
        }
    }

    /**
     * Compile a library module supplied as a Reader. The code generated by compiling the library is available
     * for importing by all subsequent compilations using the same XQueryCompiler; it is identified by an
     * "import module" declaration that specifies the module URI of the library module. No module location
     * hint is required, and if one is present, it is ignored.
     * <p>The base URI of the query should be supplied by calling {@link #setBaseURI(java.net.URI)} </p>
     * <p>Separate compilation of library modules is supported only under Saxon-EE</p>
     * @param query the text of the query
     * @throws SaxonApiException if the query compilation fails with a static error
     * @since 9.2
     */

     public void compileLibrary(Reader query) throws SaxonApiException {
        try {
            env.compileLibrary(query);
        } catch (XPathException e) {
            throw new SaxonApiException(e);
        } catch (IOException e) {
            throw new SaxonApiException(e);
        }
    }

    /**
     * Compile a library module supplied as an InputStream. The code generated by compiling the library is available
     * for importing by all subsequent compilations using the same XQueryCompiler; it is identified by an
     * "import module" declaration that specifies the module URI of the library module. No module location
     * hint is required, and if one is present, it is ignored.
     * <p>The encoding of the input stream may be specified using {@link #setEncoding(String)} </p>
     * <p>The base URI of the query should be supplied by calling {@link #setBaseURI(java.net.URI)} </p>
     * <p>Separate compilation of library modules is supported only under Saxon-EE</p>
     * @param query the text of the query
     * @throws SaxonApiException if the query compilation fails with a static error
     * @since 9.2
     */

     public void compileLibrary(InputStream query) throws SaxonApiException {
        try {
            env.compileLibrary(query, encoding);
        } catch (XPathException e) {
            throw new SaxonApiException(e);
        } catch (IOException e) {
            throw new SaxonApiException(e);
        }
    }

    /**
     * Compile a query supplied as a string.
     * <p>The base URI of the query should be supplied by calling {@link #setBaseURI(java.net.URI)} </p>
     * @param query the text of the query
     * @return an XQueryExecutable representing the compiled query
     * @throws SaxonApiException if the query compilation fails with a static error
     * @since 9.0
     */

    public XQueryExecutable compile(String query) throws SaxonApiException {
        try {
            return new XQueryExecutable(processor, env.compileQuery(query));
        } catch (XPathException e) {
            throw new SaxonApiException(e);
        }
    }

    /**
     * Compile a query supplied as a file
     * @param query the file containing the query. The URI corresponding to this file will be used as the
     * base URI of the query, overriding any URI supplied using {@link #setBaseURI(java.net.URI)} (but not
     * overriding any base URI specified within the query prolog)
     * @return an XQueryExecutable representing the compiled query
     * @throws SaxonApiException if the query compilation fails with a static error
     * @throws IOException if the file does not exist or cannot be read
     * @since 9.1
     */

    public XQueryExecutable compile(File query) throws SaxonApiException, IOException {
        try {
            String savedBaseUri = env.getBaseURI();
            env.setBaseURI(query.toURI().toString());
            XQueryExecutable exec =
                    new XQueryExecutable(processor, env.compileQuery(new FileInputStream(query), encoding));
            env.setBaseURI(savedBaseUri);
            return exec;
        } catch (XPathException e) {
            throw new SaxonApiException(e);
        }
    }

    /**
     * Compile a query supplied as an InputStream
     * <p>The base URI of the query should be supplied by calling {@link #setBaseURI(java.net.URI)} </p>
     * @param query the input stream on which the query is supplied. This will be consumed by this method
     * @return an XQueryExecutable representing the compiled query
     * @throws SaxonApiException if the query compilation fails with a static error
     * @throws IOException if the file does not exist or cannot be read
     * @since 9.1
     */

    public XQueryExecutable compile(InputStream query) throws SaxonApiException, IOException {
        try {
            return new XQueryExecutable(processor, env.compileQuery(query, encoding));
        } catch (XPathException e) {
            throw new SaxonApiException(e);
        }
    }

    /**
     * Compile a query supplied as a Reader
     * <p>The base URI of the query should be supplied by calling {@link #setBaseURI(java.net.URI)} </p>
     * @param query the input stream on which the query is supplied. This will be consumed by this method
     * @return an XQueryExecutable representing the compiled query
     * @throws SaxonApiException if the query compilation fails with a static error
     * @throws IOException if the file does not exist or cannot be read
     * @since 9.1
     */

    public XQueryExecutable compile(Reader query) throws SaxonApiException, IOException {
        try {
            return new XQueryExecutable(processor, env.compileQuery(query));
        } catch (XPathException e) {
            throw new SaxonApiException(e);
        }
    }

    /**
     * Compile an XQuery library module, together with any modules that it imports, as a free-standing
     * function library.
     * @since 9.2
     */

//    public XQueryLibrary compileLibrary(File libraryModule) throws SaxonApiException, IOException {
//        try {
//            String savedBaseUri = env.getBaseURI();
//            env.setBaseURI(libraryModule.toURI().toString());
//            XQueryLibrary exec =
//                    new XQueryLibrary(processor, env.compileLibrary(new FileInputStream(libraryModule), encoding));
//            env.setBaseURI(savedBaseUri);
//            return exec;
//        } catch (XPathException e) {
//            throw new SaxonApiException(e);
//        }

    /**
     * Get the underlying {@link net.sf.saxon.query.StaticQueryContext} object that maintains the static context
     * information on behalf of this XQueryCompiler. This method provides an escape hatch to internal Saxon
     * implementation objects that offer a finer and lower-level degree of control than the s9api classes and
     * methods. Some of these classes and methods may change from release to release.
     * @return the underlying StaticQueryContext object
     * @since 9.2
     */

    public StaticQueryContext getUnderlyingStaticContext() {
        return env;
    }




}

//
// The contents of this file are subject to the Mozilla Public License Version 1.0 (the "License");
// you may not use this file except in compliance with the License. You may obtain a copy of the
// License at http://www.mozilla.org/MPL/
//
// Software distributed under the License is distributed on an "AS IS" basis,
// WITHOUT WARRANTY OF ANY KIND, either express or implied.
// See the License for the specific language governing rights and limitations under the License.
//
// The Original Code is: all this file
//
// The Initial Developer of the Original Code is Michael H. Kay.
//
// Contributor(s):
//
