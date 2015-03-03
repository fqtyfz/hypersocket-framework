package com.hypersocket.browser.json;

import java.util.HashMap;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import com.hypersocket.auth.json.AuthenticationRequired;
import com.hypersocket.auth.json.UnauthorizedException;
import com.hypersocket.browser.BrowserLaunchable;
import com.hypersocket.browser.BrowserLaunchableColumns;
import com.hypersocket.browser.BrowserLaunchableService;
import com.hypersocket.json.ResourceList;
import com.hypersocket.permissions.AccessDeniedException;
import com.hypersocket.session.json.SessionTimeoutException;
import com.hypersocket.session.json.SessionUtils;
import com.hypersocket.tables.Column;
import com.hypersocket.tables.ColumnSort;
import com.hypersocket.tables.DataTablesResult;
import com.hypersocket.tables.json.DataTablesController;
import com.hypersocket.tables.json.DataTablesPageProcessor;

@Controller
public class BrowserLaunchableController extends DataTablesController {

	@Autowired
	BrowserLaunchableService resourceService;

	@Autowired
	SessionUtils sessionUtils;

	@AuthenticationRequired
	@RequestMapping(value = "browser/myResources", method = RequestMethod.GET, produces = { "application/json" })
	@ResponseBody
	@ResponseStatus(value = HttpStatus.OK)
	public ResourceList<BrowserLaunchable> myBrowserResources(
			final HttpServletRequest request, HttpServletResponse response)
			throws AccessDeniedException, UnauthorizedException,
			SessionTimeoutException {

		setupAuthenticatedContext(sessionUtils.getSession(request),
				sessionUtils.getLocale(request));

		try {
			ResourceList<BrowserLaunchable> list = new ResourceList<BrowserLaunchable>(
					new HashMap<String,String>(),
					resourceService.getPersonalResources(sessionUtils
							.getPrincipal(request)));
			list.getProperties().put(
					"authCode",
					sessionService.createSessionToken(
							sessionUtils.getSession(request)).getShortCode());
			return list;
		} finally {
			clearAuthenticatedContext();
		}
	}

	@AuthenticationRequired
	@RequestMapping(value = "browser/personal", method = RequestMethod.GET, produces = { "application/json" })
	@ResponseBody
	@ResponseStatus(value = HttpStatus.OK)
	public DataTablesResult tableNetworkResources(
			final HttpServletRequest request, HttpServletResponse response)
			throws AccessDeniedException, UnauthorizedException,
			SessionTimeoutException {

		setupAuthenticatedContext(sessionUtils.getSession(request),
				sessionUtils.getLocale(request));

		try {
			return processDataTablesRequest(request,
					new DataTablesPageProcessor() {

						@Override
						public Column getColumn(int col) {
							return BrowserLaunchableColumns.values()[col];
						}

						@Override
						public List<?> getPage(String searchPattern, int start,
								int length, ColumnSort[] sorting)
								throws UnauthorizedException,
								AccessDeniedException {
							return resourceService.searchPersonalResources(
									sessionUtils.getPrincipal(request),
									searchPattern, start, length, sorting);
						}

						@Override
						public Long getTotalCount(String searchPattern)
								throws UnauthorizedException,
								AccessDeniedException {
							return resourceService.getPersonalResourceCount(
									sessionUtils.getPrincipal(request),
									searchPattern);
						}
					});
		} finally {
			clearAuthenticatedContext();
		}
	}
}