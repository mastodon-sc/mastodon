/*-
 * #%L
 * Mastodon
 * %%
 * Copyright (C) 2014 - 2024 Tobias Pietzsch, Jean-Yves Tinevez
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
package org.mastodon.mamut.io.loader.util.mobie;

public enum AxesTypes
{
    TIME( "time" ),
    CHANNEL( "channel" ),
    SPACE( "space" );

    private final String typeName;

    AxesTypes( final String typeName )
    {
        this.typeName = typeName;
    }

    public static boolean contains( final String test )
    {
        for ( final AxesTypes c : AxesTypes.values() )
        {
            if ( c.typeName.equals( test ) )
            {
                return true;
            }
        }
        return false;
    }

    public static AxesTypes getAxisType( final String axisString )
    {
        if ( axisString.equals( "x" ) || axisString.equals( "y" ) || axisString.equals( "z" ) )
        {
            return AxesTypes.SPACE;
        }
        else if ( axisString.equals( "t" ) )
        {
            return AxesTypes.TIME;
        }
        else if ( axisString.equals( "c" ) )
        {
            return AxesTypes.CHANNEL;
        }
        else
        {
            return null;
        }
    }

    public String getTypeName()
    {
        return typeName;
    }
}
