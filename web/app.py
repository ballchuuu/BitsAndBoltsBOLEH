from flask import Flask, jsonify, request, json
from flask_sqlalchemy import SQLAlchemy
from datetime import datetime 
import datetime


app = Flask(__name__)
app.debug = True #to set in staging development
app.config['SQLALCHEMY_DATABASE_URI'] = 'postgresql://bitsbolts:password@localhost:5432/bitsbolts'
app.config['SQLALCHEMY_TRACK_MODIFICATIONS'] = False
db = SQLAlchemy(app)

from models import Location

@app.route("/")
def hello_world():
    return "hello"
