from rbloom import Bloom
from itertools import product
from tqdm import tqdm 
import random
import time
from string_generator import StringGenerator
import os
import seaborn as sns
    
def construct_bloom_and_query(strings, mixture_rate, bloom_error_rate):
    """Samples k strings from strings and partitions them into two sets, 
    one of size k and one of size k_prime.
    """
    random.seed(42)
    pivot = len(strings) //2
    keys = random.sample(strings, len(strings)) #shuffle the strings
    
    k_set = keys[:pivot]
    k_prime_set = keys[pivot:]
    
    offset = int((1 - mixture_rate) * pivot)
    query_set = keys[offset:offset + pivot]

    bloom = Bloom(len(k_set), bloom_error_rate)
    for item in k_set:
        bloom.add(item)

    false_positives = 0
    denom = 0
    start_time = time.time()

    for item in query_set:
        if item in bloom and item not in k_set:
            false_positives += 1
        if item not in k_set:
            denom += 1

    end_time = time.time()
    elapsed_time = end_time - start_time

    # print("Elapsed Time:", elapsed_time, "seconds")
    # print("False positive rate: {}".format(false_positives / len(query_set)))

    return elapsed_time, false_positives / denom, bloom.size_in_bits

if __name__ == "__main__":
    sg = StringGenerator(min_len=3, max_len=31)


    fp_rates = [1/(2**7), 1/(2**8), 1/(2**10)]
    queries = [5000, 10000, 20000]
    mixture = [0.2, 0.4, 0.6, 0.8]


    for rate in fp_rates:
        print("**** False positive rate: {} ****".format(rate))
        for q in queries:
            print("Query size: {}".format(q))
            sample = sg.sample_strings(31, 2*q)

            times = []
            fps = []
            sizes = []

            for m in mixture:
                elapsed_time, observed_fp, size = construct_bloom_and_query(
                    sample, m, rate
                )  
                times.append(elapsed_time)
                fps.append(observed_fp)
                sizes.append(size) 

            print("Times: {}".format(times))
            print("False positives: {}".format(fps))
            print("Sizes: {}".format(sizes))
            print()
        
        print()
            
        
        