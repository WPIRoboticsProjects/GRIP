import edu.wpi.grip.core.sockets as grip

inputs = [
    grip.SocketHints.createNumberSocketHint("a", 0.0),
    grip.SocketHints.createNumberSocketHint("b", 0.0)
]

outputs = [
    grip.SocketHints.Outputs.createNumberSocketHint("sum", 0.0),
]


def perform(a, b):
    return "I am not an Integer"
