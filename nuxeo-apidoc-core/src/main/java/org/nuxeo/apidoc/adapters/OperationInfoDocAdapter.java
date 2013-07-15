/*
 * (C) Copyright 2011 Nuxeo SA (http://nuxeo.com/) and contributors.
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
 *     Florent Guillaume
 */
package org.nuxeo.apidoc.adapters;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.nuxeo.apidoc.api.OperationInfo;
import org.nuxeo.common.utils.Path;
import org.nuxeo.ecm.automation.OperationDocumentation.Param;
import org.nuxeo.ecm.core.api.ClientException;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.PathRef;

/**
 * Adapter from a Nuxeo document to the {@link OperationInfo} interface.
 */
public class OperationInfoDocAdapter extends BaseNuxeoArtifactDocAdapter
        implements OperationInfo {

    protected OperationInfoDocAdapter(DocumentModel doc) {
        super(doc);
    }

    @Override
    public String getArtifactType() {
        return TYPE_NAME;
    }

    // artifact id with type-specific prefix
    @Override
    public String getId() {
        return ARTIFACT_PREFIX + getName();
    }

    @Override
    public String getName() {
        return safeGet(PROP_NAME);
    }

    @Override
    public String getVersion() {
        return safeGet(PROP_VERSION);
    }

    @Override
    public String getDescription() {
        return safeGet(PROP_DESCRIPTION);
    }

    @SuppressWarnings("unchecked")
    @Override
    public String[] getSignature() {
        try {
            return ((List<String>) doc.getPropertyValue(PROP_SIGNATURE)).toArray(new String[0]);
        } catch (Exception e) {
            log.error("Unable to get signature field", e);
        }
        return null;
    }

    @Override
    public String getCategory() {
        return safeGet(PROP_CATEGORY);
    }

    @Override
    public String getUrl() {
        return safeGet(PROP_URL);
    }

    @Override
    public String getLabel() {
        return safeGet(PROP_LABEL);
    }

    @Override
    public String getRequires() {
        return safeGet(PROP_REQUIRES);
    }

    @Override
    public String getSince() {
        return safeGet(PROP_SINCE);
    }

    @SuppressWarnings("unchecked")
    @Override
    public Param[] getParams() {
        List<Map<String, Serializable>> maps;
        try {
            maps = (List<Map<String, Serializable>>) doc.getPropertyValue(PROP_PARAMS);
        } catch (ClientException e) {
            throw new RuntimeException(e);
        }
        List<Param> params = new ArrayList<Param>();
        if (maps != null) {
            for (Map<String, Serializable> map : maps) {
                Param p = new Param();
                p.name = (String) map.get(PROP_PARAM_NAME);
                p.type = (String) map.get(PROP_PARAM_TYPE);
                p.widget = (String) map.get(PROP_PARAM_WIDGET);
                p.values = ((List<String>) map.get(PROP_PARAM_VALUES)).toArray(new String[0]);
                Long order = (Long) map.get(PROP_PARAM_ORDER);
                p.order = order == null ? 0 : order.intValue();
                Boolean required = (Boolean) map.get(PROP_PARAM_REQUIRED);
                p.isRequired = required == null ? false
                        : required.booleanValue();
                params.add(p);
            }
        }
        return params.toArray(new Param[params.size()]);
    }

    @Override
    public int compareTo(OperationInfo o) {
        String s1 = getLabel() == null ? getId() : getLabel();
        String s2 = o.getLabel() == null ? o.getId() : o.getLabel();
        return s1.compareTo(s2);
    }

    /**
     * Creates an actual document from the {@link OperationInfo}.
     */
    public static OperationInfo create(OperationInfo oi, CoreSession session,
            String containerPath) throws Exception {
        String name = computeDocumentName(oi.getId());
        String targetPath = new Path(containerPath).append(name).toString();
        boolean exists = session.exists(new PathRef(targetPath));
        DocumentModel doc;
        if (exists) {
            doc = session.getDocument(new PathRef(targetPath));
        } else {
            doc = session.createDocumentModel(TYPE_NAME);
            doc.setPathInfo(containerPath, name);
        }
        doc.setPropertyValue("dc:title", oi.getName());
        doc.setPropertyValue(PROP_NAME, oi.getName());
        doc.setPropertyValue(PROP_VERSION, oi.getVersion());
        doc.setPropertyValue(PROP_DESCRIPTION, oi.getDescription());
        doc.setPropertyValue(PROP_SIGNATURE, oi.getSignature());
        doc.setPropertyValue(PROP_CATEGORY, oi.getCategory());
        doc.setPropertyValue(PROP_URL, oi.getUrl());
        doc.setPropertyValue(PROP_LABEL, oi.getLabel());
        doc.setPropertyValue(PROP_REQUIRES, oi.getRequires());
        doc.setPropertyValue(PROP_SINCE, oi.getSince());
        doc.setPropertyValue(PROP_OP_CLASS, oi.getOperationClass());
        doc.setPropertyValue(PROP_CONTRIBUTING_COMPONENT, oi.getContributingComponent());
        List<Map<String, Serializable>> params = new ArrayList<Map<String, Serializable>>();
        for (Param p : oi.getParams()) {
            Map<String, Serializable> map = new HashMap<String, Serializable>();
            map.put(PROP_PARAM_NAME, p.getName());
            map.put(PROP_PARAM_TYPE, p.getType());
            map.put(PROP_PARAM_WIDGET, p.getWidget());
            map.put(PROP_PARAM_VALUES, p.getValues());
            map.put(PROP_PARAM_REQUIRED, Boolean.valueOf(p.isRequired()));
            map.put(PROP_PARAM_ORDER, Long.valueOf(p.getOrder()));
            params.add(map);
        }
        doc.setPropertyValue(PROP_PARAMS, (Serializable) params);
        if (exists) {
            doc = session.saveDocument(doc);
        } else {
            doc = session.createDocument(doc);
        }
        return new OperationInfoDocAdapter(doc);
    }


    @Override
    public String getOperationClass() {
        return safeGet(PROP_OP_CLASS);
    }

    @Override
    public String getContributingComponent() {
        return safeGet(PROP_CONTRIBUTING_COMPONENT);
    }

}
