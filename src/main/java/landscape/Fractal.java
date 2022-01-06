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

/**
 * Fractal Landscape Generator.
 *
 * Generates random fractal landscapes, with various tunable parameters, such as roughness and sea-level.
 */
public class Fractal {
    public int iterations;

    public Fractal(int iterations) {
        this.iterations = iterations;
    }

    /**
     * Generate a fractal landscape from initial array of zero height.
     */
    public double[][] generate(double roughness, int w, int h) {
        int i = 0;
        double[][] points = new double[w][h];
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
        double[][] output = new double[ox][oy];

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
        double[][] output = new double[ix][iy];

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
        double[][] output;
        double[][] input = heights;
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
        double[][] output = new double[ix][iy];

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
}