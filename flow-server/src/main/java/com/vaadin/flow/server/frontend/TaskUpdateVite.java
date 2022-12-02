/*
 * Copyright 2000-2022 Vaadin Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package com.vaadin.flow.server.frontend;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.io.UncheckedIOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Updates the Vite configuration files according with current project settings.
 * <p>
 * For internal use only. May be renamed or removed in a future release.
 */
public class TaskUpdateVite implements FallibleCommand, Serializable {

    private final Options options;

    TaskUpdateVite(Options options) {
        this.options = options;
    }

    @Override
    public void execute() {
        try {
            createConfig();
            createGeneratedConfig();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private void createConfig() throws IOException {
        // Only create it if it does not exist
        File configFile = new File(options.getNpmFolder(),
                FrontendUtils.VITE_CONFIG);
        if (configFile.exists()) {
            return;
        }

        URL resource = this.getClass().getClassLoader()
                .getResource(FrontendUtils.VITE_CONFIG);
        String template = IOUtils.toString(resource, StandardCharsets.UTF_8);
        FileUtils.write(configFile, template, StandardCharsets.UTF_8);
        log().debug("Created vite configuration file: '{}'", configFile);

    }

    private void createGeneratedConfig() throws IOException {
        // Always overwrite this
        File generatedConfigFile = new File(options.getNpmFolder(),
                FrontendUtils.VITE_GENERATED_CONFIG);
        URL resource = this.getClass().getClassLoader()
                .getResource(FrontendUtils.VITE_GENERATED_CONFIG);
        String template = IOUtils.toString(resource, StandardCharsets.UTF_8);

        template = template
                .replace("#settingsImport#",
                        "./" + options.getBuildDirectoryName() + "/"
                                + TaskUpdateSettingsFile.DEV_SETTINGS_FILE)
                .replace("#buildFolder#",
                        "./" + options.getBuildDirectoryName());
        FileUtils.write(generatedConfigFile, template, StandardCharsets.UTF_8);
        log().debug("Created vite generated configuration file: '{}'",
                generatedConfigFile);
    }

    private Logger log() {
        return LoggerFactory.getLogger(this.getClass());
    }
}
