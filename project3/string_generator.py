from itertools import product
from tqdm import tqdm 
import random

random.seed(42)

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