/*
 * (C) Copyright 2006-2009 Nuxeo SAS (http://nuxeo.com/) and contributors.
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
 *     Nuxeo - initial API and implementation
 *
 * $Id$
 */

package org.nuxeo.apidoc.browse;

import java.net.URI;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.List;
import java.util.Map;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;

import org.nuxeo.apidoc.api.AssociatedDocuments;
import org.nuxeo.apidoc.api.DocumentationItem;
import org.nuxeo.apidoc.api.NuxeoArtifact;
import org.nuxeo.apidoc.documentation.DocumentationService;
import org.nuxeo.apidoc.snapshot.SnapshotManager;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.IdRef;
import org.nuxeo.ecm.webengine.WebEngine;
import org.nuxeo.ecm.webengine.model.Template;
import org.nuxeo.ecm.webengine.model.impl.DefaultObject;
import org.nuxeo.runtime.api.Framework;

/**
 * @author <a href="mailto:td@nuxeo.com">Thierry Delprat</a>
 *
 */
public abstract class NuxeoArtifactWebObject extends DefaultObject {

    public static final String DIST_ID = "distId";

    static {
        WebEngine we = Framework.getLocalService(WebEngine.class);
        we.getRegistry().addMessageBodyReader(new DocumentationItemReader());
    }

    protected String nxArtifactId;

    @Override
    protected void initialize(Object... args) {
        nxArtifactId = (String) args[0];
    }

    protected String getNxArtifactId() {
        return nxArtifactId;
    }

    @Override
    public Template getView(String viewId) {
        return super.getView(viewId).arg(DIST_ID, getDistributionId()).arg("enableDocumentationView", true);
    }

    protected abstract NuxeoArtifact getNxArtifact();

    protected abstract Object doGet() throws Exception;

    protected String getDistributionId() {
        return (String)ctx.getProperty(DIST_ID);
    }

    public AssociatedDocuments getAssociatedDocuments() {
        NuxeoArtifact nxItem = getNxArtifact();
        return nxItem.getAssociatedDocuments(ctx.getCoreSession());
    }

    @POST
    @Produces("text/html")
    @Path(value = "updateDocumentation")
    public Object doUpdateDocumentation(DocumentationItem docItem) throws Exception {

        DocumentationService ds = Framework.getLocalService(DocumentationService.class);

        ds.updateDocumentationItem(ctx.getCoreSession(), docItem);

        String targetUrl = computeUrl("/updateDocumentation");
        return Response.seeOther(new URI(targetUrl)).build();
    }

    protected String computeUrl(String suffix) throws Exception {
        String targetUrl = ctx.getUrlPath();
        targetUrl = URLDecoder.decode(targetUrl, "ISO-8859-1");
        targetUrl= targetUrl.replace(ctx.getBasePath(), "");
        targetUrl= targetUrl.replace(suffix, "/doc");
        targetUrl = URLEncoder.encode(targetUrl, "ISO-8859-1");
        return targetUrl;
    }


    @POST
    @Produces("text/html")
    @Path(value = "createDocumentation")
    public Object doCreateDocumentation(DocumentationItem docItem) throws Exception {

        DocumentationService ds = Framework.getLocalService(DocumentationService.class);

        ds.createDocumentationItem(ctx.getCoreSession(), getNxArtifact(), docItem.getTitle(), docItem.getContent(), docItem.getType(), docItem.getApplicableVersion(), docItem.isApproved(), docItem.getRenderingType());

        String targetUrl = computeUrl("/createDocumentation");
        return Response.seeOther(new URI(targetUrl)).build();
    }

    @GET
    @Produces("text/html")
    @Path(value = "doc")
    public Object doViewDoc() throws Exception {
        NuxeoArtifact nxItem = getNxArtifact();
        AssociatedDocuments docs = nxItem.getAssociatedDocuments(ctx.getCoreSession());
        return getView("../documentation").arg("nxItem", nxItem).arg("docs", docs).arg("docView", true);
    }

    @GET
    @Produces("text/html")
    @Path(value = "createForm")
    public Object doAddDoc() throws Exception {
        NuxeoArtifact nxItem = getNxArtifact();
        List<String> versions = SnapshotManager.getAvailableVersions(ctx.getCoreSession(), nxItem);
        DocumentationItem docItem = new SimpleDocumentationItem(nxItem);
        return getView("../docForm").arg("nxItem", nxItem).arg("mode","create").arg("docItem", docItem).arg("versions", versions).arg("docView", true);
    }

    @GET
    @Produces("text/html")
    @Path(value = "editForm/{uuid}")
    public Object doEditDoc(@PathParam("uuid") String uuid) throws Exception {
        NuxeoArtifact nxItem = getNxArtifact();
        List<String> versions = SnapshotManager.getAvailableVersions(ctx.getCoreSession(), nxItem);
        DocumentModel existingDoc = ctx.getCoreSession().getDocument(new IdRef(uuid));
        DocumentationItem docItem = existingDoc.getAdapter(DocumentationItem.class);
        return getView("../docForm").arg("nxItem", nxItem).arg("mode","edit").arg("docItem", docItem).arg("versions", versions).arg("docView", true);
    }


    public Map<String, String> getCategories() throws Exception {
        DocumentationService ds = Framework.getLocalService(DocumentationService.class);
        return ds.getCategories();
    }


}