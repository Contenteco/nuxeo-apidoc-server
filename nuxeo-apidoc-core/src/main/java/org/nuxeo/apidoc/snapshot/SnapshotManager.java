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
package org.nuxeo.apidoc.snapshot;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.nuxeo.apidoc.api.NuxeoArtifact;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModel;

public interface SnapshotManager {

    void initSeamContext(HttpServletRequest request);

    DistributionSnapshot getRuntimeSnapshot();

    void addPersistentSnapshot(String key, DistributionSnapshot snapshot);

    DistributionSnapshot getSnapshot(String key, CoreSession session);

    List<DistributionSnapshot> readPersistentSnapshots(CoreSession session);

    List<DistributionSnapshot> listPersistentSnapshots(CoreSession session);

    Map<String, DistributionSnapshot> getPersistentSnapshots(CoreSession session);

    List<String> getPersistentSnapshotNames(CoreSession session);

    List<DistributionSnapshotDesc> getAvailableDistributions(CoreSession session);

    List<String> getAvailableVersions(CoreSession session, NuxeoArtifact nxItem);

    void exportSnapshot(CoreSession session, String key, OutputStream out) throws IOException;

    void importSnapshot(CoreSession session, InputStream is) throws IOException;

    DistributionSnapshot persistRuntimeSnapshot(CoreSession session);

    DistributionSnapshot persistRuntimeSnapshot(CoreSession session, String name);

    DistributionSnapshot persistRuntimeSnapshot(CoreSession session, String name, SnapshotFilter filter);

    void validateImportedSnapshot(CoreSession session, String name, String version, String pathSegment, String title);

    DocumentModel importTmpSnapshot(CoreSession session, InputStream is) throws IOException;

}
