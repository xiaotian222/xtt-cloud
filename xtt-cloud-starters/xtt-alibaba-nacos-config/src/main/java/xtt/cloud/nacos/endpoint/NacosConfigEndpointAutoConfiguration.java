/*
 * Copyright 2013-2023 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package xtt.cloud.nacos.endpoint;

import xtt.cloud.nacos.refresh.NacosRefreshHistory;
import xtt.cloud.nacos.NacosConfigManager;
import xtt.cloud.nacos.NacosConfigEnabledCondition;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.autoconfigure.endpoint.condition.ConditionalOnAvailableEndpoint;
import org.springframework.boot.actuate.autoconfigure.health.ConditionalOnEnabledHealthIndicator;
import org.springframework.boot.actuate.endpoint.annotation.Endpoint;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;

/**
 * @author xiaojing
 */
@ConditionalOnWebApplication
@ConditionalOnClass(Endpoint.class)
@Conditional(NacosConfigEnabledCondition.class)
public class NacosConfigEndpointAutoConfiguration {

	@Autowired
	private NacosConfigManager nacosConfigManager;

	@Autowired
	private NacosRefreshHistory nacosRefreshHistory;

	@ConditionalOnMissingBean
	@ConditionalOnAvailableEndpoint
	@Bean
	public NacosConfigEndpoint nacosConfigEndpoint() {
		return new NacosConfigEndpoint(nacosConfigManager.getNacosConfigProperties(),
				nacosRefreshHistory);
	}

	@Bean
	@ConditionalOnMissingBean
	@ConditionalOnEnabledHealthIndicator("nacos-config")
	public NacosConfigHealthIndicator nacosConfigHealthIndicator() {
		return new NacosConfigHealthIndicator(nacosConfigManager.getConfigService());
	}

}
