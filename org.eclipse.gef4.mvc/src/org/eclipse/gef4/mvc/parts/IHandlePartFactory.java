/*******************************************************************************
 * Copyright (c) 2014 itemis AG and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Alexander Nyßen (itemis AG) - initial API and implementation
 *     
 *******************************************************************************/
package org.eclipse.gef4.mvc.parts;

import java.util.List;
import java.util.Map;

import org.eclipse.gef4.mvc.behaviors.IBehavior;

/**
 * 
 * @author anyssen
 * 
 * @param <VR>
 */
public interface IHandlePartFactory<VR> {

	/**
	 * Creates specific {@link IHandlePart}s for the given <i>targets</i>, in
	 * the context specified by the given <i>contextBehavior</i> and
	 * <i>contextMap</i>.
	 * 
	 * As all {@link IBehavior}s should be stateless, all data required for the
	 * <i>contextBehavior</i> to be able to deliver certain information to the
	 * factory should be encapsulated in the <i>contextMap</i>, i.e.:
	 * 
	 * <pre>
	 * {@code}
	 * create(List targets, IBehavior ctxb, Map&lt;Object, Object&gt; ctxm) {
	 * 	if (ctxb instanceof ConcreteBehavior) {
	 * 		SomeParam p = ((ConcreteBehavior) ctxb).getSomeParam(ctxm);
	 * 	}
	 * }
	 * </pre>
	 * 
	 * @param targets
	 * @param contextBehavior
	 * @param contextMap
	 * @return
	 */
	public List<IHandlePart<VR>> createHandleParts(
			List<IContentPart<VR>> targets, IBehavior<VR> contextBehavior,
			Map<Object, Object> contextMap);

}
