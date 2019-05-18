from app import db
import datetime

class Location(db.Model):
	__tablename__ = 'location'
		
	user = db.Column(db.String(500), nullable=False, primary_key=True)
	x = db.Column(db.Float, nullable=False)
	y = db.Column(db.Float, nullable=False)
	time = db.Column(db.DateTime, default=datetime.datetime.utcnow, onupdate=datetime.datetime.utcnow)
	route = db.Column(db.Float, nullable=True)
	load_balance = db.Column(db.Boolean, nullable=True)
	
	def __init__(self, user, x,y,route):
		self.user = user
		self.x = x
		self.y = y
		self.route = route
		self.load_balance = False

	def __repr__(self):
		return 'x: {}, y:{}'.format(self.x, self.y)

	def serialize(self):
		result = {
		'user': self.user,
		'x':self.x,
		'y': self.y,
		'time': self.time,
		}
		return result 
		