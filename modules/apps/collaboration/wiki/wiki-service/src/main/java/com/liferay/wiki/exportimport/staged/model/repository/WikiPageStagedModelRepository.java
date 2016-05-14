package com.liferay.wiki.exportimport.staged.model.repository;

import com.liferay.exportimport.kernel.lar.PortletDataContext;
import com.liferay.exportimport.kernel.lar.StagedModelModifiedDateComparator;
import com.liferay.exportimport.staged.model.repository.StagedModelRepository;
import com.liferay.exportimport.staged.model.repository.base.BaseStagedModelRepository;
import com.liferay.portal.kernel.dao.orm.ExportActionableDynamicQuery;
import com.liferay.portal.kernel.dao.orm.QueryUtil;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.service.ServiceContext;
import com.liferay.portal.kernel.trash.TrashHandler;
import com.liferay.portal.kernel.util.MapUtil;
import com.liferay.portal.kernel.workflow.WorkflowConstants;
import com.liferay.wiki.model.WikiNode;
import com.liferay.wiki.model.WikiPage;
import com.liferay.wiki.model.WikiPageResource;
import com.liferay.wiki.service.WikiPageLocalService;
import com.liferay.wiki.service.WikiPageResourceLocalService;

import java.util.List;
import java.util.Map;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * @author Zsolt Oláh
 */
@Component(
	immediate = true,
	property = {"model.class.name=com.liferay.wiki.model.WikiPage"},
	service = StagedModelRepository.class
)
public class WikiPageStagedModelRepository extends BaseStagedModelRepository<WikiPage> {

	@Override
	public WikiPage addStagedModel(
			PortletDataContext portletDataContext, WikiPage page)
		throws PortalException {

		long userId = portletDataContext.getUserId(page.getUserUuid());

		Map<Long, Long> nodeIds =
				(Map<Long, Long>)portletDataContext.getNewPrimaryKeysMap(
					WikiNode.class);

		long nodeId = MapUtil.getLong(
				nodeIds, page.getNodeId(), page.getNodeId());

		ServiceContext serviceContext = portletDataContext.createServiceContext(
			page);

		if (portletDataContext.isDataStrategyMirror()) {
			serviceContext.setUuid(page.getUuid());
		}

		return _wikiPageLocalService.addPage(
			userId, nodeId, page.getTitle(), page.getVersion(),
			page.getContent(), page.getSummary(), page.isMinorEdit(),
			page.getFormat(), page.getHead(), page.getParentTitle(),
			page.getRedirectTitle(), serviceContext);

	}

	@Override
	public void deleteStagedModel(WikiPage page) throws PortalException {
		_wikiPageLocalService.deletePage(page);
	}

	@Override
	public void deleteStagedModel(
			String uuid, long groupId, String className, String extraData)
		throws PortalException {

		WikiPageResource pageResource =
			_wikiPageResourceLocalService.fetchWikiPageResourceByUuidAndGroupId(
				uuid, groupId);

		if (pageResource == null) {
			return;
		}

		WikiPage latestPage = _wikiPageLocalService.getLatestPage(
			pageResource.getResourcePrimKey(), WorkflowConstants.STATUS_ANY,
			true);

		deleteStagedModel(latestPage);
	}

	@Override
	public void deleteStagedModels(PortletDataContext portletDataContext)
			throws PortalException {

		_wikiPageLocalService.deletePages(portletDataContext.getScopeGroupId());

	}

	@Override
	public WikiPage fetchStagedModelByUuidAndGroupId(
		String uuid, long groupId) {

		return _wikiPageLocalService.fetchWikiPageByUuidAndGroupId(
			uuid, groupId);
	}

	@Override
	public List<WikiPage> fetchStagedModelsByUuidAndCompanyId(
		String uuid, long companyId) {

		return _wikiPageLocalService.getWikiPagesByUuidAndCompanyId(
			uuid, companyId, QueryUtil.ALL_POS, QueryUtil.ALL_POS,
			new StagedModelModifiedDateComparator<WikiPage>());
	}

	@Override
	public ExportActionableDynamicQuery getExportActionableDynamicQuery(
			PortletDataContext portletDataContext) {

		return _wikiPageLocalService.getExportActionableDynamicQuery(
				portletDataContext);
	}

	@Override
	public WikiPage saveStagedModel(WikiPage page)
			throws PortalException {

		return _wikiPageLocalService.updateWikiPage(page);
	}

	@Override
	public WikiPage updateStagedModel(PortletDataContext portletDataContext,
			WikiPage page) throws PortalException {

		long userId = portletDataContext.getUserId(page.getUserUuid());

		Map<Long, Long> nodeIds =
			(Map<Long, Long>)portletDataContext.getNewPrimaryKeysMap(
				WikiNode.class);

		long nodeId = MapUtil.getLong(
				nodeIds, page.getNodeId(), page.getNodeId());

		ServiceContext serviceContext = portletDataContext.createServiceContext(
				page);

		return _wikiPageLocalService.updatePage(
			userId, nodeId, page.getTitle(), 0.0, page.getContent(),
			page.getSummary(), page.isMinorEdit(), page.getFormat(),
			page.getParentTitle(), page.getRedirectTitle(),
			serviceContext);
	}

	protected void doRestoreStagedModel(
			PortletDataContext portletDataContext, WikiPage page)
		throws Exception {

		long userId = portletDataContext.getUserId(page.getUserUuid());

		WikiPage existingPage = fetchStagedModelByUuidAndGroupId(
			page.getUuid(), portletDataContext.getScopeGroupId());

		if ((existingPage == null) || !existingPage.isInTrash()) {
			return;
		}

		TrashHandler trashHandler = existingPage.getTrashHandler();

		if (trashHandler.isRestorable(existingPage.getResourcePrimKey())) {
			trashHandler.restoreTrashEntry(
				userId, existingPage.getResourcePrimKey());
		}
	}

	@Reference(unbind = "-")
	protected void setWikiPageResourceLocalService(
		WikiPageResourceLocalService wikiPageResourceLocalService) {

		_wikiPageResourceLocalService = wikiPageResourceLocalService;
	}

	@Reference(unbind = "-")
	protected void setWikiPageLocalService(
		WikiPageLocalService wikiPageLocalService) {

		_wikiPageLocalService = wikiPageLocalService;
	}

	private WikiPageLocalService _wikiPageLocalService;
	private WikiPageResourceLocalService _wikiPageResourceLocalService;
}