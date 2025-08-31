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

package xtt.cloud.nacos.configdata;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import xtt.cloud.nacos.client.NacosPropertySource;
import xtt.cloud.nacos.parser.NacosDataParserHandler;
import xtt.cloud.nacos.NacosConfigManager;
import xtt.cloud.nacos.NacosConfigProperties;
import xtt.cloud.nacos.NacosPropertySourceRepository;
import xtt.cloud.nacos.NacosPropertiesPrefixer;
import com.alibaba.nacos.api.config.ConfigService;
import com.alibaba.nacos.api.exception.NacosException;
import org.apache.commons.logging.Log;

import org.springframework.boot.context.config.ConfigData;
import org.springframework.boot.context.config.ConfigDataLoader;
import org.springframework.boot.context.config.ConfigDataLoaderContext;
import org.springframework.boot.context.config.ConfigDataResourceNotFoundException;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.boot.logging.DeferredLogFactory;
import org.springframework.core.env.PropertySource;

import static xtt.cloud.nacos.configdata.ConfigPreference.LOCAL;
import static xtt.cloud.nacos.configdata.ConfigPreference.REMOTE;
import static xtt.cloud.nacos.configdata.NacosConfigDataResource.NacosItemConfig;
import static org.springframework.boot.context.config.ConfigData.Option;

/**
 * Implementation of {@link ConfigDataLoader}.
 *
 * <p>
 * Load {@link ConfigData} via {@link NacosConfigDataResource}
 *
 * @author freeman
 * @since 2021.0.1.0
 */
public class NacosConfigDataLoader implements ConfigDataLoader<NacosConfigDataResource> {

	private final Log log;

	public NacosConfigDataLoader(DeferredLogFactory logFactory) {
		this.log = logFactory.getLog(getClass());
	}

	@Override
	public ConfigData load(ConfigDataLoaderContext context,
			NacosConfigDataResource resource) {
		return doLoad(context, resource);
	}

	public ConfigData doLoad(ConfigDataLoaderContext context,
			NacosConfigDataResource resource) {
		try {
			ConfigService configService = getBean(context, NacosConfigManager.class)
					.getConfigService();
			NacosConfigProperties properties = getBean(context,
					NacosConfigProperties.class);

			NacosItemConfig config = resource.getConfig();
			// pull config from nacos
			List<PropertySource<?>> propertySources = pullConfig(configService,
					config.getGroup(), config.getDataId(), config.getSuffix(),
					properties.getTimeout());

			NacosPropertySource propertySource = new NacosPropertySource(propertySources,
					config.getGroup(), config.getDataId(), new Date(),
					config.isRefreshEnabled());

			NacosPropertySourceRepository.collectNacosPropertySource(propertySource);

			return new ConfigData(propertySources, getOptions(context, resource));
		}
		catch (Exception e) {
			log.error("Error getting properties from nacos: " + resource, e);
			if (!resource.isOptional()) {
				throw new ConfigDataResourceNotFoundException(resource, e);
			}
		}
		return null;
	}

	private Option[] getOptions(ConfigDataLoaderContext context,
			NacosConfigDataResource resource) {
		List<Option> options = new ArrayList<>();
		options.add(Option.IGNORE_IMPORTS);
		options.add(Option.IGNORE_PROFILES);
		if (getPreference(context, resource) == REMOTE) {
			// mark it as 'PROFILE_SPECIFIC' config, it has higher priority,
			// will override the none profile specific config.
			// fixed https://github.com/alibaba/spring-cloud-alibaba/issues/2455
			options.add(Option.PROFILE_SPECIFIC);
		}
		return options.toArray(new Option[0]);
	}

	private ConfigPreference getPreference(ConfigDataLoaderContext context,
			NacosConfigDataResource resource) {
		Binder binder = context.getBootstrapContext().get(Binder.class);
		String prefix = NacosPropertiesPrefixer.getPrefix(binder);


		ConfigPreference preference = binder
				.bind(prefix + ".config.preference", ConfigPreference.class)
				.orElse(LOCAL);
		String specificPreference = resource.getConfig().getPreference();
		if (specificPreference != null) {
			try {
				preference = ConfigPreference.valueOf(specificPreference.toUpperCase());
			}
			catch (IllegalArgumentException ignore) {
				// illegal preference value, just ignore.
				log.error(String.format(
						"illegal preference value: %s, using default preference: %s",
						specificPreference, preference));
			}
		}
		return preference;
	}

	private List<PropertySource<?>> pullConfig(ConfigService configService, String group,
			String dataId, String suffix, long timeout)
			throws NacosException, IOException {
		String config = configService.getConfig(dataId, group, timeout);
		logLoadInfo(group, dataId, config);
		// fixed issue: https://github.com/alibaba/spring-cloud-alibaba/issues/2906 .
		String configName = group + "@" + dataId;
		return NacosDataParserHandler.getInstance().parseNacosData(configName, config, suffix);
	}

	private void logLoadInfo(String group, String dataId, String config) {
		if (config != null) {
			log.info(String.format(
					"[Nacos Config] Load config[dataId=%s, group=%s] success", dataId,
					group));
		}
		else {
			log.warn(String.format("[Nacos Config] config[dataId=%s, group=%s] is empty",
					dataId, group));
		}
		if (log.isDebugEnabled()) {
			log.debug(String.format(
					"[Nacos Config] config[dataId=%s, group=%s] content: \n%s", dataId,
					group, config));
		}
	}

	protected <T> T getBean(ConfigDataLoaderContext context, Class<T> type) {
		if (context.getBootstrapContext().isRegistered(type)) {
			return context.getBootstrapContext().get(type);
		}
		return null;
	}

}
