import edu.wpi.grip.core as grip
import java.lang.Integer

inputs = [
    grip.SocketHint("a", java.lang.Integer, grip.SocketHint.View.NONE, None, 0),
    grip.SocketHint("b", java.lang.Integer, grip.SocketHint.View.NONE, None, 0),
]

outputs = [
    grip.SocketHint("sum", java.lang.Integer),
]

def perform(a, b):
    return "I am not an Integer"
