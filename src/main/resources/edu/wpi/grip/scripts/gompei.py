import edu.wpi.grip.core as grip
from java.lang import String
from org.bytedeco.javacpp.opencv_core import *
from org.bytedeco.javacpp.opencv_imgcodecs import *

name = "Gompei"
description = "Return a picture of Gompei for testing purposes during development"


def loadImage(filename):
    url = grip.Operation.getResource("/edu/wpi/grip/images/" + filename)
    return imread(url.getPath())


images = {
    "Gompei Statue": loadImage("gompei.jpeg"),
    "Winter Gompei": loadImage("winter-gompei.jpeg"),
    "Fall Gompei": loadImage("fall-gompei.jpeg"),
}

inputs = [grip.SocketHint("Name", String, images.keys()[0], grip.SocketHint.View.SELECT, images.keys())]
outputs = [grip.SocketHint("Image", Mat, Mat.__init__)]


def perform(name):
    return images[name]
