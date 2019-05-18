from flask import Flask, jsonify, request, json
from flask_socketio import SocketIO
from flask_sqlalchemy import SQLAlchemy
from datetime import datetime 
import pgpubsub
import threaded
import datetime
import time 


app = Flask(__name__)
app.debug = True #to set in staging development
app.config['SQLALCHEMY_DATABASE_URI'] = 'postgresql://bitsbolts:bitsbolts@localhost:5432/bitsbolts'
app.config['DB_HOST'] = 'localhost:5432'
app.config['DB_USERNAME'] = 'bitsbolts'
app.config['DB_PASSWORD'] = 'bitsbolts'
app.config['DB_NAME'] = 'bitsbolts'
app.config['SQLALCHEMY_TRACK_MODIFICATIONS'] = False
db = SQLAlchemy(app)
socketio = SocketIO(app)

from models import Location

@app.route("/")
def hello_world():
    return "hello"


@app.route("/testing")
def activate_job():
    def run_job():
        pubsub = pgpubsub.connect(host=app.config['DB_HOST'], user=app.config['DB_USERNAME'],password=app.config['DB_PASSWORD'], dbname=app.config['DB_NAME'])
        pubsub.listen('locations_update')
        while True:
            for e in pubsub.events():
                temp = json.loads(e.payload)
                temp['type'] = 'apt'
                socketio.emit('updates', json.dumps(temp), namespace='/socket')
            time.sleep(10)
    thread1 = threading.Thread(target=run_job)
    thread1.start()
