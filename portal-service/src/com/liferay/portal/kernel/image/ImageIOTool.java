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

import com.liferay.portal.model.Image;

import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * @author Brian Wing Shun Chan
 * @author Alexander Chow
 */
public interface ImageIOTool {

	public BufferedImage convertImageType(BufferedImage sourceImage, int type);

	public RenderedImage crop(
		RenderedImage renderedImage, int height, int width, int x, int y);

	public BufferedImage getBufferedImage(RenderedImage paramRenderedImage);

	public byte[] getBytes(RenderedImage renderedImage, String contentType)
		throws IOException;

	public Image getImage(byte[] bytes) throws IOException;

	public Image getImage(File file) throws IOException;

	public Image getImage(InputStream is) throws IOException;

	public Image getImage(InputStream is, boolean cleanUpStream)
		throws IOException;

	public ImageBag read(byte[] bytes) throws IOException;

	public ImageBag read(File file) throws IOException;

	public ImageBag read(InputStream inputStream) throws IOException;

	public RenderedImage scale(RenderedImage renderedImage, int width);

	public RenderedImage scale(
		RenderedImage renderedImage, int maxHeight, int maxWidth);

	public void write(
			RenderedImage renderedImage, String contentType, OutputStream os)
		throws IOException;

}