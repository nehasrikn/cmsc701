from bloom_filter2 import BloomFilter
from itertools import product
from tqdm import tqdm 
import random

class StringGenerator:
    
    def __init__(self, alphabet="ACTG", min_len=15, max_len=40):
        self.alphabet = alphabet
        self.min_len = min_len
        self.max_len = max_len

    def generate(self):
        """Generates n random strings of length between min_len and max_len
        (inclusive) using the alphabet provided in the constructor.
        """
        strings = []
        for i in tqdm(range(self.min_len, self.max_len)):
            strings.extend(self.generate_for_length(i))
        return strings   
    
    def generate_for_length(self, n):
        return list(map(lambda x: "".join(x), product(self.alphabet, repeat=n)))

    def sample_strings(self, n, size):
        samples = set()
        while len(samples) < size:
            sample = ''.join(random.choice(self.alphabet) for _ in range(n))
            samples.add(sample)
        return list(samples)

if __name__ == "__main__":
    sg = StringGenerator(min_len=3, max_len=31)
    #print(len(sg.generate_for_length(31)))
    print(sg.sample_strings(31, 10000))
        
        
        