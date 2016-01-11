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

import com.liferay.portal.model.Image;

/**
 * @author Brian Wing Shun Chan
 * @author Alexander Chow
 */
@ProviderType
public interface ImageTool {

	public static final String TYPE_BMP = "bmp";

	public static final String TYPE_GIF = "gif";

	public static final String TYPE_JPEG = "jpg";

	public static final String TYPE_NOT_AVAILABLE = "na";

	public static final String TYPE_PNG = "png";

	public static final String TYPE_TIFF = "tiff";

	public Image getDefaultCompanyLogo();

	public Image getDefaultOrganizationLogo();

	public Image getDefaultSpacer();

	public Image getDefaultUserFemalePortrait();

	public Image getDefaultUserMalePortrait();

	public boolean isNullOrDefaultSpacer(byte[] bytes);

}