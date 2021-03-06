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
package org.nuxeo.apidoc.api;

public class BundleGroupFlatTree {

    protected final BundleGroup group;

    protected final int level;

    public BundleGroupFlatTree(BundleGroup group, int level) {
        this.group = group;
        this.level = level;
    }

    public BundleGroup getGroup() {
        return group;
    }

    public int getLevel() {
        return level;
    }

}
