from flask import Flask, render_template
from flask_socketio import SocketIO

app = Flask(__name__)
app.config['SECRET_KEY'] = 'secret!'
socketio = SocketIO(app)


@app.route("/")
def hello_world():
    return render_template("iot/index.html")
    
@app.route("/sysmon")
def sysmon():
    return render_template("iot/sysmon.html")


if __name__ == '__main__':
    socketio.run(app)