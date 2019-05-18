from flask import Flask, jsonify, request, json, render_template
from flask_socketio import SocketIO
from flask_sqlalchemy import SQLAlchemy
from datetime import datetime 
import psycopg2
import pgpubsub
import threading
import datetime
import time 


from scipy.spatial.distance import euclidean
import numpy as np
import pandas as pd

app = Flask(__name__)
app.debug = True #to set in staging development
app.config['SQLALCHEMY_DATABASE_URI'] = 'postgresql://bitsbolts:bitsbolts@localhost:5432/bitsbolts'
app.config['DB_HOST'] = '127.0.0.1'
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

exits = [
    (27, 27, 25, 31), # SR1 main exit
    (22, 32, 23, 30), # SR1 side exit 1
    (22, 29, 23, 27), # SR1 side exit 2
    (37, 29, 27, 40), # back of seminar rm
    (21, 25, 23, 21), # glass door ramp
    (23, 12, 30, 11) # glass doors to foyer
]
sr1 = (23, 37, 35, 26)

def midpt(rect):
    return ((rect[0]+rect[2])/2, (rect[1]+rect[3])/2)

exit_points = [midpt(e) for e in exits]
pri_exits = exit_points[:3]
sec_exits = exit_points[3:]

# Inside SR1: Compute distance from each grid to each primary exit
sr_df = pd.DataFrame(columns=['x', 'y', 'exit0', 'exit1', 'exit2'])

for x in range(sr1[0], sr1[2]):
    for y in range(sr1[3], sr1[1]):
        distances = [euclidean([x, y], list(p)) for p in pri_exits]
        row = {
            'x': x,
            'y': y,
            'exit0': euclidean([x, y], list(pri_exits[0])),
            'exit1': euclidean([x, y], list(pri_exits[1])),
            'exit2': euclidean([x, y], list(pri_exits[2]))
        }
        sr_df = sr_df.append(row, ignore_index=True)

def inside(rect, x, y):
    return x >= rect[0] and x <= rect[2] and y <= rect[1] and y >= rect[3]

def connect(start, ends):
    return [[start, (p[0], p[1])] for p in ends]

def find_shortest_route(x, y):
    if inside(sr1, x, y):
        row = sr_df[sr_df.x==x][sr_df.y==y]
        distances = row.iloc[:, 2:].values.tolist()
        shortest = min(distances)
        return "exit" + str(distances.index(shortest))
    else:
        # From primary to secondary exits
        return connect(pri_exits[0], sec_exits)

print(find_shortest_route(27, 27))

def is_exit(x, y):
    for e in exits:
        if inside(e, x, y):
            return True
    
    return False

@app.before_first_request
def activate_job():
    def run_job():
        pubsub = pgpubsub.connect(host=app.config['DB_HOST'], user=app.config['DB_USERNAME'],password=app.config['DB_PASSWORD'], dbname=app.config['DB_NAME'])
        pubsub.listen('location_update')
        while True:
            for e in pubsub.events():
                temp = json.loads(e.payload)
                print(temp)
                # temp['type'] = 'apt'
                # socketio.emit('updates', json.dumps(temp), namespace='/socket')
            time.sleep(10)
            
    thread1 = threading.Thread(target=run_job)
    thread1.start()
