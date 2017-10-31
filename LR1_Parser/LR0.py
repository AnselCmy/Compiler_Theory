from collections import defaultdict

epsilon = '$'


class Produce:
    """The presentation of a produce

    Attributes:
        produce_list: A list to store the produce after being split, like [['E', ['aB', 'bB']]].
        left: the left of the produce like 'E'.
        right: the list of right, like ['aB', 'bB'].
    """
    def __init__(self, produce_string):
        self.produce_list = []
        self.produce_string = produce_string
        self.left = ""
        self.right = []
        self.parse(produce_string)

    def parse(self, produce_string):
        input_list = produce_string.split("\n")
        for produce in input_list:
            produce = produce.split("->")
            self.left = produce[0]
            # self.right = produce[1].split("|")
            self.right = produce[1].split(",")[0].split("|")
            produce = [self.left, self.right]
            self.produce_list.append(produce)

    def after_point(self):
        """Get the character after the point"""
        pos = self.right[0].index('.') + 1
        # if the point is at the end
        if pos >= len(self.right[0]):
            return None
        else:
            return self.right[0][pos]

    def get_shift_point_produce(self):
        point_pos = self.right[0].index(".")
        temp_right = self.right[0][:point_pos] + self.right[0][point_pos+1:]
        temp_right = temp_right[:point_pos+1] + "." + temp_right[point_pos+1:]
        return Produce(self.left + "->" + temp_right)

    def replace_point(self):
        if self.right[0] == ".":
            return self.left+"->"+epsilon
        else:
            return self.produce_string.replace(".", "")


class LR0Grammar:
    """The presentation of one grammar

    Attributes:
        grammar_dict: the dict of all the produce which has left as key, right as the value,
                        like {'S': ['E'], 'B': ['cB', 'd']}.
        project_dict: the dict of all the project, like {'S': ['.E', 'E.'], 'B': ['.cB', 'c.B', 'cB.', '.d', 'd.']}
        CLOSURE: store the CLOSURE of the computed CLOSURE of one produce string.
        GO: the GO function.
        project_set: the project set of this grammar, like [ ['S->.E', 'E->.aA', 'E->.bB'], ['S->E.'] ]
    """
    def __init__(self, grammar_string):
        # variable
        self.start_char = "S"
        self.start_produce = "S->E"
        self.terminal = []
        self.nonterminal = []
        self.produce_list = []
        self.grammar_dict = defaultdict(list)
        self.project_dict = defaultdict(list)
        self.CLOSURE = defaultdict(list)
        self.GO = defaultdict(lambda: defaultdict(int))
        self.project_set = []
        self.ACTION = defaultdict(lambda: defaultdict(lambda: None))
        self.GOTO = defaultdict(lambda: defaultdict(lambda: None))
        self.stack_table = []
        # method
        self.parse(grammar_string)
        self.gen_project()
        # self.gen_project_set()
        # self.gen_analysis_table()

    def parse(self, input_string):
        """Parse the grammar string into the grammar_dict"""
        self.grammar_dict = defaultdict(list)
        self.produce_list = []
        self.nonterminal = []
        self.terminal = []
        start_flag = True
        for produce_string in input_string.split("\n"):
            produce = Produce(produce_string)
            # get start_char
            if start_flag:
                self.start_char = produce.left
                start_flag = False
                # get start produce
                self.start_produce = self.start_char + "->" + produce.right[0]
            # build grammar_dict
            self.grammar_dict[produce.left] += produce.right
            # build produce_list
            for right in produce.right:
                self.produce_list.append(produce.left+"->"+right)
            # get terminal and nonterminal
            if self.is_nonterminal(produce.left) and produce.left not in self.nonterminal:
                self.nonterminal.append(produce.left)
            for right in produce.right:
                for character in right:
                    if self.is_terminal(character) and character not in self.terminal:
                        self.terminal.append(character)
                    elif self.is_nonterminal(character) and character not in self.nonterminal:
                        self.nonterminal.append(character)

    def gen_project(self):
        """Generate the project_dict"""
        for left, right in self.grammar_dict.items():
            for one_right in right:
                if one_right == epsilon:
                    self.project_dict[left] = '.'
                else:
                    for pos in range(len(one_right)+1):
                        self.project_dict[left].append(one_right[:pos]+'.'+one_right[pos:])

    def gen_CLOSURE(self, produce_string):
        """Give one produce string, return the CLOSURE of this produce"""
        temp_CLOSURE = []
        # insert itself into this CLOSURE
        temp_CLOSURE.append(produce_string)
        old_len = 0
        new_len = 1
        while old_len != new_len:
            # iter in CLOSURE
            old_len = len(temp_CLOSURE)
            for p in temp_CLOSURE:
                p = Produce(p)
                if not p.after_point() is None and self.is_nonterminal(p.after_point()):
                    for r in self.get_first_projects(p.after_point()):
                        new_produce = p.after_point()+"->"+r
                        if new_produce not in temp_CLOSURE:
                            temp_CLOSURE.append(p.after_point()+"->"+r)
            new_len = len(temp_CLOSURE)
        self.CLOSURE[produce_string] = temp_CLOSURE
        return temp_CLOSURE

    def get_first_projects(self, nonterminal):
        """Get the project with the left of nonterminal which has the point at first position."""
        rst = []
        for p in self.project_dict[nonterminal]:
            if p.index('.') == 0:
                rst.append(p)
        return rst

    def gen_GO(self, set_num, character):
        """By giving the project set and character, compute the next project set"""
        curr_project_set = self.project_set[set_num]
        temp_CLOSURE = []
        next_project_set = []
        for produce_string in curr_project_set:
            if Produce(produce_string).after_point() == character:
                shift_string = Produce(produce_string).get_shift_point_produce().produce_string
                if not self.CLOSURE[shift_string]:
                    self.gen_CLOSURE(shift_string)
                next_project_set += self.CLOSURE[shift_string]
        next_project_set = list(set(next_project_set))
        return next_project_set

    def get_project_set_next_char(self, set_num):
        """Get the character list of the out character of this project set"""
        curr_project_set = self.project_set[set_num]
        next_char = []
        for produce_string in curr_project_set:
            after_point = Produce(produce_string).after_point()
            if not after_point is None:
                next_char.append(after_point)
        next_char_index = next_char.index
        next_char = list(set(next_char))
        next_char.sort(key=next_char_index)
        return next_char

    def is_project_set_exist(self, test_project_set):
        """To detect this project set if exist"""
        rst = False
        # iter in all the project set
        for project_set in self.project_set:
            if set(project_set) == set(test_project_set):
                rst = True
                break
        return rst

    def get_exist_project_set_num(self, test_project_set):
        """If this project exist, get the existed set num"""
        for set_num, project_set in enumerate(self.project_set):
            if set(project_set) == set(test_project_set):
                return set_num

    def gen_project_set(self):
        """The main function of generating the project set"""
        # add the first project set
        start_produce = self.start_char + "->." + self.grammar_dict[self.start_char][0]
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

    def get_produce_num(self, produce_string):
        return self.produce_list.index(produce_string)

    def gen_analysis_table(self):
        # ACTION
        for set_num, project_set in enumerate(self.project_set):
            Ik = set_num
            for produce_string in project_set:
                # (1)
                if produce_string.index(".") != len(produce_string)-1:
                    a = Produce(produce_string).after_point()
                    if self.is_terminal(a):
                        Ij = self.GO[Ik][a]
                        if self.ACTION[Ik][a] is None:
                            self.ACTION[Ik][a] = "s"+str(Ij)
                        else:
                            raise ValueError("Meet Collision")
                # (2)
                if produce_string.index(".") == len(produce_string)-1 \
                        and Produce(produce_string).replace_point() != self.start_produce:
                    Ij = self.get_produce_num(Produce(produce_string).replace_point())
                    for character in self.terminal + ["#"]:
                        if self.ACTION[Ik][character] is None:
                            self.ACTION[Ik][character] = "r"+str(Ij)
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

    def run(self, input_string):
        state = [0]
        characters = "#"
        input_string = input_string + "#"
        self.stack_table.append([state[:], characters, input_string])
        while True:
            action_string = self.ACTION[state[-1]][input_string[0]]
            # shift
            if action_string[0] == "s":
                state.append(int(action_string[1:]))
                characters += input_string[0]
                input_string = input_string[1:]
            # rule
            elif action_string[0] == "r":
                rule_produce = Produce(self.produce_list[int(action_string[1:])])
                rule_right_len = len(rule_produce.right[0])
                assert rule_produce.right[0] == characters[-rule_right_len:]
                characters = characters[:-rule_right_len] + rule_produce.left
                state = state[:-rule_right_len]
                state += [self.GO[state[-1]][rule_produce.left]]
            # acc
            elif action_string == "acc":
                break
            self.stack_table.append([state[:], characters, input_string])
            # print([state, characters, input_string], action_string)

    def is_nonterminal(self, letter):
        return str.isupper(letter)

    def is_terminal(self, letter):
        return not str.isupper(letter) and not letter == epsilon


class LR0Parser:
    def __init__(self):
        self.grammar = Grammar("S->a")

    def parse(self, input_string):
        self.grammar = Grammar(input_string)
        print(self.grammar.start_produce)
        print(self.grammar.produce_list)
        print(self.grammar.grammar_dict)
        print(self.grammar.project_dict)
        for num, project_set in enumerate(self.grammar.project_set):
            print("{}: {}".format(num, project_set))
        for key, val in self.grammar.GO.items():
            print("{}: {}".format(key, val))
        print(self.grammar.terminal)
        self.grammar.gen_analysis_table()
        print(self.grammar.ACTION)
        print(self.grammar.GOTO)


# if __name__ == "__main__":
#     grammar = LR0Grammar(test_input_string)
#     grammar.gen_analysis_table()
#     grammar.run("acccd")
    # print("CLOSURE")
    # for key, val in grammar.CLOSURE.items():
    #     print("{}: {}".format(key, val))
    # print("ACTION:")
    # for key, val in grammar.ACTION.items():
    #     print("{}: {}".format(key, val))
    # grammar.run("i*i+i")
    # print(grammar.ACTION)
    # print(grammar.terminal)
    # print(grammar.nonterminal)
    # print(grammar.project_dict)
    # for key, val in grammar.GO.items():
    #     print("{}: {}".format(key, val))
    # for key, val in grammar.ACTION.items():
    #     print("{}: {}".format(key, val))
    # for num, project_set in enumerate(grammar.project_set):
    #     print("{}: {}".format(num, project_set))