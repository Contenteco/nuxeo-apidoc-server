/*
 * (C) Copyright 2006-2010 Nuxeo SA (http://nuxeo.com/) and contributors.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser General Public License
 * (LGPL) version 2.1 which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/lgpl.html
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * Contributors:
 *     Thierry Delprat
 */
package org.nuxeo.apidoc.tree;

import javax.servlet.http.HttpSession;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.nuxeo.apidoc.adapters.BaseNuxeoArtifactDocAdapter;
import org.nuxeo.apidoc.snapshot.DistributionSnapshot;
import org.nuxeo.apidoc.snapshot.SnapshotManager;
import org.nuxeo.common.utils.Path;
import org.nuxeo.ecm.core.api.NuxeoPrincipal;
import org.nuxeo.ecm.webengine.model.WebContext;
import org.nuxeo.ecm.webengine.ui.tree.TreeItem;
import org.nuxeo.runtime.api.Framework;

public class TreeHelper {

    protected static final Log log = LogFactory.getLog(TreeHelper.class);

    public static NuxeoArtifactTree getOrBuildAnonymousTree(WebContext ctx) {
        NuxeoArtifactTree tree = (NuxeoArtifactTree) ctx.getRequest().getAttribute(
                "tree--" + ctx.getProperty("distId"));
        if (tree == null) {
            tree = buildTree(ctx);
            ctx.getRequest().setAttribute("tree--" + ctx.getProperty("distId"),
                    tree);
        }
        return tree;
    }

    public static NuxeoArtifactTree getOrBuildTree(WebContext ctx) {

        HttpSession httpSession = ctx.getRequest().getSession(true);
        NuxeoArtifactTree tree = (NuxeoArtifactTree) httpSession.getAttribute("tree--"
                + ctx.getProperty("distId"));
        if (tree == null) {
            tree = buildTree(ctx);
            httpSession.setAttribute("tree--" + ctx.getProperty("distId"), tree);
        } else {
            tree.setDs(getRequestDS(ctx));
        }
        return tree;
    }

    public static DistributionSnapshot getRequestDS(WebContext ctx) {

        String id = "tree--ds--" + ctx.getProperty("distId");

        DistributionSnapshot ds = (DistributionSnapshot) ctx.getRequest().getAttribute(
                id);
        if (ds == null) {
            SnapshotManager sm = Framework.getLocalService(SnapshotManager.class);
            ds = sm.getSnapshot((String) ctx.getProperty("distId"),
                    ctx.getCoreSession());
            ctx.getRequest().setAttribute(id, ds);
        }

        return ds;
    }

    public static NuxeoArtifactTree buildTree(WebContext ctx) {
        DistributionSnapshot ds = getRequestDS(ctx);
        return new NuxeoArtifactTree(ctx, ds);
    }

    public static String updateTree(WebContext ctx, String source) {

        BaseNuxeoArtifactDocAdapter.setLocalCoreSession(ctx.getCoreSession());

        try {
            boolean anonymous = ((NuxeoPrincipal) ctx.getPrincipal()).isAnonymous();

            NuxeoArtifactTree tree = null;
            String lastPath = null;
            HttpSession httpSession = null;
            if (anonymous) {
                tree = getOrBuildAnonymousTree(ctx);
            } else {
                tree = getOrBuildTree(ctx);
                httpSession = ctx.getRequest().getSession(true);
                lastPath = (String) httpSession.getAttribute("tree-last-path");
            }

            if ("source".equalsIgnoreCase(source) || source == null) {
                tree.enter(ctx, "/");
                return tree.getTreeAsJSONArray(ctx);
            } else if (source.startsWith("source:")) {
                String anonymousPath = source.replace("source:", "");
                tree.enter(ctx, anonymousPath);
                return tree.getTreeAsJSONArray(ctx);
            } else {
                if (lastPath != null) {
                    TreeItem lastNode = tree.getTree().find(lastPath);
                    if (lastNode != null) {
                        lastNode.collapse();
                    } else {
                        log.warn("Unable to find previous selected tree node at path "
                                + lastPath);
                    }

                    String lastBranch = new Path(lastPath).segment(0);
                    String currentBranch = new Path(source).segment(0);
                    if (!currentBranch.equals(lastBranch)) {
                        TreeItem lastBranchItem = tree.getTree().find(
                                lastBranch);
                        if (lastBranchItem != null) {
                            lastBranchItem.collapse();
                        } else {
                            log.warn("Unable to find last branch " + lastBranch);
                        }
                    }
                }

                if (httpSession != null) {
                    httpSession.setAttribute("tree-last-path", source);
                }
                ctx.getRequest().setAttribute("tree-last-path", source);

                return tree.enter(ctx, source);
            }
        } finally {
            BaseNuxeoArtifactDocAdapter.releaseLocalCoreSession();
        }
    }

}
