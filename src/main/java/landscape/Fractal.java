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

import static landscape.Constants.fileFormat;
import static landscape.Constants.ICON_FILE;
import static landscape.Utils.saveDir;
import static landscape.Utils.save;
import static landscape.Utils.RANDOM;

import com.apple.eawt.Application;

import java.io.InputStream;
import java.awt.Color;
import java.awt.BasicStroke;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.image.BufferedImage;
import java.util.Optional;

import javax.imageio.ImageIO;

public class Fractal {
    public int iterations;

    public Fractal(int iterations) {
        this.iterations = iterations;
    }

    public double[][] generate() throws Exception {
        int i = 0;
        double points[][] = new double[2][2];
        while (i < iterations) {
            points = interpolate(points, ++i);
        }
        return points;
    }

    public double[][] interpolate(double[][] input, int level) {
        int size = input.length;
        int os = (size - 1) * 2 + 1;
        double output[][] = new double[os][os];

        for (int x = 0; x < size; x++) {
            for (int y = 0; y < size; y++) {
                double p = input[x][y];
                double o = p;
                output[2*x][2*y] = o;
                if (x > 0 && y > 0) {
                    double q = (input[x-1][y-1] + input[x-1][y] + input[x][y-1] + input[x][y]) / 4d;
                    double r = RANDOM.nextGaussian() * (1d / (double) (level * level));
                    o = q + r;
                    output[2*x - 1][2*y - 1] = o;
                }
                if (x > 0) {
                    output[2*x - 1][2*y] = (p + input[x-1][y] + o) / 3d;
                }
                if (y > 0) {
                    output[2*x][2*y - 1] = (p + input[x][y-1] + o) / 3d;
                }
            }
        }

        return output;
    }

    public BufferedImage render(double[][] points, double scale, double water, int z, int b) {
        int size = points.length;
        int w = (int) (scale * size) + (2 * b);
        int h = w;
        BufferedImage image = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = image.createGraphics();

        g.setBackground(Color.BLACK);
        g.setStroke(new BasicStroke(0.5f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER));
        g.clearRect(0, 0, w, h);

        for (int j = size - 1; j >= 0; j--) {
            Polygon line = new Polygon();
            int x = b, y = b;
            for (int i = 0; i < size; i++) {
                double p = points[i][j];
                x = b + (int) (scale * i);
                y = b + (int) (scale * (size - j)) + (int) (p * z);
                line.addPoint(x, y);
            }
            line.addPoint(x, b + (int) (scale * size));
            line.addPoint(b, b + (int) (scale * size));
            g.setColor(Color.BLACK);
            g.setClip(0, 0, w, h - b);
            g.fillPolygon(line);
            g.setColor(Color.WHITE);
            g.setClip(0, 0, w, Math.min(h - b, b + (int) (scale * (size - j)) + (int) (z * water)) - 1);
            g.drawPolyline(line.xpoints, line.ypoints, line.npoints - 2);
        }

        return image;
    }

    public static void main(String[] argv) throws Exception {
        System.out.printf("+ Fractal landscape generator - %s\n", Constants.VERSION);
        System.out.printf("+ %s\n", Constants.COPYRIGHT);

        setup();

        String fileName = "fractal";
        int n = 100;

        // Parse arguments
        if (argv.length >= 1) {
            n = Integer.valueOf(argv[0]);
        }
        if (argv.length >= 2) {
            fileName = argv[1];
        }

        Fractal landscape = new Fractal(8);
        System.out.printf("- Running %d times for %d iteration\n", n, landscape.iterations);
        for (int i = 0; i < n; i++) {
            double[][] points = landscape.generate();
            BufferedImage image = landscape.render(points, 8d, 0.8d - RANDOM.nextDouble(), 512, 100);
            System.out.printf("- Rendered %d points as %d x %d image\n",
                    points.length * points[0].length, image.getWidth(), image.getHeight());
            String saveName = save(image, fileFormat(), saveDir(), fileName);
            System.out.printf("> Saved image as %s\n", saveName);
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