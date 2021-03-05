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
package org.mastodon.adapter;

import org.mastodon.Ref;

/**
 * Bidirectional mapping between "left" ({@code L}) and "right" ({@code R})
 * elements. This is used (for example) by adapters between model graph entities
 * and view graph entities.
 * <p>
 * The interface reflects that often both {@code L} and {@code R} are reusable
 * {@link Ref} objects. Also, the {@code R} wrapper often embeds a {@code L}
 * member that determines which source element the wrapper currently represents.
 *
 * @param <L>
 *            element type of "left" collection
 * @param <R>
 *            element type of "right" collection
 *
 * @author Tobias Pietzsch
 */
public interface RefBimap< L, R >
{
	/**
	 * Get the "left" element corresponding to {@code right}.
	 *
	 * @param right
	 *            element from the "right" collection.
	 *
	 * @return "left" element corresponding to {@code right}.
	 */
	public L getLeft( R right );

	/**
	 * Get the "right" element corresponding to {@code left}.
	 *
	 * @param left
	 *            element from the "left" collection.
	 * @param ref
	 *            an object reference that can be used for retrieval. Depending
	 *            on concrete implementation, this object can be cleared,
	 *            ignored or re-used.
	 *
	 * @return "right" element corresponding to {@code left}.
	 */
	public R getRight( L left, R ref );

	/**
	 * Get a "left" object reference.
	 *
	 * @param ref
	 *            an object reference that can be used for retrieval. In
	 *            implementations a {@code R} object often has an embedded
	 *            {@code L} object that can be extracted here.
	 *
	 * @return reusable object reference.
	 */
	public L reusableLeftRef( R ref );

	/**
	 * Generate a "right" object reference.
	 *
	 * @return a new, uninitialized, reference object.
	 */
	public R reusableRightRef();

	/**
	 * Release a previously created reference object.
	 *
	 * @param ref
	 *            the reference object to release.
	 */
	public void releaseRef( R ref );
}
