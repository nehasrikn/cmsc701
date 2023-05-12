from string_generator import StringGenerator
import random
import bbhash
import time
import os

def hash_string(s, bits):
    """Hashes a string to a bits bit integer.
    """
    hash_value = hash(s) # Convert hexadecimal to decimal
    hashed_int = hash_value % (2**bits/2)
    return hashed_int

def construct_mphf_and_query(strings, mixture_rate):
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


    uint_hashes = [hash_string(s, 16) for s in k_set]

    num_threads = 1
    gamma = 1.0

    mph = bbhash.PyMPHF(uint_hashes, len(uint_hashes), num_threads, gamma)
    
    false_positives = 0
    denom = 0
    start_time = time.time()

    for item in query_set:
        lookup = mph.lookup(hash_string(item, 16))
        if lookup is not None and item not in k_set:
            false_positives += 1
        if item not in k_set:
            denom += 1
    
    end_time = time.time()
    elapsed_time = end_time - start_time

    # print("Elapsed Time:", elapsed_time, "seconds")
    # print("False positive rate: {}".format(false_positives / denom))
    
    mph.save('temp.mph')
    return elapsed_time, false_positives / denom, os.path.getsize('temp.mph')


if __name__ == "__main__":
    sg = StringGenerator(min_len=3, max_len=31)
    #print(len(sg.generate_for_length(31)))

    queries = [5000, 10000, 20000]
    mixture = [0.2, 0.4, 0.6, 0.8]


    for q in queries:
        print("Query size: {}".format(q))
        sample = sg.sample_strings(31, 2*q)

        times = []
        fps = []
        sizes = []

        for m in mixture:
            elapsed_time, observed_fp, size = construct_mphf_and_query(
                sample, m
            )  
            times.append(elapsed_time)
            fps.append(observed_fp)
            sizes.append(size) 

        print("Times: {}".format(times))
        print("False positives: {}".format(fps))
        print("Sizes: {}".format(sizes))
        print()
            
        