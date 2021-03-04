/*-
 * #%L
 * Mastodon
 * %%
 * Copyright (C) 2014 - 2021 Tobias Pietzsch, Jean-Yves Tinevez
 * %%
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 * #L%
 */
package org.mastodon.grouping;

/**
 * Implements a model interface that forwards to a switchable
 * ({@link #linkTo(Object, boolean)}) backing model. This is used for grouping,
 * where the view-facing model is a {@code ForwardingModel} that redirects to
 * the model of the active group.
 * <p>
 * If Java would allow for it, this should be defined as
 * "{@code ForwardingModel<T> extends T}". Instead, {@link #asT()} provides a
 * {@code T}-typed reference.
 * </p>
 *
 * @author Tobias Pietzsch
 * @param <T>
 *            the type of the backing model.
 */
public interface ForwardingModel< T >
{
	/**
	 * Switch this {@code ForwardingModel} to a new backing {@code model}.
	 *
	 * @param model
	 *            the new backing model.
	 * @param copyCurrentStateToNewModel
	 *            whether the state of the current backing model should be
	 *            copied to the new backing model. (For example, when a view
	 *            moves from a shared group to its own singleton group, the
	 *            state is transferred.)
	 */
	public void linkTo( final T model, final boolean copyCurrentStateToNewModel );

	/**
	 * Get a {@code T} that forwards to whichever {@code model} was last
	 * {@link #linkTo(Object, boolean) linked to}.
	 *
	 * @return a {@code T} (usually {@code this}) that forwards linked
	 *         {@code model}
	 */
	@SuppressWarnings( "unchecked" )
	public default T asT()
	{
		return ( T ) this;
	}
}
