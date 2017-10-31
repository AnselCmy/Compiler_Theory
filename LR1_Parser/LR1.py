from SLR import *


class LR1Grammar(SLRGrammar):
    def __init__(self, grammar_string):
        super(LR1Grammar, self).__init__(grammar_string)
        # self.gen_project_set()
        # self.gen_analysis_table()

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

    def gen_GO(self, set_num, character):
        """By giving the project set and character, compute the next project set"""
        curr_project_set = self.project_set[set_num]
        temp_CLOSURE = []
        next_project_set = []
        for produce_string in curr_project_set:
            if Produce(produce_string).after_point() == character:
                shift_string = Produce(produce_string).get_shift_point_produce().produce_string
                shift_string += "," + produce_string.split(",")[1]
                if not self.CLOSURE[shift_string]:
                    self.gen_CLOSURE(shift_string)
                next_project_set += self.CLOSURE[shift_string]
        next_project_set = list(set(next_project_set))
        return next_project_set

    def gen_project_set(self):
        """The main function of generating the project set"""
        # add the first project set
        start_produce = self.start_char + "->." + self.grammar_dict[self.start_char][0] + ",#"
        self.project_set.append(self.gen_CLOSURE(start_produce))
        # generate project set
        curr_set_num = 0
        alloc_set_num = 1
        while True:
            if curr_set_num >= len(self.project_set):
                break
            next_char_list = self.get_project_set_next_char(curr_set_num)
            for character in next_char_list:
                next_project_set = self.gen_GO(curr_set_num, character)
                # if exist this project set
                if self.is_project_set_exist(next_project_set):
                    next_set_num = self.get_exist_project_set_num(next_project_set)
                    self.GO[curr_set_num][character] = next_set_num
                else:
                    self.project_set.append(next_project_set)
                    self.GO[curr_set_num][character] = alloc_set_num
                    alloc_set_num += 1
            curr_set_num += 1
            # print(self.project_set)

    def gen_analysis_table(self):
        # ACTION
        for set_num, project_set in enumerate(self.project_set):
            Ik = set_num
            for produce_string in project_set:
                forward = produce_string.split(",")[1]
                produce_string = produce_string.split(",")[0]
                # (1)
                if produce_string.index(".") != len(produce_string)-1:
                    a = Produce(produce_string).after_point()
                    if self.is_terminal(a):
                        Ij = self.GO[Ik][a]
                        if self.ACTION[Ik][a] is None \
                                or (not self.ACTION[Ik][a] is None and self.ACTION[Ik][a] == "s"+str(Ij)):
                            self.ACTION[Ik][a] = "s"+str(Ij)
                        else:
                            print(Ik, a)
                            raise ValueError("Meet Collision")
                # (2)
                if produce_string.index(".") == len(produce_string)-1 \
                        and Produce(produce_string).replace_point() != self.start_produce:
                    Ij = self.get_produce_num(Produce(produce_string).replace_point())
                    if self.ACTION[Ik][forward] is None:
                        self.ACTION[Ik][forward] = "r"+str(Ij)
                    else:
                        raise ValueError("Meet Collision")
                # (3)
                if produce_string == self.start_produce + ".":
                    if self.ACTION[Ik]["#"] is None:
                        self.ACTION[Ik]["#"] = "acc"
                    else:
                        raise ValueError("Meet Collision")
        # GOTO
        for Ik, val in self.GO.items():
            for A, Ij in val.items():
                if self.is_nonterminal(A):
                    self.GOTO[Ik][A] = Ij


# if __name__ == "__main__":
    # grammar = LR1Grammar(test_input_string3)
    # grammar.gen_CLOSURE('S->.E,#')
    # print(grammar.CLOSURE['S->.E,#'])
    # grammar.project_set.append(grammar.CLOSURE['S->.E,#'])
    # print(grammar.get_project_set_next_char(0))
    # print(grammar.gen_GO(0, "a"))
    # grammar.gen_project_set()
    # for num, project_set in enumerate(grammar.project_set):
    #     print("{}: {}".format(num, project_set))
    # grammar.gen_analysis_table()
    # for key, val in grammar.ACTION.items():
    #     print("{}: {}".format(key, val))
    # for key, val in grammar.GOTO.items():
    #     print("{}: {}".format(key, val))
    # grammar.gen_FIRST()
    # print(grammar.FIRST)
    # print(grammar.produce_list)
