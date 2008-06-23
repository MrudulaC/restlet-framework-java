/*
 * Copyright 2005-2008 Noelios Consulting.
 * 
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the "License"). You may not use this file except in
 * compliance with the License.
 * 
 * You can obtain a copy of the license at
 * http://www.opensource.org/licenses/cddl1.txt See the License for the specific
 * language governing permissions and limitations under the License.
 * 
 * When distributing Covered Code, include this CDDL HEADER in each file and
 * include the License file at http://www.opensource.org/licenses/cddl1.txt If
 * applicable, add the following below this CDDL HEADER, with the fields
 * enclosed by brackets "[]" replaced with your own identifying information:
 * Portions Copyright [yyyy] [name of copyright owner]
 */
package org.restlet.ext.jaxrs.internal.wrappers;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.logging.Logger;

import javax.ws.rs.Encoded;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.ext.ContextResolver;

import org.restlet.ext.jaxrs.internal.core.ThreadLocalizedContext;
import org.restlet.ext.jaxrs.internal.exceptions.ConvertRepresentationException;
import org.restlet.ext.jaxrs.internal.exceptions.IllegalPathOnMethodException;
import org.restlet.ext.jaxrs.internal.exceptions.MissingAnnotationException;
import org.restlet.ext.jaxrs.internal.util.PathRegExp;
import org.restlet.ext.jaxrs.internal.util.Util;
import org.restlet.ext.jaxrs.internal.wrappers.params.ParameterList;
import org.restlet.ext.jaxrs.internal.wrappers.provider.EntityProviders;
import org.restlet.ext.jaxrs.internal.wrappers.provider.ExtensionBackwardMapping;

/**
 * An abstract wrapper class for resource methods, sub resource methods and sub
 * resource locators.
 * 
 * @author Stephan Koops
 */
public abstract class AbstractMethodWrapper extends AbstractJaxRsWrapper {

    /**
     * the Java method that should be called. This method could be different
     * from the method containing the annotations, see section 3.6 "Annotation
     * Inheritance" of JSR-311-spec.
     * 
     * @see ResourceMethod#annotatedMethod
     */
    final Method executeMethod;

    final ResourceClass resourceClass;

    final ParameterList parameters;

    AbstractMethodWrapper(Method executeMethod, Method annotatedMethod,
            ResourceClass resourceClass, ThreadLocalizedContext tlContext,
            EntityProviders entityProviders,
            Collection<ContextResolver<?>> allCtxResolvers,
            ExtensionBackwardMapping extensionBackwardMapping,
            boolean entityAllowed, Logger logger)
            throws IllegalPathOnMethodException, IllegalArgumentException,
            MissingAnnotationException {
        super(PathRegExp.createForMethod(annotatedMethod));
        this.executeMethod = executeMethod;
        this.executeMethod.setAccessible(true);
        // NICE log message, if an Exception with no exc mapper is declared.
        this.resourceClass = resourceClass;
        boolean leaveEncoded = resourceClass.isLeaveEncoded()
                || annotatedMethod.isAnnotationPresent(Encoded.class);
        this.parameters = new ParameterList(executeMethod, annotatedMethod,
                tlContext, leaveEncoded, entityProviders, allCtxResolvers,
                extensionBackwardMapping, entityAllowed, logger);
    }

    /**
     * Returns the name of the method
     * 
     * @return Returns the name of the method
     */
    public String getName() {
        Class<?>[] paramTypes = this.executeMethod.getParameterTypes();
        StringBuilder stb = new StringBuilder();
        stb.append(this.executeMethod.getName());
        stb.append('(');
        Util.append(stb, paramTypes);
        stb.append(')');
        return stb.toString();
    }

    /**
     * @return Returns the regular expression for the URI template
     */
    @Override
    public PathRegExp getPathRegExp() {
        return super.getPathRegExp();
    }

    /**
     * @return Returns the resource class of this method.
     */
    public ResourceClass getResourceClass() {
        return this.resourceClass;
    }

    /**
     * Invokes the method and returned the created representation for the
     * response.
     * 
     * @param resourceObject
     * @return the unwrapped returned object by the wrapped method.
     * @throws InvocationTargetException
     * @throws IllegalAccessException
     * @throws IllegalArgumentException
     * @throws InvocationTargetException
     * @throws WebApplicationException
     * @throws ConvertRepresentationException
     */
    Object internalInvoke(ResourceObject resourceObject)
            throws IllegalArgumentException, IllegalAccessException,
            InvocationTargetException, ConvertRepresentationException,
            WebApplicationException {
        Object[] args = this.parameters.get();
        Object jaxRsResourceObj = resourceObject.getJaxRsResourceObject();
        return executeMethod.invoke(jaxRsResourceObj, args);
    }

    @Override
    public String toString() {
        return this.getClass().getSimpleName() + "["
                + this.executeMethod.getDeclaringClass().getSimpleName() + "."
                + this.executeMethod.getName() + "(__)]";
    }
}