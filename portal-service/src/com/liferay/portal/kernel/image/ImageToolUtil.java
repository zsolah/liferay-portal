/**
 * Copyright (c) 2000-present Liferay, Inc. All rights reserved.
 *
 * This library is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 */

package com.liferay.portal.kernel.image;

import aQute.bnd.annotation.ProviderType;

import com.liferay.portal.kernel.security.pacl.permission.PortalRuntimePermission;
import com.liferay.portal.model.Image;

import java.io.IOException;
import java.io.InputStream;

/**
 * The Image utility class.
 *
 * @author Brian Wing Shun Chan
 * @author Alexander Chow
 */
@ProviderType
public class ImageToolUtil {

	public static Image getDefaultCompanyLogo() {
		return getImageTool().getDefaultCompanyLogo();
	}

	public static Image getDefaultOrganizationLogo() {
		return getImageTool().getDefaultOrganizationLogo();
	}

	public static Image getDefaultSpacer() {
		return getImageTool().getDefaultSpacer();
	}

	public static Image getDefaultUserFemalePortrait() {
		return getImageTool().getDefaultUserFemalePortrait();
	}

	public static Image getDefaultUserMalePortrait() {
		return getImageTool().getDefaultUserMalePortrait();
	}

	public static ImageTool getImageTool() {
		PortalRuntimePermission.checkGetBeanProperty(ImageToolUtil.class);

		return _imageTool;
	}

	public static boolean isNullOrDefaultSpacer(byte[] bytes) {
		return getImageTool().isNullOrDefaultSpacer(bytes);
	}

	public void setImageTool(ImageTool imageTool) {
		PortalRuntimePermission.checkSetBeanProperty(getClass());

		_imageTool = imageTool;
	}

	private static ImageTool _imageTool;

}