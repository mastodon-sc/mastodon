/**
 * Classes that manage the grouping feature.
 * <p>
 * Groups are used to share state between views. Each view can belong to at most
 * one group at any time. A view that belongs to no group is effectively in its
 * own private group. Grouping mechanic is hidden from the views using
 * {@link ForwardingModel}s. Each view has an associated {@link GroupHandle}
 * from which it can obtain fixed forwarding models whose backing models are
 * transparently switched as group membership changes.
 */
package org.mastodon.grouping;
