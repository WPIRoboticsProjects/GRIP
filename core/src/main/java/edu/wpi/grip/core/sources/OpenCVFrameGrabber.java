/*
 * Copyright (C) 2009-2015 Samuel Audet
 *
 * Licensed either under the Apache License, Version 2.0, or (at your option)
 * under the terms of the GNU General Public License as published by
 * the Free Software Foundation (subject to the "Classpath" exception),
 * either version 2, or any later version (collectively, the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *     http://www.gnu.org/licenses/
 *     http://www.gnu.org/software/classpath/license.html
 *
 * or as provided in the LICENSE.txt file that accompanied this code.
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package edu.wpi.grip.core.sources;

import java.io.File;
import java.util.logging.Logger;

import org.bytedeco.javacpp.Loader;
import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.FrameConverter;
import org.bytedeco.javacv.OpenCVFrameConverter;

import static org.bytedeco.javacpp.opencv_core.*;
import static org.bytedeco.javacpp.opencv_imgproc.*;
import static org.bytedeco.javacpp.opencv_videoio.*;

/**
 * @author Samuel Audet
 */
public class OpenCVFrameGrabber extends FrameGrabber {
    private static final Logger logger = Logger.getLogger(OpenCVFrameGrabber.class.getName());

    public static String[] getDeviceDescriptions() throws Exception {
        tryLoad();
        throw new UnsupportedOperationException("Device enumeration not support by OpenCV.");
    }

    public static OpenCVFrameGrabber createDefault(File deviceFile) throws Exception {
        return new OpenCVFrameGrabber(deviceFile);
    }

    public static OpenCVFrameGrabber createDefault(String devicePath) throws Exception {
        return new OpenCVFrameGrabber(devicePath);
    }

    public static OpenCVFrameGrabber createDefault(int deviceNumber) throws Exception {
        return new OpenCVFrameGrabber(deviceNumber);
    }

    private static Exception loadingException = null;

    public static void tryLoad() throws Exception {
        if (loadingException != null) {
            throw loadingException;
        } else {
            try {
                Loader.load(org.bytedeco.javacpp.opencv_highgui.class);
            } catch (Throwable t) {
                throw loadingException = new Exception("Failed to load " + OpenCVFrameGrabber.class, t);
            }
        }
    }

    public OpenCVFrameGrabber(int deviceNumber) {
        this.deviceNumber = deviceNumber;
    }

    public OpenCVFrameGrabber(File file) {
        this(file.getAbsolutePath());
    }

    public OpenCVFrameGrabber(String filename) {
        this.filename = filename;
    }

    public void release() throws Exception {
        stop();
    }

    @Override
    protected void finalize() throws Throwable {
        super.finalize();
        release();
    }

    private int deviceNumber = 0;
    private String filename = null;
    private CvCapture capture = null;
    private IplImage return_image = null;
    private FrameConverter converter = new OpenCVFrameConverter.ToIplImage();

    @Override
    public double getGamma() {
        // default to a gamma of 2.2 for cheap Webcams, DV cameras, etc.
        if (gamma == 0.0) {
            return 2.2;
        } else {
            return gamma;
        }
    }

    @Override
    public String getFormat() {
        if (capture == null) {
            return super.getFormat();
        } else {
            int fourcc = (int) cvGetCaptureProperty(capture, CV_CAP_PROP_FOURCC);
            return "" + (char) (fourcc & 0xFF) +
                    (char) ((fourcc >> 8) & 0xFF) +
                    (char) ((fourcc >> 16) & 0xFF) +
                    (char) ((fourcc >> 24) & 0xFF);
        }
    }

    @Override
    public int getImageWidth() {
        if (return_image != null) {
            return return_image.width();
        } else {
            return capture == null ? super.getImageWidth() : (int) cvGetCaptureProperty(capture, CV_CAP_PROP_FRAME_WIDTH);
        }
    }

    @Override
    public int getImageHeight() {
        if (return_image != null) {
            return return_image.height();
        } else {
            return capture == null ? super.getImageHeight() : (int) cvGetCaptureProperty(capture, CV_CAP_PROP_FRAME_HEIGHT);
        }
    }

    @Override
    public int getPixelFormat() {
        return capture == null ? super.getPixelFormat() : (int) cvGetCaptureProperty(capture, CV_CAP_PROP_CONVERT_RGB);
    }

    @Override
    public double getFrameRate() {
        return capture == null ? super.getFrameRate() : (int) cvGetCaptureProperty(capture, CV_CAP_PROP_FPS);
    }

    @Override
    public void setImageMode(ImageMode imageMode) {
        if (imageMode != this.imageMode) {
            return_image = null;
        }
        super.setImageMode(imageMode);
    }

    @Override
    public int getFrameNumber() {
        return capture == null ? super.getFrameNumber() :
                (int) cvGetCaptureProperty(capture, CV_CAP_PROP_POS_FRAMES);
    }

    @Override
    public void setFrameNumber(int frameNumber) throws Exception {
        if (capture == null) {
            super.setFrameNumber(frameNumber);
        } else {
            if (cvSetCaptureProperty(capture, CV_CAP_PROP_POS_FRAMES, frameNumber) == 0) {
                throw new Exception("cvSetCaptureProperty() Error: Could not set CV_CAP_PROP_POS_FRAMES to " + frameNumber + ".");
            }
        }
    }

    @Override
    public long getTimestamp() {
        return capture == null ? super.getTimestamp() :
                Math.round(cvGetCaptureProperty(capture, CV_CAP_PROP_POS_MSEC) * 1000);
    }

    @Override
    public void setTimestamp(long timestamp) throws Exception {
        if (capture == null) {
            super.setTimestamp(timestamp);
        } else {
            if (cvSetCaptureProperty(capture, CV_CAP_PROP_POS_MSEC, timestamp / 1000.0) == 0) {
                throw new Exception("cvSetCaptureProperty() Error: Could not set CV_CAP_PROP_POS_MSEC to " + timestamp / 1000.0 + ".");
            }
        }
    }

    @Override
    public int getLengthInFrames() {
        return capture == null ? super.getLengthInFrames() :
                (int) cvGetCaptureProperty(capture, CV_CAP_PROP_FRAME_COUNT);
    }

    @Override
    public long getLengthInTime() {
        return Math.round(getLengthInFrames() * 1000000L / getFrameRate());
    }

    @Override
    public double getExposure() {
        if (capture == null) {
            return super.getExposure();
        } else {
            return cvGetCaptureProperty(capture, CV_CAP_PROP_EXPOSURE);
        }
    }

    public void start() throws Exception {
        logger.info("Starting OpenCVFrameGrabber");
        if (filename != null && filename.length() > 0) {
            capture = cvCreateFileCapture(filename);
            if (capture == null) {
                throw new Exception("cvCreateFileCapture() Error: Could not create camera capture.");
            }
        } else {
            capture = cvCreateCameraCapture(deviceNumber);
            if (capture == null) {
                throw new Exception("cvCreateCameraCapture() Error: Could not create camera capture.");
            }
        }
        if (imageWidth > 0) {
            if (cvSetCaptureProperty(capture, CV_CAP_PROP_FRAME_WIDTH, imageWidth) == 0) {
                logger.warning("Could not set CV_CAP_PROP_FRAME_WIDTH");
            }
        }
        if (imageHeight > 0) {
            if (cvSetCaptureProperty(capture, CV_CAP_PROP_FRAME_HEIGHT, imageHeight) == 0) {
                logger.warning("Could not set CV_CAP_PROP_FRAME_HEIGHT");
            }
        }
        if (frameRate > 0) {
            if (cvSetCaptureProperty(capture, CV_CAP_PROP_FPS, frameRate) == 0) {
                logger.warning("Could not set CV_CAP_PROP_FPS");
            }
        }
        if (bpp > 0) {
            cvSetCaptureProperty(capture, CV_CAP_PROP_FORMAT, bpp); // ??
        }
        cvSetCaptureProperty(capture, CV_CAP_PROP_CONVERT_RGB, imageMode == ImageMode.COLOR ? 1 : 0);
        if (exposure != -1) {
            if (cvSetCaptureProperty(capture, CV_CAP_PROP_EXPOSURE, exposure) == 0) {
                logger.warning("Could not set CV_CAP_PROP_EXPOSURE");
            }
        }

        // Before cvRetrieveFrame() starts returning something else then null
        // QTKit sometimes requires some "warm-up" time for some reason...
        // The first frame on Linux is sometimes null as well,
        // so it's probably a good idea to run this for all platforms... ?
        int count = 0;
        while (count++ < 100 && cvGrabFrame(capture) != 0 && cvRetrieveFrame(capture) == null) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException ex) {
            }
        }

        if (!triggerMode) {
            int err = cvGrabFrame(capture);
            if (err == 0) {
                throw new Exception("cvGrabFrame() Error: Could not grab frame. (Has start() been called?)");
            }
        }
    }

    public void stop() throws Exception {
        if (capture != null) {
            cvReleaseCapture(capture);
            capture = null;
        }
    }

    public void trigger() throws Exception {
        for (int i = 0; i < numBuffers + 1; i++) {
            cvQueryFrame(capture);
        }
        int err = cvGrabFrame(capture);
        if (err == 0) {
            throw new Exception("cvGrabFrame() Error: Could not grab frame. (Has start() been called?)");
        }
    }

    public Frame grab() throws Exception {
        IplImage image = cvRetrieveFrame(capture);
        if (image == null) {
            throw new Exception("cvRetrieveFrame() Error: Could not retrieve frame. (Has start() been called?)");
        }
        if (!triggerMode) {
            int err = cvGrabFrame(capture);
            if (err == 0) {
                throw new Exception("cvGrabFrame() Error: Could not grab frame. (Has start() been called?)");
            }
        }

        if (imageMode == ImageMode.GRAY && image.nChannels() > 1) {
            if (return_image == null) {
                return_image = IplImage.create(image.width(), image.height(), image.depth(), 1);
            }
            cvCvtColor(image, return_image, CV_BGR2GRAY);
        } else if (imageMode == ImageMode.COLOR && image.nChannels() == 1) {
            if (return_image == null) {
                return_image = IplImage.create(image.width(), image.height(), image.depth(), 3);
            }
            cvCvtColor(image, return_image, CV_GRAY2BGR);
        } else {
            return_image = image;
        }
        return converter.convert(return_image);
    }
}
