package com.liferay.wiki.exportimport.staged.model.repository;

import com.liferay.exportimport.kernel.lar.PortletDataContext;
import com.liferay.exportimport.staged.model.repository.StagedModelRepository;
import com.liferay.exportimport.staged.model.repository.base.BaseStagedModelRepository;
import com.liferay.portal.kernel.dao.orm.ExportActionableDynamicQuery;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.service.ServiceContext;
import com.liferay.portal.kernel.util.StringPool;
import com.liferay.wiki.model.WikiNode;
import com.liferay.wiki.service.WikiNodeLocalService;
import com.liferay.wiki.service.WikiPageLocalService;

import java.util.List;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * @author Zsolt Oláh
 */
@Component(
	immediate = true,
	property = {"model.class.name=com.liferay.wiki.model.WikiNode"},
	service = StagedModelRepository.class
)
public class WikiNodeStagedModelRepository extends BaseStagedModelRepository<WikiNode> {

	@Override
	public WikiNode addStagedModel(PortletDataContext portletDataContext,
			WikiNode node) throws PortalException {

		ServiceContext serviceContext = portletDataContext.createServiceContext(
			node);

		if (portletDataContext.isDataStrategyMirror()) {
			serviceContext.setUuid(node.getUuid());
		}
		else {
			String nodeName = getNodeName(
				portletDataContext, node, node.getName(), 2);
			node.setName(nodeName);
		}

		return _wikiNodeLocalService.addNode(
			portletDataContext.getUserId(node.getUserUuid()), node.getName(),
			node.getDescription(), serviceContext);
	}

	@Override
	public void deleteStagedModel(
			String uuid, long groupId, String className, String extraData)
		throws PortalException {

		WikiNode wikiNode = fetchStagedModelByUuidAndGroupId(uuid, groupId);

		if (wikiNode != null) {
			deleteStagedModel(wikiNode);
		}
	}

	@Override
	public void deleteStagedModel(WikiNode node) throws PortalException {
		_wikiNodeLocalService.deleteNode(node);
	}

	@Override
	public void deleteStagedModels(PortletDataContext portletDataContext)
			throws PortalException {

		_wikiNodeLocalService.deleteNodes(portletDataContext.getScopeGroupId());
	}

	@Override
	public WikiNode fetchStagedModelByUuidAndGroupId(
		String uuid, long groupId) {

		return _wikiNodeLocalService.fetchWikiNodeByUuidAndGroupId(
			uuid, groupId);
	}

	@Override
	public List<WikiNode> fetchStagedModelsByUuidAndCompanyId(
		String uuid, long companyId) {

		return _wikiNodeLocalService.getWikiNodesByUuidAndCompanyId(
			uuid, companyId);
	}

	@Override
	public ExportActionableDynamicQuery getExportActionableDynamicQuery(
			PortletDataContext portletDataContext) {

		return _wikiNodeLocalService.getExportActionableDynamicQuery(
				portletDataContext);
	}

	@Override
	public WikiNode saveStagedModel(WikiNode node)
			throws PortalException {

		return _wikiNodeLocalService.updateWikiNode(node);
	}

	@Override
	public WikiNode updateStagedModel(PortletDataContext portletDataContext,
			WikiNode node) throws PortalException {

		ServiceContext serviceContext = portletDataContext.createServiceContext(
				node);

		return _wikiNodeLocalService.updateNode(
				node.getNodeId(), node.getName(), node.getDescription(),
				serviceContext);
	}

	protected String getNodeName(
			PortletDataContext portletDataContext, WikiNode node, String name,
			int count) {

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

	@Reference(unbind = "-")
	protected void setBlogsEntryLocalService(
		WikiNodeLocalService wikiNodeLocalService) {

		_wikiNodeLocalService = wikiNodeLocalService;
	}

	private WikiNodeLocalService _wikiNodeLocalService;
}