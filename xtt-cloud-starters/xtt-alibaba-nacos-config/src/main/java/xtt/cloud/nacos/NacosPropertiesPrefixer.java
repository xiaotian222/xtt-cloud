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

package xtt.cloud.nacos;

import xtt.cloud.nacos.NacosPropertiesPrefixProvider;
import xtt.cloud.nacos.utils.StringUtils;
import org.springframework.boot.context.properties.bind.BindResult;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.core.env.Environment;

import java.util.ServiceLoader;

/**
 * @author shiyiyue
 */
public final class NacosPropertiesPrefixer {

	/**
	 * prefix from spi provider.
	 */
	public static final String PREFIX = getPrefixFromSpi();

	private NacosPropertiesPrefixer() {
	}

	private static String getPrefixFromSpi() {
		ServiceLoader<NacosPropertiesPrefixProvider> load = ServiceLoader.load(NacosPropertiesPrefixProvider.class);
		for (NacosPropertiesPrefixProvider provider : load) {
			return provider.getPrefix();
		}
		return "";
	}

	public static String getPrefix(Environment environment) {
        //这里 spring.nacos -> spring.cloud.nacos 不知道为什么原来的作者为什么写错，吊毛个坑b
		String prefix = "spring.cloud.nacos";
		String prefixFromProperties = environment.getProperty("spring.nacos.properties.prefix");
		if (StringUtils.isBlank(prefixFromProperties)) {
			if (StringUtils.isNotBlank(NacosPropertiesPrefixer.PREFIX)) {
				prefix = NacosPropertiesPrefixer.PREFIX;
			}
		}
		else {
			prefix = prefixFromProperties;
		}

		if (StringUtils.isNotBlank(prefix) && prefix.endsWith(".")) {
			prefix = prefix.substring(0, prefix.length() - 1);
		}
		return prefix;
	}


	public static String getPrefix(Binder binder) {
        //这里 spring.nacos -> spring.cloud.nacos 不知道为什么原来的作者为什么写错，吊毛个坑b
		String prefix = "spring.cloud.nacos";
		BindResult<String> bind = binder.bind("spring.nacos.properties.prefix", String.class);
		if (!bind.isBound()) {
			if (StringUtils.isNotBlank(NacosPropertiesPrefixer.PREFIX)) {
				prefix = NacosPropertiesPrefixer.PREFIX;
			}
		}
		else {
			prefix = bind.get();
		}

		if (StringUtils.isNotBlank(prefix) && prefix.endsWith(".")) {
			prefix = prefix.substring(0, prefix.length() - 1);
		}
		return prefix;
	}

}
