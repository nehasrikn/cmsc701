from bloom_filter2 import BloomFilter
from itertools import product
from tqdm import tqdm 
import random
import time
from .string_generator import StringGenerator

    
def construct_bloom_and_query(strings, expected_hit_rate, bloom_error_rate):
    """Samples k strings from strings and partitions them into two sets, 
    one of size k and one of size k_prime.
    """
    random.seed(42)

    keys = random.sample(strings, len(strings))
    
    k_set = keys[:int(len(keys) * expected_hit_rate)]
    k_prime_set = keys[int(len(keys) * expected_hit_rate):]
    
    
    query_set = random.sample(k_set, int(len(k_set) * expected_hit_rate))
    query_set.extend(random.sample(k_prime_set, int(len(k_prime_set) * (1 - expected_hit_rate))))

    bloom = BloomFilter(max_elements=len(k_set), error_rate=bloom_error_rate)
    for item in k_set:
        bloom.add(item)

    false_positives = 0
    start_time = time.time()

    for item in query_set:
        if item in bloom and item not in k_set:
            false_positives += 1

    end_time = time.time()
    elapsed_time = end_time - start_time

    print("Elapsed Time:", elapsed_time, "seconds")
    print("False positive rate: {}".format(false_positives / len(query_set)))

    return bloom

if __name__ == "__main__":
    sg = StringGenerator(min_len=3, max_len=31)
    #print(len(sg.generate_for_length(31)))
    sample = sg.sample_strings(31, 10000)

    fp_rates = [1/(2 ** 7), 1/(2**8), 1/(2**10)]

    for rate in fp_rates:
        construct_bloom_and_query(sample, 0.2, rate)
        
        
        