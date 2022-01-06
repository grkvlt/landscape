/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 * 
 * Copyright 2020-2021 by Andrew Donald Kennedy
 */
package landscape;

import static landscape.Utils.propertyFlag;

import java.util.Set;

/**
 * Configuration and system property constant definitions.
 */
public class Constants {
    // Properties for global runtime configuration
    public static final String DEBUG_KEY = "landscape.debug";
    public static final String SEED_KEY = "landscape.seed";
    public static final String SAVE_DIR_KEY = "landscape.save.dir";
    public static final String FILE_FORMAT_KEY = "landscape.save.format";
    public static final String SAVE_ALL_KEY = "images.save.all";
    public static final String COLOR_KEY = "images.color";
    public static final String DISPLAY_FRAMES_KEY = "flyover.display";

    /** Default save directory name. */
    public static final String SAVE_DIR = "Landscape";

    /** Application icon resource path. */
    public static final String ICON_FILE = "/icon.png";

    /** Debugging enable. */
    public static final Boolean DEBUG = propertyFlag(DEBUG_KEY, false);

    // Image save formats
    public static final String PNG = "PNG", JPEG = "JPEG", TIFF = "TIFF";
    public static final Set<String> FILE_FORMATS = Set.of(PNG, JPEG, TIFF);

    /** Copyright text. */
    public static final String COPYRIGHT = "Copyright 2020-2022 by Andrew Donald Kennedy";

    /** Version text. */
    public static final String VERSION = "Landscape 0.5-SNAPSHOT";

    /** About text. */
    public static final String ABOUT = VERSION + " / " + COPYRIGHT;

    /** The file format to use when {@link Utils#save saving}. */
    public static String fileFormat() {
        String format = System.getProperty(FILE_FORMAT_KEY, PNG);
        if (!FILE_FORMATS.contains(format)) {
            throw new RuntimeException(String.format("Invalid file format %s", format));
        }
        return format;
    }
}