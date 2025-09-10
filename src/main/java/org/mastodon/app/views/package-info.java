/**
 * Package containing generic classes used to build Mastodon views.
 * <p>
 * Mastodon views are {@link org.mastodon.app.views.MastodonFrameView2} that can
 * display the content of a {@link org.mastodon.app.AppModel}. They are
 * interactive, depending on the concrete AppModel capabilities.
 * <p>
 * These classes are independent from any particular Mastodon application, and
 * use the generic Mastodon classes, as well as the view components in the
 * package {@link org.mastodon.views}.
 * <p>
 * The package also contains the {@link org.mastodon.app.MastodonViewFactory}
 * hierarchy. The concrete implementations are used to create these views. The
 * factories know how to create a view, and how to de/serialize its GUI state.
 * The abstract classes there need to be subclassed for a particular app, typed
 * against a concrete AppModel.
 */
package org.mastodon.app.views;
