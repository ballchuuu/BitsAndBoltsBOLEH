from collections import Counter, OrderedDict
import threading
import time
from datetime import datetime

import numpy as np
import pandas as pd
import psycopg2
from flask import Flask, json, jsonify, render_template, request
from scipy.spatial.distance import euclidean

import pgpubsub
from flask_socketio import SocketIO
from flask_sqlalchemy import SQLAlchemy
from keras.models import load_model

from sqlalchemy.sql.functions import func

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
    return render_template("index.html")

@app.route("/api/crowd_report", methods=["GET", "POST"])
def crowd_report():
    # Load from db
    people = []
    q= Location.query.all()
    for j in q:
        people.append((j.x,j.y))
    counter = OrderedDict(Counter(people))

    return jsonify({"x": [p[0] for p in counter.keys()],
                    "y": [p[1] for p in counter.keys()],
                    "count": [c*10 for c in counter.values()]})

exits = [
    (27, 27, 31, 25), # SR1 main exit
    (22, 32, 23, 30), # SR1 side exit 1
    (22, 29, 23, 27), # SR1 side exit 2
    (37, 29, 27, 40), # back of seminar rm
    (21, 25, 23, 21), # glass door ramp
    (23, 12, 30, 11) # glass doors to foyer
]
sr1 = (23, 37, 35, 26)
area = (0, 40, 40, 0)

def load_balance():

    exits = {0:0,1:0,2:0,3:0,4:0,5:0}
    curr_time = datetime.utcnow()

    # updates the table to remove users who have not connected for more than 60 seconds
    users = Location.query.filter_by(load_balance=True).all()
    for j in users:
        if (curr_time - j.time).total_seconds() > 60000:
            print((curr_time - j.time).total_seconds())
            j.load_balance = False
            db.session.commit()
    count = db.session.query(Location.route, func.count(Location.user)).filter_by(load_balance = True).group_by(Location.route).all()
    
    #returns number of people at each exit in a dictionary
    for i in count:
        exits[i[0]] = i[1]
    return (exits)

def midpt(rect):
    return ((rect[0]+rect[2])/2, (rect[1]+rect[3])/2)

def inside(rect, x, y):
    return x >= rect[0] and x <= rect[2] and y <= rect[1] and y >= rect[3]

exit_points = [midpt(e) for e in exits]
pri_exits = exit_points[:3]
sec_exits = exit_points[3:]

# Inside SR1: Compute distance from each grid to each primary exit
sr_df = pd.DataFrame(columns=['x', 'y', 'exit0', 'exit1', 'exit2'])
for x in range(sr1[0], sr1[2]):
    for y in range(sr1[3], sr1[1]):
        row = {
            'x': x,
            'y': y,
            'exit0': euclidean([x, y], list(pri_exits[0])),
            'exit1': euclidean([x, y], list(pri_exits[1])),
            'exit2': euclidean([x, y], list(pri_exits[2]))
        }
        sr_df = sr_df.append(row, ignore_index=True)

# Outside: Compute distance from each grid to each secondary exit
outside_df = pd.DataFrame(columns=['x', 'y', 'exit3', 'exit4', 'exit5'])
for x in range(area[0], area[2]):
    for y in range(area[3], area[1]):
        if not inside(sr1, x, y):
            row = {
                'x': x,
                'y': y,
                'exit3': euclidean([x, y], list(sec_exits[0])),
                'exit4': euclidean([x, y], list(sec_exits[1])),
                'exit5': euclidean([x, y], list(sec_exits[2]))
            }
            outside_df = outside_df.append(row, ignore_index=True)

def find_shortest_route(x, y):
    if inside(sr1, x, y):
        row = sr_df[sr_df.x==x][sr_df.y==y]
        distances = row.iloc[:, 2:].values.tolist()
        shortest = min(distances)

        # get count for exit count
        exit_count = load_balance()

        for i in range(len(exit_count.keys())):
            shortest = min(distances)
            best_exit = int(distances.index(shortest))
            if exit_count[best_exit] < 5:
                #insert or update the db with the x,y,route(exit)
                output = [pri_exits[best_exit]]
                output.append(sec_exits[1])
                return output
            else:
                distances = distances.remove(shortest)
    else:
        row = outside_df[outside_df.x==x][outside_df.y==y]
        distances = row.iloc[:, 2:].values.tolist()
        shortest = min(distances)
        
        exit_count = load_balance()
        for i in range(len(exit_count.keys())):
            shortest = min(distances)
            best_exit = int(distances.index(shortest))
            if exit_count[best_exit +3] < 5:
                #insert or update the db with the x,y,route(exit)
                output = [sec_exits[best_exit]]
                return output
            else:
                distances = distances.remove(shortest)
        
print(find_shortest_route(27, 27))

def is_exit(x, y):
    for e in exits:
        if inside(e, x, y):
            return True
    
    return False
    
def getCoords(jsonObj):
    headers = ['c4:12:f5:78:2c:38 NUS SC SR1 AV',
                 '28:cf:e9:83:0d:a4 NUS Comp AV',
                 '84:b8:02:10:c7:78 ',
                 '84:b8:02:10:c7:75 ',
                 '84:b8:02:10:c7:71 NUS_2-4GHz',
                 'a8:9d:21:c4:0d:51 NUS_2-4GHz',
                 'a8:9d:21:c4:0d:5b NUS_STU_2-4GHz',
                 'a8:9d:21:c4:0d:56 NUS_Guest',
                 'a8:9d:21:c4:0d:52 eduroam',
                 'FE:89:59:3D:83:F8',
                 'D2:F6:90:C9:64:64',
                 'CA:1F:86:10:E7:CD',
                 'C9:47:66:A7:69:AD',
                 'CC:EA:DB:79:AD:73',
                 'F4:C9:8D:E0:78:62',
                 'F3:3B:79:CC:CD:2E',
                 'D9:9B:DE:C0:3E:8F',
                 'a8:9d:21:f3:86:a8 ',
                 'a8:9d:21:f3:86:a1 NUS_2-4GHz',
                 'a8:9d:21:f3:86:ab NUS_STU_2-4GHz',
                 'a8:9d:21:f3:86:a6 NUS_Guest',
                 'a8:9d:21:f3:86:a2 eduroam',
                 '84:b8:02:00:3b:b1 NUS_2-4GHz',
                 '84:b8:02:00:3b:b5 ',
                 'a8:9d:21:f3:6f:f6 NUS_Guest',
                 'a8:9d:21:f3:6f:f2 eduroam',
                 'a8:9d:21:f3:6f:f1 NUS_2-4GHz',
                 '84:b8:02:00:3c:66 NUS_Guest',
                 'a8:9d:21:c4:0e:f5 ',
                 '84:b8:02:00:3c:62 eduroam',
                 '84:b8:02:00:3c:68 ',
                 '84:b8:02:00:3b:b3 ',
                 'a8:9d:21:c4:0d:55 ',
                 'a8:9d:21:c4:0d:53 ',
                 'a8:9d:21:c4:14:05 ',
                 'a8:9d:21:c4:14:01 NUS_2-4GHz',
                 'a8:9d:21:c4:14:0b NUS_STU_2-4GHz',
                 'a8:9d:21:c4:14:06 NUS_Guest',
                 'a8:9d:21:c4:14:03 ',
                 '84:b8:02:10:c7:7b NUS_STU_2-4GHz',
                 '84:b8:02:10:c7:72 eduroam',
                 '84:b8:02:00:3b:bb NUS_STU_2-4GHz',
                 '00:3a:7d:53:3d:1b NUS_STU_2-4GHz',
                 '84:b8:02:00:3b:b6 NUS_Guest',
                 '00:3a:7d:53:3d:12 eduroam',
                 'a8:9d:21:d0:54:83 ',
                 'a8:9d:21:d0:54:8b NUS_STU_2-4GHz',
                 'a8:9d:21:d0:54:86 NUS_Guest',
                 'a8:9d:21:d0:54:81 NUS_2-4GHz',
                 'a8:9d:21:d0:54:82 eduroam',
                 '84:b8:02:00:3c:61 NUS_2-4GHz',
                 '84:b8:02:00:3c:6b NUS_STU_2-4GHz',
                 'a8:9d:21:f3:6f:f5 ',
                 'a8:9d:21:f3:6f:fb NUS_STU_2-4GHz',
                 'a8:9d:21:f3:84:9b NUS_STU_2-4GHz',
                 'a8:9d:21:c4:14:08 ',
                 '84:b8:02:10:c7:76 NUS_Guest',
                 'a8:9d:21:d0:54:85 ',
                 'a8:9d:21:d0:2d:bb NUS_STU_2-4GHz',
                 'a8:9d:21:f3:84:91 NUS_2-4GHz',
                 'a8:9d:21:f3:84:96 NUS_Guest',
                 'a8:9d:21:f3:84:92 eduroam',
                 '84:b8:02:00:3c:63 ',
                 'a8:9d:21:d0:2d:b5 ',
                 '84:b8:02:00:3c:65 ',
                 'a8:9d:21:d0:2d:b3 ',
                 'a8:9d:21:d0:2d:b2 eduroam',
                 'a8:9d:21:d0:2d:b6 NUS_Guest',
                 'a8:9d:21:d0:2d:b1 NUS_2-4GHz',
                 'a8:9d:21:f3:84:98 ',
                 'a8:9d:21:c4:0d:58 ',
                 'a8:9d:21:d0:54:88 ',
                 '84:b8:02:10:c7:73 ',
                 'a8:9d:21:f3:84:93 ',
                 'a8:9d:21:f3:6f:f8 ',
                 'a8:9d:21:ee:0e:d1 NUS_2-4GHz',
                 'a8:9d:21:ee:0e:db NUS_STU_2-4GHz',
                 'a8:9d:21:ee:0e:d6 NUS_Guest',
                 'a8:9d:21:ee:0e:d2 eduroam',
                 'a8:9d:21:44:04:16 NUS_Guest',
                 'a8:9d:21:74:09:75 ',
                 'a8:9d:21:74:09:76 NUS_Guest',
                 'a8:9d:21:74:09:72 eduroam',
                 'a8:9d:21:74:09:7b NUS_STU_2-4GHz',
                 'a8:9d:21:74:09:71 NUS_2-4GHz',
                 'a8:9d:21:f3:91:33 ',
                 'a8:9d:21:f3:91:31 NUS_2-4GHz',
                 'a8:9d:21:f3:91:3b NUS_STU_2-4GHz',
                 'a8:9d:21:f3:91:32 eduroam',
                 'a8:9d:21:44:04:12 eduroam',
                 'a8:9d:21:44:04:11 NUS_2-4GHz',
                 'a8:9d:21:f3:86:a5 ',
                 'a8:9d:21:c4:0e:f1 NUS_2-4GHz',
                 'a8:9d:21:c4:0e:f3 ',
                 'a8:9d:21:c4:0e:f6 NUS_Guest',
                 'a8:9d:21:c4:0e:f2 eduroam',
                 'a8:9d:21:74:09:73 ',
                 'a8:9d:21:f3:6f:b8 ',
                 'a8:9d:21:c4:0e:fb NUS_STU_2-4GHz',
                 'a8:9d:21:f3:6f:f3 ',
                 'a8:9d:21:ee:0c:68 ',
                 'a8:9d:21:f3:6f:b6 NUS_Guest',
                 'a8:9d:21:f3:6f:b2 eduroam',
                 '84:b8:02:00:3b:b2 eduroam',
                 'a8:9d:21:ee:0c:61 NUS_2-4GHz',
                 'a8:9d:21:ee:0c:65 ',
                 'a8:9d:21:be:c4:c1 NUS_2-4GHz',
                 'a8:9d:21:f3:6f:b5 ',
                 'a8:9d:21:f3:6f:b3 ',
                 'a8:9d:21:f3:6f:b1 NUS_2-4GHz',
                 'a8:9d:21:c4:14:02 eduroam',
                 'a8:9d:21:44:05:ab NUS_STU_2-4GHz',
                 'a8:9d:21:44:05:a6 NUS_Guest',
                 'a8:9d:21:44:05:a1 NUS_2-4GHz',
                 '84:b8:02:10:cf:eb NUS_STU_2-4GHz',
                 '84:b8:02:10:cf:e1 NUS_2-4GHz',
                 'b0:aa:77:ad:be:a2 eduroam',
                 'b0:aa:77:42:e3:c6 NUS_Guest',
                 'b0:aa:77:42:e3:c1 NUS_2-4GHz',
                 'a8:9d:21:ee:0c:6b NUS_STU_2-4GHz',
                 'a8:9d:21:ee:0c:66 NUS_Guest',
                 'a8:9d:21:ee:0c:62 eduroam',
                 '84:b8:02:00:3a:18 ',
                 '84:b8:02:00:3a:13 ',
                 '84:b8:02:00:3a:11 NUS_2-4GHz',
                 '84:b8:02:00:3a:1b NUS_STU_2-4GHz',
                 '84:b8:02:00:3a:16 NUS_Guest',
                 '84:b8:02:00:3a:12 eduroam',
                 '78:72:5d:d0:ba:13 ',
                 '78:72:5d:d0:ba:11 NUS_2-4GHz',
                 '78:72:5d:d0:ba:1b NUS_STU_2-4GHz',
                 '84:b8:02:00:3a:15 ',
                 '78:72:5d:d0:ba:16 NUS_Guest',
                 'b0:aa:77:ad:be:a1 NUS_2-4GHz',
                 'b0:aa:77:42:e3:c5 ',
                 'b0:aa:77:42:e3:c3 ',
                 'b0:aa:77:42:e3:cb NUS_STU_2-4GHz',
                 'a8:9d:21:ee:0c:63 ',
                 'a8:9d:21:f3:87:d1 NUS_2-4GHz',
                 'a8:9d:21:f3:87:db NUS_STU_2-4GHz',
                 'a8:9d:21:f3:87:d8 ',
                 'a8:9d:21:f3:87:d6 NUS_Guest',
                 'a8:9d:21:f3:87:d2 eduroam',
                 '00:3a:7d:53:3d:11 NUS_2-4GHz',
                 'a8:9d:21:f3:86:a3 ',
                 'a8:9d:21:f3:6f:bb NUS_STU_2-4GHz',
                 'a8:9d:21:0f:7e:81 NUS_2-4GHz',
                 'a8:9d:21:0f:7e:8b NUS_STU_2-4GHz',
                 'a8:9d:21:0f:7e:82 eduroam',
                 'a4:6c:2a:19:90:db NUS_STU_2-4GHz',
                 'a4:6c:2a:19:90:d6 NUS_Guest',
                 '78:72:5d:d0:ba:18 ',
                 '00:3a:7d:47:e1:3b NUS_STU_2-4GHz',
                 '84:b8:02:00:3b:b8 ',
                 '00:3a:7d:47:e1:32 eduroam',
                 'a8:9d:21:74:0d:91 NUS_2-4GHz',
                 'a8:9d:21:74:0d:9b NUS_STU_2-4GHz',
                 '00:3a:7d:47:e1:31 NUS_2-4GHz',
                 '00:3a:7d:47:e1:36 NUS_Guest',
                 'a8:9d:21:74:0d:95 ',
                 'a8:9d:21:74:0d:93 ',
                 'a8:9d:21:74:0d:96 NUS_Guest',
                 'a8:9d:21:74:0d:92 eduroam',
                 'a4:6c:2a:19:90:d8 ',
                 'a4:6c:2a:19:90:d1 NUS_2-4GHz',
                 'a4:6c:2a:19:90:d2 eduroam',
                 'a8:9d:21:f3:87:d5 ',
                 '62:a2:48:b0:f3:19 ',
                 'a8:9d:21:be:c2:71 NUS_2-4GHz',
                 'a8:9d:21:be:c2:7b NUS_STU_2-4GHz',
                 '00:3a:7d:53:3a:72 eduroam',
                 '00:3a:7d:53:3a:78 ',
                 'a8:9d:21:f3:84:95 ',
                 'a8:9d:21:74:09:78 ',
                 'a8:9d:21:be:c2:76 NUS_Guest',
                 'a8:9d:21:c4:0e:f8 ',
                 'a8:9d:21:f3:87:d3 ',
                 'a8:9d:21:44:04:15 ']
    model = load_model('my_model.h5')
    input = []
    for s in headers:
        if s in jsonObj:
            input.append(int(jsonObj[str(s)]) + 100)
        else:
            input.append(0)
    coords = model.predict(np.array([input]))
    return coords[0]
    
@app.route("/getloc", methods=["POST"])
def getloc():
    jsonObj = json.loads(request.data)
    coords = getCoords(jsonObj)
    loc = Location.query.filter_by(user=jsonObj['UUID']).all()
    if len(loc) == 0:
        new_loc = Location(user=jsonObj['UUID'], x=int(coords[0]), y=int(coords[1]))
        db.session_add(new_loc)
    else:
        loc[0].x=int(coords[0])
        loc[0].y=int(coords[1])
    db.session.commit()
    output = {'x': str(coords[0]), 'y': str(coords[1])}
    output['route'] = find_shortest_route(int(coords[0]), int(coords[1]))
    return json.dumps(output)
    
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
                socketio.emit('updates', json.dumps(temp), namespace='/socket')
            time.sleep(10)
            
    thread1 = threading.Thread(target=run_job)
    thread1.start()