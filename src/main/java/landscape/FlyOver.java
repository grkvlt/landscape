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

import static landscape.Constants.ICON_FILE;
import static landscape.Utils.RANDOM;

import com.apple.eawt.Application;

import java.io.InputStream;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.Optional;

import javax.imageio.ImageIO;

/**
 * Generates a fly-through animation of a fractal landscape.
 */
public class FlyOver implements Thread.UncaughtExceptionHandler {

    // Display frames
    private boolean display = true;

    // Save location
    private String directory = "./flyover";
    private String prefix = "frame";

    // Configure landscape properties
    private int z = 800; // Z-axis height
    private int b = 0; // Border width
    private double scale = 10d; // Scale multiplier

    private int gi = 6; // Generator iterations
    private double r = 2d + (RANDOM.nextDouble() / 4d); // Roughness
    private double water = 0.8d - RANDOM.nextDouble();
    private int fi = 5; // Filter iterations
    private double t = 0.9d; // Filter threshold
    private int w = 4, h = 40; // Initial grid

    private Fractal landscape;
    private Renderer render;
    private Screen screen;

    public FlyOver() {
        this.landscape = new Fractal(gi);
        this.render = new Renderer(false, 30);
        this.screen = new Screen();
    }

    /**
     * Generate flyover animation.
     */
    public void run() throws Exception {
        // Generate height map
        System.out.printf("- Generating landscape over %d iterations\n", gi);
        System.out.printf("- Using %.3f roughness and water %.3f\n", r, water);
        double[][] points = landscape.generate(r, w, h);
        System.out.printf("- Landscape size %d x %d\n", points.length, points[0].length);

        // Differentiate height map
        System.out.printf("- Differentiating points\n");
        double[][] gradient = landscape.differentiate(points);

        // Smooth height map
        System.out.printf("- Filtering points %d times with threshold %.3f\n", fi, t);
        double[][] smoothed = landscape.smooth(points, gradient, t, fi);

        // Display fullscreen
        Optional<Graphics2D> og = Optional.ofNullable(display ? screen.fullscreen(0) : null);

        // Render frames
        int max = (int) ((h - 1) * Math.pow(2, gi)) - 1;
        BufferedImage frame = null;
        int n = 0;
        for (int i = 50; i < max - 150; i++, n++) {
            frame = render.image(smoothed, i, i + 120, scale, water, z, b);

            // Display, save and pause
            if (display) {
                screen.display(og.get(), frame);
            } else {
                System.out.printf("- Rendered frame %04d\r", n);
            }
            Utils.save(frame, Constants.PNG, directory, prefix);
            Utils.sleep(10);
        }
        System.out.printf("+ Frames saved as %d x %d total %d\n", frame.getWidth(), frame.getHeight(), n);

        // Done!
        screen.exit();
    }

    @Override
    public void uncaughtException(Thread t, Throwable e) {
        // Log exception
        System.err.printf("! Caught exception %s\n", e.getMessage());
        e.printStackTrace(System.err);

        // Exit
        screen.exit();
        System.exit(1);
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
        if (argv.length > 0) {
            display = Boolean.parseBoolean(argv[0]);
        }
        if (argv.length > 1) {
            directory = argv[1];
        }
    
        // Set exception handler
        Thread.setDefaultUncaughtExceptionHandler(this);
    }

    /**
     * Entrypoint.
     */
    public static void main(String[] argv) throws Exception {
        System.out.printf("+ Fractal landscape flyover - %s\n", Constants.VERSION);
        System.out.printf("+ %s\n", Constants.COPYRIGHT);

        // Run flyover generator
        FlyOver flyover = new FlyOver();
        flyover.setup(argv);
        flyover.run();
        System.exit(0);
    }
}