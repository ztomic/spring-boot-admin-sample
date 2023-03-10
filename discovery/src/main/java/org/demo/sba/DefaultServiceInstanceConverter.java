/*
 * Copyright 2014-2019 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.demo.sba;

import java.net.URI;
import java.util.Map;

import de.codecentric.boot.admin.server.domain.entities.Instance;
import de.codecentric.boot.admin.server.domain.values.Registration;

import org.springframework.cloud.client.ServiceInstance;
import org.springframework.util.StringUtils;
import org.springframework.web.util.UriComponentsBuilder;

import static java.util.Collections.emptyMap;

/**
 * Converts any {@link ServiceInstance}s to {@link Instance}s. To customize the health- or
 * management-url for all instances you can set healthEndpointPath or
 * managementContextPath respectively. If you want to influence the url per service you
 * can add <code>management.context-path</code>, <code>management.port</code>,
 * <code>management.address</code> or <code>health.path</code> to the instances metadata.
 *
 * @author Johannes Edmeier
 */
public class DefaultServiceInstanceConverter {

	private static final String KEY_MANAGEMENT_PORT = "management.port";

	private static final String KEY_MANAGEMENT_PATH = "management.context-path";

	private static final String KEY_MANAGEMENT_ADDRESS = "management.address";

	private static final String KEY_HEALTH_PATH = "health.path";

	/**
	 * Default context-path to be appended to the url of the discovered service for the
	 * managment-url.
	 */
	private String managementContextPath = "/actuator";

	/**
	 * Default path of the health-endpoint to be used for the health-url of the discovered
	 * service.
	 */
	private String healthEndpointPath = "health";

	public Registration convert(ServiceInstance instance) {
		return Registration.create(instance.getServiceId(), getHealthUrl(instance).toString())
				.managementUrl(getManagementUrl(instance).toString()).serviceUrl(getServiceUrl(instance).toString())
				.metadata(getMetadata(instance)).build();
	}

	protected URI getHealthUrl(ServiceInstance instance) {
		return UriComponentsBuilder.fromUri(getManagementUrl(instance)).path("/").path(getHealthPath(instance)).build()
				.toUri();
	}

	protected String getHealthPath(ServiceInstance instance) {
		String healthPath = instance.getMetadata().get(KEY_HEALTH_PATH);
		if (StringUtils.hasText(healthPath)) {
			return healthPath;
		}
		return this.healthEndpointPath;
	}

	protected URI getManagementUrl(ServiceInstance instance) {
		return UriComponentsBuilder.newInstance().scheme(getManagementScheme(instance))
				.host(getManagementHost(instance)).port(getManagementPort(instance)).path("/")
				.path(getManagementPath(instance)).build().toUri();
	}

	private String getManagementScheme(ServiceInstance instance) {
		return this.getServiceUrl(instance).getScheme();
	}

	protected String getManagementHost(ServiceInstance instance) {
		String managementServerHost = instance.getMetadata().get(KEY_MANAGEMENT_ADDRESS);
		if (StringUtils.hasText(managementServerHost)) {
			return managementServerHost;
		}
		return getServiceUrl(instance).getHost();
	}

	protected String getManagementPort(ServiceInstance instance) {
		String managementPort = instance.getMetadata().get(KEY_MANAGEMENT_PORT);
		if (StringUtils.hasText(managementPort)) {
			return managementPort;
		}
		return String.valueOf(getServiceUrl(instance).getPort());
	}

	protected String getManagementPath(ServiceInstance instance) {
		String managementPath = instance.getMetadata().get(DefaultServiceInstanceConverter.KEY_MANAGEMENT_PATH);
		if (StringUtils.hasText(managementPath)) {
			return managementPath;
		}
		return this.managementContextPath;
	}

	protected URI getServiceUrl(ServiceInstance instance) {
		return UriComponentsBuilder.fromUri(instance.getUri()).path("/").build().toUri();
	}

	protected Map<String, String> getMetadata(ServiceInstance instance) {
		return (instance.getMetadata() != null) ? instance.getMetadata() : emptyMap();
	}

}