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

import static landscape.Constants.COLOR_KEY;
import static landscape.Constants.fileFormat;
import static landscape.Constants.SAVE_ALL_KEY;
import static landscape.Utils.propertyFlag;
import static landscape.Utils.setApplicationIcon;
import static landscape.Utils.saveDir;
import static landscape.Utils.save;
import static landscape.Utils.color;
import static landscape.Utils.RANDOM;

import java.awt.image.BufferedImage;
import java.awt.Color;

/**
 * Generates images of multiple fractal landscapes.
 */
public class Images implements Runnable {
    private String prefix = "fractal";
    private int n = 10; // Number of images
    private boolean saveAll = propertyFlag(SAVE_ALL_KEY, false);
    private boolean color = propertyFlag(COLOR_KEY, false);

    private int z = 600; // Z-axis height
    private int b = 0; // Border width
    private double scale = 12d; // Scale multiplier

    private int gi = 6; // Generator iterations
    private double r = 2d + (RANDOM.nextDouble() / 4d); // Roughness
    private int fi = 4; // Filter iterations
    private double t = 0.8d; // Filter threshold
    private int w = 4, h = 3; // Initial grid
    private double water = 0.8d - RANDOM.nextDouble(); // Water level

    private Fractal landscape = new Fractal(gi);
    private Renderer render = new Renderer();
    private BufferedImage image;
    private String fileName;

    /**
     * Multiple fractal landscape image generator.
     * 
     * Runs a loop to create multiple landscapes, by calling the {@link Fractal#generate(double, int, int)}
     * method to create a landscape, then renders it as a {@link BufferedImage} which is saved to a file.
     * Additional images of the unfiltered landscape and the gradient map will also be saved if the
     * {@code images.save.all} property is set.
     */
    @Override
    public void run() {
        if (n > 1) System.out.printf("- Running %d times\n", n);
        System.out.printf("- Using %.3f roughness and water %.3f\n", r, water);

        do {
            if (color) {
                int red = RANDOM.nextInt(32);
                int green = RANDOM.nextInt(32);
                int blue = RANDOM.nextInt(32);
                Color background = color(10 + red, 10 + green, 10 + blue);
                Color foreground = color(250 - red, 250 - green, 250 - blue);
                render.setBackground(background);
                render.setForeground(foreground);
                System.out.printf("- Setting landscape colour to #%2x%2x%2x\n",
                        background.getRed(), background.getGreen(), background.getBlue());
            }

            // Generate height map
            System.out.printf("- Generating landscape over %d iterations\n", gi);
            double[][] points = landscape.generate(r, w, h);
            System.out.printf("+ Generated %d points\n", points.length * points[0].length);
            if (saveAll) {
                image = render.image(points, scale, water, z, b);
                System.out.printf("+ Rendered as %d x %d image\n", image.getWidth(), image.getHeight());
                fileName = save(image, fileFormat(), saveDir(), prefix + "-base");
                System.out.printf("> Saved image as %s\n", fileName);
            }

            // Differentiate height map
            System.out.printf("- Differentiating points\n");
            double[][] gradient = landscape.differentiate(points);
            if (saveAll) {
                image = render.plot(points, gradient, scale / 2, water, t);
                System.out.printf("+ Plotted %d x %d image\n", image.getWidth(), image.getHeight());
                fileName = save(image, fileFormat(), saveDir(), prefix + "-map");
                System.out.printf("> Saved plot as %s\n", fileName);
            }

            // Smooth height map
            System.out.printf("- Filtering points %d times with threshold %.3f\n", fi, t);
            double[][] smoothed = landscape.smooth(points, gradient, t, fi);
            image = render.image(smoothed, scale, water, z, b);
            System.out.printf("+ Rendered as %d x %d image\n", image.getWidth(), image.getHeight());
            fileName = save(image, fileFormat(), saveDir(), prefix);
            System.out.printf("> Saved image as %s\n", fileName);
        } while (0 <-- n);
    }

    private void setup(String[] argv) {
        setApplicationIcon();

        // Parse arguments
        if (argv.length >= 1) {
            r = Double.parseDouble(argv[0]);
        }
        if (argv.length >= 2) {
            n = Integer.parseInt(argv[1]);
        }
        if (argv.length >= 3) {
            prefix = argv[2];
        }
    }

    /**
     * Entrypoint.
     */
    public static void main(String[] argv) {
        System.out.printf("+ Fractal landscape images - %s\n", Constants.VERSION);
        System.out.printf("+ %s\n", Constants.COPYRIGHT);

        // Run flyover generator
        Images images = new Images();
        images.setup(argv);
        images.run();
        System.exit(0);
    }
}