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

import java.util.ArrayList;
import java.util.List;

import org.nuxeo.apidoc.api.BundleGroup;
import org.nuxeo.apidoc.api.BundleInfo;
import org.nuxeo.apidoc.api.ComponentInfo;
import org.nuxeo.apidoc.api.ExtensionInfo;
import org.nuxeo.apidoc.api.ExtensionPointInfo;
import org.nuxeo.apidoc.api.NuxeoArtifact;
import org.nuxeo.apidoc.api.ServiceInfo;
import org.nuxeo.apidoc.api.VirtualNodesConsts;
import org.nuxeo.apidoc.snapshot.DistributionSnapshot;
import org.nuxeo.ecm.webengine.ui.tree.ContentProvider;

public class NuxeoArtifactContentProvider implements ContentProvider {

    private static final long serialVersionUID = 1L;

    protected final DistributionSnapshot ds;

    public NuxeoArtifactContentProvider(DistributionSnapshot ds) {
        this.ds = ds;
    }

    @Override
    public Object[] getElements(Object input) {
        return getChildren(input);
    }

    @Override
    public Object[] getChildren(Object ob) {

        List<NuxeoArtifact> result = new ArrayList<NuxeoArtifact>();
        NuxeoArtifact obj = (NuxeoArtifact) ob;

        if (obj.getArtifactType().equals(DistributionSnapshot.TYPE_NAME)) {
            for (BundleGroup bg : ds.getBundleGroups()) {
                result.add(bg);
            }
        } else if (obj.getArtifactType().equals(BundleInfo.TYPE_NAME)) {
            for (ComponentInfo ci : ds.getBundle(obj.getId()).getComponents()) {
                result.add(ci);
            }
        } else if (obj.getArtifactType().equals(BundleGroup.TYPE_NAME)) {
            for (String bid : ds.getBundleGroup(obj.getId()).getBundleIds()) {
                result.add(ds.getBundle(bid));
            }
            for (BundleGroup sbg : ds.getBundleGroup(obj.getId()).getSubGroups()) {
                result.add(sbg);
            }
        } else if (obj.getArtifactType().equals(ComponentInfo.TYPE_NAME)) {
            ComponentInfo ci = ds.getComponent(obj.getId());
            if (!ci.getExtensionPoints().isEmpty()) {
                result.add(new VirtualNode(ci, VirtualNodesConsts.ExtensionPoints_VNODE,
                        VirtualNodesConsts.ExtensionPoints_VNODE_NAME));
            }
            if (!ci.getServices().isEmpty()) {
                result.add(new VirtualNode(ci, VirtualNodesConsts.Services_VNODE,
                        VirtualNodesConsts.Services_VNODE_NAME));
            }
            if (!ci.getExtensions().isEmpty()) {
                result.add(new VirtualNode(ci, VirtualNodesConsts.Contributions_VNODE,
                        VirtualNodesConsts.Contributions_VNODE_NAME));
            }
        } else if (obj.getArtifactType().equals(VirtualNodesConsts.ExtensionPoints_VNODE)) {
            String cid = ((VirtualNode) obj).getComponentId();
            ComponentInfo ci = ds.getComponent(cid);
            for (ExtensionPointInfo epi : ci.getExtensionPoints()) {
                result.add(epi);
            }
        } else if (obj.getArtifactType().equals(VirtualNodesConsts.Contributions_VNODE)) {
            String cid = ((VirtualNode) obj).getComponentId();
            ComponentInfo ci = ds.getComponent(cid);
            for (ExtensionInfo ei : ci.getExtensions()) {
                result.add(ei);
            }
        } else if (obj.getArtifactType().equals(VirtualNodesConsts.Services_VNODE)) {
            String cid = ((VirtualNode) obj).getComponentId();
            ComponentInfo ci = ds.getComponent(cid);
            for (ServiceInfo si : ci.getServices()) {
                result.add(si);
            }
        }

        return result.toArray(new NuxeoArtifact[result.size()]);
    }

    @Override
    public String[] getFacets(Object object) {
        return null;
    }

    @Override
    public String getLabel(Object obj) {

        String label = null;
        if (obj instanceof NuxeoArtifact) {
            NuxeoArtifact nx = (NuxeoArtifact) obj;
            label = nx.getId();
            if (nx.getArtifactType().equals(ExtensionPointInfo.TYPE_NAME)) {
                label = ((ExtensionPointInfo) nx).getName();
            } else if (nx.getArtifactType().equals(ExtensionInfo.TYPE_NAME)) {
                String[] parts = label.split("--");
                // String component = parts[0];
                String ep = parts[1];
                label = ep;
            } else if (nx.getArtifactType().equals(ServiceInfo.TYPE_NAME)) {
                String[] parts = label.split("\\.");
                label = parts[parts.length - 1];
            } else if (nx.getArtifactType().equals(BundleGroup.TYPE_NAME)) {
                label = label.replace("grp:", "");
            } else if (nx.getArtifactType().equals(ComponentInfo.TYPE_NAME)) {
                if (label.startsWith("org.nuxeo.ecm.platform.")) {
                    label = label.replace("org.nuxeo.ecm.platform.", "...");
                } else if (label.startsWith("org.nuxeo.ecm.")) {
                    label = label.replace("org.nuxeo.ecm.", "...");
                } else if (label.startsWith("org.nuxeo.")) {
                    label = label.replace("org.nuxeo.", "...");
                }
            }
        }
        return label;
    }

    @Override
    public String getName(Object obj) {
        if (obj instanceof NuxeoArtifact) {
            NuxeoArtifact nx = (NuxeoArtifact) obj;
            return nx.getId();
        }
        return null;
    }

    @Override
    public boolean isContainer(Object ob) {
        NuxeoArtifact obj = (NuxeoArtifact) ob;

        return !(obj.getArtifactType().equals(ExtensionPointInfo.TYPE_NAME)
                || obj.getArtifactType().equals(ExtensionInfo.TYPE_NAME) || obj.getArtifactType().equals(
                ServiceInfo.TYPE_NAME));
    }

}
