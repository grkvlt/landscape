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
 * Copyright 2020 by Andrew Donald Kennedy
 */
package landscape;

import static landscape.Constants.SEED_KEY;
import static landscape.Constants.SAVE_DIR;
import static landscape.Constants.SAVE_DIR_KEY;

import java.awt.Color;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import javax.imageio.ImageIO;

public class Utils {
    /** Random number generator */
    public static final Random RANDOM = new Random();

    // Sets the random number generator seed from a system property
    static {
        Optional<Long> seed = Optional.ofNullable(Long.getLong(SEED_KEY));
        seed.ifPresent(s -> RANDOM.setSeed(s));
    }

    /**
     * Sample a random {@link Object object} from a {@link Collection collection}.
     * 
     * @param collection The sample space of objects
     * @return A single object selected at randomn
     */
    public static <O> O sample(Collection<O> collection) {
        if (collection.isEmpty()) throw new IllegalArgumentException("Collection is empty");

        List<O> list = List.copyOf(collection);
        O sample = list.get(RANDOM.nextInt(list.size()));
        return sample;
    }

    /**
     * Save an {@link BufferedImage image} to a file in a directory.
     * 
     * The image will be saved in a file with a name formatted as {@code prefix-000.png}
     * where the number {@literal 000} is a monotonically increasing integer that
     * should ensure the file is unique, and the extension {@literal png} is an example
     * of one of the possible formats the image data can be written as.
     * 
     * @param image The source image data
     * @param format The saved file format name
     * @param directory The target directory
     * @param prefix The filename prefix to use when saving the image
     * @return The filename used to save the image
     */
    public static String save(BufferedImage image, String format, String directory, String prefix) {
        int id = 0;
        String file = "";

        do {
            file = String.format("%s-%03d.%s", prefix, id++, format.toLowerCase());
        } while (Files.exists(Path.of(directory, file)));

        try {
            ImageIO.write(image, format, new File(directory, file));
        } catch (IOException ioe) {
            String message = String.format("Failed to write %s image: %s", format, ioe.getMessage());
            System.err.println(message);
            throw new RuntimeException(message, ioe);
        }

        return file;
    }

    /**
     * The directory to save files and images to.
     * 
     * The default is to use the directory named {@link #SAVE_DIR} in the
     * {@code user.home} directory. This will be created if it does
     * not exist. If the {@link #SAVE_DIR_KEY} property is set, this will
     * be used in preference, and will also be treated as a sub-directory
     * of the home directory unless an absolute path is given.
     * 
     * @return The name of the directory to use
     */
    public static String saveDir() {
        String home = System.getProperty("user.home");
        String save = System.getProperty(SAVE_DIR_KEY, SAVE_DIR);
        
        Path dir = Path.of(save).isAbsolute() ? Path.of(save) : Path.of(home, save);

        if (Files.notExists(dir)) {
            try {
                Files.createDirectories(dir);
            } catch (IOException ioe) {
                String message = String.format("Failed to create %s directory: %s", dir, ioe.getMessage());
                System.err.println(message);
                throw new RuntimeException(message, ioe);
            }
        }

        return dir.toString();
    }

    /**
     * Look up a boolean {@code key} in the {@link System#getProperties() system properties}.
     * 
     * Returns {@literal true} if the flag exists and has no value, otherwise uses the standard
     * {@link System#getProperty(String, String)} method to get the value (using {@code def} if the
     * flag is not present) and returns it as a boolean.
     * 
     * @param flag The name of the flag propoerty to look up
     * @param def The default value if the flag is not present
     */
    public static boolean propertyFlag(String flag, boolean def) {
        if (System.getProperties().containsKey(flag) && System.getProperty(flag).isEmpty()) {
            return true;
        } else return Boolean.parseBoolean(System.getProperty(flag, Boolean.toString(def)));
    }

    public static Color color(int r, int g, int b) {
        return Color.decode(String.format("0x%02x%02x%02x", r, g, b));
    }

    public static void beep() {
        Toolkit.getDefaultToolkit().beep();
    }
}