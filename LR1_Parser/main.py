from flask import Flask
from flask import render_template
app = Flask(__name__)

@app.route('/')
def hello_world():
	test = [1, 2, 3, 4]
	return render_template('main.html', test=test)

if __name__ == '__main__':
    app.run()