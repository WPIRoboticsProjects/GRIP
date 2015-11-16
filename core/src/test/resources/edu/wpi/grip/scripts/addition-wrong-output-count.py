import edu.wpi.grip.core as grip
import java.lang.Integer

inputs = [
    grip.SocketHint("a", java.lang.Integer, 0),
    grip.SocketHint("b", java.lang.Integer, 0),
]

outputs = [
    grip.SocketHint("sum", java.lang.Integer, 0),
]


def perform(a, b):
    return a + b, 3
