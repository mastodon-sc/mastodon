/**
 * <h1>Mastodon class hierarchy</h1>
 *
 * In this repository, we distinguish <b>two levels</b> for the class hierarchy:
 *
 *
 * <h2>1. Mastodon base classes.</h2>
 *
 * The base classes and interfaces to derive and implement to create an
 * application (or 'app') specific to a topic. What we call 'topic' could be
 * 'segment, track and lineage cells objects in 2D'.
 * <p>
 * The name of these classes is normally prefixed with
 * <code>MastodonSomething</code>
 * <p>
 * These classes are like abstract classes. By themselves, they cannot be made
 * to something an end-user can run. The core classes of an app would derive a
 * model for instance from a {@link org.mastodon.app.MastodonAppModel} and
 * derive an {@link org.mastodon.model.AbstractSpot} to store location of
 * objects.
 *
 *
 *
 * <h2>2. The MaMuT application.</h2>
 *
 * The classes of an app, whose topic is 'tracking and lineaging cells, with
 * cells represented as ellipsoids'.
 * <p>
 * This app is the final and central application of Mastodon. When end-users and
 * us speak of Mastodon, we mean this one. It is based on the
 * {@link org.mastodon.mamut.model.Model} and on
 * {@link org.mastodon.mamut.model.Spot}.
 * <p>
 * Because this app is the successor of the MaMuT project (<i>Wolff, Tinevez,
 * Pietzsch et al, 2018</i>) the classes of this app are called
 * <code>MaMuTSomething</code>. They are all located in the
 * <code>org.mastodon.mamut</code> package. The Mastodon base classes should not
 * depend on these classes, but the opposite is good.
 */
package org.mastodon;
