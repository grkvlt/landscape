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

import java.awt.Color;
import java.awt.Frame;
import java.awt.Graphics2D;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Window;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 * Fullscreen mode display.
 */
public class Screen {
    private List<GraphicsDevice> devices;
    private GraphicsDevice gd;
    private Frame root;
    private Window screen;

    public Screen() {
        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        devices = Arrays.asList(ge.getScreenDevices());
    }

    public int monitors() { return devices.size(); }

    public Graphics2D fullscreen(int monitor) {
        Objects.checkIndex(monitor, monitors());

        gd = devices.get(monitor);

        root = new Frame(gd.getDefaultConfiguration());
        root.setName(Constants.VERSION);
        root.enableInputMethods(true);
        root.setVisible(true);

        screen = new Window(root);
        screen.enableInputMethods(false);
        gd.setFullScreenWindow(screen);
    
        return (Graphics2D) screen.getGraphics();
    }

    public void display(Graphics2D g, BufferedImage image) {
        Objects.requireNonNull(root, "The root frame is not set");
        Objects.requireNonNull(screen, "The screen windows is not set");

        int width = screen.getWidth(), height = screen.getHeight();

        AffineTransform transform = new AffineTransform();
        float scale = (float) width / (float) image.getWidth();
        if ((int) (image.getHeight() * scale) > height) {
            scale *= (float) height / (image.getHeight() * scale);
        }
        transform.scale(scale, scale);

        g.setBackground(Color.BLACK);
        g.clearRect(0, 0, width, height);

        g.drawImage(image, transform, null);
    }

    public void exit() {
        if (Objects.isNull(gd) || Objects.isNull(root)) return;

        gd.setFullScreenWindow(null);
        root.setVisible(false);
    }
}