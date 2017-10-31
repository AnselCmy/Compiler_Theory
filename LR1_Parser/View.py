from flask import Flask, request
from flask import render_template
from Controller import *
import json
app = Flask(__name__)

@app.route('/')
def hello_world():
    return render_template('main.html', project_set=[], terminal=[], nonterminal=[],
                           analysis_table=[], analysis_stack=[],
                           grammar_string=[], grammar_name='LR0', input_string="",
                           collision=False)


@app.route('/confirm_grammar', methods=['POST'])
def confirm_grammar():
    grammar_string = request.form['grammar_string'].strip().replace('\r', '')
    grammar_name = request.form['grammar_name']
    input_string = request.form['input_string']
    print(grammar_string)
    print(grammar_name)
    print(input_string)
    controller = Controller()

    # project_set
    project_set = json.dumps(controller.get_project_set(grammar_string, grammar_name))
    # terminal
    terminal = json.dumps(controller.get_terminal(grammar_string))
    nonterminal = json.dumps(controller.get_nonterminal(grammar_string))
    # analysis_table
    analysis_table = json.dumps(controller.get_analysis_table(grammar_string, grammar_name))
    # print(analysis_table)
    collision = controller.if_collision(grammar_string, grammar_name)

    if collision == 'False':
        # analysis stack
        analysis_stack = json.dumps(controller.get_stack_table(grammar_string, grammar_name, input_string))
    else:
        analysis_stack = []
        analysis_table = []

    grammar_string = json.dumps(grammar_string.split("\n"))
    return render_template('main.html', project_set=project_set, terminal=terminal, nonterminal=nonterminal,
                           analysis_table=analysis_table, analysis_stack=analysis_stack,
                           grammar_string=grammar_string, grammar_name=grammar_name, input_string=input_string,
                           collision=collision)


if __name__ == '__main__':
    app.run()