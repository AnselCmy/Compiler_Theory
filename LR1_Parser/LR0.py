from collections import defaultdict

test_input_string = "S->E\nE->aB|bB\nA->cA|d\nB->cB|d"

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
			self.right = produce[1].split("|")
			produce = [self.left, self.right]
			self.produce_list.append(produce)
			
	def after_point(self):
		"""Get the nonterminal after the point"""
		pos = self.right[0].index('.') + 1
		return self.right[0][pos]
	
	
class Grammar:
	"""The presentation of one grammar
	
	Attributes:
		grammar_dict: the list of object of the class Produce, which is one grammar.
	"""
	def __init__(self, grammar_string):
		self.grammar_dict = defaultdict(list)
		self.project_dict = defaultdict(list)
		self.CLOSURE = defaultdict(list)
		self.parse(grammar_string)
		self.gen_project()
	
	def parse(self, input_string):
		for produce_string in input_string.split("\n"):
			produce = Produce(produce_string)
			self.grammar_dict[produce.left] = produce.right
		
	def gen_project(self):
		for left, right in self.grammar_dict.items():
			for one_right in right:
				if one_right == epsilon:
					self.project_dict[left] = '.'
				else:
					for pos in range(len(one_right)+1):
						self.project_dict[left].append(one_right[:pos]+'.'+one_right[pos:])
	
	def gen_closure(self, produce):
		temp_closure = []
		# insert itself into this CLOSURE
		temp_closure.append(produce.produce_string)
		old_len = 0
		new_len = 1
		while old_len != new_len:
			old_len = len(temp_closure)
			# iter in CLOSURE
			for p in temp_closure:
				p = Produce(p)
				if self.is_nonterminal(p.after_point()):
					for r in self.get_first_projects(p.after_point()):
						temp_closure.append(p.after_point()+"->"+r)
			# set the CLOSURE
			index = temp_closure.index
			temp_closure = list(set(temp_closure))
			temp_closure.sort(key=index)
			new_len = len(temp_closure)
		self.CLOSURE[produce.produce_string] = temp_closure

	def get_first_projects(self, nonterminal):
		"""Get the project with the left of nonterminal which has the point at first position."""
		rst = []
		for p in self.project_dict[nonterminal]:
			if p.index('.') == 0:
				rst.append(p)
		return rst

	def is_nonterminal(self, letter):
		return str.isupper(letter)


class LR0Parser:
	def __init__(self):
		self.grammar = Grammar("S->a")
	
	def parse(self, input_string):
		self.grammar = Grammar(input_string)
		print(self.grammar.grammar_dict)
		print(self.grammar.project_dict)
		self.grammar.gen_closure(Produce("E->a.A"))
		print(self.grammar.CLOSURE)
	
	def get_project_set(self):
		pass
		
		
if __name__ == "__main__":
	LR0_parser = LR0Parser()
	LR0_parser.parse(test_input_string)