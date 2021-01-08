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

import static landscape.Constants.fileFormat;
import static landscape.Constants.ICON_FILE;
import static landscape.Utils.saveDir;
import static landscape.Utils.save;
import static landscape.Utils.RANDOM;

import com.apple.eawt.Application;

import java.io.InputStream;
import java.awt.image.BufferedImage;
import java.util.Optional;

import javax.imageio.ImageIO;

/**
 * Generates image files of fractal landscapes.
 */
public class Images {
    /**
     * Main method for generator.
     * 
     * Parses arguments for configuration and initialises the class. Runs a loop
     * to create multiple landscapes, by calling the {@link #generate(int, int)}
     * method to create a landscape, then renders it as a {@link BufferedImage} which
     * is saved to a file.
     */
    public static void main(String[] argv) throws Exception {
        System.out.printf("+ Fractal landscape generator - %s\n", Constants.VERSION);
        System.out.printf("+ %s\n", Constants.COPYRIGHT);

        setup();

        String prefix = "fractal";
        int n = 10; // Number of images

        int z = 800; // Z-axis height
        int b = 50; // Border width
        double scale = 10d; // Scale multiplier

        int gi = 6; // Generator iterations
        double r = 2d; // Roughness
        int fi = 5; // Filter iterations
        double t = 0.9d; // Filter threshold
        int w = 4, h = 3; // Initial grid

        // Parse arguments
        if (argv.length >= 1) {
            r = Double.valueOf(argv[0]);
        }
        if (argv.length >= 2) {
            n = Integer.valueOf(argv[1]);
        }
        if (argv.length >= 3) {
            prefix = argv[2];
        }

        if (n > 1) System.out.printf("- Running %d times\n", n);
        Fractal landscape = new Fractal(gi);
        BufferedImage image;
        String fileName;
        do {
            // Configure landscape properties
            if (argv.length == 0) r = 2d + (RANDOM.nextDouble() / 4d);
            double water = 0.8d - RANDOM.nextDouble();
            System.out.printf("- Using %.3f roughness and water %.3f\n", r, water);

            // Generate height map
            System.out.printf("- Generating landscape over %d iterations\n", gi);
            double[][] points = landscape.generate(r, w, h);
            System.out.printf("+ Generated %d points\n", points.length * points[0].length);
            image = landscape.render(points, scale, water, z, b);
            System.out.printf("+ Rendered as %d x %d image\n", image.getWidth(), image.getHeight());
            fileName = save(image, fileFormat(), saveDir(), prefix);
            System.out.printf("> Saved image as %s\n", fileName);

            // Differentiate height map
            System.out.printf("- Differentiating points\n");
            double[][] gradient = landscape.differentiate(points);
            image = landscape.plot(points, gradient, scale / 2, water, t);
            System.out.printf("+ Plotted %d x %d image\n", image.getWidth(), image.getHeight());
            fileName = save(image, fileFormat(), saveDir(), prefix + "-map");
            System.out.printf("> Saved plot as %s\n", fileName);

            // Smooth height map
            System.out.printf("- Filtering points %d times with threshold %.3f\n", fi, t);
            double[][] smoothed = landscape.smooth(points, gradient, t, fi);
            image = landscape.render(smoothed, scale, water, z, b);
            fileName = save(image, fileFormat(), saveDir(), prefix + "-smooth");
            System.out.printf("> Saved image as %s\n", fileName);
        } while (0 <-- n);
    }

    private static final void setup() {
        // Set application icon
        Optional<String> vendor = Optional.ofNullable(System.getProperty("os.name"));
        vendor.filter(s -> s.toLowerCase().contains("mac")).ifPresent(s -> {
            try (InputStream icon = Fractal.class.getResourceAsStream(ICON_FILE)) {
                BufferedImage image = ImageIO.read(icon);
                Application.getApplication().setDockIconImage(image);
            } catch (Exception e) {
                throw new RuntimeException("Unable to load icon file", e);
            }
        });
    }
}