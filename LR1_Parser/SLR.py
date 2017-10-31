from LR0 import *


class SLRGrammar(LR0Grammar):
    def __init__(self, grammar_string):
        super(SLRGrammar, self).__init__(grammar_string)
        self.FIRST = defaultdict(list)
        self.FOLLOW = defaultdict(list)
        self.gen_FIRST()
        self.gen_FOLLOW()
        # self.gen_project_set()
        # self.gen_analysis_table()

    def gen_FIRST(self):
        while True:
            old_FIRST = self.get_old_FIRST()
            for nonterminal in self.nonterminal:
                self.get_FIRST_of(nonterminal)
                self.FIRST[nonterminal] = list(set(self.FIRST[nonterminal]))
            if self.is_FIRST_stable(old_FIRST):
                break

    def get_FIRST_of(self, c):
        if self.is_terminal(c):
            return [c]
        elif self.is_nonterminal(c):
            if self.FIRST[c]:
                return self.FIRST[c]
            else:
                for right in self.grammar_dict[c]:
                    for idx, character in enumerate(right):
                        if self.is_terminal(character):
                            self.FIRST[c].append(character)
                            break
                        elif character == epsilon:
                            self.FIRST[c].append(epsilon)
                            break
                        elif self.is_nonterminal(character):
                            if character == c:
                                if epsilon in self.FIRST[c]:
                                    continue
                                else:
                                    break
                            temp_FIRST = self.get_FIRST_of(character)[:]
                            if epsilon in temp_FIRST:
                                if idx != len(s) - 1:
                                    temp_FIRST.remove(epsilon)
                                self.FIRST[c] += temp_FIRST
                            else:
                                self.FIRST[c] += temp_FIRST
                                break
                    # self.FIRST[c] += self.get_FIRST_of_string(right)
                return self.FIRST[c][:]

    def get_FIRST_of_string(self, s):
        FIRST = []
        for idx, c in enumerate(s):
            if self.is_terminal(c):
                FIRST.append(c)
                break
            elif c == epsilon:
                FIRST.append(epsilon)
                break
            elif self.is_nonterminal(c):
                temp_FIRST = self.get_FIRST_of(c)[:]
                if epsilon in temp_FIRST:
                    if idx != len(s)-1:
                        temp_FIRST.remove(epsilon)
                    FIRST += temp_FIRST
                else:
                    FIRST += temp_FIRST
                    break
        return FIRST

    def get_old_FIRST(self):
        old_FIRST = defaultdict(list)
        for key, val in self.FIRST.items():
            old_FIRST[key] = val[:]
        return old_FIRST

    def is_FIRST_stable(self, old_FIRST):
        rst = True
        for key, val in self.FIRST.items():
            if set(self.FIRST[key]) != set(old_FIRST[key]):
                rst = False
                break
        return rst

    def gen_FOLLOW(self):
        while True:
            self.FOLLOW[self.start_char].append("#")
            old_FOLLOW = self.get_old_FOLLOW()
            for produce in self.produce_list:
                produce = Produce(produce)
                for idx, character in enumerate(produce.right[0]):
                    if self.is_nonterminal(character):
                        if idx != len(produce.right[0])-1:
                            last_FIRST = self.get_FIRST_of_string(produce.right[0][idx+1:])
                            if epsilon in last_FIRST:
                                last_FIRST.remove(epsilon)
                                self.FOLLOW[character] += last_FIRST
                                self.FOLLOW[character] += self.FOLLOW[produce.left][:]
                            else:
                                self.FOLLOW[character] += last_FIRST
                        else:
                            self.FOLLOW[character] += self.FOLLOW[produce.left][:]
                        self.FOLLOW[character] = list(set(self.FOLLOW[character]))
            if self.is_FOLLOW_stable(old_FOLLOW):
                break

    def is_FOLLOW_stable(self, old_FOLLOW):
        rst = True
        for key, val in self.FOLLOW.items():
            if set(self.FOLLOW[key]) != set(old_FOLLOW[key]):
                rst = False
                break
        return rst

    def get_old_FOLLOW(self):
        old_FOLLOW = defaultdict(list)
        for key, val in self.FOLLOW.items():
            old_FOLLOW[key] = val[:]
        return old_FOLLOW

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
                    for character in self.FOLLOW[produce_string[0]]:
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


# if __name__ == "__main__":
    # grammar = SLRGrammar(test_input_string1)
    # for num, project_set in enumerate(grammar.project_set):
    #     print("{}: {}".format(num, project_set))
    # print(grammar.FOLLOW)
    # print("ACTION:")
    # for key, val in grammar.ACTION.items():
    #     print("{}: {}".format(key, val))
    # grammar.run("i*i+i")
    # grammar.gen_FIRST()
    # grammar.gen_FIRST()
    # print(grammar.FIRST)
    # grammar.gen_FOLLOW()
    # print(grammar.FOLLOW)
    # grammar.gen_analysis_table()
    # grammar.run("i*i+i")
