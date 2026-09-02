/*-
 * #%L
 * Mastodon
 * %%
 * Copyright (C) 2014 - 2026 Tobias Pietzsch, Jean-Yves Tinevez
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
package org.mastodon.mamut.io.loader.util.credentials;

import java.util.function.Consumer;

import org.janelia.saalfeldlab.n5.universe.N5Factory;

import software.amazon.awssdk.auth.credentials.AwsCredentials;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.regions.providers.DefaultAwsRegionProviderChain;
import software.amazon.awssdk.services.s3.S3ClientBuilder;

/**
 * Configurations for the S3 client that {@link N5Factory} builds.
 * <p>
 * They all enable cross-region access. An {@code s3://bucket/key} URI does not
 * carry the region of the bucket, so the AWS SDK v2 falls back on the region of
 * the local AWS configuration. When the bucket lives in another region, S3
 * answers <i>301 Moved Permanently</i> and the container cannot be read.
 * Enabling cross-region access lets the client follow the redirect. The AWS
 * SDK v1 version of {@code n5-aws-s3} did not have this problem, because it
 * defaulted to the global {@code us-east-1} endpoint.
 * <p>
 * They also fall back on a default region when the machine has no AWS
 * configuration at all. The AWS SDK v2 refuses to build a client without a
 * region, which makes {@code n5-universe} give up on the S3 backend and fall
 * back on plain HTTP, and that cannot list the contents of an S3-compatible
 * server. The AWS SDK v1 version of {@code n5-aws-s3} defaulted to
 * {@code us-east-1} for exactly this reason.
 * <p>
 * Because {@link N5Factory#s3Configuration(Consumer)} <i>replaces</i> the
 * previous configuration instead of adding to it, credentials, cross-region
 * access and the region fallback must be set by one and the same
 * {@link Consumer}.
 */
public class S3Configurations
{

    /**
     * Does not specify credentials, which lets {@code n5-aws-s3} try anonymous
     * access first, and fall back on the default credentials provider chain if
     * the bucket cannot be read anonymously.
     *
     * @return a configuration for the S3 client builder.
     */
    public static Consumer< S3ClientBuilder > anonymousFirst()
    {
        return builder -> common( builder );
    }

    /**
     * Uses the default credentials provider chain (environment, system
     * properties, profile file, container and instance metadata).
     *
     * @return a configuration for the S3 client builder.
     */
    public static Consumer< S3ClientBuilder > defaultCredentials()
    {
        return builder -> common( builder ).credentialsProvider( DefaultCredentialsProvider.create() );
    }

    /**
     * Uses the specified credentials.
     *
     * @param credentials
     *            the credentials to authenticate with.
     * @return a configuration for the S3 client builder.
     */
    public static Consumer< S3ClientBuilder > credentials( final AwsCredentials credentials )
    {
        return builder -> common( builder ).credentialsProvider( StaticCredentialsProvider.create( credentials ) );
    }

    /**
     * The part every configuration shares: cross-region access, plus a default
     * region if the machine does not configure one.
     */
    private static S3ClientBuilder common( final S3ClientBuilder builder )
    {
        builder.crossRegionAccessEnabled( true );
        if ( !hasDefaultRegion() )
        {
            // Only as a last resort: leaving the region unset lets the region
            // carried by the URI, when there is one, take precedence.
            builder.region( FALLBACK_REGION );
        }
        return builder;
    }

    /**
     * Region used when the machine has no AWS configuration. This is the one
     * the AWS SDK v1 version of {@code n5-aws-s3} defaulted to.
     */
    private static final Region FALLBACK_REGION = Region.US_EAST_1;

    private static boolean hasDefaultRegion()
    {
        try
        {
            return DefaultAwsRegionProviderChain.builder().build().getRegion() != null;
        }
        catch ( final RuntimeException e )
        {
            return false;
        }
    }
}
