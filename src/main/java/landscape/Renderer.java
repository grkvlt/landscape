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

import static landscape.Utils.RANDOM;

import java.awt.Color;
import java.awt.BasicStroke;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Polygon;
import java.awt.image.BufferedImage;

/**
 * Fractal Landscape Renderer.
 *
 * Renders fractal landscape data as a {@link BufferedImage bitmap} in various
 * styles.
 */
public class Renderer {
    private boolean jitter;
    private int overscan;

    public Renderer() {
        this(true, 0);
    }

    public Renderer(boolean jitter, int overscan) {
        this.jitter = jitter;
        this.overscan = overscan;
    }

    /**
     * Render the landscape points as an image.
     */
    public BufferedImage image(double[][] points, double scale, double water, int z, int b) {
        return image(points, 0, points[0].length, scale, water, z, b);
    }

    /**
     * Render a slice of the landscape points as an image.
     */
    public BufferedImage image(double[][] points, int y0, int y1, double scale, double water, int z, int b) {
        // Set image size
        int sx = points.length;
        int sy = y1 - y0;
        int w = (int) (scale * sx) + (2 * b);
        int h = (int) (scale * sy) + (2 * b);
        
        // Create the image object and setup graphics environment with anti-aliasing
        BufferedImage image = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = image.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);

        // Define the background colour and stroke format
        g.setBackground(Color.BLACK);
        g.setStroke(new BasicStroke(2.0f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_ROUND));

        // Clear the image
        g.clearRect(0, 0, w, h);

        for (int j = y1 - 1 + overscan; j >= y0 - overscan; j--) {
            Polygon line = new Polygon();
            int x = b, y;
            for (int i = 0; i < sx; i++) {
                // Scale points to screen space for rendering
                double p = points[i][j];
                x = b + (int) (scale * i);
                y = b + (int) (scale * (y1 - j)) + (int) (p * z);
                if (jitter) x += (RANDOM.nextDouble() - 0.5d) * scale; // Add x-axis jitter
                line.addPoint(x, y);
            }

            // Add extra points to the fill polygon for full z clipping
            line.addPoint(x, b + (int) (scale * sy));
            line.addPoint(b, b + (int) (scale * sy));
            g.setColor(Color.BLACK);
            g.setClip(0, 0, w, h - b);
            g.fillPolygon(line);

            // Draw the landscape outline (without added points from above)
            g.setColor(Color.WHITE);
            g.setClip(0, 0, w, Math.min(h - b, b + (int) (scale * (y1 - j)) + (int) (z * water)) - 1);
            g.drawPolyline(line.xpoints, line.ypoints, line.npoints - 2);
        }

        return image;
    }

    /**
     * Plot the landscape as a shaded map based on gradient.
     */
    public BufferedImage plot(double[][] points, double[][] gradient, double scale, double water, double threshold) {
        // Set image size
        int sx = points.length;
        int sy = points[0].length;
        int s = (int) scale;
        int w = s * sx;
        int h = s * sy;
        
        // Create the image object and setup graphics environment
        BufferedImage image = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = image.createGraphics();

        // Define the background colour
        g.setBackground(Color.BLACK);

        // Clear the image
        g.clearRect(0, 0, w, h);

        for (int j = 0; j < sy; j++) {
            int x, y;
            for (int i = 0; i < sx; i++) {
                double p = points[i][j];
                double q = Math.abs(gradient[i][j] * 100d);
                int c = Math.min(255, (int) (q * 255d));
                x = (int) (scale * i);
                y = (int) (scale * (sy - j));
                if (p < water) {
                    // Set clipping box
                    Polygon box = new Polygon();
                    box.addPoint(x, y);
                    box.addPoint(x, y + s);
                    box.addPoint(x + s, y + s);
                    box.addPoint(x + s, y);
                    g.setClip(box);

                    // Plot pixel
                    if (q < threshold) {
                        g.setColor(Utils.color(0, c, c / 2));
                    } else {
                        g.setColor(Utils.color(c, c, c));
                    }
                    g.fillArc(x - s, y - s, s * 3, s * 3, 0, 360);
                }
            }
        }

        return image;
    }
}