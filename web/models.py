from app import db
import datetime

class Location(db.Model):
	__tablename__ = 'location'
		
	user = db.Column(db.String(500), nullable=False, primary_key=True)
	x = db.Column(db.Float, nullable=False)
	y = db.Column(db.Float, nullable=False)
	time = db.Column(db.DateTime, nullable=False)
	
	def __init__(self, user, x,y,time):
		self.user = user
		self.x = x
		self.y = y
		self.time = 

	# def __repr__(self):
	# 	return 'Name: {}, NRIC_no: {}'.format(self.name, self.nric_no)

	# def serialize(self):
	# 	result = {
	# 	'NRIC_no': self.nric_no,
	# 	'name':self.name,
	# 	'year_of_birth': self.year_of_birth,
	# 	'gender': self.gender,
	# 	'mobile_no':self.mobile_no,
	# 	'weight': self.weight,
	# 	'height': self.height,
	# 	'tracker_type': self.tracker_type,
	# 	'date_joined': self.date_joined, 
	# 	'total_healthpoints': self.total_healthpoints
	# 	}
	# 	return result 
		