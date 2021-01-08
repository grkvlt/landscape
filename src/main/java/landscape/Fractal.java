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
 * Fractal Landscape Generator.
 *
 * Generates random fractal landscapes, with various
 * tuneable parameters, such as roughness and sea-level.
 */
public class Fractal {
    public int iterations;

    public Fractal(int iterations) {
        this.iterations = iterations;
    }

    /**
     * Generate a fractal landscape from initial array of zero height.
     */
    public double[][] generate(double roughness, int w, int h) throws Exception {
        int i = 0;
        double points[][] = new double[w][h];
        while (i < iterations) {
            points = interpolate(points, roughness, ++i);
        }
        return points;
    }

    /**
     * Interpolate points with scaled random noise.
     */
    private double[][] interpolate(double[][] input, double roughness, int level) {
        int ix = input.length;
        int iy = input[0].length;
        int ox = (ix - 1) * 2 + 1;
        int oy = (iy - 1) * 2 + 1;
        double output[][] = new double[ox][oy];

        for (int x = 0; x < ix; x++) {
            for (int y = 0; y < iy; y++) {
                double p = input[x][y];
                double o = p;
                output[2*x][2*y] = o;
                if (x > 0 && y > 0) {
                    double q = (input[x-1][y-1] + input[x-1][y] + input[x][y-1] + input[x][y]) / 4d;
                    double r = (RANDOM.nextDouble() - 0.5d) * Math.pow(level, -roughness);
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
    
    /**
     * Differentiate height map into gradient map.
     */
    public double[][] differentiate(double[][] input) {
        int ix = input.length;
        int iy = input[0].length;
        double output[][] = new double[ix][iy];

        for (int x = 0; x < ix; x++) {
            for (int y = 0; y < iy; y++) {
                double o = 0d;
                if (x > 0 && y > 0) {
                    double p = input[x][y];
                    double w = input[x-1][y] - p;
                    double nw = input[x-1][y-1] - p;
                    double n = input[x][y-1] - p;
                    o = (w + nw + n) / 3d;
                }
                output[x][y] = o;
            }
        }

        return output;
    }
    
    /**
     * Apply smoothing filter iteratively.
     */
    public double[][] smooth(double[][] heights, double[][] gradient, double threshold, int iterations) {
        int ix = heights.length;
        int iy = heights[0].length;
        double output[][] = new double[ix][iy];
        double input[][] = heights;
        int n = iterations;

        do {
            output = filter(input, gradient, threshold);
            input = output;
        } while (n --> 0);

        return output;
    }

    /**
     * Low pass filter height map based on gradient threshold.
     */
    private double[][] filter(double[][] input, double[][] gradient, double threshold) {
        int ix = input.length;
        int iy = input[0].length;
        double output[][] = new double[ix][iy];

        for (int x = 0; x < ix; x++) {
            for (int y = 0; y < iy; y++) {
                double p = input[x][y];
                double o = p;
                if (x > 0 && y > 0) {
                    double q = gradient[x][y];
                    if (Math.abs(q * 100d) < threshold) {
                        o = (input[x-1][y] + input[x-1][y-1] + input[x][y-1] + p) / 4d;
                    }
                }
                output[x][y] = o;
            }
        }

        return output;
    }

    /**
     * Render the landscape points as an image.
     */
    public BufferedImage render(double[][] points, double scale, double water, int z, int b) {
        // Set image size
        int sx = points.length;
        int sy = points[0].length;
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

        for (int j = sy - 1; j >= 0; j--) {
            Polygon line = new Polygon();
            int x = b, y = b;
            for (int i = 0; i < sx; i++) {
                // Scale points to screen space for rendering
                double p = points[i][j];
                x = b + (int) (scale * i);
                y = b + (int) (scale * (sy - j)) + (int) (p * z);
                x += (RANDOM.nextDouble() - 0.5d) * scale; // Add x-axis jitter
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
            g.setClip(0, 0, w, Math.min(h - b, b + (int) (scale * (sy - j)) + (int) (z * water)) - 1);
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
                y = (int) (scale * j);
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