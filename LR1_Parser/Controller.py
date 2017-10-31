from LR1 import *

test_input_string0 = "S->E\nE->aA|bB\nA->cA|d\nB->cB|d"
test_input_string1 = "S->E\nE->E+T\nE->T\nT->T*F\nT->F\nF->(E)\nF->i"
test_input_string2 = "E->TA\nA->+TA|$\nT->FB\nB->*FB|$\nF->(E)|i"
test_input_string3 = "E->S\nB->aB\nS->BB\nB->b"


class Controller(object):
    def __init__(self):
        super(Controller, self).__init__()

    def get_project_set(self, grammar_string, grammar_name):
        if grammar_name in ['LR0', 'SLR']:
            grammar = LR0Grammar(grammar_string)
        elif grammar_name == 'LR1':
            grammar = LR1Grammar(grammar_string)
        grammar.gen_project_set()
        return grammar.project_set

    def get_GO(self, grammar_string, grammar_name):
        if grammar_name in ['LR0', 'SLR']:
            grammar = LR0Grammar(grammar_string)
        elif grammar_name == 'LR1':
            grammar = LR1Grammar(grammar_string)
        grammar.gen_project_set()
        return grammar.GO

    def get_terminal(self, grammar_string):
        grammar = LR0Grammar(grammar_string)
        return grammar.terminal

    def get_nonterminal(self, grammar_string):
        grammar = LR0Grammar(grammar_string)
        return grammar.nonterminal

    def get_analysis_table(self, grammar_string, grammar_name):
        if grammar_name == 'LR0':
            grammar = LR0Grammar(grammar_string)
        elif grammar_name == 'SLR':
            grammar = SLRGrammar(grammar_string)
        elif grammar_name == 'LR1':
            grammar = LR1Grammar(grammar_string)
        grammar.gen_project_set()
        grammar.gen_analysis_table()
        # rebuild the format
        table_head = grammar.terminal + ['#'] + grammar.nonterminal
        # print(grammar.terminal)
        # print(grammar.nonterminal)
        analysis_table = [[' ' for _ in table_head] for _ in range(len(grammar.project_set))]
        for key, val in grammar.ACTION.items():
            for character, state in val.items():
                analysis_table[key][table_head.index(character)] = state
        for key, val in grammar.GOTO.items():
            for character, state in val.items():
                analysis_table[key][table_head.index(character)] = str(state)
        # print("controller:", grammar.terminal)
        # print("controller: ", table_head)
        # print("controller: ", analysis_table)
        return analysis_table

    def get_stack_table(self, grammar_string, grammar_name, input_string):
        if grammar_name == 'LR0':
            grammar = LR0Grammar(grammar_string)
        elif grammar_name == 'SLR':
            grammar = SLRGrammar(grammar_string)
        elif grammar_name == 'LR1':
            grammar = LR1Grammar(grammar_string)
        grammar.gen_project_set()
        grammar.gen_analysis_table()
        grammar.run(input_string)
        stack_table = grammar.stack_table
        for entry in stack_table:
            temp_str = ""
            for num in entry[0]:
                temp_str += str(num) + ' '
            entry[0] = temp_str.strip()
        return stack_table

    def if_collision(self, grammar_string, grammar_name):
        if grammar_name == 'LR0':
            grammar = LR0Grammar(grammar_string)
        elif grammar_name == 'SLR':
            grammar = SLRGrammar(grammar_string)
        elif grammar_name == 'LR1':
            grammar = LR1Grammar(grammar_string)
        grammar.gen_project_set()
        grammar.gen_analysis_table()
        return str(grammar.if_meet_collison)


if __name__ == "__main__":
    controller = Controller()
    # for num, project_set in enumerate(controller.get_project_set(test_input_string0, 'LR0')):
    #     print(num, project_set)
    # for key, val in controller.get_analysis_table(test_input_string1, 'LR0')[0].items():
    #     print(key, val)
    print(controller.get_stack_table(test_input_string1, 'SLR', "i*i+i"))