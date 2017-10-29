from SLR import *

test_input_string3 = "E->S\nB->aB\nS->BB\nB->b"


class LR1Grammar(SLRGrammar):
    def __init__(self, grammar_string):
        super(LR1Grammar, self).__init__(grammar_string)

    def gen_CLOSURE(self, produce_string):
        """Give one produce string, return the CLOSURE of this produce"""
        # here the produce_string is list with the forward search string
        forward_idx = produce_string.split(",")[1]
        produce_string_idx = produce_string.split(",")[0]
        temp_CLOSURE = []
        # insert itself into this CLOSURE
        temp_CLOSURE.append(produce_string_idx+','+forward_idx)
        old_len = 0
        new_len = 1
        while old_len != new_len:
            # iter in CLOSURE
            old_len = len(temp_CLOSURE)
            for p in temp_CLOSURE:
                produce = Produce(p.split(',')[0])
                forward = p.split(',')[1]
                if not produce.after_point() is None and self.is_nonterminal(produce.after_point()):
                    for r in self.get_first_projects(produce.after_point()):
                        new_produce = produce.after_point()+"->"+r
                        new_forward_list = self.get_FIRST_of_string(produce.right[0][produce.right[0].index(".")+2:]+forward)
                        for new_forward in new_forward_list:
                            if new_produce+","+new_forward not in temp_CLOSURE:
                                temp_CLOSURE.append(new_produce+","+new_forward)
            new_len = len(temp_CLOSURE)
        self.CLOSURE[produce_string_idx+","+forward_idx] = temp_CLOSURE
        return temp_CLOSURE


if __name__ == "__main__":
    grammar = LR1Grammar(test_input_string3)
    grammar.gen_CLOSURE('S->B.B,#')
    print(grammar.CLOSURE['S->B.B,#'])
    # for num, project_set in enumerate(grammar.project_set):
    #     print("{}: {}".format(num, project_set))
    # grammar.gen_FIRST()
    # print(grammar.FIRST)
    # print(grammar.produce_list)
