import edu.wpi.grip.core as grip
import java.lang.Integer

name = "Add"

description = "Compute the sum of two integers"

inputs = [
    grip.SocketHint("a", java.lang.Integer, 0),
    grip.SocketHint("b", java.lang.Integer, 0),
]

outputs = [
    grip.SocketHint("sum", java.lang.Integer, 0),
]

def perform(a, b):
    return a + b
