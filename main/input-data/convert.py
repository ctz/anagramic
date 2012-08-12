
by_length = {}
letters = set()
input = '2of12inf.txt'
output = 'data.java'

for l in open(input):
	l = l.strip()
	if len(l) == 0: continue
	if l.endswith('%'): continue
	by_length.setdefault(len(l), set()).add(l)
	letters.update(l)

print len(letters), 'letters'

f = open(output, 'w')

for i in range(0, 10):
	here = list(sorted(by_length.get(i, set())))
	print >>f, '\t\t"|' + '|'.join(here) + '|",'
	
f.close()
	
for ll, words in by_length.items():
	print len(words), 'of length', ll