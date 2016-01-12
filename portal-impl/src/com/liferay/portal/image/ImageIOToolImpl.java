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

package com.liferay.portal.image;

import com.liferay.portal.kernel.image.ImageBag;
import com.liferay.portal.kernel.image.ImageIOTool;
import com.liferay.portal.kernel.image.ImageTool;
import com.liferay.portal.kernel.io.unsync.UnsyncByteArrayOutputStream;
import com.liferay.portal.kernel.util.ArrayUtil;
import com.liferay.portal.kernel.util.StringUtil;
import com.liferay.portal.model.Image;
import com.liferay.portal.model.impl.ImageImpl;
import com.liferay.portal.util.FileImpl;
import com.liferay.portal.util.PropsValues;

import java.awt.AlphaComposite;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsConfiguration;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.RenderedImage;
import java.awt.image.WritableRaster;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Queue;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;

/**
 * @author Brian Wing Shun Chan
 * @author Alexander Chow
 * @author Shuyang Zhou
 */

public class ImageIOToolImpl implements ImageIOTool {

	public static ImageIOTool getInstance() {
		return _instance;
	}

	public BufferedImage convertImageType(BufferedImage sourceImage, int type) {
		BufferedImage targetImage = new BufferedImage(
			sourceImage.getWidth(), sourceImage.getHeight(), type);

		Graphics2D graphics = targetImage.createGraphics();

		graphics.drawRenderedImage(sourceImage, null);

		graphics.dispose();

		return targetImage;
	}

	@Override
	public RenderedImage crop(
		RenderedImage renderedImage, int height, int width, int x, int y) {

		Rectangle rectangle = new Rectangle(x, y, width, height);

		Rectangle croppedRectangle = rectangle.intersection(
			new Rectangle(renderedImage.getWidth(), renderedImage.getHeight()));

		BufferedImage bufferedImage = getBufferedImage(renderedImage);

		return bufferedImage.getSubimage(
			croppedRectangle.x, croppedRectangle.y, croppedRectangle.width,
			croppedRectangle.height);
	}

	@Override
	public BufferedImage getBufferedImage(RenderedImage renderedImage) {
		if (renderedImage instanceof BufferedImage) {
			return (BufferedImage)renderedImage;
		}

		ColorModel colorModel = renderedImage.getColorModel();

		WritableRaster writableRaster =
			colorModel.createCompatibleWritableRaster(
				renderedImage.getWidth(), renderedImage.getHeight());

		Hashtable<String, Object> properties = new Hashtable<>();

		String[] keys = renderedImage.getPropertyNames();

		if (!ArrayUtil.isEmpty(keys)) {
			for (String key : keys) {
				properties.put(key, renderedImage.getProperty(key));
			}
		}

		BufferedImage bufferedImage = new BufferedImage(
			colorModel, writableRaster, colorModel.isAlphaPremultiplied(),
			properties);

		renderedImage.copyData(writableRaster);

		return bufferedImage;
	}

	@Override
	public byte[] getBytes(RenderedImage renderedImage, String contentType)
		throws IOException {

		UnsyncByteArrayOutputStream baos = new UnsyncByteArrayOutputStream();

		write(renderedImage, contentType, baos);

		return baos.toByteArray();
	}

	@Override
	public Image getImage(byte[] bytes) throws IOException {
		if (bytes == null) {
			return null;
		}

		ImageBag imageBag = read(bytes);

		RenderedImage renderedImage = imageBag.getRenderedImage();

		if (renderedImage == null) {
			throw new IOException("Unable to decode image");
		}

		String type = imageBag.getType();

		int height = renderedImage.getHeight();
		int width = renderedImage.getWidth();
		int size = bytes.length;

		Image image = new ImageImpl();

		image.setTextObj(bytes);
		image.setType(type);
		image.setHeight(height);
		image.setWidth(width);
		image.setSize(size);

		return image;
	}

	@Override
	public Image getImage(File file) throws IOException {
		byte[] bytes = _fileUtil.getBytes(file);

		return getImage(bytes);
	}

	@Override
	public Image getImage(InputStream is) throws IOException {
		byte[] bytes = _fileUtil.getBytes(is, -1, true);

		return getImage(bytes);
	}

	@Override
	public Image getImage(InputStream is, boolean cleanUpStream)
		throws IOException {

		byte[] bytes = _fileUtil.getBytes(is, -1, cleanUpStream);

		return getImage(bytes);
	}

	@Override
	public ImageBag read(byte[] bytes) throws IOException {
		String formatName = null;
		ImageInputStream imageInputStream = null;
		Queue<ImageReader> imageReaders = new LinkedList<>();
		RenderedImage renderedImage = null;

		try {
			imageInputStream = ImageIO.createImageInputStream(
				new ByteArrayInputStream(bytes));

			Iterator<ImageReader> iterator = ImageIO.getImageReaders(
				imageInputStream);

			while ((renderedImage == null) && iterator.hasNext()) {
				ImageReader imageReader = iterator.next();

				imageReaders.offer(imageReader);

				imageReader.setInput(imageInputStream);

				renderedImage = imageReader.read(0);

				formatName = imageReader.getFormatName();
			}
		}
		finally {
			while (!imageReaders.isEmpty()) {
				ImageReader imageReader = imageReaders.poll();

				imageReader.dispose();
			}

			if (imageInputStream != null) {
				imageInputStream.close();
			}
		}

		formatName = StringUtil.toLowerCase(formatName);

		String type = ImageTool.TYPE_JPEG;

		if (formatName.contains(ImageTool.TYPE_BMP)) {
			type = ImageTool.TYPE_BMP;
		}
		else if (formatName.contains(ImageTool.TYPE_GIF)) {
			type = ImageTool.TYPE_GIF;
		}
		else if (formatName.contains("jpeg") || type.equals("jpeg")) {
			type = ImageTool.TYPE_JPEG;
		}
		else if (formatName.contains(ImageTool.TYPE_PNG)) {
			type = ImageTool.TYPE_PNG;
		}
		else if (formatName.contains(ImageTool.TYPE_TIFF)) {
			type = ImageTool.TYPE_TIFF;
		}
		else {
			throw new IllegalArgumentException(type + " is not supported");
		}

		return new ImageBag(renderedImage, type);
	}

	@Override
	public ImageBag read(File file) throws IOException {
		return read(_fileUtil.getBytes(file));
	}

	@Override
	public ImageBag read(InputStream inputStream) throws IOException {
		return read(_fileUtil.getBytes(inputStream));
	}

	@Override
	public RenderedImage scale(RenderedImage renderedImage, int width) {
		if (width <= 0) {
			return renderedImage;
		}

		int imageHeight = renderedImage.getHeight();
		int imageWidth = renderedImage.getWidth();

		double factor = (double)width / imageWidth;

		int scaledHeight = (int)Math.round(factor * imageHeight);
		int scaledWidth = width;

		return doScale(renderedImage, scaledHeight, scaledWidth);
	}

	@Override
	public RenderedImage scale(
		RenderedImage renderedImage, int maxHeight, int maxWidth) {

		int imageHeight = renderedImage.getHeight();
		int imageWidth = renderedImage.getWidth();

		if (maxHeight == 0) {
			maxHeight = imageHeight;
		}

		if (maxWidth == 0) {
			maxWidth = imageWidth;
		}

		if ((imageHeight <= maxHeight) && (imageWidth <= maxWidth)) {
			return renderedImage;
		}

		double factor = Math.min(
			(double)maxHeight / imageHeight, (double)maxWidth / imageWidth);

		int scaledHeight = Math.max(1, (int)Math.round(factor * imageHeight));
		int scaledWidth = Math.max(1, (int)Math.round(factor * imageWidth));

		return doScale(renderedImage, scaledHeight, scaledWidth);
	}

	@Override
	public void write(
			RenderedImage renderedImage, String contentType, OutputStream os)
		throws IOException {

		if (contentType.contains(ImageTool.TYPE_BMP)) {
			ImageIO.write(renderedImage, "bmp", os);
		}
		else if (contentType.contains(ImageTool.TYPE_GIF)) {
			ImageIO.write(renderedImage, "gif", os);
		}
		else if (contentType.contains(ImageTool.TYPE_JPEG) ||
				 contentType.contains("jpeg")) {

			ImageIO.write(renderedImage, "jpeg", os);
		}
		else if (contentType.contains(ImageTool.TYPE_PNG)) {
			ImageIO.write(renderedImage, ImageTool.TYPE_PNG, os);
		}
		else if (contentType.contains(ImageTool.TYPE_TIFF) ||
				 contentType.contains("tif")) {

			ImageIO.write(renderedImage, "tiff", os);
		}
	}

	protected RenderedImage doScale(
		RenderedImage renderedImage, int scaledHeight, int scaledWidth) {

			// See http://www.oracle.com/technetwork/java/index-137037.html

			BufferedImage originalBufferedImage = getBufferedImage(
				renderedImage);

			ColorModel originalColorModel =
				originalBufferedImage.getColorModel();

			Graphics2D originalGraphics2D =
				originalBufferedImage.createGraphics();

			if (originalColorModel.hasAlpha()) {
				originalGraphics2D.setComposite(AlphaComposite.Src);
			}

			GraphicsConfiguration originalGraphicsConfiguration =
				originalGraphics2D.getDeviceConfiguration();

			BufferedImage scaledBufferedImage =
				originalGraphicsConfiguration.createCompatibleImage(
					scaledWidth, scaledHeight,
					originalBufferedImage.getTransparency());

			Graphics scaledGraphics = scaledBufferedImage.getGraphics();

			scaledGraphics.drawImage(
				originalBufferedImage.getScaledInstance(
					scaledWidth, scaledHeight, java.awt.Image.SCALE_SMOOTH),
				0, 0, null);

			originalGraphics2D.dispose();

			return scaledBufferedImage;
		}

		protected byte[] toMultiByte(int intValue) {
			int numBits = 32;
			int mask = 0x80000000;

			while ((mask != 0) && ((intValue & mask) == 0)) {
				numBits--;
				mask >>>= 1;
			}

			int numBitsLeft = numBits;
			byte[] multiBytes = new byte[(numBitsLeft + 6) / 7];

			int maxIndex = multiBytes.length - 1;

			for (int b = 0; b <= maxIndex; b++) {
				multiBytes[b] =
					(byte)((intValue >>> ((maxIndex - b) * 7)) & 0x7f);

				if (b != maxIndex) {
					multiBytes[b] |= (byte)0x80;
				}
			}

			return multiBytes;
		}

		private ImageIOToolImpl() {
			ImageIO.setUseCache(PropsValues.IMAGE_IO_USE_DISK_CACHE);
		}

		private static final FileImpl _fileUtil = FileImpl.getInstance();

		private static final ImageIOTool _instance = new ImageIOToolImpl();

}