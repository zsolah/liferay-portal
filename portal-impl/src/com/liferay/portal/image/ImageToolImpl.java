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
import com.liferay.portal.kernel.image.ImageTool;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.security.pacl.DoPrivileged;
import com.liferay.portal.kernel.util.ArrayUtil;
import com.liferay.portal.kernel.util.PropsKeys;
import com.liferay.portal.kernel.util.StringUtil;
import com.liferay.portal.model.Image;
import com.liferay.portal.model.impl.ImageImpl;
import com.liferay.portal.util.FileImpl;
import com.liferay.portal.util.PropsUtil;
import com.liferay.portal.util.PropsValues;

import java.awt.image.RenderedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
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
@DoPrivileged
public class ImageToolImpl implements ImageTool {

	public static ImageTool getInstance() {
		return _instance;
	}

	public void afterPropertiesSet() {
		ClassLoader classLoader = getClass().getClassLoader();

		try {
			InputStream is = classLoader.getResourceAsStream(
				PropsUtil.get(PropsKeys.IMAGE_DEFAULT_SPACER));

			if (is == null) {
				_log.error("Default spacer is not available");
			}

			_defaultSpacer = getImage(is);
		}
		catch (Exception e) {
			_log.error(
				"Unable to configure the default spacer: " + e.getMessage());
		}

		try {
			InputStream is = classLoader.getResourceAsStream(
				PropsUtil.get(PropsKeys.IMAGE_DEFAULT_COMPANY_LOGO));

			if (is == null) {
				_log.error("Default company logo is not available");
			}

			_defaultCompanyLogo = getImage(is);
		}
		catch (Exception e) {
			_log.error(
				"Unable to configure the default company logo: " +
					e.getMessage());
		}

		try {
			InputStream is = classLoader.getResourceAsStream(
				PropsUtil.get(PropsKeys.IMAGE_DEFAULT_ORGANIZATION_LOGO));

			if (is == null) {
				_log.error("Default organization logo is not available");
			}

			_defaultOrganizationLogo = getImage(is);
		}
		catch (Exception e) {
			_log.error(
				"Unable to configure the default organization logo: " +
					e.getMessage());
		}

		try {
			InputStream is = classLoader.getResourceAsStream(
				PropsUtil.get(PropsKeys.IMAGE_DEFAULT_USER_FEMALE_PORTRAIT));

			if (is == null) {
				_log.error("Default user female portrait is not available");
			}

			_defaultUserFemalePortrait = getImage(is);
		}
		catch (Exception e) {
			_log.error(
				"Unable to configure the default user female portrait: " +
					e.getMessage());
		}

		try {
			InputStream is = classLoader.getResourceAsStream(
				PropsUtil.get(PropsKeys.IMAGE_DEFAULT_USER_MALE_PORTRAIT));

			if (is == null) {
				_log.error("Default user male portrait is not available");
			}

			_defaultUserMalePortrait = getImage(is);
		}
		catch (Exception e) {
			_log.error(
				"Unable to configure the default user male portrait: " +
					e.getMessage());
		}
	}

	@Override
	public Image getDefaultCompanyLogo() {
		return _defaultCompanyLogo;
	}

	@Override
	public Image getDefaultOrganizationLogo() {
		return _defaultOrganizationLogo;
	}

	@Override
	public Image getDefaultSpacer() {
		return _defaultSpacer;
	}

	@Override
	public Image getDefaultUserFemalePortrait() {
		return _defaultUserFemalePortrait;
	}

	@Override
	public Image getDefaultUserMalePortrait() {
		return _defaultUserMalePortrait;
	}

	@Override
	public boolean isNullOrDefaultSpacer(byte[] bytes) {
		if (ArrayUtil.isEmpty(bytes) ||
			Arrays.equals(bytes, getDefaultSpacer().getTextObj())) {

			return true;
		}
		else {
			return false;
		}
	}

	private ImageBag read(byte[] bytes) throws IOException {
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

		String type = TYPE_JPEG;

		if (formatName.contains(TYPE_BMP)) {
			type = TYPE_BMP;
		}
		else if (formatName.contains(TYPE_GIF)) {
			type = TYPE_GIF;
		}
		else if (formatName.contains("jpeg") || type.equals("jpeg")) {
			type = TYPE_JPEG;
		}
		else if (formatName.contains(TYPE_PNG)) {
			type = TYPE_PNG;
		}
		else if (formatName.contains(TYPE_TIFF)) {
			type = TYPE_TIFF;
		}
		else {
			throw new IllegalArgumentException(type + " is not supported");
		}

		return new ImageBag(renderedImage, type);
	}

	private ImageToolImpl() {
		ImageIO.setUseCache(PropsValues.IMAGE_IO_USE_DISK_CACHE);
	}

	public Image getImage(InputStream is) throws IOException {
		byte[] bytes = _fileUtil.getBytes(is, -1, true);

		return getImage(bytes);
	}

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

	private static final Log _log = LogFactoryUtil.getLog(ImageToolImpl.class);

	private static final ImageTool _instance = new ImageToolImpl();

	private static final FileImpl _fileUtil = FileImpl.getInstance();

	private Image _defaultCompanyLogo;
	private Image _defaultOrganizationLogo;
	private Image _defaultSpacer;
	private Image _defaultUserFemalePortrait;
	private Image _defaultUserMalePortrait;

}