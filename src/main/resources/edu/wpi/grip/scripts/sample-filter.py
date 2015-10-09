import edu.wpi.grip.core as grip
from java.lang import Number
import org.bytedeco.javacpp.opencv_core as core
import org.bytedeco.javacpp.opencv_imgproc as imgproc

name = "Sample filter"
description = "do some operations on an image"

inputs = [
    grip.SocketHint("in", core.Mat, core.Mat()),
    grip.SocketHint("a", Number, 16, grip.SocketHint.View.SLIDER, [1, 255]),
]

outputs = [grip.SocketHint("out", core.Mat, core.Mat())]

tmp = core.Mat()
lines = core.Mat()
lineSegmentDetector = imgproc.createLineSegmentDetector()


def perform(mat, a):
    if not mat.empty():
        imgproc.GaussianBlur(mat, tmp, core.Size(99), 16.0)
        core.absdiff(mat, tmp, tmp)
        imgproc.threshold(tmp, tmp, a, 255, imgproc.THRESH_BINARY)
        imgproc.GaussianBlur(tmp, tmp, core.Size(99), 2.0)

        return tmp
    else:
        return mat
