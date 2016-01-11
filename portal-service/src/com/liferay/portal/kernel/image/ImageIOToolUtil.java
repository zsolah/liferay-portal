/**
 * Copyright (c) 2000-present Liferay, Inc. All rights reserved.
 *
 * This library is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General public static License as published by the Free
 * Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General public static License for more
 * details.
 */

package com.liferay.portal.kernel.image;

import com.liferay.portal.kernel.security.pacl.permission.PortalRuntimePermission;
import com.liferay.portal.model.Image;

import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * The ImageIO utility class.
 *
 * @author Brian Wing Shun Chan
 * @author Alexander Chow
 */
public class ImageIOToolUtil {

	public static BufferedImage convertImageType(BufferedImage sourceImage, int type) {
		return getImageIOTool().convertImageType(sourceImage, type);
	}

	public static RenderedImage crop(
		RenderedImage renderedImage, int height, int width, int x, int y) {
		return getImageIOTool().crop(renderedImage, height, width, x, y);
	}

	public static BufferedImage getBufferedImage(RenderedImage paramRenderedImage) {
		return getImageIOTool().getBufferedImage(paramRenderedImage);
	}

	public static byte[] getBytes(RenderedImage renderedImage, String contentType)
		throws IOException {
		return getImageIOTool().getBytes(renderedImage, contentType);
	}

	public static Image getImage(byte[] bytes) throws IOException {
		return getImageIOTool().getImage(bytes);
	}

	public static Image getImage(File file) throws IOException {
		return getImageIOTool().getImage(file);
	}

	public static Image getImage(InputStream is) throws IOException {
		return getImageIOTool().getImage(is);
	}

	public static Image getImage(InputStream is, boolean cleanUpStream)
		throws IOException {
		return getImageIOTool().getImage(is, cleanUpStream);
	}

	public static ImageBag read(byte[] bytes) throws IOException {
		return getImageIOTool().read(bytes);
	}

	public static ImageBag read(File file) throws IOException {
		return getImageIOTool().read(file);
	}

	public static ImageBag read(InputStream inputStream) throws IOException {
		return getImageIOTool().read(inputStream);
	}

	public static RenderedImage scale(RenderedImage renderedImage, int width) {
		return getImageIOTool().scale(renderedImage, width);
	}

	public static RenderedImage scale(
		RenderedImage renderedImage, int maxHeight, int maxWidth) {
		return getImageIOTool().scale(renderedImage, maxHeight, maxWidth);
	}

	public static void write(
			RenderedImage renderedImage, String contentType, OutputStream os)
		throws IOException {
		getImageIOTool().write(renderedImage, contentType, os);
	}

	public static ImageIOTool getImageIOTool() {
		PortalRuntimePermission.checkGetBeanProperty(ImageIOToolUtil.class);

		return _imageIOTool;
	}

	public void setImageIOTool(ImageIOTool imageIOTool) {
		PortalRuntimePermission.checkSetBeanProperty(getClass());

		_imageIOTool = imageIOTool;
	}

	private static ImageIOTool _imageIOTool;
}