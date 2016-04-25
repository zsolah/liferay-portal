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

package com.liferay.wiki.lar;

import com.liferay.document.library.kernel.exception.NoSuchFileException;
import com.liferay.document.library.kernel.model.DLFileEntry;
import com.liferay.exportimport.kernel.lar.ExportImportPathUtil;
import com.liferay.exportimport.kernel.lar.PortletDataContext;
import com.liferay.exportimport.kernel.lar.StagedModelDataHandler;
import com.liferay.exportimport.kernel.lar.StagedModelDataHandlerUtil;
import com.liferay.exportimport.lar.BaseStagedModelDataHandler;
import com.liferay.exportimport.staged.model.repository.StagedModelRepository;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.repository.model.FileEntry;
import com.liferay.portal.kernel.service.ServiceContext;
import com.liferay.portal.kernel.util.GetterUtil;
import com.liferay.portal.kernel.util.MapUtil;
import com.liferay.portal.kernel.util.StreamUtil;
import com.liferay.portal.kernel.util.Validator;
import com.liferay.portal.kernel.xml.Element;
import com.liferay.portlet.documentlibrary.lar.FileEntryUtil;
import com.liferay.wiki.exportimport.content.processor.WikiPageExportImportContentProcessor;
import com.liferay.wiki.model.WikiNode;
import com.liferay.wiki.model.WikiPage;
import com.liferay.wiki.model.WikiPageResource;
import com.liferay.wiki.service.WikiPageLocalService;
import com.liferay.wiki.service.WikiPageResourceLocalService;

import java.io.InputStream;
import java.util.List;
import java.util.Map;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * @author Zsolt Berentey
 * @author Akos Thurzo
 */
@Component(immediate = true, service = StagedModelDataHandler.class)
public class WikiPageStagedModelDataHandler
	extends BaseStagedModelDataHandler<WikiPage> {

	public static final String[] CLASS_NAMES = {WikiPage.class.getName()};

	@Override
	public String[] getClassNames() {
		return CLASS_NAMES;
	}

	@Override
	protected void doExportStagedModel(
			PortletDataContext portletDataContext, WikiPage page)
		throws Exception {

		Element pageElement = portletDataContext.getExportDataElement(page);

		StagedModelDataHandlerUtil.exportReferenceStagedModel(
			portletDataContext, page, page.getNode(),
			PortletDataContext.REFERENCE_TYPE_PARENT);

		String content =
			_wikiPageExportImportContentProcessor.
				replaceExportContentReferences(
					portletDataContext, page, page.getContent(),
					portletDataContext.getBooleanParameter(
						"wiki", "referenced-content"),
					true);

		page.setContent(content);

		if (page.isHead()) {
			for (FileEntry fileEntry : page.getAttachmentsFileEntries()) {
				StagedModelDataHandlerUtil.exportReferenceStagedModel(
					portletDataContext, page, fileEntry,
					PortletDataContext.REFERENCE_TYPE_WEAK);
			}
		}

		WikiPageResource pageResource =
			_wikiPageResourceLocalService.getPageResource(
				page.getResourcePrimKey());

		pageElement.addAttribute("page-resource-uuid", pageResource.getUuid());

		portletDataContext.addClassedModel(
			pageElement, ExportImportPathUtil.getModelPath(page), page);
	}

	@Override
	protected void doImportMissingReference(
			PortletDataContext portletDataContext, String uuid, long groupId,
			long pageId)
		throws Exception {

		WikiPage existingPage = fetchMissingReference(uuid, groupId);

		if (existingPage == null) {
			return;
		}

		Map<Long, Long> pageIds =
			(Map<Long, Long>)portletDataContext.getNewPrimaryKeysMap(
				WikiPage.class);

		pageIds.put(pageId, existingPage.getPageId());
	}

	@Override
	protected void doImportStagedModel(
			PortletDataContext portletDataContext, WikiPage page)
		throws Exception {

		long userId = portletDataContext.getUserId(page.getUserUuid());

		Element pageElement =
			portletDataContext.getImportDataStagedModelElement(page);

		String content =
			_wikiPageExportImportContentProcessor.
				replaceImportContentReferences(
					portletDataContext, page, page.getContent());

		page.setContent(content);

		Map<Long, Long> nodeIds =
			(Map<Long, Long>)portletDataContext.getNewPrimaryKeysMap(
				WikiNode.class);

		long nodeId = MapUtil.getLong(
			nodeIds, page.getNodeId(), page.getNodeId());

		WikiPage importedPage = (WikiPage)page.clone();

		WikiPage existingPage =
			_stagedModelRepository.fetchStagedModelByUuidAndGroupId(
				page.getUuid(), portletDataContext.getScopeGroupId());

		if (existingPage == null) {
			importedPage = _stagedModelRepository.addStagedModel(
				portletDataContext, page);

			WikiPageResource pageResource =
				_wikiPageResourceLocalService.getPageResource(
					importedPage.getResourcePrimKey());

			String pageResourceUuid = GetterUtil.getString(
				pageElement.attributeValue("page-resource-uuid"));

			if (Validator.isNotNull(pageResourceUuid)) {
				pageResource.setUuid(
					pageElement.attributeValue("page-resource-uuid"));

				_wikiPageResourceLocalService.updateWikiPageResource(
					pageResource);
			}
		}
		else {
			existingPage = fetchStagedModelByUuidAndGroupId(
				page.getUuid(), portletDataContext.getScopeGroupId());

			if (existingPage == null) {
				existingPage = _wikiPageLocalService.fetchPage(
					nodeId, page.getTitle(), page.getVersion());
			}

			if (existingPage == null) {
				importedPage = _stagedModelRepository.updateStagedModel(
					portletDataContext, page);
			}
			else {
				importedPage = existingPage;
			}
		}

		if (page.isHead()) {
			List<Element> attachmentElements =
				portletDataContext.getReferenceDataElements(
					pageElement, DLFileEntry.class,
					PortletDataContext.REFERENCE_TYPE_WEAK);

			for (Element attachmentElement : attachmentElements) {
				String path = attachmentElement.attributeValue("path");

				FileEntry fileEntry =
					(FileEntry)portletDataContext.getZipEntryAsObject(path);

				InputStream inputStream = null;

				try {
					String binPath = attachmentElement.attributeValue(
						"bin-path");

					if (Validator.isNull(binPath) &&
						portletDataContext.isPerformDirectBinaryImport()) {

						try {
							inputStream = FileEntryUtil.getContentStream(
								fileEntry);
						}
						catch (NoSuchFileException nsfe) {
						}
					}
					else {
						inputStream =
							portletDataContext.getZipEntryAsInputStream(
								binPath);
					}

					if (inputStream == null) {
						if (_log.isWarnEnabled()) {
							_log.warn(
								"Unable to import attachment for file entry " +
									fileEntry.getFileEntryId());
						}

						continue;
					}

					_wikiPageLocalService.addPageAttachment(
						userId, importedPage.getNodeId(),
						importedPage.getTitle(), fileEntry.getTitle(),
						inputStream, null);
				}
				finally {
					StreamUtil.cleanUp(inputStream);
				}
			}
		}

		portletDataContext.importClassedModel(page, importedPage);

		Map<Long, Long> pageIds =
			(Map<Long, Long>)portletDataContext.getNewPrimaryKeysMap(
				WikiPage.class + ".pageId");

		pageIds.put(page.getPageId(), importedPage.getPageId());
	}

	@Reference(unbind = "-")
	protected void setWikiPageExportImportContentProcessor(
		WikiPageExportImportContentProcessor
			wikiPageExportImportContentProcessor) {

		_wikiPageExportImportContentProcessor =
			wikiPageExportImportContentProcessor;
	}

	@Reference(unbind = "-")
	protected void setWikiPageLocalService(
		WikiPageLocalService wikiPageLocalService) {

		_wikiPageLocalService = wikiPageLocalService;
	}

	@Reference(unbind = "-")
	protected void setWikiPageResourceLocalService(
		WikiPageResourceLocalService wikiPageResourceLocalService) {

		_wikiPageResourceLocalService = wikiPageResourceLocalService;
	}

	protected StagedModelRepository<WikiPage> getStagedModelRepository() {
		return _stagedModelRepository;
	}

	@Reference(
		target =
			"(model.class.name=com.liferay.wiki.model.WikiPage)",
		unbind = "-"
	)
	protected void setStagedModelRepository(
		StagedModelRepository<WikiPage> stagedModelRepository) {

		_stagedModelRepository = stagedModelRepository;
	}

	private static final Log _log = LogFactoryUtil.getLog(
		WikiPageStagedModelDataHandler.class);

	private WikiPageExportImportContentProcessor
		_wikiPageExportImportContentProcessor;
	private WikiPageLocalService _wikiPageLocalService;
	private WikiPageResourceLocalService _wikiPageResourceLocalService;
	private StagedModelRepository<WikiPage> _stagedModelRepository;

}