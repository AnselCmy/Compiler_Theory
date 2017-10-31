from flask import Flask, request
from flask import render_template
from Controller import *
import json
app = Flask(__name__)


@app.route('/')
def hello_world():
    return render_template('main.html', project_set=[], terminal=[], nonterminal=[],
                           analysis_table=[])


@app.route('/confirm_grammar', methods=['POST'])
def confirm_grammar():
    grammar_string = request.form['grammar_string'].strip().replace('\r', '')
    # print(grammar_string)
    grammar_name = request.form['grammar_name']
    controller = Controller()
    # project_set
    project_set = json.dumps(controller.get_project_set(grammar_string, grammar_name))
    # GO = json.dumps(controller.get_GO(grammar_string, grammar_name))
    # print(controller.get_GO(grammar_string, grammar_name))
    # print(GO)
    # terminal
    terminal = json.dumps(controller.get_terminal(grammar_string))
    nonterminal = json.dumps(controller.get_nonterminal(grammar_string))
    # analysis_table
    analysis_table = json.dumps(controller.get_analysis_table(grammar_string, grammar_name))
    print(project_set)
    print(analysis_table)
    return render_template('main.html', project_set=project_set, terminal=terminal, nonterminal=nonterminal,
                           analysis_table=analysis_table)

if __name__ == '__main__':
    app.run()