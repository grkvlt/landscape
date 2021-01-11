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

    private String prefix = "fractal";
    private int n = 10; // Number of images

    private int z = 800; // Z-axis height
    private int b = 50; // Border width
    private double scale = 10d; // Scale multiplier

    private int gi = 6; // Generator iterations
    private double r = 2d + (RANDOM.nextDouble() / 4d); // Roughness
    private int fi = 5; // Filter iterations
    private double t = 0.9d; // Filter threshold
    private int w = 4, h = 3; // Initial grid
    private double water = 0.8d - RANDOM.nextDouble(); // Water level

    private Fractal landscape = new Fractal(gi);
    private Renderer render = new Renderer();
    private BufferedImage image;
    private String fileName;

    /**
     * Fractal landscape image generator.
     * 
     * Runs a loop to create multiple landscapes, by calling the {@link #generate(int, int)}
     * method to create a landscape, then renders it as a {@link BufferedImage} which
     * is saved to a file.
     */
    public void run() throws Exception {
        if (n > 1) System.out.printf("- Running %d times\n", n);
        System.out.printf("- Using %.3f roughness and water %.3f\n", r, water);

        do {
            // Generate height map
            System.out.printf("- Generating landscape over %d iterations\n", gi);
            double[][] points = landscape.generate(r, w, h);
            System.out.printf("+ Generated %d points\n", points.length * points[0].length);
            image = render.image(points, scale, water, z, b);
            System.out.printf("+ Rendered as %d x %d image\n", image.getWidth(), image.getHeight());
            fileName = save(image, fileFormat(), saveDir(), prefix);
            System.out.printf("> Saved image as %s\n", fileName);

            // Differentiate height map
            System.out.printf("- Differentiating points\n");
            double[][] gradient = landscape.differentiate(points);
            image = render.plot(points, gradient, scale / 2, water, t);
            System.out.printf("+ Plotted %d x %d image\n", image.getWidth(), image.getHeight());
            fileName = save(image, fileFormat(), saveDir(), prefix + "-map");
            System.out.printf("> Saved plot as %s\n", fileName);

            // Smooth height map
            System.out.printf("- Filtering points %d times with threshold %.3f\n", fi, t);
            double[][] smoothed = landscape.smooth(points, gradient, t, fi);
            image = render.image(smoothed, scale, water, z, b);
            fileName = save(image, fileFormat(), saveDir(), prefix + "-smooth");
            System.out.printf("> Saved image as %s\n", fileName);
        } while (0 <-- n);
    }

    private void setup(String[] argv) {
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
    }

    /**
     * Entrypoint.
     */
    public static void main(String[] argv) throws Exception {
        System.out.printf("+ Fractal landscape images - %s\n", Constants.VERSION);
        System.out.printf("+ %s\n", Constants.COPYRIGHT);

        // Run flyover generator
        Images images = new Images();
        images.setup(argv);
        images.run();
        System.exit(0);
    }
}