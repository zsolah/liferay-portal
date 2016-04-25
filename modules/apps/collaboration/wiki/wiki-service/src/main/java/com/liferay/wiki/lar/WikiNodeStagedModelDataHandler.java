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

import com.liferay.exportimport.kernel.lar.ExportImportPathUtil;
import com.liferay.exportimport.kernel.lar.PortletDataContext;
import com.liferay.exportimport.kernel.lar.StagedModelDataHandler;
import com.liferay.exportimport.lar.BaseStagedModelDataHandler;
import com.liferay.exportimport.staged.model.repository.StagedModelRepository;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.service.ServiceContext;
import com.liferay.portal.kernel.trash.TrashHandler;
import com.liferay.portal.kernel.util.StringPool;
import com.liferay.portal.kernel.xml.Element;
import com.liferay.wiki.configuration.WikiGroupServiceConfiguration;
import com.liferay.wiki.model.WikiNode;
import com.liferay.wiki.model.WikiPage;
import com.liferay.wiki.service.WikiNodeLocalService;
import com.liferay.wiki.service.util.WikiServiceComponentProvider;

import java.util.List;
import java.util.Map;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * @author Zsolt Berentey
 */
@Component(immediate = true, service = StagedModelDataHandler.class)
public class WikiNodeStagedModelDataHandler
	extends BaseStagedModelDataHandler<WikiNode> {

	public static final String[] CLASS_NAMES = {WikiNode.class.getName()};

	@Override
	public String[] getClassNames() {
		return CLASS_NAMES;
	}

	@Override
	protected void doExportStagedModel(
			PortletDataContext portletDataContext, WikiNode node)
		throws Exception {

		Element nodeElement = portletDataContext.getExportDataElement(node);

		portletDataContext.addClassedModel(
			nodeElement, ExportImportPathUtil.getModelPath(node), node);
	}

	@Override
	protected void doImportMissingReference(
			PortletDataContext portletDataContext, String uuid, long groupId,
			long nodeId)
		throws Exception {

		WikiNode existingNode = fetchMissingReference(uuid, groupId);

		if (existingNode == null) {
			return;
		}

		Map<Long, Long> nodeIds =
			(Map<Long, Long>)portletDataContext.getNewPrimaryKeysMap(
				WikiNode.class);

		nodeIds.put(nodeId, existingNode.getNodeId());
	}

	@Override
	protected void doImportStagedModel(
			PortletDataContext portletDataContext, WikiNode node)
		throws Exception {

		ServiceContext serviceContext = portletDataContext.createServiceContext(
			node);

		WikiNode importedNode = (WikiNode)node.clone();

		WikiServiceComponentProvider wikiServiceComponentProvider =
			WikiServiceComponentProvider.getWikiServiceComponentProvider();

		WikiGroupServiceConfiguration wikiGroupServiceConfiguration =
			wikiServiceComponentProvider.getWikiGroupServiceConfiguration();

		WikiNode existingNode = _stagedModelRepository.fetchStagedModelByUuidAndGroupId(
			node.getUuid(), portletDataContext.getScopeGroupId());

		if (portletDataContext.isDataStrategyMirror()) {
			if (existingNode == null) {
				serviceContext.setUuid(node.getUuid());

				importedNode = _stagedModelRepository.addStagedModel(
					portletDataContext, importedNode);
			}
			else {
				importedNode.setNodeId(existingNode.getNodeId());
				importedNode = _stagedModelRepository.updateStagedModel(
					portletDataContext, importedNode);
			}
		}
		else {
			String initialNodeName =
				wikiGroupServiceConfiguration.initialNodeName();

			if ((existingNode != null) &&
				initialNodeName.equals(existingNode.getName())) {

				importedNode.setNodeId(existingNode.getNodeId());
				importedNode = _stagedModelRepository.updateStagedModel(
					portletDataContext, importedNode);
			}
			else {
				String nodeName = getNodeName(
					portletDataContext, node, node.getName(), 2);

				importedNode.setName(nodeName);
				importedNode = _stagedModelRepository.addStagedModel(
					portletDataContext, importedNode);
			}
		}

		portletDataContext.importClassedModel(node, importedNode);
	}

	@Override
	protected void doRestoreStagedModel(
			PortletDataContext portletDataContext, WikiNode node)
		throws Exception {

		long userId = portletDataContext.getUserId(node.getUserUuid());

		WikiNode existingNode = _stagedModelRepository.fetchStagedModelByUuidAndGroupId(
			node.getUuid(), portletDataContext.getScopeGroupId());

		if ((existingNode == null) || !existingNode.isInTrash()) {
			return;
		}

		TrashHandler trashHandler = existingNode.getTrashHandler();

		if (trashHandler.isRestorable(existingNode.getNodeId())) {
			trashHandler.restoreTrashEntry(userId, existingNode.getNodeId());
		}
	}

	protected String getNodeName(
			PortletDataContext portletDataContext, WikiNode node, String name,
			int count)
		throws Exception {

		WikiNode existingNode = _wikiNodeLocalService.fetchNode(
			portletDataContext.getScopeGroupId(), name);

		if (existingNode == null) {
			return name;
		}

		String nodeName = node.getName();

		return getNodeName(
			portletDataContext, node,
			nodeName.concat(StringPool.SPACE).concat(String.valueOf(count)),
			++count);
	}

	protected StagedModelRepository<WikiNode> getStagedModelRepository() {
		return _stagedModelRepository;
	}

	@Reference(
		target =
			"(model.class.name=com.liferay.wiki.model.WikiNode)",
		unbind = "-"
	)
	protected void setStagedModelRepository(
		StagedModelRepository<WikiNode> stagedModelRepository) {

		_stagedModelRepository = stagedModelRepository;
	}

	@Reference(unbind = "-")
	protected void setWikiNodeLocalService(
		WikiNodeLocalService wikiNodeLocalService) {

		_wikiNodeLocalService = wikiNodeLocalService;
	}

	private StagedModelRepository<WikiNode> _stagedModelRepository;
	private WikiNodeLocalService _wikiNodeLocalService;

}