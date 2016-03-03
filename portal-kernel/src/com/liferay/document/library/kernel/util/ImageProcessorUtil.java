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

package com.liferay.document.library.kernel.util;

import com.liferay.portal.kernel.repository.model.FileEntry;
import com.liferay.portal.kernel.repository.model.FileVersion;
import com.liferay.portal.kernel.util.ProxyFactory;

import java.io.InputStream;
import java.util.Set;

/**
 * @author Sergio González
 */
public class ImageProcessorUtil {

	public static void cleanUp(FileEntry fileEntry) {

		_imageProcessor.cleanUp(fileEntry);
	}

	public static void cleanUp(FileVersion fileVersion) {

		_imageProcessor.cleanUp(fileVersion);
	}

	public static void generateImages(
			FileVersion sourceFileVersion, FileVersion destinationFileVersion)
		throws Exception {

		_imageProcessor.generateImages(
			sourceFileVersion, destinationFileVersion);
	}

	public static Set<String> getImageMimeTypes() {

		return _imageProcessor.getImageMimeTypes();
	}

	public static InputStream getPreviewAsStream(FileVersion fileVersion)
		throws Exception {

		return _imageProcessor.getPreviewAsStream(fileVersion);
	}

	public static long getPreviewFileSize(FileVersion fileVersion)
		throws Exception {

		return _imageProcessor.getPreviewFileSize(fileVersion);
	}

	public static String getPreviewType(FileVersion fileVersion) {

		return _imageProcessor.getPreviewType(fileVersion);
	}

	public static InputStream getThumbnailAsStream(
			FileVersion fileVersion, int index)
		throws Exception {

		return _imageProcessor.getThumbnailAsStream(fileVersion, index);
	}

	public static long getThumbnailFileSize(FileVersion fileVersion, int index)
		throws Exception {

		return _imageProcessor.getThumbnailFileSize(fileVersion, index);
	}

	public static String getThumbnailType(FileVersion fileVersion) {

		return _imageProcessor.getThumbnailType(fileVersion);
	}

	public static boolean hasImages(FileVersion fileVersion) {

		return _imageProcessor.hasImages(fileVersion);
	}

	public static boolean isImageSupported(FileVersion fileVersion) {

		return _imageProcessor.isImageSupported(fileVersion);
	}

	public static boolean isImageSupported(String mimeType) {

		return _imageProcessor.isImageSupported(mimeType);
	}

	public static boolean isSupported(String mimeType) {

		return _imageProcessor.isSupported(mimeType);
	}

	public static void storeThumbnail(
			long companyId, long groupId, long fileEntryId, long fileVersionId,
			long custom1ImageId, long custom2ImageId, InputStream is,
			String type)
		throws Exception {

		_imageProcessor.storeThumbnail(
			companyId, groupId, fileEntryId, fileVersionId, custom1ImageId,
			custom2ImageId, is, type);
	}

	public static void trigger(
		FileVersion sourceFileVersion, FileVersion destinationFileVersion) {

		_imageProcessor.trigger(sourceFileVersion, destinationFileVersion);
	}

	/**
	 * @deprecated As of 6.2.0
	 */
	@Deprecated
	public void setImageProcessor(ImageProcessor imageProcessor) {
	}

	private static final ImageProcessor _imageProcessor =
		ProxyFactory.newServiceTrackedInstance(ImageProcessor.class);
}